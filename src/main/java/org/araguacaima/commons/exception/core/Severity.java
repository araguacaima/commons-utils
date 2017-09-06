package org.araguacaima.commons.exception.core;

import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Severity of the Log statement
 *
 * @author Alejandro Manuel Mendez Araguacaima (AMMA)
 * Changes:<br>
 * <ul>
 * <li> 2014-11-26 (AMMA)  Creacion de la clase. </li>
 * </ul>
 */
public class Severity implements Comparable, Serializable {

    public static final int DEBUG_INT = 5;
    public static final Severity DEBUG = new Severity("DEBUG", DEBUG_INT);
    public static final int ERROR_INT = 2;
    public static final Severity ERROR = new Severity("ERROR", ERROR_INT);
    public static final int FATAL_INT = 1;
    // This follows the Enumeration Pattern, so we can get type-checking at compiler time
    public static final Severity FATAL = new Severity("FATAL", FATAL_INT);
    public static final int INFO_INT = 4;
    public static final Severity INFO = new Severity("INFO", INFO_INT);
    public static final int WARNING_INT = 3;
    public static final Severity WARNING = new Severity("WARNING", WARNING_INT);
    private static final long serialVersionUID = 611670406564212155L;
    // The severities collections is lazy instantiated (check getSeverities())
    private static Map<String, Severity> severities = null;
    private final int level;
    private final String name;

    private Severity(String name, int level) {
        this.name = name;
        this.level = level;
    }

    /**
     * Returns an <b>unordered</b> list of the Severities
     *
     * @return An unordered list of the Severities
     */
    public static Collection getSeverities() {
        if (severities == null) {
            initMap();
        }

        return severities.entrySet();
    }

    private static void initMap() {
        severities = new HashMap<>(6);
        severities.put(DEBUG.getName(), DEBUG);
        severities.put(INFO.getName(), INFO);
        severities.put(WARNING.getName(), WARNING);
        severities.put(ERROR.getName(), ERROR);
        severities.put(FATAL.getName(), FATAL);
    }

    public String getName() {
        return name;
    }

    public static Severity getSeverity(String name) {
        if (severities == null) {
            initMap();
        }

        return severities.get(name.toUpperCase());
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Severity severity = (Severity) o;

        return level == severity.level;
    }

    public boolean greaterThan(Severity severity) {
        return this.compareTo(severity) > 0;
    }

    public int compareTo(Object o) {
        Severity severity = (Severity) o;

        return severity.hashCode() - this.hashCode();
    }

    public int hashCode() {
        return this.getIntLevel();
    }

    public int getIntLevel() {
        return level;
    }

    public boolean lessThan(Severity severity) {
        return this.compareTo(severity) < 0;
    }

    private Object readResolve()
            throws ObjectStreamException {
        switch (level) {
            case FATAL_INT:
                return FATAL;

            case WARNING_INT:
                return WARNING;

            case ERROR_INT:
                return ERROR;

            case INFO_INT:
                return INFO;

            case DEBUG_INT:
                return DEBUG;

            default:
                throw new ObjectStreamException("Unrecognized severity level " + level) {
                    private static final long serialVersionUID = 3821654125627402340L;
                };
        }
    }

    public String toString() {
        return this.getName();
    }
}
