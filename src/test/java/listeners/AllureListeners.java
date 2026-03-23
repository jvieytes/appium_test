package listeners;

import io.qameta.allure.listener.TestLifecycleListener;
import io.qameta.allure.model.TestResult;
import utilities.DriverProvider;
import utilities.FileManager;
import utilities.Logs;

public class AllureListeners implements TestLifecycleListener {
    @Override
    public void beforeTestStop(TestResult reuslt) {
        Logs.debug("Tomando evidencias para allure");

        final var status = reuslt.getStatus();

        switch (status) {
            case BROKEN, FAILED -> {
                if (new DriverProvider().get() != null) {
                    FileManager.getScrenshot();
                    FileManager.getPageSource();
                }
            }
        }
    }
}
