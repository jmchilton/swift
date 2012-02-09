package edu.mayo.mprc.searchdb.dao;

import edu.mayo.mprc.database.PersistableBase;

import java.io.File;
import java.util.Date;

/**
 * Information about a particular mass spectrometry sample. The sample corresponds to a single .RAW or .mgf file.
 * <p/>
 * The useful information here is the total amount of MS2 spectra, and amount of identified MS2 spectra. This can be
 * utilized for normalization. Other metadata might include date of collection, instrument serial number, instrument
 * method, etc. The goal is to provide enough data for statistical normalization.
 *
 * @author Roman Zenka
 */
public class TandemMassSpectrometrySample extends PersistableBase {
    /**
     * Link to the .RAW or .mgf file that was analyzed. It can contain a null or just a filename without the path in case the file could not be found.
     */
    private File file;

    /**
     * When the file got last modified. Can store an old value in case only the modification time changed, but the
     * file contents remained identical.
     */
    private Date lastModified;

    /**
     * Number of survey or MS1 spectra.
     */
    private int ms1Spectra;

    /**
     * Number of MS2, MS3, etc.. spectra. This can be important for normalization.
     */
    private int msnSpectra;

    /**
     * Serial number of the instrument.
     */
    private String instrumentSerialNumber;

    /**
     * Time when the instrument started to collect the data.
     */
    private Date startTime;

    /**
     * For how long have the data been collected - in seconds.
     */
    private double runTimeInSeconds;

    /**
     * A long, instrument specific text containing all the information about how was the instrument tuned for this
     * sample - what kind of voltages were set where, what calibration parameters were used, etc.
     */
    private String tuneMethod;

    /**
     * Information about the method that was used to collect the sample. How many MS2 scans were utilized, with what settings,
     * what was the isolation window, etc. While {@link #tuneMethod} is about how the instrument was tuned,
     * this is about what the instrument was told to do on a particular sample.
     */
    private String instrumentMethod;

    /**
     * Information about the sample being processed. Who entered it, what vial it was in, what was the volume, etc.
     */
    private String sampleInformation;

    /**
     * Were there any errors in processing? To save us space, if the error log contains only "No errors" message,
     * null should be stored here.
     */
    private String errorLog;

    /**
     * As the instrument processes the data, it reports current values for a lot of systems - e.g. temperatures,
     * pressures, voltages. This log summarizes this information over the entire run, containing a report of
     * minimum, maximum, average, standard deviation. This should allow us to spot issues without having to store
     * this data on a per-spectrum basis.
     */
    private String statusLogRanges;

    /**
     * Empty constructor for Hibernate.
     */
    public TandemMassSpectrometrySample() {
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public Date getLastModified() {
        return lastModified;
    }

    public void setLastModified(Date lastModified) {
        this.lastModified = lastModified;
    }

    public int getMs1Spectra() {
        return ms1Spectra;
    }

    public void setMs1Spectra(int ms1Spectra) {
        this.ms1Spectra = ms1Spectra;
    }

    public int getMsnSpectra() {
        return msnSpectra;
    }

    public void setMsnSpectra(int msnSpectra) {
        this.msnSpectra = msnSpectra;
    }

    public String getInstrumentSerialNumber() {
        return instrumentSerialNumber;
    }

    public void setInstrumentSerialNumber(String instrumentSerialNumber) {
        this.instrumentSerialNumber = instrumentSerialNumber;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public double getRunTimeInSeconds() {
        return runTimeInSeconds;
    }

    public void setRunTimeInSeconds(double runTimeInSeconds) {
        this.runTimeInSeconds = runTimeInSeconds;
    }

    public String getTuneMethod() {
        return tuneMethod;
    }

    public void setTuneMethod(String tuneMethod) {
        this.tuneMethod = tuneMethod;
    }

    public String getInstrumentMethod() {
        return instrumentMethod;
    }

    public void setInstrumentMethod(String instrumentMethod) {
        this.instrumentMethod = instrumentMethod;
    }

    public String getSampleInformation() {
        return sampleInformation;
    }

    public void setSampleInformation(String sampleInformation) {
        this.sampleInformation = sampleInformation;
    }

    public String getErrorLog() {
        return errorLog;
    }

    public void setErrorLog(String errorLog) {
        this.errorLog = errorLog;
    }

    public String getStatusLogRanges() {
        return statusLogRanges;
    }

    public void setStatusLogRanges(String statusLogRanges) {
        this.statusLogRanges = statusLogRanges;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TandemMassSpectrometrySample that = (TandemMassSpectrometrySample) o;

        if (getMs1Spectra() != that.getMs1Spectra()) return false;
        if (getMsnSpectra() != that.getMsnSpectra()) return false;
        if (Double.compare(that.getRunTimeInSeconds(), getRunTimeInSeconds()) != 0) return false;
        if (getErrorLog() != null ? !getErrorLog().equals(that.getErrorLog()) : that.getErrorLog() != null)
            return false;
        if (getFile() != null ? !getFile().equals(that.getFile()) : that.getFile() != null) return false;
        if (getInstrumentMethod() != null ? !getInstrumentMethod().equals(that.getInstrumentMethod()) : that.getInstrumentMethod() != null)
            return false;
        if (getInstrumentSerialNumber() != null ? !getInstrumentSerialNumber().equals(that.getInstrumentSerialNumber()) : that.getInstrumentSerialNumber() != null)
            return false;
        if (getLastModified() != null ? !getLastModified().equals(that.getLastModified()) : that.getLastModified() != null)
            return false;
        if (getSampleInformation() != null ? !getSampleInformation().equals(that.getSampleInformation()) : that.getSampleInformation() != null)
            return false;
        if (getStartTime() != null ? !getStartTime().equals(that.getStartTime()) : that.getStartTime() != null)
            return false;
        if (getStatusLogRanges() != null ? !getStatusLogRanges().equals(that.getStatusLogRanges()) : that.getStatusLogRanges() != null)
            return false;
        if (getTuneMethod() != null ? !getTuneMethod().equals(that.getTuneMethod()) : that.getTuneMethod() != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = getFile() != null ? getFile().hashCode() : 0;
        result = 31 * result + (getLastModified() != null ? getLastModified().hashCode() : 0);
        result = 31 * result + getMs1Spectra();
        result = 31 * result + getMsnSpectra();
        result = 31 * result + (getInstrumentSerialNumber() != null ? getInstrumentSerialNumber().hashCode() : 0);
        result = 31 * result + (getStartTime() != null ? getStartTime().hashCode() : 0);
        temp = getRunTimeInSeconds() != +0.0d ? Double.doubleToLongBits(getRunTimeInSeconds()) : 0L;
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + (getTuneMethod() != null ? getTuneMethod().hashCode() : 0);
        result = 31 * result + (getInstrumentMethod() != null ? getInstrumentMethod().hashCode() : 0);
        result = 31 * result + (getSampleInformation() != null ? getSampleInformation().hashCode() : 0);
        result = 31 * result + (getErrorLog() != null ? getErrorLog().hashCode() : 0);
        result = 31 * result + (getStatusLogRanges() != null ? getStatusLogRanges().hashCode() : 0);
        return result;
    }
}


