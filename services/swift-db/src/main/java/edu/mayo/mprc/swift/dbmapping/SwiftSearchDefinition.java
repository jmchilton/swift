package edu.mayo.mprc.swift.dbmapping;

import edu.mayo.mprc.database.PersistableBase;
import edu.mayo.mprc.swift.params2.SearchEngineParameters;
import edu.mayo.mprc.workspace.User;

import java.io.File;
import java.util.List;

/**
 * Describes a basic Swift search, exactly as the user entered it in the UI.
 */
public class SwiftSearchDefinition extends PersistableBase {
	private String title;
	private User user;
	private File outputFolder;
	private SpectrumQa qa;
	private PeptideReport peptideReport;
	private Boolean publicMgfFiles;
	private Boolean publicSearchFiles;

	private SearchEngineParameters searchParameters;
	private List<FileSearch> inputFiles;

	public SwiftSearchDefinition() {
	}

	public SwiftSearchDefinition(String title, User user, File outputFolder, SpectrumQa qa, PeptideReport peptideReport, SearchEngineParameters searchParameters, List<FileSearch> inputFiles, boolean publicMgfFiles, boolean publicSearchFiles) {
		this.title = title;
		this.user = user;
		this.outputFolder = outputFolder;
		this.qa = qa;
		this.peptideReport = peptideReport;
		this.searchParameters = searchParameters;
		this.inputFiles = inputFiles;
		this.publicMgfFiles = publicMgfFiles;
		this.publicSearchFiles = publicSearchFiles;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public File getOutputFolder() {
		return outputFolder;
	}

	public void setOutputFolder(File outputFolder) {
		this.outputFolder = outputFolder;
	}

	public SpectrumQa getQa() {
		return qa;
	}

	public void setQa(SpectrumQa qa) {
		this.qa = qa;
	}

	public PeptideReport getPeptideReport() {
		return peptideReport;
	}

	public void setPeptideReport(PeptideReport peptideReport) {
		this.peptideReport = peptideReport;
	}

	public SearchEngineParameters getSearchParameters() {
		return searchParameters;
	}

	public void setSearchParameters(SearchEngineParameters searchParameters) {
		this.searchParameters = searchParameters;
	}

	public List<FileSearch> getInputFiles() {
		return inputFiles;
	}

	public void setInputFiles(List<FileSearch> inputFiles) {
		this.inputFiles = inputFiles;
	}

	public Boolean getPublicMgfFiles() {
		return publicMgfFiles;
	}

	public void setPublicMgfFiles(Boolean publicMgfFiles) {
		this.publicMgfFiles = publicMgfFiles;
	}

	public Boolean getPublicSearchFiles() {
		return publicSearchFiles == null ? false : publicSearchFiles;
	}

	public void setPublicSearchFiles(Boolean publicSearchFiles) {
		this.publicSearchFiles = publicSearchFiles;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || !(o instanceof SwiftSearchDefinition)) {
			return false;
		}

		SwiftSearchDefinition that = (SwiftSearchDefinition) o;

		if (getInputFiles() != null ? !getInputFiles().equals(that.getInputFiles()) : that.getInputFiles() != null) {
			return false;
		}
		if (getOutputFolder() != null ? !getOutputFolder().equals(that.getOutputFolder()) : that.getOutputFolder() != null) {
			return false;
		}
		if (getPeptideReport() != null ? !getPeptideReport().equals(that.getPeptideReport()) : that.getPeptideReport() != null) {
			return false;
		}
		if (getQa() != null ? !getQa().equals(that.getQa()) : that.getQa() != null) {
			return false;
		}
		if (getTitle() != null ? !getTitle().equals(that.getTitle()) : that.getTitle() != null) {
			return false;
		}
		if (getUser() != null ? !getUser().equals(that.getUser()) : that.getUser() != null) {
			return false;
		}
		if (getPublicMgfFiles() != null ? !getPublicMgfFiles().equals(that.getPublicMgfFiles()) : that.getPublicMgfFiles() != null) {
			return false;
		}

		if (getPublicSearchFiles() != null ? !getPublicSearchFiles().equals(that.getPublicMgfFiles()) : that.getPublicSearchFiles() != null) {
			return false;
		}

		return true;
	}

	@Override
	public int hashCode() {
		int result = getTitle() != null ? getTitle().hashCode() : 0;
		result = 31 * result + (getUser() != null ? getUser().hashCode() : 0);
		result = 31 * result + (getOutputFolder() != null ? getOutputFolder().hashCode() : 0);
		result = 31 * result + (getQa() != null ? getQa().hashCode() : 0);
		result = 31 * result + (getPeptideReport() != null ? getPeptideReport().hashCode() : 0);
		result = 31 * result + (getInputFiles() != null ? getInputFiles().hashCode() : 0);
		result = 31 * result + (getPublicMgfFiles() != null ? getPublicMgfFiles().hashCode() : 0);
		result = 31 * result + (getPublicSearchFiles() != null ? getPublicSearchFiles().hashCode() : 0);
		return result;
	}
}
