/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package vibrantdoors;

import connectionManager.MyConnection;
import connectionManager.Utility;
import homebase.HomeBaseDetailScraping;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.scene.paint.Material;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

/**
 *
 * @author Khushbu
 */
public class VibrantDoorsDetailScrape {

    public static void main(String[] args) {
        startCrawler();
    }

    private static void startCrawler() {
        String selectQ = "select link_id, link from ross.vibrantdoors_link_master where is_scraped=0";
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
            // System.out.println(""+doc.html());
            String title = "";
            String price = "";
            String material = "";
            String size = "";
            String desc = "";
            String details = "";
            String brand = "";

            // String availableColors = "";
            title = doc.getElementsByAttributeValue("itemprop", "name").get(4).attr("content");
            brand = doc.getElementsByAttributeValue("itemprop", "brand").first().attr("content");
            price = doc.getElementsByClass("price").first().text();
            material = StringUtils.substringBetween(doc.html(), " Material\n"
                    + "            </div>", "</div>");

            material = Utility.html2text(material);
            //   price = price.replace("from", "").trim();
            if (doc.getElementsByClass("description").isEmpty()) {
                desc = doc.getElementsByClass("description").first().nextElementSibling().text();
            }
            if (doc.getElementsByClass("detail_wrap").size() >= 2
                    && doc.getElementsByClass("detail_wrap").get(1).previousElementSibling().text().contains("Specifications ")) {
                details = doc.getElementsByClass("detail_wrap").get(1).text();
            }
            boolean isFirst = true;
            // if (select != null) {
            if (!doc.getElementsByAttributeValue("name", "product").isEmpty()) {
                Element select = doc.getElementsByAttributeValue("name", "product").first();
                for (Element o : select.getElementsByTag("option")) {
                    if (isFirst) {
                        isFirst = false;
                        continue;
                    }
                    if (size.equals("")) {
                        size = o.text();
                    } else {
                        size = size + " | " + o.text();
                    }
                }
            }
            String insert = "INSERT INTO `ross`.`vibrantdoors_detail_master`\n"
                    + "(\n"
                    + "`title`,\n"
                    + "`price`,\n"
                    + "`size`,\n"
                    + "`details`,\n"
                    + "`desc`,\n"
                    + "`brand`,\n"
                    + "`material`,\n"
                    + "`link_id`)\n"
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
            if (MyConnection.insertData(insert)) {
                String updateQ = "update ross.vibrantdoors_link_master set is_scraped=1 where link_id=" + id;
                MyConnection.insertData(updateQ);
                System.out.println("INserted!");
            }

            // System.out.println(title + ";" + price + ";" + material + ";" + size + ";" + deliveryCharge + ";" + desc);
        } catch (IOException ex) {
            Logger.getLogger(HomeBaseDetailScraping.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
