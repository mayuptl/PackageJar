package core.config;

import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;

import java.io.FileReader;

/**
 * Utility class for reading values from a Maven {@code pom.xml} file.
 * <p>
 * This class currently supports extracting common properties such as
 * {@code artifactId} and {@code version}.
 * </p>
 */
public class CorePomReader {

    /** Private constructor for a utility class; all methods are static. */
    private CorePomReader() { }
    /**
     * Retrieves a specific value from the {@code pom.xml} file based on the provided key.
     *
     * <p>Supported keys:</p>
     * <ul>
     *     <li>{@code artifactId} - Returns the artifact ID of the project.</li>
     *     <li>{@code version} - Returns the project version. If not explicitly defined,
     *     it attempts to retrieve the version from the parent POM.</li>
     * </ul>
     *
     * @param key the name of the property to retrieve (e.g., {@code "artifactId"}, {@code "version"})
     * @return the value associated with the given key, or {@code null} if the key is not supported
     *         or an error occurs while reading the file
     */
    public static String getPomValue(String key) {
        try (FileReader reader = new FileReader("pom.xml")) {
            MavenXpp3Reader mavenReader = new MavenXpp3Reader();
            Model model = mavenReader.read(reader);
            // Return artifactId if requested
            if ("artifactId".equalsIgnoreCase(key)) {
                return model.getArtifactId();
            }
            // Return version if requested
            if ("version".equalsIgnoreCase(key)) {
                String version = model.getVersion();
                // Fallback to parent version if not defined in current POM
                if (version == null && model.getParent() != null) {
                    version = model.getParent().getVersion();
                }
                return version;
            }
        } catch (Exception e) {
            // Print stack trace for debugging purposes
            e.printStackTrace();
        }
        // Return null if key is unsupported or an error occurs
        return null;
    }
}