package core.config;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ConfigReader {
    // Static instance to hold the merged properties, loaded only once
    private static final Properties CACHED_PROPS = new Properties();
    // Static block to initialize the properties when the class is loaded
    static {
        loadMergedProperties();
    }
    /**
     * Loads the properties with the correct merging priority:
     * 1. Load the default properties from INSIDE the current JAR (lower priority).
     * 2. Overlay those defaults with the properties from the external consumer classpath (higher priority).
     * The result is stored in the static CACHED_PROPS object.
     */
    private static void loadMergedProperties() {
        InputStream defaultsStream = null;
        for (int i = 0; i < 2; i++) {
            try {
                defaultsStream = ConfigReader.class.getResourceAsStream("/default-config.properties");
                if (defaultsStream != null) {
                    CACHED_PROPS.load(defaultsStream);
                    //System.out.println("INFO: Loaded default-config.properties from inside JAR.\n");
                    break;
                } else {
                    System.err.println("WARNING: default-config.properties not found inside JAR (attempt " + (i + 1) + ")");
                }
            } catch (IOException e) {
                System.err.println("WARNING: Failed to load default-config.properties from inside JAR (attempt " + (i + 1) + ")");
            }
        }
        //Step 2 : Load consumer properties
        try (InputStream overrideStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("config.properties")) {
            if (overrideStream != null) {
                Properties overrideProps = new Properties();
                overrideProps.load(overrideStream);
                for (String key : overrideProps.stringPropertyNames()) {
                    String defaultValue = CACHED_PROPS.getProperty(key);
                    String overrideValue = overrideProps.getProperty(key);
                    if (defaultValue != null && !defaultValue.equals(overrideValue)) {
                        System.out.printf("%n");
                        System.out.printf("INFO: User's config.properties value overrides JAR's value as belwo:%n");
                        System.out.printf("%s :(New value) %s | (Old value): %s %n", key, overrideValue, defaultValue);
                    }
                }
                CACHED_PROPS.putAll(overrideProps);
            }
        } catch (IOException e) {
            System.out.println("INFO: Using default values because no config.properties file found or no keys match with JAR's default-config.properties file keys");
        }
        injectSystemProperty("LOG_FILE_DIR", "log4j2.logDir");
        injectSystemProperty("LOG_FILE_NAME", "log4j2.fileName");
       //System.out.println("INFO: ConfigReader initialization complete. Log4j system properties set.");
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
    /**
     * Get a String property value. Throws RuntimeException if the key is missing.
     * Use this for MANDATORY properties.
     */
    public static String getStrProp(String key) {
        String value = CACHED_PROPS.getProperty(key);
        if (value == null || value.trim().isEmpty()) {
            throw new RuntimeException("❌ Missing or empty mandatory property for key: " + key);
        }
        return value.trim();
    }

    /**
     * Get a String property value, falling back to a default if the key is missing.
     * Use this for OPTIONAL properties.
     *
     * @param key          The property key to look up.
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
     * Get an integer property value. Throws RuntimeException if the key is missing
     * or the parsed value is invalid.
     * Use this for MANDATORY properties.
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
     * Get a boolean property value. Throws RuntimeException if the key is missing.
     * Use this for MANDATORY properties.
     */
    public static boolean getBoolProp(String key) {
        String value = getStrProp(key);
        return Boolean.parseBoolean(value.trim());
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
     * Helper to read properties from a project-level directory (File System).
     * The path is resolved relative to the directory where the JVM process starts (usually project root).
     *
     * @param filePath The relative path from the project root (e.g., "test-config/default-config.properties").
     * @return The loaded Properties object.
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
     * Reads a String property safely from a given file path.
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
     * Reads an integer property from a given file path.
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
     * Reads a boolean property from a given file path.
     */
    public static boolean getBoolPropFromPath(String key, String filePath) {
        String value = getStrPropFromPath(key, filePath);
        return Boolean.parseBoolean(value.trim());
    }
}