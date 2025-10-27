package common.log;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class LogExtractorUtils {

    private static final String DEFAULT_LOG_PATH = "executionLogs/Logs.log";

    /** <b>Overload 1: Extracts logs using TestCaseName as the unique filter and the default path: executionLogs/Logs.log <b/>
     * @param uniqueLogFilter The test method name (e.g., "ToCheckLogin").
     * @return The extracted logs as a single String.
     */
    public static String toGet_TestCaseLogs(String uniqueLogFilter) {
        return toGet_TestCaseLogs_CoreLogic(uniqueLogFilter, DEFAULT_LOG_PATH);
    }

    /** <b>Overload 2: Extracts logs using TestCaseName as the unique filter and a custom path.</b>
     * @param uniqueLogFilter The test method name (e.g., "ToCheckLogin").
     * @param customPath The user-provided path to the log file.
     * @return The extracted logs as a single String.
     */
    public static String toGet_TestCaseLogs(String uniqueLogFilter, String customPath) {
        String finalPath = (customPath != null && !customPath.trim().isEmpty())
                ? customPath
                : DEFAULT_LOG_PATH;
        return toGet_TestCaseLogs_CoreLogic(uniqueLogFilter, finalPath);
    }

    private static String toGet_TestCaseLogs_CoreLogic(String testCaseName, String filePath) {
        List<String> logs = new ArrayList<>();
        Path logFilePath = Paths.get(filePath);
        boolean isCapturing = false;

        // Define the unique, explicit markers (Must be present in your log configuration!)
        final String START_MARKER = "Test case started";
        final String END_PASS_MARKER = "Test case pass";
        final String END_FAIL_MARKER = "Test case fail";
        final String GENERIC_END_MARKER = "***** Test execution completed *****";

        try {
            if (!Files.exists(logFilePath)) {
                System.err.println("Error: Log file not found at " + filePath);
                return "ERROR: Log file not found.";
            }

            List<String> allLines = Files.readAllLines(logFilePath);

            for (String line : allLines) {

                // --- 1. START LOGIC: Find the unique start marker ---
                if (!isCapturing && line.contains(START_MARKER)) {
                    isCapturing = true;
                    // Add the start marker line immediately
                    logs.add(line);
                    continue; // Skip remaining checks for the start line
                }

                // --- 2. STOP LOGIC (Boundary Check) ---
                if (isCapturing) {

                    // Explicit Pass/Fail for the current test
                    boolean currentTestEnded = line.contains(testCaseName) &&
                            (line.contains(END_PASS_MARKER) ||
                                    line.contains(END_FAIL_MARKER) ||
                                    line.contains(GENERIC_END_MARKER));

                    // Start of the next test (The missing end marker case)
                    // This checks if the current line is a START MARKER for ANY other test.
                    boolean nextTestStarted = !line.contains(testCaseName) && line.contains("Test case started");

                    // If ANY stop condition is met, stop capturing
                    if (currentTestEnded || nextTestStarted) {

                        // If the current test ended explicitly, include the end line before stopping.
                        if (currentTestEnded) {
                            logs.add(line);
                        }

                        // If nextTestStarted is true, the current line is the start of the next test,
                        // so we do NOT add it to the logs. We simply stop and break.

                        isCapturing = false;
                        break; // Optimization: Stop reading the file
                    }

                    // --- 3. CAPTURE LOGS (Only run if no stop condition was met) ---
                    logs.add(line);
                }
            }

        } catch (IOException e) {
            System.err.println("An unexpected error occurred while reading log file: " + e.getMessage());
            e.printStackTrace();
            return "ERROR: Failed to read log file due to IOException.";
        }

        return String.join("\n", logs);
    }
    // This mehtod will extract log from log file, using testCasName
    public static String toGet_TestCaseLogs1(String testCaseName) {
        List<String> logs = new ArrayList<>();
        String filePath = "executionLogs/Logs.log";  // Path to the log file
        try {
            // Read all lines from the log file
            List<String> allLines = Files.readAllLines(Paths.get(filePath));
            int count=1;
            for (String line : allLines) {
                // Check if the line contains the start of the test case
                if(count==1) {
                    if (line.contains(testCaseName)) {
                        // Mark that the test case has started
                        count=0;
                    }
                }
                // Capture the logs for the specific test case
                if (count==0) {
                    logs.add(line);
                }
                // Check if the line contains the end of the test case
                //***** Test execution completed *****
                if (line.contains(testCaseName +"***** Browser Closed *****")) {
                    count=1;
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        // Convert logs to string (you can also return the List if needed)
        return String.join("\n", logs);
    }
}