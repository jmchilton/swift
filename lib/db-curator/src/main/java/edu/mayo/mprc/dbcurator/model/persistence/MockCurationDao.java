package edu.mayo.mprc.dbcurator.model.persistence;

import edu.mayo.mprc.database.Change;
import edu.mayo.mprc.database.Evolvable;
import edu.mayo.mprc.dbcurator.model.Curation;
import edu.mayo.mprc.dbcurator.model.FastaSource;
import edu.mayo.mprc.dbcurator.model.HeaderTransform;
import edu.mayo.mprc.dbcurator.model.SourceDatabaseArchive;
import org.joda.time.DateTime;

import java.util.Date;
import java.util.List;

public final class MockCurationDao implements CurationDao {
	@Override
	public Curation getCuration(int curationID) {
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
	public void addCuration(Curation curation) {

	}

	@Override
	public void save(SourceDatabaseArchive archive) {
	}

	@Override
	public void deleteCuration(Curation curation, Change change) {
	}

	@Override
	public FastaSource getDataSourceByName(String urlString) {
		return null;
	}

	@Override
	public FastaSource getDataSourceByUrl(String urlString) {
		return null;
	}

	@Override
	public SourceDatabaseArchive findSourceDatabaseInExistence(String s, DateTime urlLastMod) {
		return null;
	}

	@Override
	public HeaderTransform getHeaderTransformByUrl(String url) {
		return null;
	}

	@Override
	public HeaderTransform getHeaderTransformByName(String name) {
		return null;
	}

	@Override
	public List<Curation> getMatchingCurations(Curation templateCuration, Date earliestRunDate, Date latestRunDate) {
		return null;
	}

	@Override
	public Curation findCuration(String shortDbName) {
		return null;
	}

	@Override
	public Curation getCurationByShortName(String uniqueName) {
		return null;
	}

	@Override
	public List<Curation> getCurationsByShortname(String curationShortName) {
		return null;
	}

	@Override
	public List<Curation> getCurationsByShortname(String shortname, boolean ignoreCase) {
		return null;
	}

	@Override
	public List<FastaSource> getCommonSources() {
		return null;
	}

	@Override
	public void addHeaderTransform(HeaderTransform sprotTrans) {
	}

	@Override
	public List<HeaderTransform> getCommonHeaderTransforms() {
		return null;
	}

	@Override
	public long countAll(Class<? extends Evolvable> clazz) {
		return 0;
	}

	@Override
	public long rowCount(Class<?> clazz) {
		return 0;
	}

	@Override
	public Curation addLegacyCuration(String legacyName) {
		return null;
	}

	@Override
	public void addFastaSource(FastaSource source) {
	}

	@Override
	public void flush() {
	}
}
