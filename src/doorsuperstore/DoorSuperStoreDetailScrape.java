/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package doorsuperstore;

import connectionManager.MyConnection;
import connectionManager.Utility;
import homebase.HomeBaseDetailScraping;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

/**
 *
 * @author Khushbu
 */
public class DoorSuperStoreDetailScrape {

    public static void main(String[] args) {
        startCrawler();
    }

    private static void startCrawler() {
        String selectQ = "select link_id, link from ross.doorsuperstore_link_master where is_scraped=0";
        MyConnection.getConnection("ross");
        ResultSet rs = MyConnection.getResultSet(selectQ);
        try {
            while (rs.next()) {
                String url = rs.getString("link");
                int id = rs.getInt("link_id");
                scrapeDetails(url, id);
                Thread.sleep(1000);
            }
        } catch (SQLException ex) {
            Logger.getLogger(HomeBaseDetailScraping.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InterruptedException ex) {
            Logger.getLogger(DoorSuperStoreDetailScrape.class.getName()).log(Level.SEVERE, null, ex);
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
            String brand = "";
            //String material = "";
            String size = "";
            String desc = "";
            String specification = "";
            String techdetails = "";

            // String availableColors = "";
            title = doc.getElementsByClass("title").first().text();
            if (!doc.getElementsByClass("price__cost").isEmpty()) {
                price = doc.getElementsByClass("price__cost").first().text();
                price = price.replace("from", "").trim();
            }
            desc = StringUtils.substringBetween(doc.html(), "Product Description</a>", "</ul>");
            desc = Utility.html2text(desc);
            techdetails = StringUtils.substringBetween(doc.html(), "<h3>Technical Information", "</ul>");
            techdetails = Utility.html2text(techdetails);
            if (doc.getElementById("js-product-specifications") != null) {
                specification = doc.getElementById("js-product-specifications").html();
                specification = specification.replaceAll("<span class=\"value\">", ":");
                specification = specification.replaceAll("<span class=\"heading\"", " | <span ");
                specification = Utility.html2text(specification);
                specification = specification.replace("Specifications Details", "").trim();
                specification = specification.substring(1, specification.length());
            } else {
                specification = StringUtils.substringBetween(doc.html(), "MATERIAL:&nbsp;", "</p>");
                if (specification != null) {
                    specification = specification.replaceAll("<br />", " | ");
                    specification = Utility.html2text(specification);
                    specification = specification.substring(0, specification.length() - 1).trim();
                }
            }
            if (specification == null) {
                specification = StringUtils.substringBetween(doc.html(), "<h3>Specifications", "</p>");
                if (specification != null) {
                    specification = specification.replaceAll("<br />", " | ");
                    specification = Utility.html2text(specification);
                    specification = specification.substring(0, specification.length() - 1).trim();
                }
            }
            brand = StringUtils.substringBetween(doc.html(), "\"brand\":\"", "\"");

            Element select = doc.getElementsByAttributeValue("data-attribute-name", "Size").first();
            boolean isFirst = true;
            if (select != null) {
                for (Element o : select.getElementsByTag("option")) {
                    // System.out.println("" + sizes);
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
            // material = StringUtils.substringBetween(doc.html(), "<span class=\"heading\" data-heading=\"material\">Material</span>", "</span>");
            // material = Utility.html2text(material);

            //  Element e = doc.getElementsByClass("breadcrumb").first();
            // material = StringUtils.substringBeforeLast(e.text(), "/");
            // material = StringUtils.substringAfterLast(material, "/");
            /*if (!doc.getElementsByAttributeValue("data-heading", "material").isEmpty()) {
                Element em = doc.getElementsByAttributeValue("data-heading", "material").first();
                TextNode  ae = (TextNode) em.nextSibling();
               
                material = ae.text();
            }*/
            // System.out.println(title + ";" + price + ";" + material + ";" + size + ";" + deliveryCharge + ";" + desc);
            String insertQ = "INSERT INTO `ross`.`doorsuperstore_detail_master`\n"
                    + "(\n"
                    + "`title`,\n"
                    + "`price`,\n"
                    + "`size`,\n"
                    + "`brand`,\n"
                    + "`specification`,\n"
                    + "`tech_details`,\n"
                    + "`desc`,\n"
                    + "`link_id`)\n"
                    + "VALUES\n"
                    + "("
                    + "'" + Utility.prepareString(title) + "',"
                    + "'" + Utility.prepareString(price) + "',"
                    + "'" + Utility.prepareString(size) + "',"
                    + "'" + Utility.prepareString(brand) + "',"
                    + "'" + Utility.prepareString(specification) + "',"
                    + "'" + Utility.prepareString(techdetails) + "',"
                    + "'" + Utility.prepareString(StringEscapeUtils.unescapeJava(desc)) + "',"
                    + id
                    + ")";
            MyConnection.getConnection("ross");
            if (MyConnection.insertData(insertQ)) {
                String updateQ = "update doorsuperstore_link_master set is_scraped=1 where link_id=" + id;
                MyConnection.insertData(updateQ);
                System.out.println("INserted!");
            }

        } catch (IOException ex) {
            Logger.getLogger(HomeBaseDetailScraping.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
