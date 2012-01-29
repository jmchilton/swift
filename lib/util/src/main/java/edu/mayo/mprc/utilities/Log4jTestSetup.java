package edu.mayo.mprc.utilities;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.util.Enumeration;

/**
 * Set up log4j for unit tests in case it was not configured from the outside.
 *
 * @author Roman Zenka
 */
public class Log4jTestSetup {
    private static boolean configured = false;

    /**
     * Returns true if it appears that log4j have been previously configured. This code
     * checks to see if there are any appenders defined for log4j which is the
     * definitive way to tell if log4j is already initialized
     */
    private static boolean isConfigured() {
        if (configured) {
            return true;
        }
        Enumeration appenders = Logger.getRoot().getAllAppenders();
        if (appenders.hasMoreElements()) {
            configured = true;
            return true;
        } else {
            Enumeration loggers = LogManager.getCurrentLoggers();
            while (loggers.hasMoreElements()) {
                Logger c = (Logger) loggers.nextElement();
                if (c.getAllAppenders().hasMoreElements()) {
                    configured = true;
                    return true;
                }
            }
        }
        return false;
    }

    public synchronized static void configure() {
        if (!isConfigured()) {
            BasicConfigurator.configure();
        }
    }
}
