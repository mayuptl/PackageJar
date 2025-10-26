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

public class JsonReader {
    /**
     * This class contains two methods.
     * 1 getJsonInput : useful in get input as per given testCaseName
     * 2 getJsonInputs : useful in to execute the same test case with multiple different inputs.
    */
    private final String TCID_KEY = "testCaseName";
    /**
     * <p>1 getJsonInput : useful in get input as per given testCaseName</p>
     * Useful in get input as per given testCaseName from json file.
     * @param jsonFilePath The full path to the JSON data file.
     * @param testCaseName The value to search for (e.g., "TC_001_Login").
     * @return A {@code Object[][]} containing the test case's input data.
     * * <h4>Example Usage in a TestNG DataProvider (Single Test Case):</h4>
     * <p>This example demonstrates how to use this method to run a TestNG test method
     * only once, using the data for a specific TCID.</p>
     * <pre>{@code
     * JsonReader jsonReader = new JsonReader();
     * @DataProvider(name = "getJsonData")
     * public Object[] getJsonData()
     * {
     *     return jsonReader.getJsonInput("path/to/data.json", "TC_001_Login");
     * }
     * }</pre>
     * * <p>The JSON file must contain an array of objects, similar to the following structure:</p>
     * <pre>{@code
     * [
     *      {
     *          "testCaseName": "TC_001_Login",
     *          "username": "adminUser",
     *          "password": "securePassword123"
     *      },
     *      {
     *          "testCaseName": "TC_002_Invalid_Password",
     *          "username": "adminUser",
     *          "password": "wrongPassword"
     *      }
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
     * Reads all data from a JSON file (containing an array of objects) and maps it.
     * This is typically used to execute the same test case with multiple different inputs.
     * @param jsonFilePath The full path to the JSON data file.
     * @return A List of HashMaps, where each HashMap represents one test case (data row).
     * @throws RuntimeException if the file is not found, cannot be read, or the JSON is invalid.
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
