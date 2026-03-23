package utilities;

import io.qameta.allure.Attachment;
import org.apache.commons.io.FileUtils;
import org.jsoup.Jsoup;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class FileManager {
    private static final String screenshotPath = "src/test/resources/screenshots";
    private static final String pageSourcePath = "src/test/resources/pageStructure";

    public static void getScreenshot(String screenshotName) {
        Logs.debug("Tomando screenshot");

        final var screenshotFile = ((TakesScreenshot) new DriverProvider().get()).getScreenshotAs(OutputType.FILE);

        final var path = String.format("%s/%s.png", screenshotPath, screenshotName);

        try {
            FileUtils.copyFile(screenshotFile, new File(path));
        } catch (IOException ioException) {
            Logs.error("Error al tomar screenshot: %s", ioException.getLocalizedMessage());
        }
    }

    public static void getPageSource(String fileName) {
        Logs.debug("Tomando page source");

        final var path = String.format("%s/%s.xml", pageSourcePath, fileName);

        try {
            final var file = new File(path);

            Logs.debug("Creando los directorios padres sie s que no existen");
            if (file.getParentFile().mkdir()) {
                final var fileWriter = new FileWriter(file);
                final var pageSource = new DriverProvider().get().getPageSource();
                if (pageSource != null) {
                    fileWriter.write(Jsoup.parse(pageSource).toString());
                }
                fileWriter.close();
            }
        } catch (IOException ioException) {
            Logs.error("Error al tomar el page source: %s", ioException.getLocalizedMessage());
        }
    }

    public static void deletePreviousEvidence() {
        try {
            Logs.debug("Borrando las carpetas de evidencias");
            FileUtils.deleteDirectory(new File(screenshotPath));
            FileUtils.deleteDirectory(new File(pageSourcePath));
        } catch (IOException ioException) {
            Logs.error("Error al borrar las evidencias: %s", ioException.getLocalizedMessage());
        }
    }

    @Attachment(value = "screenshot", type = "image/png")
    public static byte[] getScrenshot() {
        return ((TakesScreenshot) new DriverProvider().get()).getScreenshotAs(OutputType.BYTES);
    }

    @Attachment(value = "pageSource", type = "text/html", fileExtension = ".html")
    public static String getPageSource() {
        final var pageSource = new DriverProvider().get().getPageSource();

        return pageSource != null ? pageSource : "Error al tomar el page source";
    }
}
