package automation;

import org.testng.Assert;
import org.testng.annotations.Test;
import utilities.BaseTest;
import utilities.Logs;

public class PrimerosTests extends BaseTest {

    @Test(groups = regression)
    public void primerTest() {
        Logs.info("Esperamos 3 segundos!!!");
        sleep(3000); //3000MS = 3 segundos
    }

    @Test(groups = regression)
    public void fallidoTest() {
        Logs.info("Esperando 2 segundos");
        sleep(2000);

        Logs.info("Forzando que falle el test");
        Assert.assertEquals(3, 2);
    }
}
