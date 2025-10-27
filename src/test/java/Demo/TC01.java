package Demo;

import core.base.AppUtilTestBase;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TC01 extends AppUtilTestBase {

    @Test
    public void one()
    {
        Assert.assertTrue(true);
    }
    @Test
    public void two()
    {
        Assert.fail();
    }
}
