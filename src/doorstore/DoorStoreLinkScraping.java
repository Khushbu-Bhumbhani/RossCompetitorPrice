/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package doorstore;

import connectionManager.MyConnection;
import connectionManager.Utility;
import homebase.HomeBaseLinkScraping;
import java.io.IOException;
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
public class DoorStoreLinkScraping {

    public static void main(String[] args) {
        crawlInternalDoorCategoryLinks();

    }

    private static void crawlInternalDoorCategoryLinks() {
        String catURL[] = {
           // "https://www.doorstore.co.uk/products/internal-doors/oak-doors/641/",
            //"https://www.doorstore.co.uk/products/internal-doors/white-internal-doors/665/",
            "https://www.doorstore.co.uk/products/internal-doors/laminate-doors/706/",
            "https://www.doorstore.co.uk/products/internal-doors/walnut-doors/642/",
            "https://www.doorstore.co.uk/products/internal-doors/glazed-doors/673/",
            "https://www.doorstore.co.uk/products/internal-doors/contemporary-doors/660/",
            "https://www.doorstore.co.uk/products/internal-doors/rustic-oak-doors/661/",
            "https://www.doorstore.co.uk/products/internal-doors/paint-grade-doors/648/",
            "https://www.doorstore.co.uk/products/internal-doors/firecheck-doors/654/"
        };
        for (String s : catURL) {
            startLinkScraping(s);
        }
    }

    private static void startLinkScraping(String url) {
        //   String mainURL = caturl;
        String material = StringUtils.substringBetween(url, "internal-doors/", "/");
        material = material.replace("-", " ").trim();
        //  boolean hasNextPage = false;
        //  int pageno = 1;
        //   do {
        try {
            //    String url = mainURL + "?page=" + pageno;
            System.out.println("" + url);
            Document doc = Jsoup.connect(url)
                    .ignoreContentType(true)
                    .ignoreHttpErrors(true)
                    .timeout(0).get();

            // if (!doc.getElementsByClass("collection-grid").isEmpty()) {
            for (Element a : doc.getElementsByClass("products2").first().getElementsByTag("a")) {
                
                String detail = "https://www.doorstore.co.uk" + a.attr("href");
                  System.out.println("d:" + detail);
                saveToDB(detail, material);
            }
            /*  } else {
                    System.out.println("NO products");
                }*/
 /* if (!doc.getElementsContainingOwnText("Load More").isEmpty()) {
                    hasNextPage = true;
                    pageno++;
                } else {
                    hasNextPage = false;
                }*/
        } catch (IOException ex) {
            Logger.getLogger(HomeBaseLinkScraping.class.getName()).log(Level.SEVERE, null, ex);
        }
        // } while (hasNextPage);
    }

    private static void saveToDB(String url, String material) {

        String insertQ = "INSERT INTO `ross`.`doorstore_link_master`\n"
                + "(\n"
                + "`link`,\n"
                + "`material`\n"
                + ")\n"
                + "VALUES\n"
                + "("
                + "'" + Utility.prepareString(url) + "',"
                + "'" + Utility.prepareString(material) + "'"
                + ")";

        MyConnection.getConnection("ross");
        MyConnection.insertData(insertQ);

    }

}
