/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wayfair;

import connectionManager.MyConnection;
import connectionManager.Utility;
import homebase.HomeBaseDetailScraping;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import oakwooddoors.OakWoodDoors;
import oakwooddoors.OakWoodDoorsDetailScrape;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

/**
 *
 * @author Khushbu
 */
public class WayfairDetailScraping {

    public static void main(String[] args) {
        startCrawler();
    }

    private static void startCrawler() {
        String selectQ = "select link_id, link from ross.wayfair_link_master where is_scraped=0 limit 0,5";
        MyConnection.getConnection("ross");
        ResultSet rs = MyConnection.getResultSet(selectQ);
        System.setProperty("webdriver.chrome.driver", "C:\\Users\\Khushbu\\Downloads\\chromedriver_win32(2)\\chromedriver.exe");

        //String url = "https://www.wholesaledeals.co.uk/";
        ChromeOptions options = new ChromeOptions();
        // options.addArguments("--headless");
        ChromeDriver driver = new ChromeDriver(options);
        try {
            while (rs.next()) {
                String url = rs.getString("link");
                int id = rs.getInt("link_id");
                scrapeDetails(url, id, driver);
                System.out.println("sleeping...");
                Thread.sleep(60 * 1000);

            }
        } catch (SQLException ex) {
            Logger.getLogger(HomeBaseDetailScraping.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InterruptedException ex) {
            Logger.getLogger(OakWoodDoorsDetailScrape.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private static void scrapeDetails(String url, int id, ChromeDriver driver) {
        driver.get(url);
        OakWoodDoors.waitForJSandJQueryToLoad(driver);
        System.out.println("" + url);
        Document doc = Jsoup.parse(driver.getPageSource());

        String title = "";
        String price = "";
        String woodType = "";
        String size = "";
        String desc = "";
        String details = "";
        String brand = "";
        String delivery = "";
        // String availableColors = "";
        title = doc.getElementsByAttributeValue("property", "og:title").first().attr("content");
        //  System.out.println(""+doc.html());
        price = doc.getElementsByClass("StandardPriceBlock").first().text();
        delivery = doc.getElementsByClass("ShippingHeadline").first().text();

        //   price = price.replace("from", "").trim();
        /* if (doc.getElementById("product-attribute-specs-table") != null) {
                details = doc.getElementById("product-attribute-specs-table").html();
            }
            if (details != null) {
                details = details.replaceAll("</th>", ":");
                details = details.replaceAll("</tr>", " | ");
                details = Utility.html2text(details);
                if (!details.equals("")) {
                    details = details.substring(0, details.length() - 1);
                }
            }*/
        desc = StringUtils.substringBetween(doc.html(), "\"description\":\"", "\"");
        desc = Utility.html2text(desc);
        brand = StringUtils.substringBetween(doc.html(), "\"brand\":\"", "\"");
        brand = Utility.html2text(brand);

        //"label":"Size mm","options":
        String varient[] = StringUtils.substringsBetween(doc.html(), "\"Door Size\",\"name\":\"", "\"");
        //  System.out.println(""+varient);
        if (varient != null) {
            //    price = "£" + StringUtils.substringBetween(varient, "\"basePrice\":\"", "\"");
            for (String s : varient) {
                if (size.equals("")) {
                    size = s;
                } else {
                    size = size + " | " + s;
                }
            }
        }
        System.out.println("------");
        //     System.out.println("" + title + ";" + size + ";" + price + ";" + desc + ";" + details);
        String insertQ = "INSERT INTO `ross`.`wayfair_detail_master`\n"
                + "(\n"
                + "`title`,\n"
                + "`price`,\n"
                + "`size`,\n"
                + "`delivery`,\n"
                + "`brand`,\n"
                + "`desc`,\n"
                + "`link_id`)\n"
                + "VALUES\n"
                + "("
                + "'" + Utility.prepareString(title) + "',"
                + "'" + Utility.prepareString(price) + "',"
                + "'" + Utility.prepareString(size) + "',"
                + "'" + Utility.prepareString(delivery) + "',"
                + "'" + Utility.prepareString(brand) + "',"
                + "'" + Utility.prepareString(desc) + "',"
                + id
                + ")";
        MyConnection.getConnection("ross");
        if (MyConnection.insertData(insertQ)) {
            String updateQ = "update ross.wayfair_link_master set is_scraped=1 where link_id=" + id;
            MyConnection.insertData(updateQ);
            System.out.println("INserted!");
        }


        /*if (!doc.getElementsByAttributeValue("data-heading", "material").isEmpty()) {
                Element em = doc.getElementsByAttributeValue("data-heading", "material").first();
                TextNode  ae = (TextNode) em.nextSibling();
               
                material = ae.text();
            }*/
        // System.out.println(title + ";" + price + ";" + material + ";" + size + ";" + deliveryCharge + ";" + desc);
    }
}
