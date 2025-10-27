package Demo;

import core.base.AppUtilTestBase;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TC02 extends AppUtilTestBase {

    @Test
    public void one2()
    {
        Assert.assertTrue(true);
    }
    @Test
    public void two2()
    {
        Assert.fail();
    }
}
