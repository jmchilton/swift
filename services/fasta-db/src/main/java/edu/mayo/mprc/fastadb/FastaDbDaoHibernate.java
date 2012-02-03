package edu.mayo.mprc.fastadb;

import com.google.common.base.Preconditions;
import edu.mayo.mprc.MprcException;
import edu.mayo.mprc.database.DaoBase;
import edu.mayo.mprc.dbcurator.model.Curation;
import edu.mayo.mprc.fasta.FASTAInputStream;
import edu.mayo.mprc.utilities.FileUtilities;
import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.StatelessSession;

import java.io.File;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Collection;

/**
 * @author Roman Zenka
 */
public class FastaDbDaoHibernate extends DaoBase implements FastaDbDao {
    private static final Logger LOGGER = Logger.getLogger(FastaDbDaoHibernate.class);
    private final String MAP = "edu/mayo/mprc/fastadb/";

    @Override
    public ProteinSequence getProteinSequence(Curation database, String accessionNumber) {
        Preconditions.checkNotNull(database, "Database has to be specified");
        ProteinSequence sequence = (ProteinSequence) getSession()
                .createQuery("select e.sequence from ProteinDatabaseEntry e where e.database=:database and e.accessionNumber=:accessionNumber")
                .setEntity("database", database)
                .setString("accessionNumber", accessionNumber)
                .uniqueResult();
        return sequence;
    }

    @Override
    public ProteinSequence addProteinSequence(ProteinSequence proteinSequence) {
        if (proteinSequence.getId() == null) {
            return save(proteinSequence, nullSafeEq("sequence", proteinSequence.getSequence()), false);
        }
        return proteinSequence;
    }

    private ProteinSequence addProteinSequence(StatelessSession session, ProteinSequence proteinSequence) {
        if (proteinSequence.getId() == null) {
            return saveStateless(session, proteinSequence, nullSafeEq("sequence", proteinSequence.getSequence()), false);
        }
        return proteinSequence;
    }

    @Override
    public ProteinSequence getProteinSequence(int proteinId) {
        return (ProteinSequence) getSession().get(ProteinSequence.class, proteinId);
    }

    @Override
    public PeptideSequence addPeptideSequence(PeptideSequence peptideSequence) {
        if (peptideSequence.getId() == null) {
            return save(peptideSequence, nullSafeEq("sequence", peptideSequence.getSequence()), false);
        }
        return peptideSequence;
    }

    @Override
    public PeptideSequence getPeptideSequence(int peptideId) {
        return (PeptideSequence) getSession().get(PeptideSequence.class, peptideId);
    }

    @Override
    public long countDatabaseEntries(Curation database) {
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
    public void addFastaDatabase(Curation database) {
        final StatelessSession session = getDatabasePlaceholder().getSessionFactory().openStatelessSession();
        Query entryCount = session.createQuery("select count(*) from ProteinDatabaseEntry p where p.database=:database").setEntity("database", database);
        if ((Long) entryCount.uniqueResult() != 0) {
            // We have loaded the database already
            return;
        }

        final File fasta = database.getFastaFile().getFile();
        final FASTAInputStream stream = new FASTAInputStream(fasta);
        long numSequencesRead = 0;
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
                if (numSequencesRead % 10000 == 0) {
                    LOGGER.info(MessageFormat.format("Loading [{0}] to database: {1,number,#.##} percent done.", fasta.getAbsolutePath(), stream.percentRead() * 100));
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
