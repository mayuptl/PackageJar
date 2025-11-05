package managers;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.MediaEntityBuilder;
import com.aventstack.extentreports.Status;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.aventstack.extentreports.reporter.configuration.Theme;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static core.config.ConfigReader.getStrProp;

// Removed all imports related to Map and ConcurrentHashMap

public class ExtentManager {
    private static ExtentReports extent;
    private static final String DEFAULT_REPORT_PATH = getStrProp("EXTENT_REPORT","execution-output/test-reports/ExtentReport.html");
    // NEW: ThreadLocal to store the current running test's ExtentTest instance
    private static final ThreadLocal<ExtentTest> currentTest = new ThreadLocal<>();
    private static final Map<String,ExtentTest> classNodeMap = new ConcurrentHashMap<>();

    /**
     * <b>Initializes ExtentReports using a custom path</b>
     */
    public static ExtentReports getReportInstance(String reportFilePath) {
        if (extent == null) {
            ExtentSparkReporter sparkReporter = new ExtentSparkReporter(reportFilePath);
            sparkReporter.config().setTheme(Theme.STANDARD);
            sparkReporter.config().setDocumentTitle("Test Automation Report");
            sparkReporter.config().setReportName("Test Results");
            extent = new ExtentReports();
            extent.attachReporter(sparkReporter);
            extent.setSystemInfo("OS", System.getProperty("os.name"));
            extent.setSystemInfo("Java Version", System.getProperty("java.version"));
        }
        return extent;
    }

    /**
     * <b>Initializes ExtentReports using the default path: execution-output/reports/ExtentReport.html</b>
     */
    public static ExtentReports getReportInstance() {
        return getReportInstance(DEFAULT_REPORT_PATH);
    }
    public static ExtentTest getOrCreateClassNode(String clasName)
    {
        return classNodeMap.computeIfAbsent(clasName,k->getReportInstance().createTest(k));
    }
    /**
     * Returns the ExtentTest instance associated with the current thread.
     */
    public static ExtentTest getTest() {
        return currentTest.get();
    }
    /**
     * Sets the current ExtentTest instance for the thread. Called by the Listener (onTestStart).
     * @param test The ExtentTest instance for the current method.
     */
    public static void setTest(ExtentTest test) {
        currentTest.set(test);
    }
    /**
     * Removes the ExtentTest instance from the current thread's storage. Called by the Listener (onTestSuccess/Failure/Skip).
     */
    public static void removeTest() {
        currentTest.remove();
    }
}