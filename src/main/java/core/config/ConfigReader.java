package core.config;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ConfigReader {

    private static final String RESOURCE_PATH = "config.properties";

    /**
     * Helper method to load properties from the classpath.
     * This method is called repeatedly for every property access.
     */
    private static Properties loadPropertiesAlways() throws IOException {
        Properties props = new Properties();

        // Use the ClassLoader to find the file within the consumer's compiled project resources.
        try (InputStream is = ConfigReader.class.getClassLoader().getResourceAsStream(RESOURCE_PATH)) {

            if (is == null) {
                throw new FileNotFoundException("Resource not found on classpath: " + RESOURCE_PATH +
                        " (Ensure file is in src/main/resources or src/test/resources)");
            }
            props.load(is);
        }
        return props;
    }

    // --- Public Getter Methods (Read on Every Call) ---
    public static String getStrProp(String key) throws IOException {
        Properties prop = loadPropertiesAlways(); // Always re-reads the file
        return prop.getProperty(key);
    }

    public static int getIntProp(String key) throws IOException, NumberFormatException {
        String value = getStrProp(key);
        return Integer.parseInt(value.trim());
    }

    public static boolean getBoolProp(String key) throws IOException {
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

        // Using FileInputStream to read directly from the file system path
        try (FileInputStream fis = new FileInputStream(filePath)) {
            props.load(fis);
        }
        return props;
    }

    public static String getStrPropFromPath(String key, String filePath) throws IOException {
        Properties prop = loadFromFileSystem(filePath);
        return prop.getProperty(key);
    }

    public static int getIntPropFromPath(String key, String filePath) throws IOException {
        String value = getStrPropFromPath(key,filePath);
        return Integer.parseInt(value.trim());
    }

    public static boolean getBoolPropFromPath(String key, String filePath) throws IOException {
        String value = getStrPropFromPath(key,filePath);
        return Boolean.parseBoolean(value.trim());
    }
}