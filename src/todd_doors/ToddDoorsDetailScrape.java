/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package todd_doors;

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
public class ToddDoorsDetailScrape {

    public static void main(String[] args) {
        startCrawler();
    }

    private static void startCrawler() {
        String selectQ = "select link_id, link from ross.todddoors_link_master where is_scraped=0";
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
            String desc = "";
            String specification = "";
            String brand = "";

            // String availableColors = "";
            title = doc.getElementById("product-details-form").getElementsByTag("h1").first().text();
            //    price = doc.getElementsByClass("price__cost").first().text();
            //   price = price.replace("from", "").trim();
            desc = doc.getElementById("quickTab-description").text();
            specification = doc.getElementById("quickTab-spec").html();

            if (specification != null) {
                material = StringUtils.substringBetween(specification, "Timber</th>", "</tr>");
                material = Utility.html2text(material);
                specification = specification.replaceAll("</th>", ":");
                specification = specification.replaceAll("</tr>", " | ");
                specification = Utility.html2text(specification);
                specification = specification.substring(0, specification.length() - 1);
            }

            //Element select = doc.getElementsByClass("product-variant").first();
            boolean isFirst = true;
            // if (select != null) {
            brand = StringUtils.substringBetween(doc.html(), "brand': '", "'");
            if (doc.getElementById("product-variant") != null) {
                for (Element o : doc.getElementById("product-variant").getElementsByTag("option")) {
                    if (isFirst) {
                        isFirst = false;
                        continue;
                    }
                    size = o.text();
                    price = o.attr("now").replace("&#163;", "£");

                    // System.out.println("------");
                    //  System.out.println("" + title + ";" + size + ";" + price + ";" + desc + ";" + specification);
                    String insertQ = "INSERT INTO `ross`.`todddoors_deatils_master`\n"
                            + "("
                            + "`title`,\n"
                            + "`price`,\n"
                            + "`size`,\n"
                            + "`specification`,\n"
                            + "`desc`,\n"
                            + "`brand`,\n"
                            + "`material_timber`,\n"
                            + "`link_id`)"
                            + "VALUES\n"
                            + "("
                            + "'" + Utility.prepareString(title) + "',"
                            + "'" + Utility.prepareString(price) + "',"
                            + "'" + Utility.prepareString(size) + "',"
                            + "'" + Utility.prepareString(specification) + "',"
                            + "'" + Utility.prepareString(desc) + "',"
                            + "'" + Utility.prepareString(brand) + "',"
                            + "'" + Utility.prepareString(material) + "',"
                            + id
                            + ")";
                    MyConnection.getConnection("ross");
                    if (MyConnection.insertData(insertQ)) {
                        String updateQ = "update ross.todddoors_link_master set is_scraped=1 where link_id=" + id;
                        MyConnection.insertData(updateQ);
                        System.out.println("INserted!");
                    }
                    // }
                }
            } else {
                //add single row
                if (doc.getElementById("now-price") != null) {
                    price = doc.getElementById("now-price").text();
                    price = price.replace("&#163;", "£");
                    price = price.replace("Now from", "").trim();
                }
                String insertQ = "INSERT INTO `ross`.`todddoors_deatils_master`\n"
                        + "("
                        + "`title`,\n"
                        + "`price`,\n"
                        + "`size`,\n"
                        + "`specification`,\n"
                        + "`desc`,\n"
                        + "`brand`,\n"
                        + "`material_timber`,\n"
                        + "`link_id`)"
                        + "VALUES\n"
                        + "("
                        + "'" + Utility.prepareString(title) + "',"
                        + "'" + Utility.prepareString(price) + "',"
                        + "'" + Utility.prepareString(size) + "',"
                        + "'" + Utility.prepareString(specification) + "',"
                        + "'" + Utility.prepareString(desc) + "',"
                        + "'" + Utility.prepareString(brand) + "',"
                        + "'" + Utility.prepareString(material) + "',"
                        + id
                        + ")";
                MyConnection.getConnection("ross");
                if (MyConnection.insertData(insertQ)) {
                    String updateQ = "update ross.todddoors_link_master set is_scraped=1 where link_id=" + id;
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
