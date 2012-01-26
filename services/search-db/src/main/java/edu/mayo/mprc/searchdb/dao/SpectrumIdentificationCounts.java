package edu.mayo.mprc.searchdb.dao;

/**
 * Count of identified spectra, split by the spectrum charge.
 *
 * @author Roman Zenka
 */
public final class SpectrumIdentificationCounts {
    /**
     * Number of identified spectra (total, not unique)
     */
    private int numberOfIdentifiedSpectra;

    /**
     * Number of identified +1H spectra
     */
    private int numberOfIdentified1HSpectra;

    /**
     * Number of identified +2H spectra
     */
    private int numberOfIdentified2HSpectra;

    /**
     * Number of identified +3H spectra
     */
    private int numberOfIdentified3HSpectra;

    /**
     * Number of identified +4H spectra
     */
    private int numberOfIdentified4HSpectra;

    public SpectrumIdentificationCounts() {
    }

    /**
     * A new spectrum appeared. Based on the charege, update the spectrum statistics
     *
     * @param spectrumCharge Charge of the spectrum.
     */
    public void addSpectrum(int spectrumCharge) {
        numberOfIdentifiedSpectra++;
        switch (spectrumCharge) {
            case 1:
                numberOfIdentified1HSpectra++;
                break;
            case 2:
                numberOfIdentified2HSpectra++;
                break;
            case 3:
                numberOfIdentified3HSpectra++;
                break;
            case 4:
                numberOfIdentified4HSpectra++;
                break;
        }
    }

    public int getNumberOfIdentifiedSpectra() {
        return numberOfIdentifiedSpectra;
    }

    public void setNumberOfIdentifiedSpectra(int numberOfIdentifiedSpectra) {
        this.numberOfIdentifiedSpectra = numberOfIdentifiedSpectra;
    }

    public int getNumberOfIdentified1HSpectra() {
        return numberOfIdentified1HSpectra;
    }

    public void setNumberOfIdentified1HSpectra(int numberOfIdentified1HSpectra) {
        this.numberOfIdentified1HSpectra = numberOfIdentified1HSpectra;
    }

    public int getNumberOfIdentified2HSpectra() {
        return numberOfIdentified2HSpectra;
    }

    public void setNumberOfIdentified2HSpectra(int numberOfIdentified2HSpectra) {
        this.numberOfIdentified2HSpectra = numberOfIdentified2HSpectra;
    }

    public int getNumberOfIdentified3HSpectra() {
        return numberOfIdentified3HSpectra;
    }

    public void setNumberOfIdentified3HSpectra(int numberOfIdentified3HSpectra) {
        this.numberOfIdentified3HSpectra = numberOfIdentified3HSpectra;
    }

    public int getNumberOfIdentified4HSpectra() {
        return numberOfIdentified4HSpectra;
    }

    public void setNumberOfIdentified4HSpectra(int numberOfIdentified4HSpectra) {
        this.numberOfIdentified4HSpectra = numberOfIdentified4HSpectra;
    }
}
