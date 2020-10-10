/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package buildingsupplies;

import oakwooddoors.*;
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
public class BuildingSuppliesDoorsDetailScrape {

    public static void main(String[] args) {
        startCrawler();
    }

    private static void startCrawler() {
        String selectQ = "select link_id, link from ross.buildingsuppliesonline_link_master where is_scraped=0";
        MyConnection.getConnection("ross");
        ResultSet rs = MyConnection.getResultSet(selectQ);
        try {
            while (rs.next()) {
                String url = rs.getString("link");
                int id = rs.getInt("link_id");
                scrapeDetails(url, id);
                Thread.sleep(5000);
            }
        } catch (SQLException ex) {
            Logger.getLogger(HomeBaseDetailScraping.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InterruptedException ex) {
            Logger.getLogger(BuildingSuppliesDoorsDetailScrape.class.getName()).log(Level.SEVERE, null, ex);
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
            String brand = "";

            // String availableColors = "";
            title = doc.getElementsByAttributeValue("name", "title").first().attr("content");
//            price = doc.getElementsByClass("price-including-tax").first().text();
            //   price = price.replace("from", "").trim();
            if (doc.getElementById("description") != null) {
                desc = doc.getElementById("description").text();
            }

            brand = StringUtils.substringBetween(doc.html(), "\"brand\":\"", "\"");
            brand = Utility.html2text(brand);
            Element table = doc.getElementById("super-product-table");
            boolean isFirst = true;
            if (table != null) {
                for (Element tr : table.getElementsByTag("tr")) {
                    if (isFirst) {
                        isFirst = false;
                        continue;
                    }
                    size = tr.getElementsByClass("product-item-name").first().text();
                    if (size.contains("-")) {
                        size = StringUtils.substringAfter(size, "-").trim();
                    }
                    price = tr.getElementsByClass("price-final_price").first().text();
                    String insertQ = "INSERT INTO `buildingsuppliesonline_detail_master`\n"
                            + "(\n"
                            + "`title`,\n"
                            + "`price`,\n"
                            + "`size`,\n"
                            + "`desc`,\n"
                            + "`brand`,\n"
                            + "`link_id`) "
                            + "VALUES\n"
                            + "("
                            + "'" + Utility.prepareString(title) + "',"
                            + "'" + Utility.prepareString(price) + "',"
                            + "'" + Utility.prepareString(size) + "',"
                            + "'" + Utility.prepareString(desc) + "',"
                            + "'" + Utility.prepareString(brand) + "',"
                            + id
                            + ")";
                    MyConnection.getConnection("ross");
                    if (MyConnection.insertData(insertQ)) {
                        String updateQ = "update ross.buildingsuppliesonline_link_master set is_scraped=1 where link_id=" + id;
                        MyConnection.insertData(updateQ);
                        System.out.println("INserted!");
                    }

                }
            } else {
                price = doc.getElementsByAttributeValue("itemprop", "price").first().attr("content");
                size = StringUtils.substringBetween(doc.html(), "Full Size: </strong>", "</li>");
                size = Utility.html2text(size);
                String insertQ = "INSERT INTO `buildingsuppliesonline_detail_master`\n"
                        + "(\n"
                        + "`title`,\n"
                        + "`price`,\n"
                        + "`size`,\n"
                        + "`desc`,\n"
                        + "`brand`,\n"
                        + "`link_id`) "
                        + "VALUES\n"
                        + "("
                        + "'" + Utility.prepareString(title) + "',"
                        + "'" + Utility.prepareString(price) + "',"
                        + "'" + Utility.prepareString(size) + "',"
                        + "'" + Utility.prepareString(desc) + "',"
                        + "'" + Utility.prepareString(brand) + "',"
                        + id
                        + ")";
                MyConnection.getConnection("ross");
                if (MyConnection.insertData(insertQ)) {
                    String updateQ = "update ross.buildingsuppliesonline_link_master set is_scraped=1 where link_id=" + id;
                    MyConnection.insertData(updateQ);
                    System.out.println("INserted!");
                }

            }

            // System.out.println(title + ";" + price + ";" + material + ";" + size + ";" + deliveryCharge + ";" + desc);
        } catch (IOException ex) {
            Logger.getLogger(HomeBaseDetailScraping.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
