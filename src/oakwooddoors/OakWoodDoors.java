/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package oakwooddoors;


import connectionManager.Utility;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 *
 * @author Khushbu
 */
public class OakWoodDoors {

    public static void main(String[] args) {
        String url[] = {
            //   "https://www.oakwooddoors.co.uk/internal-door-solid-white-primed-ely#prodTop",
            "https://www.oakwooddoors.co.uk/internal-door-white-primed-mexicano-special-offer#prodTop"
        // "https://www.oakwooddoors.co.uk/internal-door-walnut-alcaraz-prefinished-special-offer#prodTop"
        };
        System.setProperty("webdriver.chrome.driver", "C:\\Users\\Khushbu\\Downloads\\chromedriver_win32(2)\\chromedriver.exe");

        //String url = "https://www.wholesaledeals.co.uk/";
        ChromeOptions options = new ChromeOptions();
        // options.addArguments("--headless");
        ChromeDriver driver = new ChromeDriver(options);
        for (String s : url) {
            // detailScrape(s);
            detailScrapeWithSelenium(s, driver);
        }
    }

    private static void detailScrape(String url) {
        try {
            Document doc = Jsoup.connect(url)
                    .ignoreContentType(true)
                    .ignoreHttpErrors(true)
                    .timeout(0).get();

            String title = "";
            String price = "";
            String desc = "";
            String size = "";
            String material = "";
            String delivery = "";

            title = doc.getElementsByAttributeValue("itemprop", "name").first().text();
            price = doc.getElementsByAttributeValue("itemprop", "price").first().text();
            desc = doc.getElementsByAttributeValue("itemprop", "description").first().text();
            delivery = doc.getElementById("product_tabs_delivery_contents").text();
            material = StringUtils.substringBetween(doc.text(), "Wood Type</th>", "</tr>");
            material = Utility.html2text(material);

        } catch (IOException ex) {
            Logger.getLogger(OakWoodDoors.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private static void detailScrapeWithSelenium(String url, ChromeDriver driver) {

        String title = "";
        String price = "";
        String desc = "";
        String size = "";
        String material = "";
        String delivery = "";
        driver.get(url);
        Document doc = Jsoup.parse(driver.getPageSource());
        title = doc.getElementsByAttributeValue("itemprop", "name").first().text();
        price = doc.getElementsByAttributeValue("itemprop", "price").first().text();
        desc = doc.getElementsByAttributeValue("itemprop", "description").first().text();
        delivery = doc.getElementById("product_tabs_delivery_contents").text();
        material = StringUtils.substringBetween(doc.text(), "Wood Type", "</tr>");
        material = Utility.html2text(material);
        waitForJSandJQueryToLoad(driver);
        if (doc.getElementById("product-options-wrapper") != null) {
            try {
                WebElement e = driver.findElementById("product-options-wrapper");
                int selectSize = doc.getElementById("product-options-wrapper").getElementsByTag("select").size();
                if (selectSize == 1) {
                    WebElement select1 = e.findElement(By.tagName("select"));
                    int optionSize = select1.findElements(By.tagName("option")).size();
                    System.out.println("OPTIONSIZE:" + optionSize);
                    if (optionSize == 1) {
                        System.out.println("NO options...");
                        String options = StringUtils.substringBetween(doc.html(), "\"options\":", "}});");
                        String ops[] = StringUtils.substringsBetween(options, "\"label\":\"", "\",");
                        for (String oc : ops) {
                            //oc=oc.replace("\"", "");
                            System.out.println("oc:" + oc);
                            select1.sendKeys(ops);
                        }
                    } else {
                        for (WebElement o : select1.findElements(By.tagName("option"))) {
                            System.out.println("Option :" + o.getText());
                            o.click();
                            waitForJSandJQueryToLoad(driver);
                            System.out.println("Clicked...");
                            String newPrice = driver.findElementByClassName("price-including-tax").getText();
                            System.out.println("New Price:" + newPrice);
                        }
                    }
                } else if (selectSize == 2) {
                    System.out.println("Two select found");
                    System.out.println(""+e.getText());
                } else if (selectSize == 0) {
                    System.out.println("0 select found...");
                }

            } catch (org.openqa.selenium.NoSuchElementException e) {
                System.out.println("No size option available..");
            }
        } else {
            System.out.println("no options - select found no");
        }
        System.out.println("" + title + ";" + price + ";" + material + ";" + desc + ";" + delivery);
    }
    
     public static boolean waitForJSandJQueryToLoad(ChromeDriver driver) {

        WebDriverWait wait = new WebDriverWait(driver, 30);

        // wait for jQuery to load
        ExpectedCondition<Boolean> jQueryLoad = new ExpectedCondition<Boolean>() {
            @Override
            public Boolean apply(WebDriver driver) {
                try {
                    // return ((Long) ((JavascriptExecutor) getDriver()).executeScript("return jQuery.active") == 0);
                    return ((JavascriptExecutor) driver).executeScript("return jQuery.active == 0").equals(true);
                } catch (Exception e) {
                    // no jQuery present
                    return true;
                }
            }
        };

        // wait for Javascript to load
        ExpectedCondition<Boolean> jsLoad = new ExpectedCondition<Boolean>() {
            @Override
            public Boolean apply(WebDriver driver) {
                // return ((JavascriptExecutor) getDriver()).executeScript("return document.readyState")
                //        .toString().equals("complete");
                return ((JavascriptExecutor) driver).executeScript("return document.readyState").equals("complete");
            }
        };

        return wait.until(jQueryLoad) && wait.until(jsLoad);
    }
}
