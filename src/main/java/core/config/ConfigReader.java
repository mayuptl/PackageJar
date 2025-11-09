package core.config;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Utility class for reading configuration properties from various sources.
 * It supports two main modes of operation:
 * * 1. **Cached Access (get...Prop):** Reads the core 'config.properties' file once on
 * startup, merging defaults from the JAR with overrides from the external classpath.
 * 2. **File System Access (get...PropFromPath):** Reads properties ad-hoc from a
 * specified file path every time the method is called, for specific, non-core configs.
 */
public final class ConfigReader {
    // We use the same name for the resource inside the JAR and the consumer override
    private static final String RESOURCE_PATH = "config.properties";

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
     * Loads the core properties with the correct merging priority:
     * 1. Load the default properties from INSIDE the current JAR (lower priority).
     * 2. Overlay those defaults with the properties from the external consumer classpath (higher priority).
     * The result is stored in the static CACHED_PROPS object.
     */
    private static void loadMergedProperties() {
        // --- STEP 1: Load JAR Defaults (Lowest Priority) ---
        // We use ConfigReader.class.getResourceAsStream to specifically look inside the current JAR.
        //System.out.println("DEBUG: loadMergedProperties() called by: " + Thread.currentThread().getStackTrace()[3]);
        try (InputStream defaultsStream = ConfigReader.class.getResourceAsStream("/" + RESOURCE_PATH)) {
            if (defaultsStream != null) {
                CACHED_PROPS.load(defaultsStream);
              //  System.out.println("INFO: Loaded default properties from inside JAR.");
            }
        } catch (IOException e) {
            // Log this, but it shouldn't stop the application if the config file is missing inside the JAR.
            System.err.println("WARNING: Failed to load default config from inside JAR: " + e.getMessage());
        }

        // --- STEP 2: Load Consumer Overrides (Highest Priority) ---
        // We use the Thread Context ClassLoader to find the file on the external classpath.
        // If found, it overwrites existing keys in CACHED_PROPS.
        try (InputStream overrideStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(RESOURCE_PATH)) {
            if (overrideStream != null) {
                Properties overrideProps = new Properties();
                overrideProps.load(overrideStream);

                // Merge the overrides into the cached properties, consumer keys will overwrite defaults
                CACHED_PROPS.putAll(overrideProps);
                //System.out.println("INFO: Overlaid properties with consumer config from classpath.");
            }
            // --- STEP 3: Map Config Keys to Log4j System Properties (Simplified) ---
            // This process MUST happen before Log4j reads its configuration file.
            // 1. Map LOG_FILE_DIR (Config Key) to log4j2.logDir (System Property Key)
            injectSystemProperty("LOG_FILE_DIR", "log4j2.logDir");
            // 2. Map LOG_FILE_NAME (Config Key) to log4j2.fileName (System Property Key)
            injectSystemProperty("LOG_FILE_NAME", "log4j2.fileName");
        } catch (IOException e) {
            // This is acceptable; no consumer override file was found.
            System.out.println("INFO: No consumer config.properties file found on external classpath. Using defaults.");
        }
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
            //System.out.println("INFO: Set Log4j System Property " + systemPropertyKey + " = " + configValue);
        } else if (System.getProperty(systemPropertyKey) != null) {
            // Case 2: System Property already set externally -> KEEP EXISTING VALUE
          //  System.out.println("INFO: System Property " + systemPropertyKey + " already set externally. Keeping existing value.");
        } else {
            // Case 3: Config key not found -> WARN
            System.err.println("Configuration key '" + configKey + "' not found in config.properties.");
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
        // Boolean.parseBoolean is very forgiving (only "true" is true), so we don't
        // need extra number format handling.
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