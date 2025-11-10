package core.config;
import java.io.InputStream;
import java.io.IOException;
import java.util.Properties;

public class Config {
    private static final String RESOURCE_NAME = "config.properties";
    // Store properties once loaded and merged
    private static Properties appProperties;
    /**
     * Initializes and returns the final, merged application properties.
     * Merging order: Defaults (inside JAR) are overlaid by Overrides (external classpath).
     * This method ensures the TCCL (Thread Context ClassLoader) is used for the overrides,
     * which fixes issues in shaded/consumer environments.
     * * @return The merged Properties object, or an empty Properties object if loading fails.
     */
    public static Properties getApplicationProperties() {
        if (appProperties == null) {
            appProperties = loadAndMergeProperties();
        }
        return appProperties;
    }

    /**
     * Performs the robust loading and merging of default and override configurations.
     */
    private static Properties loadAndMergeProperties() {
        Properties mergedProps = new Properties();
        // --- 1. Load Defaults (Lowest Priority) ---
        // Use Config.class.getResourceAsStream to reliably find the resource *inside* this JAR.
        try (InputStream defaultsStream = Config.class.getResourceAsStream("/" + RESOURCE_NAME)) {
            if (defaultsStream != null) {
                mergedProps.load(defaultsStream);
                System.out.println("INFO: Loaded default properties from inside JAR.");
            } else {
                System.out.println("WARN: Default " + RESOURCE_NAME + " not found inside JAR.");
            }
        } catch (IOException e) {
            System.err.println("WARNING: Failed to read default properties from JAR: " + e.getMessage());
        }
        // --- 2. Load Overrides (Highest Priority) using TCCL ---
        // Use TCCL to find the override file on the external classpath (consumer project).
        try (InputStream overrideStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(RESOURCE_NAME)) {
            if (overrideStream != null) {
                Properties overrideProps = new Properties();
                overrideProps.load(overrideStream);
                // Merge the overrides into the defaults. Overrides win.
                mergedProps.putAll(overrideProps);
                System.out.println("INFO: Overlaid properties with consumer config from external classpath (TCCL).");
            } else {
                System.out.println("INFO: No consumer " + RESOURCE_NAME + " file found on external classpath. Using defaults only.");
            }
        } catch (IOException e) {
            // This is acceptable; no consumer override file was found, or an error occurred during read.
            System.err.println("WARNING: Error reading consumer override properties: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("FATAL: General error during override resource loading: " + e.getMessage());
        }
        return mergedProps;
    }
}