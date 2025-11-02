package Demo;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.WebDriver;

public class POMTwo {
    WebDriver driver;
    Logger log= LogManager.getLogger("POMTwo");
    public POMTwo(WebDriver driver)
    {
        this.driver= driver;
    }

    public void logCheck()
    {
        log.info("This is from pom - POMTwologCheck");
    }

    public void logCheckAnother()
    {
        log.info("This is from pom - POMTwologCheckAnother");
    }

}
