/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package doorstore;

import connectionManager.MyConnection;
import connectionManager.Utility;
import homebase.HomeBaseDetailScraping;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

/**
 *
 * @author Khushbu
 */
public class DoorStoreDetailScrape {

    public static void main(String[] args) {
        startCrawler();
    }

    private static void startCrawler() {
        String selectQ = "select link_id, link from ross.doorstore_link_master where is_scraped=0";
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
            String detailsImageURL = "";
            //String brand = "";

            // String availableColors = "";
            title = doc.getElementsByClass("prodCat").first().text();
            // price = doc.getElementsByAttributeValue("data-test-id", "product-primary-price").first().text();
            //   price = price.replace("from", "").trim();
            if (!doc.getElementsByClass("webText").isEmpty()) {
                desc = doc.getElementsByClass("webText").first().text();
            }
            if (!doc.getElementsByClass("subimg").isEmpty()) {
                detailsImageURL = "https://www.doorstore.co.uk"+doc.getElementsByClass("subimg").first().attr("src");
            }

            if (!doc.getElementsByClass("tbl").isEmpty()) {
                Element varientE = doc.getElementsByClass("tbl").first();
                boolean isFirst = true;
                // if (varientE != null) {
                for (Element o : varientE.getElementsByClass("tr")) {
                    if (isFirst) {
                        isFirst = false;
                        continue;
                    }
                    //  System.out.println(""+o.html());
                    if (!o.getElementsByClass("tc-title").isEmpty()) {
                        size = o.getElementsByClass("tc-title").first().text();
                        price = o.getElementsByClass("tc-price").first().text();
                        String insertQ = "INSERT INTO `ross`.`doorstore_detail_master`\n"
                                + "(\n"
                                + "`title`,\n"
                                + "`price`,\n"
                                + "`size`,\n"
                                + "`detail_image_url`,\n"
                                + "`desc`,\n"
                                + "`link_id`)"
                                + "VALUES\n"
                                + "("
                                + "'" + Utility.prepareString(title) + "',"
                                + "'" + Utility.prepareString(price) + "',"
                                + "'" + Utility.prepareString(size) + "',"
                                + "'" + Utility.prepareString(detailsImageURL) + "',"
                                + "'" + Utility.prepareString(desc) + "',"
                                + id
                                + ")";
                        MyConnection.getConnection("ross");
                        if (MyConnection.insertData(insertQ)) {
                            String updateQ = "update ross.doorstore_link_master set is_scraped=1 where link_id=" + id;
                             MyConnection.insertData(updateQ);
                            System.out.println("INserted!");
                        }
                        //    System.out.println("" + title + ";" + size + ";" + price + ";" + desc + ";" + details);
                    }
                }
                // }
            }
            //Element select = doc.getElementsByClass("product-variant").first();
            //boolean isFirst = true;
            // if (select != null) {
            // brand = StringUtils.substringBetween(doc.html(), "Brand</th>", "</tr");
            // brand = Utility.html2text(brand);

            // }
        } catch (IOException ex) {
            Logger.getLogger(HomeBaseDetailScraping.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
