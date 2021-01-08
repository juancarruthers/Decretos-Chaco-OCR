package Download;

import org.apache.commons.io.FileUtils;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.Time;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class DecretoDownloader {

    private WebDriver driver;
    private WebDriverWait driverWaitTime;

    public DecretoDownloader(String p_driverName, String p_driverPath, long p_time, String p_decretosPage){
        System.setProperty(p_driverName, p_driverPath);
        this.instantiateWebDriver();
        this.driverWaitTime = new WebDriverWait(this.driver, p_time);
        this.driver.get(p_decretosPage);
    }

    //To implement
    private void instantiateWebDriver(){
        this.driver = new FirefoxDriver();
    }

    public void makeQueryToDOM() throws WebDriverException{

        //Get DOM objects
        final WebElement button = this.driver.findElement(By.name("BuscarF"));
        final WebElement checkbox = this.driver.findElement(By.name("chkfch"));
        final WebElement textBoxSinceDate = this.driver.findElement(By.name("fechadesde"));
        final WebElement textBoxToDate = this.driver.findElement(By.name("fechahasta"));

        //Remove "readonly" Attribute from inputs
        JavascriptExecutor js = (JavascriptExecutor) this.driver;
        js.executeScript("document.getElementById('fechadesde').removeAttribute('readonly', '')");
        js.executeScript("document.getElementById('fechahasta').removeAttribute('readonly', '')");

        //Trigger events
        checkbox.click();
        textBoxSinceDate.sendKeys("2020-09-29");
        textBoxToDate.sendKeys("2021-01-07");
        button.click();

        this.waitTableBuffering("Decreto");

    }

    public void getDataFromDOMTable() throws WebDriverException{
        final List rows = this.driver.findElements(By.xpath(".//*[@id='Res']/table/tbody/tr/td[1]"));
        final int numberRows = rows.size();

        final WebElement baseTable = this.driver.findElement(By.tagName("table"));

        for (int i = 2; i <= (numberRows + 1); i++){
            for (int j = 1; j<=6; j++){
                if (j != 6){
                    System.out.print(baseTable.findElement(By.xpath("//*[@id='Res']/table/tbody/tr[" + i + "]/td[" + j + "]")).getText() + "\t");
                }else{
                    System.out.print(baseTable.findElement(By.xpath("//*[@id='Res']/table/tbody/tr[" + i + "]/td[" + j + "]/a")).getAttribute("href") + "\t");
                }
            }
            System.out.print("\n");
        }

    }

    public void waitTableBuffering(String p_legend){

        //Wait Until Table is buffered
        final WebElement res = this.driver.findElement(By.id("Res"));
        this.driverWaitTime.until(ExpectedConditions.textToBePresentInElement(res,p_legend));
    }

    //GETTERS

    public WebDriver getDriver(){
        return this.driver;
    }

    private WebDriverWait getDriverWaitTime(){
        return this.driverWaitTime;
    }




    public static void main(String[] args) {
        // Create a new instance of the html unit driver
        // Notice that the remainder of the code relies on the interface,
        // not the implementation.

        DecretoDownloader test = new DecretoDownloader("webdriver.gecko.driver", "lib/geckodriver", 100, "http://gestion.chaco.gov.ar/public/index");

        try{

            test.makeQueryToDOM();
            WebElement paginatorLegend = test.getDriver().findElement(By.xpath("//*[@id='Res']/div/font/table/tbody/tr[2]/td"));
            final int indexOfLastNumber = paginatorLegend.getText().indexOf('e') + 2;
            final int numberOfPages = Integer.parseInt(paginatorLegend.getText().substring(indexOfLastNumber));
            final JavascriptExecutor js = (JavascriptExecutor) test.getDriver();

            for(int i = 2; i <= numberOfPages; i++) {
                test.getDataFromDOMTable();
                js.executeScript("getDecsFchTem(" + i + ")");
                System.out.println(i);
                test.waitTableBuffering("Página " + i + " de " + numberOfPages);
            }
            test.getDataFromDOMTable();
            System.out.println("Termino");
            test.getDriver().quit();

        }catch (WebDriverException e){
            System.out.println("Exception");
            System.out.println(e.getMessage());
            test.getDriver().quit();
        }

        // Check the title of the page

    }



    /*private void downloadFile (){
        try {
            final File decreto = new File("Downloads/" + this.fileId + ".pdf");
            if (!decreto.exists()){
                final URL decretosPage = new URL(this.url + fileId);
                FileUtils.copyURLToFile(decretosPage, decreto);
                this.checkFileSize(decreto);
            }else{
                System.out.println(LocalDateTime.now() + ":El archivo ya existe");
            }


        } catch (Exception e) {
            System.out.println(e);
        }
    }

    private void checkFileSize(File p_file){
        Long fileSize = FileUtils.sizeOf(p_file);
        if (fileSize == 0){
            FileUtils.deleteQuietly(p_file);
            System.out.println(LocalDateTime.now() + ": Archivo " + this.fileId + " Vacio");
        }else{
            System.out.println(LocalDateTime.now() + ": Archivo " + this.fileId + " Descargado");
        }
    }*/
}
