/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package onlinedoorstore;

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
public class OnlineDoorsSoreDetailScrape {

    public static void main(String[] args) {
        startCrawler();
    }

    private static void startCrawler() {
        String selectQ = "select link_id, link from ross.onlinedoorstore_link_master where is_scraped=0";
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
            //String material = "";
            String size = "";
            String desc = "";
            String details = "";
            String brand = "";

            // String availableColors = "";
            title = doc.getElementsByClass("product_title").first().text();
            //    price = doc.getElementsByClass("price__cost").first().text();
            //   price = price.replace("from", "").trim();
            if (doc.getElementById("tab-description") != null) {
                desc = doc.getElementById("tab-description").text();
            }
            if (doc.getElementById("tab-additional_information") != null) {
                details = doc.getElementById("tab-additional_information").getElementsByTag("table").first().html();
            }
            if (details != null) {
                details = details.replaceAll("</th>", ":");
                details = details.replaceAll("</tr>", " | ");
                details = Utility.html2text(details);
                if (!details.equals("")) {
                    details = details.substring(0, details.length() - 1);
                }
            }

            //Element select = doc.getElementsByClass("product-variant").first();
            //boolean isFirst = true;
            // if (select != null) {
            brand = StringUtils.substringBetween(doc.html(), "Brand</th>", "</tr");
            brand = Utility.html2text(brand);
            String varient = StringUtils.substringBetween(doc.html(), "data-product_variations=\"", "\"");
            if (varient != null && StringUtils.substringsBetween(varient, "attribute_pa_available-sizes", "display_regular_price")!=null) {
                for (String o : StringUtils.substringsBetween(varient, "attribute_pa_available-sizes", "display_regular_price")) {
                    size = StringUtils.substringBefore(o, "}");
                    size = size.replace("&quot;:&quot;", "");
                    size = size.replace("&quot;", "");
                    price = "£" + StringUtils.substringBetween(o, "display_price&quot;:", ",");
                    System.out.println("------");
                    //     System.out.println("" + title + ";" + size + ";" + price + ";" + desc + ";" + details);
                    String insertQ = "INSERT INTO `ross`.`onlinedoorstore_detail_master`\n"
                            + "(\n"
                            + "`title`,\n"
                            + "`price`,\n"
                            + "`size`,\n"
                            + "`details`,\n"
                            + "`brand`,\n"
                            + "`desc`,\n"
                            + "`link_id`)\n"
                            + "VALUES\n"
                            + "("
                            + "'" + Utility.prepareString(title) + "',"
                            + "'" + Utility.prepareString(price) + "',"
                            + "'" + Utility.prepareString(size) + "',"
                            + "'" + Utility.prepareString(details) + "',"
                            + "'" + Utility.prepareString(brand) + "',"
                            + "'" + Utility.prepareString(desc) + "',"
                            + id
                            + ")";
                    MyConnection.getConnection("ross");
                    if (MyConnection.insertData(insertQ)) {
                        String updateQ = "update ross.onlinedoorstore_link_master set is_scraped=1 where link_id=" + id;
                         MyConnection.insertData(updateQ);
                        System.out.println("INserted!");
                    }
                    // }
                }
            } else {
                //add single record
              
                System.out.println("------");
                //     System.out.println("" + title + ";" + size + ";" + price + ";" + desc + ";" + details);
                String insertQ = "INSERT INTO `ross`.`onlinedoorstore_detail_master`\n"
                        + "(\n"
                        + "`title`,\n"
                        + "`price`,\n"
                        + "`size`,\n"
                        + "`details`,\n"
                        + "`brand`,\n"
                        + "`desc`,\n"
                        + "`link_id`)\n"
                        + "VALUES\n"
                        + "("
                        + "'" + Utility.prepareString(title) + "',"
                        + "'" + Utility.prepareString(price) + "',"
                        + "'" + Utility.prepareString(size) + "',"
                        + "'" + Utility.prepareString(details) + "',"
                        + "'" + Utility.prepareString(brand) + "',"
                        + "'" + Utility.prepareString(desc) + "',"
                        + id
                        + ")";
                MyConnection.getConnection("ross");
                if (MyConnection.insertData(insertQ)) {
                    String updateQ = "update ross.onlinedoorstore_link_master set is_scraped=1 where link_id=" + id;
                    MyConnection.insertData(updateQ);
                    System.out.println("INserted!");
                }
            }

            /*if (!doc.getElementsByAttributeValue("data-heading", "material").isEmpty()) {
                Element em = doc.getElementsByAttributeValue("data-heading", "material").first();
                TextNode  ae = (TextNode) em.nextSibling();
               
                material = ae.text();
            }*/
            // System.out.println(title + ";" + price + ";" + material + ";" + size + ";" + deliveryCharge + ";" + desc);
        } catch (IOException ex) {
            Logger.getLogger(HomeBaseDetailScraping.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
