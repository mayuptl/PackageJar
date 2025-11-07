package core.logging;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import static core.config.ConfigReader.getIntProp;
import static core.config.ConfigReader.getStrProp;

public class LogExtractorUtil {

    //private static final String DEFAULT_LOG_PATH = getStrProp("DEFAULT_LOG_PATH");
    private static final String DEFAULT_LOG_PATH = getStrProp("LOG_FILE_PATH", "execution-output/test-logs/Logs.log");

    /**
     * <b>Overload 1: Extracts logs using TestCaseName as the unique filter and the default path: ${user.dir}/execution-output/test-logs/Logs.log <b/>
     *
     * @param testCaseName The test method name (e.g., "ToCheckLogin").
     * @return The extracted logs as a single String.
     */
    public static String toGetTestCaseLogs(String testCaseName, String driverID) {
        return toGetTestCaseLogsCoreLogic(testCaseName, driverID, DEFAULT_LOG_PATH);
    }

    /**
     * <b>Overload 2: Extracts logs using TestCaseName as the unique filter and a custom path.</b>
     *
     * @param testCaseName The test method name (e.g., "ToCheckLogin").
     * @param customPath      The user-provided path to the log file.
     * @return The extracted logs as a single String.
     */
    public static String toGetTestCaseLogs(String testCaseName, String driverID, String customPath) {
        String finalPath = (customPath != null && !customPath.trim().isEmpty())
                ? customPath
                : DEFAULT_LOG_PATH;
        return toGetTestCaseLogsCoreLogic(testCaseName, driverID, finalPath);
    }

    private static String toGetTestCaseLogsCoreLogic(String testCaseName, String driverID, String filePath) {
        final Path logFilePath = Paths.get(filePath);
        List<String> logs = new ArrayList<>();
        try {
            if (!Files.exists(logFilePath)) {
                System.err.println("Error: Log file not found at " + filePath);
                return "ERROR: Log file not found.";
            }
            final String START_MARKER = getStrProp("START_MARKER", "Test case started");
            final String END_PASS_MARKER = getStrProp("END_PASS_MARKER", "Test case pass");
            final String END_FAIL_MARKER = getStrProp("END_FAIL_MARKER", "Test case fail");

            // Pre-calculate common regex patterns to avoid recalculating in the loop
            final String DRIVER_ID_PATTERN = ".*\\[" + Pattern.quote(driverID) + "\\].*";
            final String TEST_CASE_NAME_PATTERN = ".*" + Pattern.quote(testCaseName) + ".*";
            final String START_MARKER_PATTERN = ".*" + Pattern.quote(START_MARKER) + ".*";
            final String END_PASS_MARKER_PATTERN = ".*" + Pattern.quote(END_PASS_MARKER) + ".*";
            final String END_FAIL_MARKER_PATTERN = ".*" + Pattern.quote(END_FAIL_MARKER) + ".*";

            List<String> allLines = Files.readAllLines(logFilePath);
            boolean isCapturing = false;
            final int MAX_CAPTURE_LINES = getIntProp("MAX_CAPTURE_LINES", 500);
            int captureLineCount = 0;

            for (String line : allLines) {
                if (!isCapturing) {
                    boolean isDriverIDMatch = line.matches(DRIVER_ID_PATTERN);
                    boolean isTestCaseNameMatch = line.matches(TEST_CASE_NAME_PATTERN);
                    boolean isStartMarkerMatch = line.matches(START_MARKER_PATTERN);

                    boolean startCondition = isDriverIDMatch && isTestCaseNameMatch && isStartMarkerMatch;
                    if (startCondition) {
                        isCapturing = true;
                        logs.add(line);
                        continue;
                    }
                } else {
                    captureLineCount ++;
                    boolean currentLine = (line.matches(DRIVER_ID_PATTERN) || line.matches(TEST_CASE_NAME_PATTERN));
                    if (currentLine) {
                        logs.add(line);
                        boolean isEndPassMarkerMatch = line.matches(END_PASS_MARKER_PATTERN);
                        boolean isEndFailMarkerMatch = line.matches(END_FAIL_MARKER_PATTERN);
                        boolean stopCondition = (isEndPassMarkerMatch || isEndFailMarkerMatch);
                        if (stopCondition) {
                            isCapturing = false;
                            break;
                        }
                    }
                    if (captureLineCount >= MAX_CAPTURE_LINES) {
                        isCapturing = false;
                        break;
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("An unexpected error occurred while reading log file: " + e.getMessage());
            return "ERROR: Failed to read log file due to IOException: " + e.getMessage();
        }
        return String.join("\n", logs);
    }
}