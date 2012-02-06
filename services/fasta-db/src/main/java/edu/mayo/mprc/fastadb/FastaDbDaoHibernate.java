package edu.mayo.mprc.fastadb;

import com.google.common.base.Preconditions;
import edu.mayo.mprc.MprcException;
import edu.mayo.mprc.daemon.progress.PercentDone;
import edu.mayo.mprc.daemon.progress.ProgressReporter;
import edu.mayo.mprc.database.DaoBase;
import edu.mayo.mprc.database.DatabasePlaceholder;
import edu.mayo.mprc.dbcurator.model.Curation;
import edu.mayo.mprc.fasta.FASTAInputStream;
import edu.mayo.mprc.utilities.FileUtilities;
import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.StatelessSession;

import javax.annotation.Nullable;
import java.io.File;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Collection;

/**
 * @author Roman Zenka
 */
public class FastaDbDaoHibernate extends DaoBase implements FastaDbDao {
    private static final Logger LOGGER = Logger.getLogger(FastaDbDaoHibernate.class);
    // Progress will be checked each X spectra
    public static final int REPORT_FREQUENCY = 100;
    // Only if we reported at least this many milliseconds ago, we will send another update
    public static final int REPORT_EACH_MILLIS = 1000;
    private static final String MAP = "edu/mayo/mprc/fastadb/";
    public static final float PERCENT = 100.0f;

    public FastaDbDaoHibernate() {
    }

    public FastaDbDaoHibernate(final DatabasePlaceholder databasePlaceholder) {
        super(databasePlaceholder);
    }

    @Override
    public ProteinSequence getProteinSequence(final Curation database, final String accessionNumber) {
        Preconditions.checkNotNull(database, "Database has to be specified");
        ProteinSequence sequence = (ProteinSequence) getSession()
                .createQuery("select e.sequence from ProteinDatabaseEntry e where e.database=:database and e.accessionNumber=:accessionNumber")
                .setEntity("database", database)
                .setString("accessionNumber", accessionNumber)
                .uniqueResult();
        return sequence;
    }

    @Override
    public ProteinSequence addProteinSequence(final ProteinSequence proteinSequence) {
        if (proteinSequence.getId() == null) {
            return save(proteinSequence, nullSafeEq("sequence", proteinSequence.getSequence()), false);
        }
        return proteinSequence;
    }

    private ProteinSequence addProteinSequence(final StatelessSession session, final ProteinSequence proteinSequence) {
        if (proteinSequence.getId() == null) {
            return saveStateless(session, proteinSequence, nullSafeEq("sequence", proteinSequence.getSequence()), false);
        }
        return proteinSequence;
    }

    @Override
    public ProteinSequence getProteinSequence(final int proteinId) {
        return (ProteinSequence) getSession().get(ProteinSequence.class, proteinId);
    }

    @Override
    public PeptideSequence addPeptideSequence(final PeptideSequence peptideSequence) {
        if (peptideSequence.getId() == null) {
            return save(peptideSequence, nullSafeEq("sequence", peptideSequence.getSequence()), false);
        }
        return peptideSequence;
    }

    @Override
    public PeptideSequence getPeptideSequence(final int peptideId) {
        return (PeptideSequence) getSession().get(PeptideSequence.class, peptideId);
    }

    @Override
    public long countDatabaseEntries(final Curation database) {
        return (Long) getSession().createQuery("select count(*) from ProteinDatabaseEntry p where p.database=:database").setEntity("database", database)
                .uniqueResult();
    }

    /**
     * This method opens its own stateless session for its duration, so you do not need to call {@link #begin}
     * or {@link #commit} around this method. This makes the method quite special.
     * <p/>
     * If the curation was already previously loaded into the database, the method does nothing.
     *
     * @param database Database to load data for.
     */
    @Override
    public void addFastaDatabase(Curation database, @Nullable ProgressReporter progressReporter) {
        final StatelessSession session = getDatabasePlaceholder().getSessionFactory().openStatelessSession();
        Query entryCount = session.createQuery("select count(*) from ProteinDatabaseEntry p where p.database=:database").setEntity("database", database);
        if ((Long) entryCount.uniqueResult() != 0) {
            // We have loaded the database already
            return;
        }

        final File fasta = database.getFastaFile().getFile();
        final FASTAInputStream stream = new FASTAInputStream(fasta);
        long numSequencesRead = 0;
        long timestamp = System.currentTimeMillis();
        try {
            stream.beforeFirst();
            session.getTransaction().begin();
            while (stream.gotoNextSequence()) {
                numSequencesRead++;
                final String header = stream.getHeader();
                final String sequence = stream.getSequence();
                int space = header.indexOf(' ');
                final String accessionNumber;
                if (space >= 1) {
                    accessionNumber = header.substring(1, space);
                } else {
                    accessionNumber = header.substring(1);
                }
                final ProteinSequence proteinSequence = addProteinSequence(session, new ProteinSequence(sequence));
                final ProteinDatabaseEntry entry = new ProteinDatabaseEntry(database, accessionNumber, proteinSequence);
                // We know that we will never save two identical entries (fasta has each entry unique and we have not
                // loaded the database yet. So no need to check)
                saveStateless(session, entry, null, false);
                if (numSequencesRead % REPORT_FREQUENCY == 0) {
                    long timeNow = System.currentTimeMillis();
                    if (timeNow - timestamp > REPORT_EACH_MILLIS) {
                        if (progressReporter != null) {
                            progressReporter.reportProgress(new PercentDone((float) stream.percentRead() * PERCENT));
                        }
                        LOGGER.info(MessageFormat.format("Loading [{0}] to database: {1,number,#.##} percent done.", fasta.getAbsolutePath(), stream.percentRead() * PERCENT));
                        timestamp = timeNow;
                    }
                }
            }
            LOGGER.info(MessageFormat.format("Loaded [{0}] to database: {1,number} sequences added.", fasta.getAbsolutePath(), numSequencesRead));
            session.getTransaction().commit();
        } catch (Exception e) {
            session.getTransaction().rollback();
            throw new MprcException("Could not add FASTA file to database " + database.getTitle(), e);
        } finally {
            FileUtilities.closeQuietly(stream);
            session.close();
        }
    }

    @Override
    public Collection<String> getHibernateMappings() {
        return Arrays.asList(
                MAP + "PeptideSequence.hbm.xml",
                MAP + "ProteinDatabaseEntry.hbm.xml",
                MAP + "ProteinSequence.hbm.xml"
        );
    }
}
