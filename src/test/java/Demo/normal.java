package Demo;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.Test;

public class normal  {//extends CoreBaseTest
    @Test
    public void jar()
    {
        Logger log = LogManager.getLogger("swara");
        log.info("Test case started");
        log.info("INFO");
        Assert.assertTrue(true);
        log.info("Test case passed");
    }
}
