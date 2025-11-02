package Demo;

import core.base.AppUtilTestBase;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TC01 extends AppUtilTestBase {

    @Test
    public void one()
    {
        Logger log= LogManager.getLogger("one");
        System.out.println("Current working directory: " + System.getProperty("user.dir"));
        log.info("Test case started");
        log.info("This test1 log to test driver id logic This test1 log to test driver id logic This test1 log to test driver id logic");
        log.error("This test1 log to test driver id logic This test1 log to test driver id logic This test1 log to test driver id logic");
        log.warn("This test1 log to test driver id logic");
        log.debug("This test1 log to test driver id logic");
        Assert.assertTrue(true);
    }
    @Test
    public void two()
    {
        Logger log= LogManager.getLogger("two");
        log.info("Test case started");
        log.info("This test2 log to test driver id logic");
        log.error("This test2 log to test driver id logic");
        log.warn("This test2 log to test driver id logic");
        log.debug("This test2 log to test driver id logic");

      //  Assert.fail();
    }
}
