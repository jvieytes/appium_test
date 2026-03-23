package utilities;

import io.appium.java_client.android.AndroidDriver;
import listeners.SuiteListeners;
import listeners.TestListeners;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.asserts.SoftAssert;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

@Listeners({TestListeners.class, SuiteListeners.class})
public class BaseTest {
    protected SoftAssert softAssert;
    protected final String smoke = "smoke";
    protected final String regression = "regression";
    protected AndroidDriver driver;

    @BeforeMethod(alwaysRun = true)
    public void masterSetUp() {
        softAssert = new SoftAssert();

        Logs.debug("Inicializando el driver");
        driver = initDriver();

        Logs.debug("Asignando el driver al driver provider");
        new DriverProvider().set(driver);
    }

    @AfterMethod(alwaysRun = true)
    public void masterTearDown() {
        Logs.debug("Matando el driver");
        driver.quit();
    }

    protected void sleep(int timeMS) {
        try {
            Thread.sleep(timeMS);
        } catch (InterruptedException interruptedException) {
            Logs.error("InterruptedException: %s",
                    interruptedException.getLocalizedMessage());
        }
    }

    private static AndroidDriver initDriver() {
        try {
            final var appiumUrl = "http://127.0.0.1:4723/";
            final var desiredCapabilities = getDesiredCapabilities();

            Logs.debug("Inicialiado el driver");
            return new AndroidDriver(new URL(appiumUrl), desiredCapabilities);
        } catch (MalformedURLException malformedURLException) {
            Logs.error("Error al inicializar el driver: %s", malformedURLException.getLocalizedMessage());
            throw new RuntimeException();
        }
    }

    private static DesiredCapabilities getDesiredCapabilities() {
        final var desiredCapabilities = new DesiredCapabilities();

        //final var fileAPK = new File("src/test/resources/apk/sauceLabs.apk");
        final var fileAPK = new File("src/test/resources/apk/calculator.apk");

        desiredCapabilities.setCapability("appium:autoGrantPermissions", true);
        //desiredCapabilities.setCapability("appium:appWaitActivity", "com.swaglabsmobileapp.MainActivity");
        desiredCapabilities.setCapability("appium:appWaitActivity", "com.android.calculator2.Calculator");
        desiredCapabilities.setCapability("appium:platformName", "Android");
        desiredCapabilities.setCapability("appium:automationName", "UiAutomator2");
        desiredCapabilities.setCapability("appium:app", fileAPK.getAbsolutePath());

        return desiredCapabilities;
    }
}
