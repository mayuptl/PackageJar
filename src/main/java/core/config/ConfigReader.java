package core.config;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ConfigReader {
    // We use the same name for the resource inside the JAR and the consumer override
    private static final String RESOURCE_PATH = "config.properties";

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
        // --- STEP 1: Load JAR Defaults (Lowest Priority) ---
        // We use ConfigReader.class.getResourceAsStream to specifically look inside the current JAR.
        try (InputStream defaultsStream = ConfigReader.class.getResourceAsStream("/" + RESOURCE_PATH)) {
            if (defaultsStream != null) {
                CACHED_PROPS.load(defaultsStream);
                System.out.println("INFO: Loaded default properties from inside JAR.");
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
                System.out.println("INFO: Overlaid properties with consumer config from classpath.");
            }
        } catch (IOException e) {
            // This is acceptable; no consumer override file was found.
            System.out.println("INFO: No consumer config.properties file found on external classpath. Using defaults.");
        }
    }

    // The methods below are simplified to use the CACHED_PROPS object directly.

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
    /**
     * Helper to read properties from a project-level directory (File System).
     * The path is resolved relative to the directory where the JVM process starts (usually project root).
     *
     * @param filePath The relative path from the project root (e.g., "test-config/config.properties").
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