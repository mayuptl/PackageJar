package core.json;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
/**
 * Utility class for reading test data from JSON files.
 * This class provides methods to retrieve either a single data entry based on a test case ID
 * or a complete list of data entries from a JSON array.
 * 1 getJsonInput : useful in to get input as per given testCaseName
 * 2 getJsonInputs : useful in to execute the same test case with multiple different inputs.
 * It uses Jackson Databind for mapping JSON content to Java collections.
 */
public class JsonReader {
    /**
     * Key used in the JSON data objects to identify the test case name/ID.
     * This key is mandatory for the {@code getJsonInput} method to work correctly.
     */
    private static final String TCID_KEY = "testCaseName";
    /**
     * Retrieves the input data for a specific test case ID from a JSON array file.
     * <p>The method searches for an object where the key defined by {@code TCID_KEY}
     * matches the {@code testCaseName} provided.</p>
     *
     * @param jsonFilePath The full path to the JSON data file. The JSON must be an array of objects.
     * @param testCaseName The unique ID/Name of the test case to look for (e.g., "TC_001_Login").
     * @return A {@code Object[][]} containing the single matching data HashMap,
     * formatted for direct use in TestNG's DataProvider (i.e., {@code Object[1][1]}).
     * @throws RuntimeException if the file is not found, cannot be read, the JSON is invalid,
     * or the specified testCaseName is not found in the data.
     *
     * <h4>Example JSON Structure (Required for this method):</h4>
     * <pre>{@code
     * [
     * {
     * "testCaseName": "TC_001_Login",
     * "username": "adminUser",
     * "password": "securePassword123"
     * },
     * // ... other test cases ...
     * ]
     * }</pre>
     */
    public Object[][] getJsonInput(String jsonFilePath, String testCaseName) {
        String jsonCont;
        try {
            // Read file content
            jsonCont = FileUtils.readFileToString(new File(jsonFilePath), StandardCharsets.UTF_8);
        } catch (FileNotFoundException e) {
            // Provide a clear message for file not found
            throw new RuntimeException("Json file not found at location: " + jsonFilePath, e);
        } catch (IOException e) {
            // Handle other IO errors (e.g., permission issues)
            System.out.println("Json file not found at location: " + jsonFilePath);
            System.out.println("or");
            System.out.println("Error in reading the JSON file");
            throw new RuntimeException("Error reading JSON file at: " + jsonFilePath, e);
        }
        ObjectMapper mapper = new ObjectMapper();
        try {
            // Deserialize JSON string into a List of HashMaps
            List<HashMap<String, String>> dataList =
                    mapper.readValue(jsonCont, new TypeReference<List<HashMap<String, String>>>() {
                    });

            // Iterate and find the matching entry
            for (HashMap<String, String> entry : dataList) {
                // Use the internal constant key (TCID_KEY) for the search
                if (entry.containsKey(TCID_KEY) && entry.get(TCID_KEY).equals(testCaseName))
                {
                    // Create a 2D array: [1 row] [1 column] to hold the single HashMap
                    Object[][] dataArray = new Object[1][1];
                    dataArray[0][0] = entry;
                    return dataArray;
                }
            }
            // Handle the case where the ID is not found in the JSON data
            throw new RuntimeException("Test case ID '" + testCaseName + "' not found in JSON data using key: " + TCID_KEY); // Minor clarification
        } catch (IOException e) {
            // Catch Jackson deserialization errors (invalid JSON format)
            throw new RuntimeException("Error parsing JSON content from: " + jsonFilePath, e);
        }
    }

    /**
     * 2 getJsonInputs : useful in to execute the same test case with multiple different inputs.
     * Reads all objects from a JSON array file and returns them as a List of HashMaps.
     * This method is useful for DataProviders that need to iterate through all rows
     * or a subset of rows in the JSON file.
     *
     * @param jsonFilePath The full path to the JSON data file. The JSON must be an array of objects.
     * @return A {@code List} of {@code HashMap<String, String>}, where each HashMap represents
     * one test data entry (data row).
     * @throws IOException if the JSON content is invalid or a file reading error occurs.
     * @throws RuntimeException if the file is not found or cannot be read.
     *
     * * <h4>Example Usage in a TestNG DataProvider:</h4>
     * <p>The DataProvider must wrap the returned List elements into an {@code Object[]} array.</p>
     * <pre>{@code
     * JsonReader jsonReader = new JsonReader();
     * @DataProvider(name = "getData")
     * public Object[] getData() {
     *      List<HashMap<String, String>> data = jsonReader.getJsonInputs("path/to/data.json");
     *      // To execute the test case 2 times (first two rows)
     *      return new Object[] {data.get(0), data.get(1)};
     *      * // To execute the test case 1 time (first row)
     *      // return new Object[] {data.get(0)};
     * }
     * }</pre>
     * * <h4>Expected JSON Structure:</h4>
     * <p>The JSON file must contain an array of objects, similar to the following structure:</p>
     * <pre>{@code
     * [
     *      {
     *          "username" : "Admin",
     *          "password" : "admin123"
     *      },
     *      {
     *          "username" :"Mayur1",
     *          "password" :"Mayur01@2025"
     *      }
     * ]
     * }</pre>
     */
    public List<HashMap<String, String>> getJsonInputs(String jsonFilePath) throws IOException {
        String jsonCont;
        try {
            jsonCont = FileUtils.readFileToString(new File(jsonFilePath), StandardCharsets.UTF_8);
        } catch (FileNotFoundException e) {
            // Provide a clear message for file not found
            throw new RuntimeException("Json file not found at location: " + jsonFilePath, e);
        } catch (IOException e) {
            // Handle other IO errors (e.g., permission issues)
            System.out.println("Json file not found at location: " + jsonFilePath);
            System.out.println("or");
            System.out.println("Error in reading the JSON file");
            throw new RuntimeException("Error reading JSON file at: " + jsonFilePath, e);
        }
        // String to List of HashMap- Jackson Databind
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(jsonCont, new TypeReference<List<HashMap<String, String>>>() {
        });
    }
}
