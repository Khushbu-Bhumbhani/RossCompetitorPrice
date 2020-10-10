/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package oakwooddoors;

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
public class OakWoodDoorsDetailScrape {

    public static void main(String[] args) {
        startCrawler();
    }

    private static void startCrawler() {
        String selectQ = "select link_id, link from ross.oakwooddoors_link_master where is_scraped=0";
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
            Logger.getLogger(OakWoodDoorsDetailScrape.class.getName()).log(Level.SEVERE, null, ex);
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
            String woodType = "";
            String size = "";
            String desc = "";
            String details = "";
            String brand = "";

            // String availableColors = "";
            title = doc.getElementsByAttributeValue("itemprop", "name").first().text();
            price = doc.getElementsByClass("price-including-tax").first().text();
            //   price = price.replace("from", "").trim();
            if (doc.getElementsByAttributeValue("itemprop", "description") != null) {
                desc = doc.getElementsByAttributeValue("itemprop", "description").first().text();
            }
            if (doc.getElementById("product-attribute-specs-table") != null) {
                details = doc.getElementById("product-attribute-specs-table").html();
            }
            if (details != null) {
                details = details.replaceAll("</th>", ":");
                details = details.replaceAll("</tr>", " | ");
                details = Utility.html2text(details);
                if (!details.equals("")) {
                    details = details.substring(0, details.length() - 1);
                }
            }
            woodType = StringUtils.substringBetween(doc.html(), "Wood Type</th>", "</td>");
            woodType = Utility.html2text(woodType);
            brand = StringUtils.substringBetween(doc.html(), "Manufacturer</th>", "</td>");
            brand = Utility.html2text(brand);

            //"label":"Size mm","options":
            String varient = StringUtils.substringBetween(doc.html(), "\"label\":\"Size mm\",\"options\":", "}});");
            //  System.out.println(""+varient);
            if (varient != null && StringUtils.substringsBetween(varient, "{\"id\":", "}") != null) {
                //    price = "£" + StringUtils.substringBetween(varient, "\"basePrice\":\"", "\"");
                for (String o : StringUtils.substringsBetween(varient, "{\"id\":", "}")) {
                    String s = StringUtils.substringBetween(o, "\"label\":\"", "\"");
                    if (size.equals("")) {
                        size = s;
                    } else {
                        size = size + " | " + s;
                    }
                }
            } else {
                boolean isFirst = true;
                // if (select != null) {
                if (!doc.getElementsByClass("product-custom-option").isEmpty()) {
                    Element select = doc.getElementsByClass("product-custom-option").first();
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
            }
            System.out.println("------");
            //     System.out.println("" + title + ";" + size + ";" + price + ";" + desc + ";" + details);
            String insertQ = "INSERT INTO `ross`.`oakwooddoor_detail_master`\n"
                    + "(\n"
                    + "`title`,\n"
                    + "`price`,\n"
                    + "`size`,\n"
                    + "`details`,\n"
                    + "`woodtype`,\n"
                    + "`brand`,\n"
                    + "`desc`,\n"
                    + "`link_id`)\n"
                    + "VALUES\n"
                    + "("
                    + "'" + Utility.prepareString(title) + "',"
                    + "'" + Utility.prepareString(price) + "',"
                    + "'" + Utility.prepareString(size) + "',"
                    + "'" + Utility.prepareString(details) + "',"
                    + "'" + Utility.prepareString(woodType) + "',"
                    + "'" + Utility.prepareString(brand) + "',"
                    + "'" + Utility.prepareString(desc) + "',"
                    + id
                    + ")";
            MyConnection.getConnection("ross");
            if (MyConnection.insertData(insertQ)) {
                String updateQ = "update ross.oakwooddoors_link_master set is_scraped=1 where link_id=" + id;
                 MyConnection.insertData(updateQ);
                System.out.println("INserted!");
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
