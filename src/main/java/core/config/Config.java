package core.config;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Provides robust, single-instance loading and merging of configuration properties.
 *
 * Merging Priority (Consumer overrides JAR):
 * 1. Default properties inside the JAR (base config) are loaded first.
 * 2. User's local properties (file system override) are loaded and merged second.
 */
public class Config {

    // File names and expected paths
    private static final String DEFAULT_CONFIG_FILE = "config.properties"; // Inside JAR
    private static final String LOCAL_OVERRIDE_FILE = "src/main/resources/config.properties"; // Local file system (Consumer's project root)

    // Using AtomicReference to hold the single, immutable instance of merged properties
    private static final AtomicReference<Properties> MERGED_PROPERTIES = new AtomicReference<>();

    private Config() {
        // Prevent instantiation
    }

    /**
     * Public accessor to retrieve the application properties, loading them if necessary.
     * This method ensures thread-safe, one-time loading.
     *
     * @return The merged Properties object.
     */
    public static Properties getApplicationProperties() {
        if (MERGED_PROPERTIES.get() == null) {
            loadAndMergeProperties();
        }
        return MERGED_PROPERTIES.get();
    }

    /**
     * Handles the core loading and merging logic.
     */
    private static void loadAndMergeProperties() {
        Properties mergedProps = new Properties();

        // 1. Load DEFAULT properties from the JAR (Classpath) - BASE
        try (InputStream jarStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(DEFAULT_CONFIG_FILE)) {
            if (jarStream != null) {
                mergedProps.load(jarStream);
                System.out.println("INFO: Loaded " + mergedProps.size() + " default properties from JAR: " + DEFAULT_CONFIG_FILE);
            } else {
                System.err.println("WARNING: Default config file not found in JAR: " + DEFAULT_CONFIG_FILE);
            }
        } catch (IOException e) {
            System.err.println("FATAL: Error loading properties from JAR: " + DEFAULT_CONFIG_FILE);
            e.printStackTrace();
        }

        // 2. Load OVERRIDE properties from the local file system (Consumer project) - OVERRIDE
        try (FileInputStream localStream = new FileInputStream(LOCAL_OVERRIDE_FILE)) {
            Properties overrideProps = new Properties();
            overrideProps.load(localStream);

            // Override JAR properties with local properties
            mergedProps.putAll(overrideProps);
            System.out.println("INFO: Successfully applied " + overrideProps.size() + " local properties from file system. Final count: " + mergedProps.size());
        } catch (FileNotFoundException e) {
            // This is acceptable, the local override file is optional.
            System.out.println("INFO: No local override file found at: " + LOCAL_OVERRIDE_FILE + ". Using JAR defaults only.");
        } catch (IOException e) {
            System.err.println("ERROR: Error loading properties from local override file: " + LOCAL_OVERRIDE_FILE);
            e.printStackTrace();
        }

        // Finalize the merged properties (thread-safe assignment)
        MERGED_PROPERTIES.compareAndSet(null, mergedProps);
    }
}