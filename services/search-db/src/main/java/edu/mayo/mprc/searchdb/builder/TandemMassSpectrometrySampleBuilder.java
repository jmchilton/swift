package edu.mayo.mprc.searchdb.builder;

import edu.mayo.mprc.searchdb.dao.TandemMassSpectrometrySample;

import java.io.File;
import java.util.Date;

/**
 * @author Roman Zenka
 */
public class TandemMassSpectrometrySampleBuilder implements Builder<TandemMassSpectrometrySample> {
    private File file;
    private Date lastModified;
    private int ms1Spectra;
    private int msnSpectra;
    private String instrumentSerialNumber;
    private Date startTime;
    private double runTimeInSeconds;
    private String tuneMethod;
    private String instrumentMethod;
    private String sampleInformation;
    private String errorLog;
    private String statusLogRanges;

    @Override
    public TandemMassSpectrometrySample build() {
        return new TandemMassSpectrometrySample(file, lastModified, ms1Spectra, msnSpectra, instrumentSerialNumber,
                startTime, runTimeInSeconds, tuneMethod, instrumentMethod, sampleInformation, errorLog, statusLogRanges);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TandemMassSpectrometrySampleBuilder that = (TandemMassSpectrometrySampleBuilder) o;

        if (ms1Spectra != that.ms1Spectra) return false;
        if (msnSpectra != that.msnSpectra) return false;
        if (Double.compare(that.runTimeInSeconds, runTimeInSeconds) != 0) return false;
        if (errorLog != null ? !errorLog.equals(that.errorLog) : that.errorLog != null) return false;
        if (file != null ? !file.equals(that.file) : that.file != null) return false;
        if (instrumentMethod != null ? !instrumentMethod.equals(that.instrumentMethod) : that.instrumentMethod != null)
            return false;
        if (instrumentSerialNumber != null ? !instrumentSerialNumber.equals(that.instrumentSerialNumber) : that.instrumentSerialNumber != null)
            return false;
        if (lastModified != null ? !lastModified.equals(that.lastModified) : that.lastModified != null) return false;
        if (sampleInformation != null ? !sampleInformation.equals(that.sampleInformation) : that.sampleInformation != null)
            return false;
        if (startTime != null ? !startTime.equals(that.startTime) : that.startTime != null) return false;
        if (statusLogRanges != null ? !statusLogRanges.equals(that.statusLogRanges) : that.statusLogRanges != null)
            return false;
        if (tuneMethod != null ? !tuneMethod.equals(that.tuneMethod) : that.tuneMethod != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = file != null ? file.hashCode() : 0;
        result = 31 * result + (lastModified != null ? lastModified.hashCode() : 0);
        result = 31 * result + ms1Spectra;
        result = 31 * result + msnSpectra;
        result = 31 * result + (instrumentSerialNumber != null ? instrumentSerialNumber.hashCode() : 0);
        result = 31 * result + (startTime != null ? startTime.hashCode() : 0);
        temp = runTimeInSeconds != +0.0d ? Double.doubleToLongBits(runTimeInSeconds) : 0L;
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + (tuneMethod != null ? tuneMethod.hashCode() : 0);
        result = 31 * result + (instrumentMethod != null ? instrumentMethod.hashCode() : 0);
        result = 31 * result + (sampleInformation != null ? sampleInformation.hashCode() : 0);
        result = 31 * result + (errorLog != null ? errorLog.hashCode() : 0);
        result = 31 * result + (statusLogRanges != null ? statusLogRanges.hashCode() : 0);
        return result;
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
}
