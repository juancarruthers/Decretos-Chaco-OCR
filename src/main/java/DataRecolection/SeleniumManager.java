package DataRecolection;

import org.apache.commons.io.input.BrokenInputStream;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.safari.SafariDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class SeleniumManager {

    private WebDriver driver;
    private WebDriverWait driverWaitTime;
    private BrowserDriver browser;
    private float cpuUtilization;

    private SeleniumManager(BrowserDriver browser, String cpuUtilization, long webDriverWaitTime, String decretosPage){

        this.browser = browser;
        this.cpuUtilization = Float.parseFloat(cpuUtilization) / 100;
        this.instantiateWebDriver();
        this.driverWaitTime = new WebDriverWait(this.driver, webDriverWaitTime);
        this.driver.get(decretosPage);
    }

    //GETTERS
    protected WebDriver getDriver(){
        return this.driver;
    }

    //To implement
    private void instantiateWebDriver(){

        FileManager fileManager = new FileManager();
        String driverPath = fileManager.getResourcesPath("drivers/");
        String os = System.getProperty("os.name");

        switch (this.browser){
            case FIREFOX:
                String arch = System.getProperty("sun.arch.data.model");
                driverPath += "firefox/" + os.toLowerCase() + "/x" + arch + "/geckodriver";
                System.setProperty("webdriver.gecko.driver", driverPath);
                this.driver = new FirefoxDriver();
                break;

            case CHROME:
                driverPath += "chrome/" + os.toLowerCase() + "/chromedriver";
                System.setProperty("webdriver.chrome.driver", driverPath);
                this.driver = new ChromeDriver();
                break;

            case SAFARI:
                this.driver = new SafariDriver();
                break;
        }


    }

    private void makeQueryToDOM(String sinceDate, String toDate){

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
    private void extractDataFromDOMTable(){
        try {
            final List <WebElement> rows = this.driver.findElements(By.xpath(".//div[@id='Res']/table/tbody/tr/td[1]"));
            final int numberRows = rows.size();
            final int numOfCores = Runtime.getRuntime().availableProcessors();
            final ExecutorService pool = Executors.newFixedThreadPool((int) (numOfCores * this.cpuUtilization));


            for (int i = (numberRows + 1); i >= 2; i--) {
                pool.submit(new RowDataExtractor(this.driver, i));
            }

            pool.shutdown();
            pool.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
        }catch (InterruptedException | WebDriverException e){
            Thread.currentThread().interrupt();
            System.out.println(e.getMessage());
        }

    }

    private void waitTableBuffering(String legend){

        //Wait Until Table is buffered
        final WebElement res = this.driver.findElement(By.id("Res"));
        this.driverWaitTime.until(ExpectedConditions.textToBePresentInElement(res, legend));
    }

    private int getNumberOfPages(){

        WebElement paginatorLegend = this.getDriver().findElement(By.xpath("//div[@id='Res']/div/font/table/tbody/tr[2]/td"));
        final int indexOfLastNumber = paginatorLegend.getText().indexOf('e') + 2;
        return Integer.parseInt(paginatorLegend.getText().substring(indexOfLastNumber));

    }

    public static void recolectData() throws WebDriverException{

        final FileManager fileManager = new FileManager();
        List<String> programProperties = new ArrayList<>();
        programProperties.add("CPU-Usage-Percentage");
        programProperties.add("Browser");
        HashMap<String, String> propertiesValues = fileManager.getPropValues("program.properties" , programProperties);

        final SeleniumManager downloader = new SeleniumManager(BrowserDriver.valueOf(propertiesValues.get("Browser")), propertiesValues.get("CPU-Usage-Percentage"), 300, "http://gestion.chaco.gov.ar/public/index");
        final LocalDateTime start = LocalDateTime.now();

        final String toDate = DateTimeFormatter.ofPattern("dd/MM/yyyy").format(LocalDateTime.now().minusDays(1));
        final String sinceDate = DecretosChacoDatabase.getLastDate();

        try{

            downloader.makeQueryToDOM(sinceDate, toDate);
            final int numberOfPages = downloader.getNumberOfPages();
            final JavascriptExecutor js = (JavascriptExecutor) downloader.getDriver();

            for(int i = numberOfPages; i >= 1; i--) {
                js.executeScript("getDecsFchTem(" + i + ")");
                downloader.waitTableBuffering("Página " + i + " de " + numberOfPages);
                downloader.extractDataFromDOMTable();
            }


        }catch (WebDriverException e){
            System.err.println(e.getMessage());
            downloader.getDriver().quit();
            throw new WebDriverException("error 1: server disconnected");
        }

        fileManager.deleteResource("temp-downloads");
        downloader.getDriver().quit();
        DecretosChacoDatabase.close();
        System.out.println("\n\nStart Time: " + start + " - End Time: " + LocalDateTime.now());
    }




}
