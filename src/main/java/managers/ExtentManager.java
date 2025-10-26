package managers;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.aventstack.extentreports.reporter.configuration.Theme;

// Removed all imports related to Map and ConcurrentHashMap

public class ExtentManager {
    private static ExtentReports extent;

    // ThreadLocal for the single-level ExtentTest (Method Node)
    private static final ThreadLocal<ExtentTest> methodLoggerThread = new ThreadLocal<>();

    // Removed: classNodeMap and classNodeThread
    // Removed: Accessors for Class Node (getClassNode, setClassNode, removeClassNode)

    private static final String DEFAULT_REPORT_PATH = "target/reports/Extent/ExtentReport.html";

    // --- Accessors for Method Node ---
    /** Public method to retrieve the thread's METHOD logger instance */
    public static ExtentTest getTest() {
        return methodLoggerThread.get();
    }

    /** Method to remove the instance after a test is done (Crucial for ThreadLocal cleanup) */
    public static void removeTest() {
        methodLoggerThread.remove();
    }

    // --- Initialization & Flush ---
    public static synchronized ExtentReports getReporter() {
        if (extent == null) {
            initializeReports(DEFAULT_REPORT_PATH);
        }
        return extent;
    }

    /** Initializes ExtentReports using a custom or default path. */
    public static synchronized void initializeReports(String reportFilePath) {
        if (extent == null) {
            ExtentSparkReporter spark = new ExtentSparkReporter(reportFilePath);
            spark.config().setTheme(Theme.STANDARD);
            spark.config().setDocumentTitle("Test Automation Report");
            spark.config().setReportName("Test Results");

            extent = new ExtentReports();
            extent.attachReporter(spark);
            extent.setSystemInfo("OS", System.getProperty("os.name"));
            extent.setSystemInfo("Java Version", System.getProperty("java.version"));
        }
    }

    /** Initializes ExtentReports using the default path. */
    public static synchronized void initializeReports() {
        initializeReports(DEFAULT_REPORT_PATH);
    }

    // --- Node Creation (Flat Structure Logic) ---
    /** * Creates a top-level test logger for the current method.
     * @param testName The method name of the test case
     */
    public static ExtentTest createTest(String testName) {
        if (extent == null) {
            throw new IllegalStateException("ExtentReports is not initialized. Call initializeReports() in a Listener's onStart method first.");
        }

        // Always create a top-level test node
        ExtentTest test = extent.createTest(testName);

        // Store the test node in its ThreadLocal
        methodLoggerThread.set(test);
        return test;
    }

    /** Method to flush the final report (called once after all tests finish) */
    public static void flushReports() {
        if (extent != null) {
            extent.flush();
        }
    }
}