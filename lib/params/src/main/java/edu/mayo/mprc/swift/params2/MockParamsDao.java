package edu.mayo.mprc.swift.params2;

import edu.mayo.mprc.MprcException;
import edu.mayo.mprc.database.Change;
import edu.mayo.mprc.unimod.ModSet;
import edu.mayo.mprc.workspace.User;

import java.util.List;
import java.util.Map;

/**
 * Static version of ParamsDao - does not enable any modifications, only serves default test values.
 */
public final class MockParamsDao implements ParamsDao {


	@Override
	public List<IonSeries> ionSeries() {
		return IonSeries.getInitial();
	}

	private void notModifiable() {
		throw new MprcException("MockParamsDao is not modifiable, method not implemented");
	}

	@Override
	public void addIonSeries(IonSeries ionSeries, Change creation) {
		notModifiable();
	}

	@Override
	public IonSeries updateIonSeries(IonSeries ionSeries, Change creation) {
		notModifiable();
		return null;
	}

	@Override
	public void deleteIonSeries(IonSeries ionSeries, Change deletion) {
		notModifiable();
	}

	@Override
	public List<Instrument> instruments() {
		return Instrument.getInitial();
	}

	@Override
	public Instrument getInstrumentByName(String name) {
		for (Instrument instrument : Instrument.getInitial()) {
			if (instrument.getName().equals(name)) {
				return instrument;
			}
		}
		return null;
	}

	@Override
	public Instrument addInstrument(Instrument instrument, Change change) {
		notModifiable();
		return null;
	}

	@Override
	public Instrument updateInstrument(Instrument instrument, Change change) {
		notModifiable();
		return null;
	}

	@Override
	public void deleteInstrument(Instrument instrument, Change change) {
		notModifiable();
	}

	@Override
	public List<Protease> proteases() {
		return Protease.getInitial();
	}

	@Override
	public Protease getProteaseByName(String name) {
		for (Protease protease : Protease.getInitial()) {
			if (protease.getName().equals(name)) {
				return protease;
			}
		}
		return null;
	}

	@Override
	public void addProtease(Protease protease, Change change) {
		notModifiable();
	}

	@Override
	public Protease updateProtease(Protease protease, Change change) {
		notModifiable();
		return null;
	}

	@Override
	public void deleteProtease(Protease protease, Change change) {
		notModifiable();
	}

	@Override
	public StarredProteins addStarredProteins(StarredProteins starredProteins) {
		notModifiable();
		return null;
	}

	@Override
	public ExtractMsnSettings addExtractMsnSettings(ExtractMsnSettings extractMsnSettings) {
		notModifiable();
		return null;
	}

	@Override
	public ScaffoldSettings addScaffoldSettings(ScaffoldSettings scaffoldSettings) {
		notModifiable();
		return null;
	}

	@Override
	public ModSet updateModSet(ModSet modSet) {
		notModifiable();
		return null;
	}

	@Override
	public SearchEngineParameters addSearchEngineParameters(SearchEngineParameters parameters) {
		notModifiable();
		return null;
	}

	@Override
	public SearchEngineParameters getSearchEngineParameters(int key) {
		return null;
	}

	@Override
	public List<SavedSearchEngineParameters> savedSearchEngineParameters() {
		return null;
	}

	@Override
	public SavedSearchEngineParameters getSavedSearchEngineParameters(int key) {
		return null;
	}

	@Override
	public SavedSearchEngineParameters findSavedSearchEngineParameters(String name) {
		return null;
	}

	@Override
	public SavedSearchEngineParameters addSavedSearchEngineParameters(SavedSearchEngineParameters params, Change change) {
		notModifiable();
		return null;
	}

	@Override
	public void deleteSavedSearchEngineParameters(SavedSearchEngineParameters params, Change change) {
		notModifiable();
	}

	@Override
	public SavedSearchEngineParameters findBestSavedSearchEngineParameters(SearchEngineParameters parameters, User user) {
		return null;
	}

	@Override
	public SearchEngineParameters mergeParameterSet(SearchEngineParameters ps) {
		return null;
	}

	@Override
	public void begin() {
	}

	@Override
	public void commit() {
	}

	@Override
	public void rollback() {
	}

	@Override
	public String check(Map<String, String> params) {
		return null;
	}

	@Override
	public void initialize(Map<String, String> params) {
	}
}
