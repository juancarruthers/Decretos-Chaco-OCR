package DataRecolection;

import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class SeleniumManager {

    private WebDriver driver;
    private WebDriverWait driverWaitTime;

    public SeleniumManager(String driverName, String driverPath, long time, String decretosPage){
        System.setProperty(driverName, driverPath);
        this.instantiateWebDriver();
        this.driverWaitTime = new WebDriverWait(this.driver, time);
        this.driver.get(decretosPage);
    }

    //GETTERS
    public WebDriver getDriver(){
        return this.driver;
    }

    //To implement
    private void instantiateWebDriver(){
        this.driver = new FirefoxDriver();
    }

    public void makeQueryToDOM(String sinceDate, String toDate){

        try {

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
            textBoxSinceDate.sendKeys(sinceDate);
            textBoxToDate.sendKeys(toDate);
            button.click();

            this.waitTableBuffering("Decreto");

        }catch (WebDriverException e){
            System.out.println(e.getMessage());
        }

    }

    //DATABASE OPERATIONS ------------->>PARALLELIZABLE
    public void extractDataFromDOMTable(){
        try {
            final List <WebElement> rows = this.driver.findElements(By.xpath(".//div[@id='Res']/table/tbody/tr/td[1]"));
            final int numberRows = rows.size();
            final ExecutorService pool = Executors.newFixedThreadPool(numberRows);


            for (int i = 2; i <= (numberRows + 1); i++) {
                pool.submit(new RowDataExtractor(this.driver, i));
            }

            pool.shutdown();
            pool.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
        }catch (InterruptedException | WebDriverException e){
            Thread.currentThread().interrupt();
            System.out.println(e.getMessage());
        }

    }

    public void waitTableBuffering(String legend){

        //Wait Until Table is buffered
        final WebElement res = this.driver.findElement(By.id("Res"));
        this.driverWaitTime.until(ExpectedConditions.textToBePresentInElement(res, legend));
    }

    public int getNumberOfPages(){

        WebElement paginatorLegend = this.getDriver().findElement(By.xpath("//div[@id='Res']/div/font/table/tbody/tr[2]/td"));
        final int indexOfLastNumber = paginatorLegend.getText().indexOf('e') + 2;
        return Integer.parseInt(paginatorLegend.getText().substring(indexOfLastNumber));

    }

    public static void main (String [] args){
        final SeleniumManager downloader = new SeleniumManager("webdriver.gecko.driver", "lib/geckodriver", 100, "http://gestion.chaco.gov.ar/public/index");
        final LocalDateTime start = LocalDateTime.now();

        try{

            downloader.makeQueryToDOM("2020-12-28","2020-12-31");
            final int numberOfPages = downloader.getNumberOfPages();
            final JavascriptExecutor js = (JavascriptExecutor) downloader.getDriver();

            for(int i = 2; i <= numberOfPages; i++) {
                downloader.extractDataFromDOMTable();
                js.executeScript("getDecsFchTem(" + i + ")");
                downloader.waitTableBuffering("Página " + i + " de " + numberOfPages);
            }
            downloader.getDriver().quit();

            System.out.println("\n\nStart Time: " + start + " - End Time: " + LocalDateTime.now());

        }catch (WebDriverException e){
            System.err.println(e.getMessage());
            downloader.getDriver().quit();
        }
    }




}
