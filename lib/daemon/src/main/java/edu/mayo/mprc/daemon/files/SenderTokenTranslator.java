package edu.mayo.mprc.daemon.files;

/**
 * Translates a {@link FileToken} to a version that is suitable for transfer over the wire.
 */
public interface SenderTokenTranslator {
	/**
	 * Translate a {@link FileToken} before it gets transfered. As a {@link FileTokenHolder} you
	 * have to translate all your tokens using this method and replace your original tokens with the translated versions.
	 *
	 * @param fileToken Token to be translated.
	 * @return Translated token. Store it in place of your original one (you are not losing any information here).
	 */
	FileToken translateBeforeTransfer(FileToken fileToken);
}
