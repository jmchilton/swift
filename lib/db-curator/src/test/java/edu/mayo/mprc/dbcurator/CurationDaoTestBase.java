package edu.mayo.mprc.dbcurator;

import edu.mayo.mprc.database.DaoTest;
import edu.mayo.mprc.database.FileTokenToDatabaseTranslator;
import edu.mayo.mprc.database.FileType;
import edu.mayo.mprc.dbcurator.model.persistence.CurationDaoImpl;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;

import java.io.File;

/**
 * A base for all {@link edu.mayo.mprc.dbcurator.model.persistence.CurationDao} test cases.
 *
 * @author Roman Zenka
 */
public abstract class CurationDaoTestBase extends DaoTest {
    protected CurationDaoImpl curationDao;

    @BeforeClass()
    public void setup() {
        FileType.initialize(new FileTokenToDatabaseTranslator() {
            @Override
            public String fileToDatabaseToken(File file) {
                return file.getAbsolutePath();
            }

            @Override
            public File databaseTokenToFile(String tokenPath) {
                return new File(tokenPath);
            }
        });

        curationDao = new CurationDaoImpl();
        initializeDatabase(curationDao);
    }

    @AfterClass()
    public void teardown() {
        teardownDatabase();
    }
}
