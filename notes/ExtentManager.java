package managers;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.aventstack.extentreports.reporter.configuration.Theme;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ExtentManager {
    private static ExtentReports extent;
    // ThreadLocal for the METHOD-LEVEL ExtentTest (child node)
    private static final ThreadLocal<ExtentTest> methodLoggerThread = new ThreadLocal<>();
    // ThreadLocal for the CLASS-LEVEL ExtentTest (parent node)
   // private static final ThreadLocal<ExtentTest> classNodeThread = new ThreadLocal<>();
    private static final Map<String, ExtentTest> classNodeMap = new ConcurrentHashMap<>();

    private static final String DEFAULT_REPORT_PATH = "target/reports/Extent/ExtentReport.html";
    // --- Accessors for Class Node (Parent) ---
    /** New accessor method to retrieve the CLASS node by class name (FQCN) */
    public static ExtentTest getClassNode(String className) { // Needs class name argument
        return classNodeMap.get(className);
    }
    /** Setter for the thread's CLASS node, mapped by class name */
    public static void setClassNode(String className, ExtentTest classNode) { // Needs class name argument
        classNodeMap.put(className, classNode);
    }
    /** Remover for the class node by class name */
    public static void removeClassNode(String className) { // Needs class name argument
        classNodeMap.remove(className);
    }
    // --- Accessors for Method Node (Child) ---
    /** Public method to retrieve the thread's METHOD logger instance (the child node) */
    public static ExtentTest getTest() {
        return methodLoggerThread.get();
    }
    /** Method to remove the instance after a test is done (Called by Method Listener) */
    public static void removeTest() {
        methodLoggerThread.remove();
    }
    // --- Initialization & Flush ---
    public static synchronized ExtentReports getReporter() {
        if (extent == null) {
            // Ensure initialization happens if someone tries to get the reporter directly
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
    /** Initializes ExtentReports using the default path: target/reports/Extent/ExtentReport.html */
    public static synchronized void initializeReports() {
        initializeReports(DEFAULT_REPORT_PATH);
    }
    // --- Node Creation (The Hierarchy Logic) ---
    /** * Creates the test logger for the current method.
     * **CRUCIAL:** It attaches the new method node as a CHILD of the Class Node.
     * @param testName The method name of the test case
     */
    public static ExtentTest createTest(String testName,String className) {
        if (extent == null) {
            throw new IllegalStateException("ExtentReports is not initialized. Call initializeReports() in a Listener's onStart method first.");
        }
        // 1. Get the parent node (created by the ClassReportListener)
        ExtentTest parentNode = getClassNode(className);
        // 2. Create the child test (method) node
        ExtentTest test;
        if (parentNode != null) {
            test = parentNode.createNode(testName);
        } else {
            test = extent.createTest(testName);
        }
        // 3. Store the child node in its ThreadLocal
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