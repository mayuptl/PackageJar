package listeners;

import com.aventstack.extentreports.ExtentTest;
import managers.ExtentManager; // Assuming you renamed ExtentManager to ClassExtentManager
import org.testng.ITestClass;
import org.testng.IClassListener;

// Implement IClassListener (for TestNG lifecycle) and IClassReportListener (your custom contract)
public class ClassReportListener implements IClassListener, IClassReportListener {

    // Removed: private static final ThreadLocal<ExtentTest> classTest = new ThreadLocal<>();
    // The node management is now handled by ClassExtentManager

    @Override
    public void onStart(ITestClass testClass) {
        String className = testClass.getRealClass().getSimpleName();
        // 💡 CHANGE 1: Use FQCN as the map key
        String fqcn = testClass.getRealClass().getName();
        // 💡 DEBUG STEP: Print the FQCN being used as the key
        System.out.println("Extent Report DEBUG: Setting Class Node Key: " + fqcn);
        ExtentTest node = ExtentManager.getReporter().createTest(className);
        // 💡 CHANGE 2: Store it using the FQCN key
        ExtentManager.setClassNode(fqcn, node);
        node.info("Class Test Execution Started: " + className);
    }

    @Override
    public void onFinish(ITestClass testClass) {
        // 💡 CHANGE 3: Use FQCN to retrieve and remove the node
        String fqcn = testClass.getRealClass().getName();
        ExtentTest node = ExtentManager.getClassNode(fqcn); // Note: getClassNode now needs FQCN
        if (node != null) {
            node.info("Class Test Execution Completed.");
        }
        // 💡 CHANGE 4: Remove it using the FQCN key
        ExtentManager.removeClassNode(fqcn);
    }

    // --- Implementations for IClassListener (Required by TestNG) ---
    @Override
    public void onBeforeClass(ITestClass testClass) {
        // Implementation logic if needed before the @BeforeClass method runs
    }

    @Override
    public void onAfterClass(ITestClass testClass) {
        // Implementation logic if needed after the @AfterClass method runs
    }

    // --- Implementation for IClassReportListener (If it contains a getter) ---
    // If your IClassReportListener interface included this method:
    // public ExtentTest getClassNode();

 /*   @Override
    public ExtentTest getClassNode() {
        // Correctly delegates access to the thread-safe manager
        return ClassExtentManager.getClassNode();
    }*/
}