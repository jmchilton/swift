package edu.mayo.mprc.unimod;

import edu.mayo.mprc.MprcException;
import edu.mayo.mprc.database.Change;
import edu.mayo.mprc.database.DaoBase;
import edu.mayo.mprc.database.DatabasePlaceholder;
import edu.mayo.mprc.utilities.ResourceUtilities;
import org.apache.log4j.Logger;
import org.hibernate.Session;

import java.util.*;

/**
 * Provides access to the unimod set from the database.
 * Since the set is reasonably small, it can be loaded completely in one go, indexed in memory
 * and processed more efficiently.
 * <p/>
 * The DAO allows you to return current modification set, and to upgrade the set from a given file.
 */
public final class UnimodDaoImpl extends DaoBase implements UnimodDao {
    private static final Logger LOGGER = Logger.getLogger(UnimodDaoImpl.class);

    private static final String HBM_DIR = "edu/mayo/mprc/unimod/";

    public UnimodDaoImpl() {
    }

    public UnimodDaoImpl(DatabasePlaceholder databasePlaceholder) {
        super(databasePlaceholder);
    }

    @Override
    public Collection<String> getHibernateMappings() {
        return Arrays.asList(
                HBM_DIR + "Mod.hbm.xml",
                HBM_DIR + "ModSet.hbm.xml",
                HBM_DIR + "ModSpecificity.hbm.xml",
                "edu/mayo/mprc/database/Change.hbm.xml");
    }

    @Override
    public Unimod load() {
        final Session session = getSession();
        try {
            final List<Mod> list = (List<Mod>) allCriteria(Mod.class)
                    .setReadOnly(true)
                    .list();
            Unimod unimod = new Unimod();
            for (Mod mod : list) {
                unimod.add(mod);
                session.evict(mod);
            }
            return unimod;
        } catch (Exception t) {
            throw new MprcException("Cannot load unimod data from database", t);
        }
    }

    @Override
    public UnimodUpgrade upgrade(Unimod unimod, Change request) {
        try {
            final List<Mod> list = (List<Mod>) allCriteria(Mod.class).list();
            UnimodUpgrade upgrade = new UnimodUpgrade();
            upgrade.upgrade(list, unimod, request, getSession());
            return upgrade;
        } catch (Exception t) {
            throw new MprcException("Database upgrade " + (request != null ? request : "") + " failed", t);
        }
    }

    @Override
    public String check(Map<String, String> params) {
        if (countAll(Mod.class) == 0) {
            return "No unimod modifications defined";
        }
        return null;
    }

    @Override
    public void initialize(Map<String, String> params) {
        if (countAll(Mod.class) == 0) {
            Change change = new Change("Installing initial unimod modifications", new Date());
            LOGGER.info(change.getReason());
            Unimod unimod = getDefaultUnimod();

            final UnimodUpgrade upgrade = upgrade(unimod, change);
            LOGGER.debug("Unimod install results: " + upgrade.toString());
        }
    }

    private static Unimod getDefaultUnimod() {
        Unimod unimod = new Unimod();
        try {
            unimod.parseUnimodXML(ResourceUtilities.getStream("classpath:edu/mayo/mprc/unimod/unimod.xml", Unimod.class));
        } catch (Exception t) {
            throw new MprcException("Unable to parse default unimod set", t);
        }
        return unimod;
    }

}
