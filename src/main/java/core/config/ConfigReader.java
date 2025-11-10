package core.config;

import java.util.Properties;

/**
 * Utility class for reading configuration properties from various sources.
 * It caches the merged properties (JAR defaults + Consumer overrides)
 * loaded by the internal 'Config' class.
 *
 * NOTE: The public 'initialize()' method MUST be called by the consumer project
 * early in the test lifecycle (e.g., in a static block of the base test class)
 * to set critical system properties (like Log4j paths) before the Log4j framework initializes.
 */
public final class ConfigReader {

    // Static instance to hold the merged properties, loaded only once
    private static final Properties CACHED_PROPS = new Properties();
    private static volatile boolean isInitialized = false;

    /**
     * Private constructor to prevent external instantiation of this utility class.
     */
    private ConfigReader() {}

    // Static block to trigger the initial attempt at loading the properties.
    static {
        loadMergedProperties();
    }

    /**
     * Loads the core properties by delegating the merging process to the 'Config' class.
     * This method is idempotent and ensures the properties are loaded once.
     */
    private static void loadMergedProperties() {
        if (isInitialized) return;

        // 1. DELEGATE LOADING: Call the robust loading method from the 'Config' class
        // This handles loading JAR defaults and overriding with consumer's local file.
        Properties loadedProps = Config.getApplicationProperties();

        if (loadedProps == null || loadedProps.isEmpty()) {
            System.err.println("FATAL: Could not load any properties. Check 'Config' class for errors.");
            return;
        }

        // 2. CACHE: Store the robustly loaded and merged properties
        CACHED_PROPS.putAll(loadedProps);
        isInitialized = true;
    }

    /**
     * Public method to ensure configuration is loaded and critical system properties
     * (like Log4j settings) are set BEFORE Log4j attempts to initialize.
     *
     * IMPORTANT: This MUST be called by the consumer project at the very start
     * of their execution (e.g., in a static block of their base test class).
     */
    public static void initialize() {
        if (!isInitialized) {
            loadMergedProperties();
        }

        // --- Inject Config Keys to Log4j System Properties (Crucial Fix for Log4j ERROR) ---
        // This is necessary because Log4j starts before the properties file is guaranteed to be read.
        injectSystemProperty("LOG_FILE_DIR", "log4j2.logDir");
        injectSystemProperty("LOG_FILE_NAME", "log4j2.fileName");

        System.out.println("INFO: ConfigReader initialization complete. Log4j system properties set.");
    }

    /**
     * Helper method to map a configuration key's value to a system property key,
     * but only if the system property has not been set externally.
     */
    private static void injectSystemProperty(String configKey, String systemPropertyKey) {
        final String configValue = CACHED_PROPS.getProperty(configKey);

        // Only set the System Property if it hasn't been set yet
        if (System.getProperty(systemPropertyKey) == null) {
            if (configValue != null) {
                System.setProperty(systemPropertyKey, configValue);
            } else {
                // If a key needed for Log4j is missing, print a severe warning.
                System.err.println("WARNING: Configuration key '" + configKey + "' is missing from merged properties, cannot set Log4j system property.");
            }
        }
    }

    // ====================================================================
    // Remaining Config Access Methods
    // ====================================================================

    /**
     * Retrieves a property value by key from the merged configuration cache.
     * Returns null if the key is not found.
     */
    public static String getProperty(String key) {
        return CACHED_PROPS.getProperty(key);
    }

    /**
     * Retrieves a property value by key, throwing a RuntimeException if the value
     * is missing or empty, as it is considered mandatory.
     */
    public static String getStrProp(String key) {
        String value = CACHED_PROPS.getProperty(key);
        if (value == null || value.trim().isEmpty()) {
            throw new RuntimeException("❌ Missing or empty mandatory property for key: " + key);
        }
        return value.trim();
    }

    /**
     * Retrieves a property value as an integer.
     */
    public static int getIntProp(String key) {
        String value = getStrProp(key);
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw new RuntimeException("❌ Property '" + key + "' is not a valid integer: " + value, e);
        }
    }

    /**
     * Retrieves a property value as a boolean.
     */
    public static boolean getBooleanProp(String key) {
        String value = getStrProp(key);
        return Boolean.parseBoolean(value);
    }

    /**
     * Get a String property value, falling back to a default if the key is missing.
     * Use this for OPTIONAL properties.
     * @param key The property key to look up.
     * @param defaultValue The value to return if the key is missing or empty.
     * @return The property value or the default value.
     */
    public static String getStrProp(String key, String defaultValue) {
        String value = CACHED_PROPS.getProperty(key);
        if (value == null || value.trim().isEmpty()) {
            return defaultValue;
        }
        return value.trim();
    }

    /**
     * Get an integer property value safely, using a default value if the key is missing
     * or the parsed value is invalid.
     * Use this for OPTIONAL properties.
     */
    public static int getIntProp(String key, int defaultValue) {
        String value = getStrProp(key, String.valueOf(defaultValue));

        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            // If the consumer provides an invalid value, log an error and return the default.
            System.err.println("ERROR: Invalid integer format for key: " + key + " -> " + value + ". Using default value: " + defaultValue);
            return defaultValue;
        }
    }
    /**
     * Get a boolean property value safely, using a default value if the key is missing.
     * Use this for OPTIONAL properties.
     */
    public static boolean getBoolProp(String key, boolean defaultValue) {
        String value = getStrProp(key, String.valueOf(defaultValue));
        return Boolean.parseBoolean(value.trim());
    }

    /**
     * Get a boolean property value. Throws RuntimeException if the key is missing.
     * Use this for MANDATORY properties.
     */
    public static boolean getBoolProp(String key) {
        // getStrProp(key) handles missing or empty keys by throwing a RuntimeException.
        // Boolean.parseBoolean is very forgiving (only "true" is true), so we don't
        // need extra number format handling.
        String value = getStrProp(key);
        return Boolean.parseBoolean(value.trim());
    }
}