/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package todd_doors;

import connectionManager.MyConnection;
import connectionManager.Utility;
import oakwooddoors.OakWoodDoors;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

/**
 *
 * @author Khushbu
 */
public class ToddDoorsLinkScraping {

    public static void main(String[] args) {
        crawlInternalDoorCategoryLinks();

    }

    private static void crawlInternalDoorCategoryLinks() {
        String catURL[] = {
            "https://www.todd-doors.co.uk/internal-glazed-doors",
            "https://www.todd-doors.co.uk/internal-doors/traditional/walnut",
            "https://www.todd-doors.co.uk/internal-doors/traditional/white",
            "https://www.todd-doors.co.uk/internal-doors/contemporary/oak",
            "https://www.todd-doors.co.uk/internal-doors/contemporary/walnut",
            "https://www.todd-doors.co.uk/internal-doors/contemporary/white",
            "https://www.todd-doors.co.uk/internal-doors/contemporary/grey",
            "https://www.todd-doors.co.uk/internal-doors/glazed/frosted",
            "https://www.todd-doors.co.uk/internal-doors/glazed/half",
            "https://www.todd-doors.co.uk/internal-doors/glazed/oak",
            "https://www.todd-doors.co.uk/internal-doors/glazed/panel",
            "https://www.todd-doors.co.uk/internal-doors/glazed/white",
            "https://www.todd-doors.co.uk/internal-doors/black",
            "https://www.todd-doors.co.uk/internal-white-doors",
            "https://www.todd-doors.co.uk/internal-doors/grey",
            "https://www.todd-doors.co.uk/cottage-doors",
            "https://www.todd-doors.co.uk/mexicano-doors",
            "https://www.todd-doors.co.uk/internal-doors/2-panel",
            "https://www.todd-doors.co.uk/internal-doors/4-panel",
            "https://www.todd-doors.co.uk/internal-doors/6-panel",
            "https://www.todd-doors.co.uk/moulded-panel-doors"};
        System.setProperty("webdriver.chrome.driver", "C:\\Users\\Khushbu\\Downloads\\chromedriver_win32(2)\\chromedriver.exe");

        //String url = "https://www.wholesaledeals.co.uk/";
        ChromeOptions options = new ChromeOptions();
        // options.addArguments("--headless");
        ChromeDriver driver = new ChromeDriver(options);
        for (String s : catURL) {
            startLinkScraping(s, driver);
        }
    }

    private static void startLinkScraping(String caturl, ChromeDriver driver) {
        String mainURL = caturl;
        String material = StringUtils.substringAfterLast(caturl, "/");
        material = material.replace("-", " ").trim();
        String category = "";
        //int pageno = 1;

        boolean hasNextPage = false;
        String url = mainURL;

        driver.get(url);
        do {

            System.out.println("URL::" + driver.getCurrentUrl());

            OakWoodDoors.waitForJSandJQueryToLoad(driver);
            // String url = mainURL + "?page=" + pageno;
            Document doc = Jsoup.parse(driver.getPageSource());
            category = doc.getElementsByClass("page-title").text();
            System.out.println("Cate:" + category);
            //if (!doc.getElementsByClass("collection-grid").isEmpty()) {
            for (Element e : doc.getElementsByClass("item-box")) {
                Element a = e.getElementsByTag("a").first();
                String detail = "https://www.todd-doors.co.uk" + a.attr("href");
                System.out.println("d:" + detail);
                saveToDB(detail, material, category);
            }

            if (!doc.getElementsByClass("pager").isEmpty() && !doc.getElementsByClass("pager").first().getElementsByClass("next-page").isEmpty()) {
                hasNextPage = true;

                WebElement element = driver.findElementByClassName("next-page").findElement(By.tagName("a"));
                JavascriptExecutor executor = (JavascriptExecutor) driver;
                executor.executeScript("arguments[0].click();", element);

                OakWoodDoors.waitForJSandJQueryToLoad(driver);
                //   pageno++;
            } else {
                hasNextPage = false;
            }

        } while (hasNextPage);
    }

    private static void saveToDB(String url, String material, String category) {

        String insertQ = "INSERT INTO `ross`.`todddoors_link_master`\n"
                + "(\n"
                + "`link`,\n"
                + "`material`,\n"
                + "`category`)\n"
                + "VALUES\n"
                + "("
                + "'" + Utility.prepareString(url) + "',"
                + "'" + Utility.prepareString(material) + "',"
                + "'" + Utility.prepareString(category) + "'"
                + ")";

        MyConnection.getConnection("ross");
        MyConnection.insertData(insertQ);

    }

}
