package edu.mayo.mprc.enginedeployment;

import edu.mayo.mprc.daemon.WorkPacketBase;
import edu.mayo.mprc.daemon.files.FileToken;
import edu.mayo.mprc.daemon.files.FileTokenFactory;
import edu.mayo.mprc.daemon.files.SenderTokenTranslator;
import edu.mayo.mprc.fasta.DatabaseAnnotation;
import edu.mayo.mprc.fasta.FastaFile;

import java.io.File;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Request to deploy/undeploy a database.
 *
 * @author Eric Winter
 */
public final class DeploymentRequest extends WorkPacketBase {
	private static final long serialVersionUID = 20071220L;

	/**
	 * Information about the fasta file to use.
	 */
	FastaFile fastaFile;

	/**
	 * When set to true, this is an undeployment request, false - deployment.
	 */
	private boolean undeployment;

	/**
	 * is ready to hold any properties that the specific deployer may need in order to perform the deployment.  The requester
	 * and the service will need to work in concert on these properties.  Examples for this include the properties to Sequest's makedb
	 * and to Mascots monitor and mascot.dat.  There is a deployment.properties file that will be defaulted to if a given property is not
	 * found in the given property.
	 */
	private Map<String, Serializable> properties;

	protected DeploymentRequest(String taskId) {
		// Deployment is always done with caching (so far).
		super(taskId, false);
	}

	/**
	 * makes a request out of a curation but copies only the essential information to send to the deployer.
	 */
	public DeploymentRequest(String taskid, FastaFile fastaFile) {
		this(taskid);
		assert fastaFile.getFile() != null : "The deployment request must have a valid Curation.";

		this.fastaFile = fastaFile;
	}

	public boolean isUndeployment() {
		return undeployment;
	}

	/**
	 * @param undeployment if true, flags this request as a database undeployment.
	 */
	public void setUndeployment(boolean undeployment) {
		this.undeployment = undeployment;
	}

	public String getShortName() {
		return fastaFile.getName();
	}

	public String getTitle() {
		return fastaFile.getDescription();
	}

	public DatabaseAnnotation getAnnotation() {
		return fastaFile.getAnnotation();
	}

	public Object getProperty(String key) {
		if (this.properties == null) {
			return null;
		}

		Object value = properties.get(key);

		if (value instanceof FileToken) {
			value = getTranslator().getFile((FileToken) value);
		}

		return value;
	}

	public void addProperty(String key, Serializable value) {
		if (this.properties == null) {
			this.properties = new HashMap<String, Serializable>();
		}

		if (value instanceof File) {
			value = FileTokenFactory.createAnonymousFileToken((File) value);
		}

		properties.put(key, value);
	}

	public File getCurationFile() {
		return fastaFile.getFile();
	}

	public void translateOnSender(SenderTokenTranslator translator) {
		super.translateOnSender(translator);
		// We need to translate all the extra properties that are not picked up automatically
		if (properties != null) {
			for (Map.Entry<String, Serializable> property : properties.entrySet()) {
				if (property.getValue() instanceof FileToken) {
					properties.put(property.getKey(), translator.translateBeforeTransfer((FileToken) property.getValue()));
				}
			}
		}
	}
}
