package listeners;

import core.initializer.FrameworkInitializer;
import org.testng.IExecutionListener;
import java.io.IOException;

public class FrameworksExecutionListener implements IExecutionListener {
    @Override
    public void onExecutionStart()
    {
        try{
            FrameworkInitializer.init();
        } catch (IOException e) {
           e.printStackTrace();
        }
    }
    @Override
    public void onExecutionFinish()
    {

    }

}
