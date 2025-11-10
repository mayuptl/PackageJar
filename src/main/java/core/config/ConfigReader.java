package core.config;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

// IMPORTANT: This import allows ConfigReader to use your robust Config class.

/**
 * Utility class for reading configuration properties from various sources.
 * It now relies entirely on the separate 'Config' class for robust, merged
 * property loading via the Thread Context ClassLoader (TCCL).
 */
public final class ConfigReader {

    // Static instance to hold the merged properties, loaded only once
    private static final Properties CACHED_PROPS = new Properties();

    // Static block to initialize the properties when the class is loaded
    static {
        loadMergedProperties();
    }

    /**
     * Private constructor to prevent external instantiation of this utility class.
     */
    private ConfigReader() {
        // Utility class: all methods are static.
    }

    /**
     * LOADS the core properties with the correct merging priority by delegating
     * the robust loading and merging process entirely to the 'Config' class.
     * * THIS IS THE MAIN CHANGE: We call Config.getApplicationProperties()
     */
    private static void loadMergedProperties() {
        // 1. DELEGATE LOADING: Call the robust loading method from the 'Config' class
        Properties loadedProps = Config.getApplicationProperties();

        if (loadedProps == null || loadedProps.isEmpty()) {
            System.err.println("FATAL: Could not load any properties. Check 'Config' class for errors.");
            return;
        }

        // 2. CACHE: Store the robustly loaded and merged properties
        CACHED_PROPS.putAll(loadedProps);
        System.out.println("INFO: ConfigReader initialized successfully with " + CACHED_PROPS.size() + " properties.");

        // --- STEP 3: Map Config Keys to Log4j System Properties (Simplified) ---
        injectSystemProperty("LOG_FILE_DIR", "log4j2.logDir");
        injectSystemProperty("LOG_FILE_NAME", "log4j2.fileName");
    }

    /**
     * Helper method to map a configuration key's value to a system property key,
     * but only if the system property has not been set externally.
     *
     * @param configKey The key in the cached properties (e.g., "LOG_FILE_DIR").
     * @param systemPropertyKey The target system property key (e.g., "log4j2.logDir").
     */
    private static void injectSystemProperty(String configKey, String systemPropertyKey) {
        final String configValue = CACHED_PROPS.getProperty(configKey);
        if (configValue != null && System.getProperty(systemPropertyKey) == null) {
            // Case 1: Config key found, System Property NOT set -> SET IT
            System.setProperty(systemPropertyKey, configValue);
        } else if (System.getProperty(systemPropertyKey) == null) {
            // Case 3: Config key not found -> WARN
            System.err.println("Configuration key '" + configKey + "' not found in config.properties for system property mapping.");
        }
    }

    /**
     * Retrieves a property value by key from the merged configuration cache.
     *
     * @param key The key to look up.
     * @return The property value or null if not found.
     */
    public static String getProperty(String key) {
        return CACHED_PROPS.getProperty(key);
    }

    // ====================================================================
    // 1. CACHED PROPERTY ACCESS METHODS (Uses merged config.properties)
    // ====================================================================

    /**
     * Gets a mandatory String property value from the cached core configuration.
     *
     * @param key The property key to look up.
     * @return The non-null, non-empty String property value.
     * @throws RuntimeException if the key is missing or the value is empty.
     */
    public static String getStrProp(String key) {
        String value = CACHED_PROPS.getProperty(key);
        if (value == null || value.trim().isEmpty()) {
            throw new RuntimeException("❌ Missing or empty mandatory property for key: " + key);
        }
        return value.trim();
    }

