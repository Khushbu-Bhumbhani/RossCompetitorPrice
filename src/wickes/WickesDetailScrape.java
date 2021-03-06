/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wickes;

import connectionManager.MyConnection;
import connectionManager.Utility;
import homebase.HomeBaseDetailScraping;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

/**
 *
 * @author Khushbu
 */
public class WickesDetailScrape {

    public static void main(String[] args) {
        startCrawler();
    }

    private static void startCrawler() {
        String selectQ = "select link_id, link from ross.wickes_link_master where is_scraped=0";
        MyConnection.getConnection("ross");
        ResultSet rs = MyConnection.getResultSet(selectQ);
        try {
            while (rs.next()) {
                String url = rs.getString("link");
                int id = rs.getInt("link_id");
                scrapeDetails(url, id);
            }
        } catch (SQLException ex) {
            Logger.getLogger(HomeBaseDetailScraping.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private static void scrapeDetails(String url, int id) {
        try {
            System.out.println("" + url);
            Document doc = Jsoup.connect(url)
                    .ignoreContentType(true)
                    .ignoreHttpErrors(true)
                    .timeout(0).get();

            String title = "";
            String price = "";
            String size = "";
            String desc = "";
            String details = "";
            String brand = "";
            String material = "";

            // String availableColors = "";
            title = doc.getElementsByClass("pdp__heading").first().text();
            /* price = StringUtils.substringBetween(doc.html(), "price: \"", "\"");
            price = Utility.html2text(price);*/
            price = doc.getElementsByClass("pdp-price__new-price").first().text();
            //   price = price.replace("from", "").trim();
            if (doc.getElementById("product-details") != null) {
                details = doc.getElementById("product-details").html();
            }
            desc = doc.getElementsByClass("product-main-info__description").first().text();
            /* if (doc.getElementsByAttributeValue("data-tab", "Details").size() >= 2) {
                details = doc.getElementsByAttributeValue("data-tab", "Details").get(1).html();
            }*/
            if (details != null) {
                details = details.replaceAll("</li>", " | ");
                details = Utility.html2text(details);
                if (!details.equals("")) {
                    details = details.substring(0, details.length() - 1);
                }
            }

            //Element select = doc.getElementsByClass("product-variant").first();
            //boolean isFirst = true;
            // if (select != null) {
            brand = StringUtils.substringBetween(doc.html(), "Brand Name:</strong>", "</li>");
            Element varientE = doc.getElementById("variant");
            boolean isFirst = true;
            if (varientE != null) {
                for (Element o : varientE.getElementsByTag("option")) {
                    if (isFirst) {
                        isFirst = false;
                        continue;
                    }
                    if (size.equals("")) {
                        size = o.text();
                    } else {
                        size = size + " | " + o.text();
                    }
                    //    System.out.println("" + title + ";" + size + ";" + price + ";" + desc + ";" + details);

                }
            }
            material = StringUtils.substringBetween(doc.html(), "Material:</strong>", "</li>");
            material = Utility.html2text(material);
            String insertQ = "INSERT INTO `wickes_detail_master`\n"
                    + "(\n"
                    + "`title`,\n"
                    + "`price`,\n"
                    + "`size`,\n"
                    + "`details`,\n"
                    + "`desc`,\n"
                    + "`brand`,\n"
                    + "`material`,\n"
                    + "`link_id`) "
                    + "VALUES\n"
                    + "("
                    + "'" + Utility.prepareString(title) + "',"
                    + "'" + Utility.prepareString(price) + "',"
                    + "'" + Utility.prepareString(size) + "',"
                    + "'" + Utility.prepareString(details) + "',"
                    + "'" + Utility.prepareString(desc) + "',"
                    + "'" + Utility.prepareString(brand) + "',"
                    + "'" + Utility.prepareString(material) + "',"
                    + id
                    + ")";
            MyConnection.getConnection("ross");
            if (MyConnection.insertData(insertQ)) {
                String updateQ = "update ross.wickes_link_master set is_scraped=1 where link_id=" + id;
                MyConnection.insertData(updateQ);
                System.out.println("INserted!");
            }
            // }
            // System.out.println(title + ";" + price + ";" + material + ";" + size + ";" + deliveryCharge + ";" + desc);
        } catch (IOException ex) {
            Logger.getLogger(HomeBaseDetailScraping.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
