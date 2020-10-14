/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package doorsofdistinction;

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
public class DoorsOfDisincationDetailScrape {

    public static void main(String[] args) {
        startCrawler();
    }

    private static void startCrawler() {
        String selectQ = "select link_id, link from ross.doorsofdistinction_link_master where is_scraped=0 limit 0,1";
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
            //  String desc = "";
            // String detailsImageURL = "";
            //String brand = "";

            // String availableColors = "";
            title = doc.getElementsByAttributeValue("bgcolor", "#E5E5E5").first().text();
            // price = doc.getElementsByAttributeValue("data-test-id", "product-primary-price").first().text();
            //   price = price.replace("from", "").trim();
            /* if (!doc.getElementsByClass("webText").isEmpty()) {
                desc = doc.getElementsByClass("webText").first().text();
            }
            if (!doc.getElementsByClass("subimg").isEmpty()) {
                detailsImageURL = "https://www.doorstore.co.uk"+doc.getElementsByClass("subimg").first().attr("src");
            }*/

            String varient[] = StringUtils.substringsBetween(doc.html(), "Imperial Sizes", "</table>");
            // boolean isFirst = true;
            int index = 0;
            if (varient != null) {
                for (String o : varient) {

                    Document sd = Jsoup.parse(o);
                   // System.out.println("" + sd.html());
                    String tickness = StringUtils.substringBefore(o, "</u>");
                    tickness = tickness.replace("-", "");
                  //  String heading = "";
                  //  Element ele = doc.getElementsContainingOwnText("Imperial Sizes").get(index);
                    //ele = ele.parent().parent().parent();
                //    heading = ele.text();
                    for (Element e : sd.getElementsByClass("smallbluetext")) {
                        if (size.equals("")) {
                            size = e.text();
                        } else {
                            size = size + " | " + e.text();
                        }
                    }
                    if (!sd.getElementsByClass("Red161").isEmpty()) {
                        price = sd.getElementsByClass("Red161").last().text();
                    } else if (!sd.getElementsByClass("Red16").isEmpty()) {
                        price = sd.getElementsByClass("Red16").last().text();
                    }
                    String insertQ = "INSERT INTO `ross`.`doorsofdistinction_detail_master`\n"
                            + "(\n"
                            + "`title`,\n"
                            + "`price`,\n"
                            + "`size`,\n"
                            + "`thickness`,\n"
                            //      + "`detail_image_url`,\n"
                       //     + "`header`,\n"
                            + "`link_id`)"
                            + "VALUES\n"
                            + "("
                            + "'" + Utility.prepareString(title) + "',"
                            + "'" + Utility.prepareString(price) + "',"
                            + "'" + Utility.prepareString(size) + "',"
                            + "'" + Utility.prepareString(tickness) + "',"
                            //        + "'" + Utility.prepareString(detailsImageURL) + "',"
                        //    + "'" + Utility.prepareString(heading) + "',"
                            + id
                            + ")";
                    MyConnection.getConnection("ross");
                    if (MyConnection.insertData(insertQ)) {
                        String updateQ = "update ross.doorsofdistinction_link_master set is_scraped=1 where link_id=" + id;
                        MyConnection.insertData(updateQ);
                        System.out.println("INserted!");
                    }
                    //    System.out.println("" + title + ";" + size + ";" + price + ";" + desc + ";" + details);

                }
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