    /**
     * Gets an optional String property value from the cached core configuration,
     * using a default value if the key is missing or empty.
     *
     * @param key The property key to look up.
     * @param defaultValue The value to return if the key is missing or empty.
     * @return The property value or the provided default value.
     */
    public static String getStrProp(String key, String defaultValue) {
        String value = CACHED_PROPS.getProperty(key);
        if (value == null || value.trim().isEmpty()) {
            return defaultValue;
        }
        return value.trim();
    }
    /**
     * Gets a mandatory integer property value from the cached core configuration.
     *
     * @param key The property key to look up.
     * @return The parsed integer value.
     * @throws RuntimeException if the key is missing, empty, or the value cannot be parsed as an integer.
     */
    public static int getIntProp(String key) {
        String value = getStrProp(key); // Will throw RuntimeException if key is missing/empty

        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            throw new RuntimeException("❌ Invalid integer value for mandatory key: " + key + " -> " + value, e);
        }
    }
    /**
     * Gets an optional integer property value from the cached core configuration,
     * using a default value if the key is missing or invalid.
     *
     * @param key The property key to look up.
     * @param defaultValue The value to return if the key is missing or invalid.
     * @return The parsed integer value or the provided default value.
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
     * Gets a mandatory boolean property value from the cached core configuration.
     *
     * @param key The property key to look up.
     * @return The parsed boolean value (true if "true", false otherwise).
     * @throws RuntimeException if the key is missing or empty.
     */
    public static boolean getBoolProp(String key) {
        // getStrProp(key) handles missing or empty keys by throwing a RuntimeException.
        String value = getStrProp(key);
        return Boolean.parseBoolean(value.trim());
    }
    /**
     * Gets an optional boolean property value from the cached core configuration,
     * using a default value if the key is missing or empty.
     *
     * @param key The property key to look up.
     * @param defaultValue The value to return if the key is missing or empty.
     * @return The parsed boolean value or the provided default value.
     */
    public static boolean getBoolProp(String key, boolean defaultValue) {
        String value = getStrProp(key, String.valueOf(defaultValue));
        return Boolean.parseBoolean(value.trim());
    }
    // ====================================================================
    // 2. FILE SYSTEM ACCESS METHODS (Reads from specified path, not cached)
    // ====================================================================
    /**
     * Helper to read properties from a project-level directory (File System).
     * The path is resolved relative to the directory where the JVM process starts (usually project root).
     *
     * @param filePath The relative path from the project root (e.g., "test-config/config.properties").
     * @return The loaded Properties object.
     * @throws IOException If the file is not found or cannot be read.
     */
    private static Properties loadFromFileSystem(String filePath) throws IOException {
        Properties props = new Properties();

        try (FileInputStream fis = new FileInputStream(filePath)) {
            props.load(fis);
        } catch (FileNotFoundException e) {
            throw new IOException("❌ Config file not found: " + filePath +
                    "\n➡ Ensure the path is correct and the file exists.", e);
        } catch (IOException e) {
            throw new IOException("❌ Failed to load config file: " + filePath, e);
        }

        return props;
    }
    /**
     * Reads a mandatory String property from a specified file path.
     *
     * @param key The property key to look up.
     * @param filePath The file system path to the properties file.
     * @return The non-null, non-empty String property value.
     * @throws RuntimeException if the file cannot be read, the key is missing, or the value is empty.
     */
    public static String getStrPropFromPath(String key, String filePath) {
        try {
            Properties prop = loadFromFileSystem(filePath);
            String value = prop.getProperty(key);

            if (value == null || value.trim().isEmpty()) {
                throw new RuntimeException("❌ Missing or empty property for key: " + key +
                        " in file: " + filePath);
            }

            return value.trim();

        } catch (IOException e) {
            throw new RuntimeException("❌ Unable to read property file: " + filePath, e);
        }
    }
    /**
     * Reads an optional String property from a specified file path,
     * returning a default value if the key is missing or empty.
     *
     * @param key The property key to look up.
     * @param filePath The file system path to the properties file.
     * @param defaultValue The value to return if the key is missing or empty.
     * @return The property value or the provided default value.
     * @throws RuntimeException if the file cannot be read.
     */
    public static String getStrPropFromPath(String key, String filePath, String defaultValue) {
        try {
            Properties prop = loadFromFileSystem(filePath);
            String value = prop.getProperty(key);

            if (value == null || value.trim().isEmpty()) {
                return defaultValue;
            }

            return value.trim();

        } catch (IOException e) {
            throw new RuntimeException("❌ Unable to read property file: " + filePath, e);
        }
    }
    /**
     * Reads a mandatory integer property from a specified file path.
     *
     * @param key The property key to look up.
     * @param filePath The file system path to the properties file.
     * @return The parsed integer value.
     * @throws RuntimeException if the file cannot be read, the key is missing, or the value is not a valid integer.
     */
    public static int getIntPropFromPath(String key, String filePath) {
        String value = getStrPropFromPath(key, filePath);
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            throw new RuntimeException("❌ Invalid integer value for key: " + key +
                    " in file: " + filePath + " -> " + value);
        }
    }
    /**
     * Reads an optional integer property from a specified file path,
     * returning a default value if the key is missing or invalid.
     *
     * @param key The property key to look up.
     * @param filePath The file system path to the properties file.
     * @param defaultValue The value to return if the key is missing or invalid.
     * @return The parsed integer value or the provided default value.
     * @throws RuntimeException if the file cannot be read.
     */
    public static int getIntPropFromPath(String key, String filePath, int defaultValue) {
        // Use the optional String method for safety
        String value = getStrPropFromPath(key, filePath, String.valueOf(defaultValue));

        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            System.err.println("ERROR: Invalid integer format for key: " + key +
                    " in file: " + filePath + " -> " + value + ". Using default: " + defaultValue);
            return defaultValue;
        }
    }
    /**
     * Reads a mandatory boolean property from a specified file path.
     *
     * @param key The property key to look up.
     * @param filePath The file system path to the properties file.
     * @return The parsed boolean value (true if "true", false otherwise).
     * @throws RuntimeException if the file cannot be read, or the key is missing or empty.
     */
    public static boolean getBoolPropFromPath(String key, String filePath) {
        String value = getStrPropFromPath(key, filePath);
        return Boolean.parseBoolean(value.trim());
    }
    /**
     * Reads an optional boolean property from a specified file path,
     * returning a default value if the key is missing or empty.
     *
     * @param key The property key to look up.
     * @param filePath The file system path to the properties file.
     * @param defaultValue The value to return if the key is missing or empty.
     * @return The parsed boolean value or the provided default value.
     * @throws RuntimeException if the file cannot be read.
     */
    public static boolean getBoolPropFromPath(String key, String filePath, boolean defaultValue) {
        // Use the optional String method for safety
        String value = getStrPropFromPath(key, filePath, String.valueOf(defaultValue));
        return Boolean.parseBoolean(value.trim());
    }
}