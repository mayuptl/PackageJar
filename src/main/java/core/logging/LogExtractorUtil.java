package core.logging;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static core.config.ConfigReader.getStrProp;

public class LogExtractorUtil {

    //private static final String DEFAULT_LOG_PATH = getStrProp("DEFAULT_LOG_PATH");
    private static final String DEFAULT_LOG_PATH = getStrProp("DEFAULT_LOG_PATH", "execution-output/test-logs/Logs.log");

    /**
     * <b>Overload 1: Extracts logs using TestCaseName as the unique filter and the default path: ${user.dir}/execution-output/test-logs/Logs.log <b/>
     *
     * @param uniqueLogFilter The test method name (e.g., "ToCheckLogin").
     * @return The extracted logs as a single String.
     */
    public static String toGetTestCaseLogs(String uniqueLogFilter, String driverID) {
        return toGetTestCaseLogsCoreLogic(uniqueLogFilter, driverID, DEFAULT_LOG_PATH);
    }

    /**
     * <b>Overload 2: Extracts logs using TestCaseName as the unique filter and a custom path.</b>
     *
     * @param uniqueLogFilter The test method name (e.g., "ToCheckLogin").
     * @param customPath      The user-provided path to the log file.
     * @return The extracted logs as a single String.
     */
    public static String toGetTestCaseLogs(String uniqueLogFilter, String driverID, String customPath) {
        String finalPath = (customPath != null && !customPath.trim().isEmpty())
                ? customPath
                : DEFAULT_LOG_PATH;
        return toGetTestCaseLogsCoreLogic(uniqueLogFilter, driverID, finalPath);
    }

    private static String toGetTestCaseLogsCoreLogic(String testCaseName, String driverID, String filePath) {
        List<String> logs = new ArrayList<>();
        Path logFilePath = Paths.get(filePath);
        boolean isCapturing = false;
        // Define the unique, explicit markers (Must be present in your log configuration!)
        final String START_MARKER = "Test case started";
        final String END_PASS_MARKER = "Test case pass";
        final String END_FAIL_MARKER = "Test case fail";
       // final String GENERIC_END_MARKER = "***** Test execution completed *****";
        try {
            if (!Files.exists(logFilePath)) {
                System.err.println("Error: Log file not found at " + filePath);
                return "ERROR: Log file not found.";
            }
            List<String> allLines = Files.readAllLines(logFilePath);
            for (String line : allLines)
            {
                boolean preciseStartCondition =
                        !isCapturing &&
                                line.contains(testCaseName) &&
                                line.contains(START_MARKER) &&
                                (driverID == null || driverID.isEmpty() || line.contains(driverID));
                boolean fallbackStartCondition =
                        !isCapturing &&
                                line.contains(testCaseName) &&
                                line.contains(START_MARKER);
                // Check if EITHER the precise condition OR the fallback condition is met.
                if (preciseStartCondition || fallbackStartCondition)
                {
                    isCapturing = true;
                    logs.add(line);
                    continue;
                }
                // --- STOP CAPTURE LOGIC (Only executes if capture is already running) ---
                if (isCapturing)
                {
                    // 1. Check if the current line belongs to the Test Case ID (essential for precise STOP)
                    // If driverID is null/empty, this check passes (true). If it has a value, the line MUST contain it.
                    boolean isDriverIdCheckPassedForStop =
                            (driverID == null || driverID.isEmpty() || line.contains(driverID));

                    // 2. Current Test Ended (Explicit Marker Found)
                    boolean explicitEndMarkersFound =
                            (line.contains(END_PASS_MARKER) || line.contains(END_FAIL_MARKER)); //|| line.contains(GENERIC_END_MARKER)
                    // 3. Start of the next test (The missing end marker case - Log Bleed stop)
                    // Stops capturing if a different test starts, indicating a missed end marker.
                    // The line must NOT contain the testCaseName, MUST contain the START_MARKER,
                    // AND (if an ID is set) MUST NOT contain the current test's driverID.
                    boolean nextTestStarted =
                            !line.contains(testCaseName) &&
                                    line.contains(START_MARKER) &&
                                    (driverID == null || driverID.isEmpty() || !line.contains(driverID)); // Added ID exclusion check
                    // Stop condition: EITHER (A) A precise, ID-checked end OR (B) A non-ID-checked log bleed stop.
                    boolean shouldStopCapturing =
                            (explicitEndMarkersFound && isDriverIdCheckPassedForStop) || nextTestStarted;
                    if (shouldStopCapturing) {
                        // If the current test ended explicitly, include the end line before stopping.
                        if (explicitEndMarkersFound) {
                            logs.add(line);
                        }
                        // If 'nextTestStarted' is true, the current line is the start of the next test,
                        // so we do NOT add it to the logs. We simply stop and break.
                        isCapturing = false;
                        // Optimization: Stop reading the file entirely once the correct end is found.
                        break;
                    }
                    // 4. CAPTURE LOGS (Only run if no stop condition was met)
                    // This is the correct placement for adding lines during an active capture.
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
}