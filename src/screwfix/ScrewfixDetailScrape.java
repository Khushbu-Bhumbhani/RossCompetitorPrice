/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package screwfix;

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
public class ScrewfixDetailScrape {

    public static void main(String[] args) {
        startCrawler();
    }

    private static void startCrawler() {
        String selectQ = "select link_id, link from ross.screwfix_link_master where is_scraped=0";
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
            String finish = "";
            // String availableColors = "";
            title = doc.getElementsByAttributeValue("property", "og:title").first().attr("content");
            /* price = StringUtils.substringBetween(doc.html(), "price: \"", "\"");
            price = Utility.html2text(price);*/
            price = doc.getElementById("product_price").text();
            //   price = price.replace("from", "").trim();
            if (doc.getElementById("product-details") != null) {
                details = doc.getElementById("product-details").html();
            }
            desc = doc.getElementById("product_long_description_container").text();
            desc = desc + " " + doc.getElementById("product_specification_list").text();
            /* if (doc.getElementsByAttributeValue("data-tab", "Details").size() >= 2) {
                details = doc.getElementsByAttributeValue("data-tab", "Details").get(1).html();
            }*/
            Element table = doc.getElementById("product_selling_attributes_table");
            boolean isFirst = true;
            for (Element tr : table.getElementsByTag("tr")) {
                if (isFirst) {
                    isFirst = false;
                    continue;
                }
           //     System.out.println(""+tr.getElementsByTag("td").size()+" "+tr.text());
                details = details + tr.getElementsByTag("td").get(0).text() + ": " + tr.getElementsByTag("td").get(1).text() + " | ";
            }

            if (!details.equals("")) {
                details = details.substring(0, details.length() - 1);
            }

            //Element select = doc.getElementsByClass("product-variant").first();
            //boolean isFirst = true;
            // if (select != null) {
            brand = StringUtils.substringBetween(doc.html(), "Brand</td>", "</tr>");
            brand = Utility.html2text(brand);

            material = StringUtils.substringBetween(doc.html(), "Construction Material (Building)</td>", "</tr>");
            material = Utility.html2text(material);

            finish = StringUtils.substringBetween(doc.html(), "Door Finish Type</td>", "</tr>");
            finish = Utility.html2text(finish);

            size = StringUtils.substringBetween(doc.html(), "Door Height</td>", "</tr>");
            size = size + " X " + StringUtils.substringBetween(doc.html(), "Door Width</td>", "</tr>");
            size= Utility.html2text(size);
            
            String insertQ = "INSERT INTO `ross`.`screwfix_detail_master`\n"
                    + "(\n"
                    + "`title`,\n"
                    + "`price`,\n"
                    + "`size`,\n"
                    + "`details`,\n"
                    + "`desc`,\n"
                    + "`brand`,\n"
                    + "`material`,\n"
                    + "`link_id`,\n"
                    + "`finish`)\n"
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
                    + ",'" + Utility.prepareString(finish) + "'"
                    + ")";
            MyConnection.getConnection("ross");
            if (MyConnection.insertData(insertQ)) {
                String updateQ = "update ross.screwfix_link_master set is_scraped=1 where link_id=" + id;
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
