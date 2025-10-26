package listeners;

import org.testng.ITestClass;

public interface IClassReportListener {
    void onStart(ITestClass testClass);
    void onFinish(ITestClass testClass);
}
