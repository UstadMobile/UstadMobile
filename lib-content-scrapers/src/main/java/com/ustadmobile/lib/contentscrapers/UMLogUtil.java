package com.ustadmobile.lib.contentscrapers;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;

public class UMLogUtil {

    private static final Logger LOG = LogManager.getRootLogger();

    public static void logTrace(String message) {
        LOG.log(Level.TRACE, message);
    }

    public static void logDebug(String message) {
        LOG.log(Level.DEBUG, message);
    }

    public static void logInfo(String message) {
        LOG.log(Level.INFO, message);
    }

    public static void logError(String message) {
        LOG.log(Level.ERROR, message);
    }

    public static void logWarn(String message) {
        LOG.log(Level.WARN, message);
    }

    public static void logFatal(String message) {
        LOG.log(Level.FATAL, message);
    }

    public static void setLevel(String level) {
        Level logLevel = Level.ERROR;
        switch (level.toUpperCase()) {
            case "TRACE":
                logLevel = Level.TRACE;
                break;
            case "DEBUG":
                logLevel = Level.DEBUG;
                break;
            case "INFO":
                logLevel = Level.INFO;
                break;
            case "ERROR":
                logLevel = Level.ERROR;
                break;
            case "WARN":
                logLevel = Level.WARN;
                break;
            case "FATAL":
                logLevel = Level.FATAL;
                break;
        }
        Configurator.setRootLevel(logLevel);
    }
}
