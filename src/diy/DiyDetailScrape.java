/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diy;

import onlinedoorstore.*;
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
public class DiyDetailScrape {

    public static void main(String[] args) {
        startCrawler();
    }

    private static void startCrawler() {
        String selectQ = "select link_id, link from ross.diy_link_master where is_scraped=0";
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
            title = doc.getElementsByAttributeValue("property", "og:title").first().attr("content");
            price = doc.getElementsByAttributeValue("data-test-id","product-primary-price").first().text();
            //   price = price.replace("from", "").trim();
            if (!doc.getElementsByAttributeValue("data-test-id", "ProductDescText").isEmpty()) {
                desc = doc.getElementsByAttributeValue("data-test-id", "ProductDescText").first().text();
            }
            if (!doc.getElementsByClass("f16ac490 eddf1b8e").isEmpty()) {
                details = doc.getElementsByClass("f16ac490 eddf1b8e").first().html();
            }
            if (details != null) {
                details = details.replaceAll("</th>", ":");
                details = details.replaceAll("</tr>", " | ");
                details = Utility.html2text(details);
                if (!details.equals("")) {
                    details = details.substring(0, details.length() - 1);
                }
            }
            if (!doc.getElementsByClass("e1b2e4bb").isEmpty()) {
                Element varientE = doc.getElementsByClass("e1b2e4bb").first();
                // boolean isFirst = true;
                if (varientE != null) {
                    for (Element o : varientE.getElementsByTag("option")) {
                        /*if (isFirst) {
                            isFirst = false;
                            continue;
                        }*/
                        if (size.equals("")) {
                            size = o.text();
                        } else {
                            size = size + " | " + o.text();
                        }
                        //    System.out.println("" + title + ";" + size + ";" + price + ";" + desc + ";" + details);

                    }
                }
            }
            //Element select = doc.getElementsByClass("product-variant").first();
            //boolean isFirst = true;
            // if (select != null) {
            // brand = StringUtils.substringBetween(doc.html(), "Brand</th>", "</tr");
            // brand = Utility.html2text(brand);
            String insertQ = "INSERT INTO `ross`.`diy_detail_master`\n"
                    + "(\n"
                    + "`title`,\n"
                    + "`price`,\n"
                    + "`size`,\n"
                    + "`details`,\n"
                    + "`desc`,link_id)\n"
                    + "VALUES\n"
                    + "("
                    + "'" + Utility.prepareString(title) + "',"
                    + "'" + Utility.prepareString(price) + "',"
                    + "'" + Utility.prepareString(size) + "',"
                    + "'" + Utility.prepareString(details) + "',"
                    + "'" + Utility.prepareString(desc) + "',"
                    + id
                    + ")";
            MyConnection.getConnection("ross");
            if (MyConnection.insertData(insertQ)) {
                String updateQ = "update ross.diy_link_master set is_scraped=1 where link_id=" + id;
                MyConnection.insertData(updateQ);
                System.out.println("INserted!");
            }
            // }
        } catch (IOException ex) {
            Logger.getLogger(HomeBaseDetailScraping.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
