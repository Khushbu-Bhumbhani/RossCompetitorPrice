/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package homebase;

import connectionManager.MyConnection;
import connectionManager.Utility;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

/**
 *
 * @author Khushbu
 */
public class HomeBaseDetailScraping {

    public static void main(String[] args) {

        // String url = "https://www.homebase.co.uk/aston-3-panel-primed-solid-internal-door-686mm-wide_p210739";
        //int id = 1;
        stratCrawler();
        //  scrapeDetails(url, id);
    }

    private static void stratCrawler() {
        String selectQ = "select link_id, link from ross.homebase_link_master where is_scraped=0";
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
            String material = "";
            String size = "";
            String deliveryCharge = "";
            String desc = "";
            // String availableColors = "";

            title = doc.getElementsByClass("page-title").first().text();
            price = doc.getElementsByClass("product-price").first().text();
            deliveryCharge = StringUtils.substringBetween(doc.html(), "\"shippingCost\":\"", "\"");
            if (!doc.getElementsByClass("tab-container__tab").isEmpty()
                    && doc.getElementsByClass("tab-container__tab").size() >= 2) {
                desc = doc.getElementsByClass("tab-container__tab").get(1).html();
                desc = desc.replaceAll("</dd>", " | ");
                desc = desc.replaceAll("</dt>", ": ");
                desc = Utility.html2text(desc);
                desc = desc.replace("Product Details", "").trim();
                desc = desc.replace("Details", "").trim();
                desc = desc.substring(0, desc.length() - 1).trim();
            }
            String str = StringUtils.substringBetween(doc.html(), "\"type\":\"Size\",\"title\":\"Width (mm)\",\"options\":[{", "]");
            if (str != null) {
                for (String sizes : StringUtils.substringsBetween(str, "\"heading\":\"", "\"")) {
                    // System.out.println("" + sizes);
                    if (size.equals("")) {
                        size = sizes;
                    } else {
                        size = size + " | " + sizes;
                    }
                }
            }
            material = StringUtils.substringBetween(desc, "Material :", "|");
            // System.out.println(title + ";" + price + ";" + material + ";" + size + ";" + deliveryCharge + ";" + desc);
            String insertQ = "INSERT INTO `ross`.`homebase_detail_master`\n"
                    + "(\n"
                    + "`title`,\n"
                    + "`price`,\n"
                    + "`size`,\n"
                    + "`delivery_charge`,\n"
                    + "`material`,\n"
                    + "`desc`,\n"
                    + "`link_id`)\n"
                    + "VALUES\n"
                    + "("
                    + "'" + Utility.prepareString(title) + "',"
                    + "'" + Utility.prepareString(price) + "',"
                    + "'" + Utility.prepareString(size) + "',"
                    + "'" + Utility.prepareString(deliveryCharge) + "',"
                    + "'" + Utility.prepareString(material) + "',"
                    + "'" + Utility.prepareString(desc) + "',"
                    + id
                    + ")";
            MyConnection.getConnection("ross");
            if (MyConnection.insertData(insertQ)) {
                String updateQ = "update homebase_link_master set is_scraped=1 where link_id=" + id;
                 MyConnection.insertData(updateQ);
                System.out.println("INserted!");
            }

        } catch (IOException ex) {
            Logger.getLogger(HomeBaseDetailScraping.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
