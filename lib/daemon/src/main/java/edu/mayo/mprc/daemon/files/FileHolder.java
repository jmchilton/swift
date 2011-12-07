package edu.mayo.mprc.daemon.files;

import edu.mayo.mprc.MprcException;

import java.io.File;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A class implementing the {@link FileTokenHolder} protocol that does not actually hold any {@link FileToken} objects
 * itself.
 * <p/>
 * Reflection is used to harvest a list of all files, other FileTokenHolders, and List<File> objects. The files are translated to FileToken
 * objects and stored in {@link #tokenMap}. The other side then automatically translates the FileToken instances back
 * to Files and sets the matching properties.
 * <p/>
 * This way the user can send an object message with multiple references to files, and the files get transferred to the target
 * system if necessary.
 */
public class FileHolder implements FileTokenHolder {
	private static final long serialVersionUID = 20110418L;
	private HashMap<FieldIndex, FileToken> tokenMap;
	private transient ReceiverTokenTranslator translator;
	private transient FileTokenSynchronizer synchronizer;

	public FileHolder() {
	}

	public ReceiverTokenTranslator getTranslator() {
		return translator;
	}

	public FileTokenSynchronizer getSynchronizer() {
		return synchronizer;
	}

	@Override
	public void translateOnSender(SenderTokenTranslator translator) {
		tokenMap = new HashMap<FieldIndex, FileToken>();
		for (Field field : getFields()) {
			field.setAccessible(true);
			if (serializableFileField(field)) {
				addFileToken(translator, field);
			} else if (serializableFileListField(field)) {
				addFileTokenList(translator, field);
			} else if (serializableFileMapField(field)) {
				addFileTokenMap(translator, field);
			} else if (serializableFileTokenField(field)) {
				callTranslateOnSender(translator, field);
			}
		}
	}

	@Override
	public void translateOnReceiver(ReceiverTokenTranslator translator, FileTokenSynchronizer synchronizer) {
		this.translator = translator;
		this.synchronizer = synchronizer;
		// Set all directly accessible fields
		for (Map.Entry<FieldIndex, FileToken> entry : tokenMap.entrySet()) {
			final File file = translator.getFile(entry.getValue());
			if (entry.getKey().getIndex() == null) {
				setFileField(entry.getKey().getField(), file);
			} else {
				setFileIndexedField(entry.getKey(), file);
			}
		}
		// Set all the file token holder fields
		for (Field field : getFields()) {
			if (serializableFileTokenField(field)) {
				FileTokenHolder fileTokenHolder = getFileTokenHolder(field);
				if (fileTokenHolder != null) {
					fileTokenHolder.translateOnReceiver(translator, synchronizer);
				}
			}
		}
	}

	/**
	 * @return A list of all fields to be considered. They are all set as accessible.
	 */
	private List<Field> getFields() {
		List<Field> fields = new ArrayList<Field>();
		addFields(this.getClass(), fields);
		return fields;
	}

	private void addFields(Class<?> clazz, List<Field> fields) {
		final Field[] declaredFields = clazz.getDeclaredFields();
		for (Field field : declaredFields) {
			field.setAccessible(true);
			fields.add(field);
		}
		if (!this.getClass().equals(FileHolder.class) && clazz.getSuperclass() != null) {
			addFields(clazz.getSuperclass(), fields);
		}
	}

	@Override
	public void synchronizeFileTokensOnReceiver() {
		// Nothing is being uploaded by default
	}

	/**
	 * Upload the file for a given field name. To be called from {@link #synchronizeFileTokensOnReceiver()} implementations.
	 *
	 * @param fieldName Name of the field to upload.
	 */
	public void uploadAndWait(String fieldName) {
		final FileToken fileToken = tokenMap.get(new FieldIndex(fieldName, null));
		synchronizer.uploadAndWait(fileToken);
	}

	private boolean isSerializableField(Field field) {
		return !Modifier.isTransient(field.getModifiers()) && !Modifier.isStatic(field.getModifiers());
	}

	private boolean serializableFileTokenField(Field field) {
		return FileTokenHolder.class.isAssignableFrom(field.getType()) && isSerializableField(field);
	}

	private boolean serializableFileField(Field field) {
		return File.class.isAssignableFrom(field.getType()) && isSerializableField(field);
	}

	/**
	 * @param field Object field.
	 * @return True if the field corresponds to a list of objects, at least one of which is a file.
	 */
	private boolean serializableFileListField(Field field) {
		if (List.class.isAssignableFrom(field.getType()) && isSerializableField(field)) {
			List list = getFieldList(field);
			if (list != null) {
				for (Object o : list) {
					if (o instanceof File) {
						return true;
					}
				}
			}
		}
		return false;
	}

	/**
	 * @param field Object field.
	 * @return True if the field corresponds to a map from a Serializable(not File) to File. These are the only maps we support right now.
	 */
	private boolean serializableFileMapField(Field field) {
		if (Map.class.isAssignableFrom(field.getType()) && isSerializableField(field)) {
			Map map = getFieldMap(field);
			if (map != null) {
				for (Object o : map.entrySet()) {
					if (o instanceof Map.Entry) {
						Map.Entry entry = (Map.Entry) o;
						final Object key = entry.getKey();
						final Object value = entry.getValue();
						if (key instanceof Serializable && !(key instanceof File) && value instanceof File) {
							return true;
						}
						if (key instanceof File || value instanceof File) {
							throw new MprcException("Cannot correctly serialize map entries that are not in <Serializable, File> format.");
						}
					}
				}
			}
		}
		return false;
	}

	private List getFieldList(Field field) {
		List list = null;
		try {
			list = (List) field.get(this);
		} catch (IllegalAccessException e) {
			throwFieldAccess(field, e);
		}
		return list;
	}

	private Map getFieldMap(Field field) {
		Map map = null;
		try {
			map = (Map) field.get(this);
		} catch (IllegalAccessException e) {
			throwFieldAccess(field, e);
		}
		return map;
	}

	private void setFileField(String fieldName, File file) {
		Field field = getFieldForName(this.getClass(), fieldName);
		try {
			field.set(this, file);
		} catch (IllegalAccessException e) {
			throwFieldAccess(field, e);
		}
	}

	private void setFileIndexedField(FieldIndex fieldIndex, File file) {
		final Field field = getFieldForName(this.getClass(), fieldIndex.getField());
		if (List.class.isAssignableFrom(field.getType())) {
			final Serializable index = fieldIndex.getIndex();
			if (index instanceof Integer) {
				getFieldList(field).set((Integer) index, file);
			} else {
				throw new MprcException("The list index was not an integer: " + index);
			}
		} else if (Map.class.isAssignableFrom(field.getType())) {
			final Serializable index = fieldIndex.getIndex();
			getFieldMap(field).put(index, file);
		}
	}

	private Field getFieldForName(Class<?> clazz, String fieldName) {
		if (clazz == null) {
			return null;
		}
		Field field = null;
		try {
			field = clazz.getDeclaredField(fieldName);
			field.setAccessible(true);
		} catch (NoSuchFieldException ignore) {
			// Failed to obtain, ask the parent
			field = getFieldForName(clazz.getSuperclass(), fieldName);
		}
		if (field == null) {
			throw new MprcException("Could not access field " + fieldName + " of " + this.getClass().getCanonicalName());
		}
		return field;
	}

	private void addFileToken(SenderTokenTranslator translator, Field field) {
		File file = null;
		try {
			file = (File) field.get(this);
		} catch (IllegalAccessException e) {
			throwFieldAccess(field, e);
		}
		tokenMap.put(new FieldIndex(field.getName(), null), token(translator, file));
	}

	private void addFileTokenList(SenderTokenTranslator translator, Field field) {
		int index = 0;
		for (Object o : getFieldList(field)) {
			if (o instanceof File) {
				File file = (File) o;
				FileToken value = token(translator, file);
				tokenMap.put(new FieldIndex(field.getName(), index), value);
			}
			index++;
		}
	}

	private void addFileTokenMap(SenderTokenTranslator translator, Field field) {
		for (Object o : getFieldMap(field).entrySet()) {
			if (o instanceof Map.Entry) {
				Map.Entry entry = (Map.Entry) o;
				if (entry.getKey() instanceof Serializable && entry.getValue() instanceof File) {
					File file = (File) entry.getValue();
					FileToken value = token(translator, file);
					tokenMap.put(new FieldIndex(field.getName(), (Serializable) entry.getKey()), value);
				}
			}
		}
	}

	private FileToken token(SenderTokenTranslator translator, File file) {
		return translator.translateBeforeTransfer(FileTokenFactory.createAnonymousFileToken(file));
	}

	private void callTranslateOnSender(SenderTokenTranslator translator, Field field) {
		FileTokenHolder fileTokenHolder = getFileTokenHolder(field);
		if (fileTokenHolder != null) {
			fileTokenHolder.translateOnSender(translator);
		}
	}

	private FileTokenHolder getFileTokenHolder(Field field) {
		FileTokenHolder fileTokenHolder = null;
		try {
			fileTokenHolder = (FileTokenHolder) field.get(this);
		} catch (IllegalAccessException e) {
			throwFieldAccess(field, e);
		}
		return fileTokenHolder;
	}

	private void throwFieldAccess(Field field, IllegalAccessException e) {
		throw new MprcException("Could not serialize field " + field.getName() + " of " + this.getClass().getCanonicalName(), e);
	}
}
