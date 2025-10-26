package Demo;

import managers.DriverManager;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;

public class BaseTest {

    @BeforeClass
    public void init()
    {
        WebDriver driver = new ChromeDriver();
        DriverManager.setDriver(driver);
    }

    @AfterClass
    public void tear()
    {
        DriverManager.quitDriver();
    }
}
