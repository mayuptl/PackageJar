package managers;

import org.openqa.selenium.WebDriver;

public class DriverManager {

    // private static WebDriver driver;
    private static final ThreadLocal<WebDriver> threadLocalDriver = new ThreadLocal<>();
    /** Return the driver instance specific to the current thread */
    public static WebDriver getDriver()
    {
        return threadLocalDriver.get();
    }
    /** Store the driver instance for the current thread */
    public static void setDriver(WebDriver webDriver)
    {
        threadLocalDriver.set(webDriver);
    }
    /** Quite driver
     * Remove the driver from ThreadLocal to prevent memory leaks */
    public static void quitDriver()
    {
        WebDriver driver = threadLocalDriver.get();
        if (driver != null)
        {
            driver.quit();
            // Important: Remove the driver from ThreadLocal to prevent memory leaks
            threadLocalDriver.remove();
        }
    }
}
