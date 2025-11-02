package core.config;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ConfigReader {
    private static final String RESOURCE_PATH = "config.properties";
    /**
     * Loads the properties file from the classpath.
     */
    private static Properties loadPropertiesAlways() throws IOException {
        Properties props = new Properties();

        try (InputStream is = ConfigReader.class.getClassLoader().getResourceAsStream(RESOURCE_PATH)) {
            if (is == null) {
                throw new IOException("❌ Resource not found: " + RESOURCE_PATH +
                        " (Ensure it's in src/main/resources or src/test/resources)");
            }
            props.load(is);
        }
        return props;
    }
    /**
     * Get a String property value safely.
     */
    public static String getStrProp(String key) {
        try {
            Properties prop = loadPropertiesAlways();
            String value = prop.getProperty(key);

            if (value == null || value.trim().isEmpty()) {
                throw new RuntimeException("❌ Missing or empty property for key: " + key);
            }
            return value.trim();

        } catch (IOException e) {
            throw new RuntimeException("❌ Failed to load config.properties", e);
        }
    }
    /**
     * Get a String property value, falling back to a default if the key is missing.
     * Use this for OPTIONAL properties.
     * @param key The property key to look up.
     * @param defaultValue The value to return if the key is missing or empty.
     * @return The property value or the default value.
     */
    public static String getStrProp(String key, String defaultValue) {
        try {
            Properties prop = loadPropertiesAlways();
            String value = prop.getProperty(key);
            if (value == null || value.trim().isEmpty()) {
                // If key is missing or empty, return the provided default value
                return defaultValue;
            }
            return value.trim();
        } catch (IOException e) {
            throw new RuntimeException("❌ Failed to load config.properties", e);
        }
    }
    /**
     * Get an integer property value safely.
     */
    public static int getIntProp(String key) {
        String value = getStrProp(key);
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            throw new RuntimeException("❌ Invalid integer value for key: " + key + " -> " + value);
        }
    }
    /**
     * Get a boolean property value safely.
     */
    public static boolean getBoolProp(String key) {
        String value = getStrProp(key);
        return Boolean.parseBoolean(value.trim());
    }

    /**
     * Helper to read properties from a project-level directory (File System).
     * The path is resolved relative to the directory where the JVM process starts (usually project root).
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
