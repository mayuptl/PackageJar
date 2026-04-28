package core.config;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Utility to read and cache values from pom.xml files within the classpath.
 * This version uses a thread-safe cache to avoid repeated XML parsing.
 */
public class CorePomReader {

    // Cache structure: Map<ArtifactId, Map<TagName, Value>>
    private static final Map<String, Map<String, String>> CACHE = new ConcurrentHashMap<>();

    private CorePomReader() { }

    /**
     * Reads a value from a pom.xml. Results are cached after the first read.
     * * @param artifactId The artifactId to look for (e.g., "my-library")
     * @param tagName    The XML tag to read (e.g., "version")
     * @return The text content of the tag, or null if not found.
     */
    public static String getPomValue(String artifactId, String tagName) {
        // Check cache first
        Map<String, String> artifactCache = CACHE.computeIfAbsent(artifactId, k -> new ConcurrentHashMap<>());

        if (artifactCache.containsKey(tagName)) {
            return artifactCache.get(tagName);
        }

        // Cache miss: Parse the XML
        String value = fetchFromPom(artifactId, tagName);

        if (value != null) {
            artifactCache.put(tagName, value);
        }

        return value;
    }

    /**
     * Internal method to perform the actual XML parsing logic.
     */
    private static String fetchFromPom(String artifactId, String tagName) {
        try {
            InputStream is = findPomStream(artifactId);

            if (is == null) {
                return null;
            }

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            // Disable DTD loading for security and performance
            factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);

            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(is);
            doc.getDocumentElement().normalize();

            NodeList list = doc.getElementsByTagName(tagName);
            if (list.getLength() > 0) {
                // Return the first occurrence found in the POM
                return list.item(0).getTextContent().trim();
            }
        } catch (Exception e) {
            // Fallback to console or logger
            System.err.println("[CorePomReader] Could not read " + tagName + " for " + artifactId);
        }
        return null;
    }

    /**
     * Attempts to locate the pom.xml stream based on the artifactId.
     * Uses the convention: META-INF/maven/${groupId}/${artifactId}/pom.xml
     */
    private static InputStream findPomStream(String artifactId) {
        // Approach: Use the package name of this class as a heuristic for GroupId
        String groupPath = CorePomReader.class.getPackage().getName().replace(".", "/");

        // We try a few levels of the package path to find the Maven metadata
        // because the GroupId might be 'com.company.project' while package is 'com.company.project.core.config'
        String[] parts = groupPath.split("/");
        StringBuilder currentPath = new StringBuilder();

        for (String part : parts) {
            if (currentPath.length() > 0) currentPath.append("/");
            currentPath.append(part);

            String fullPath = "META-INF/maven/" + currentPath.toString() + "/" + artifactId + "/pom.xml";
            InputStream is = CorePomReader.class.getClassLoader().getResourceAsStream(fullPath);
            if (is != null) return is;
        }

        return null;
    }

    /**
     * Clears the cache if needed (e.g., during hot reloads in development).
     */
    public static void clearCache() {
        CACHE.clear();
    }
}