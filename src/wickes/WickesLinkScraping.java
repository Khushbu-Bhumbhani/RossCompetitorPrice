/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wickes;

import directdoors.*;
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
public class WickesLinkScraping {

    public static void main(String[] args) {
        crawlInternalDoorCategoryLinks();

    }

    private static void crawlInternalDoorCategoryLinks() {
        String catURL[] = {
            //"https://www.wickes.co.uk/Products/Doors+Windows/Internal-Doors/Glazed-Doors/c/1000660",
            //  "https://www.wickes.co.uk/Products/Doors+Windows/Internal-Doors/Internal-Oak-Veneer-Doors/c/1000656",
            "https://www.wickes.co.uk/Products/Doors+Windows/Internal-Doors/Internal-White-Doors/c/1000657",
            "https://www.wickes.co.uk/Products/Doors+Windows/Internal-Doors/Internal-Pine-Doors/c/1000655",
            "https://www.wickes.co.uk/Products/Doors+Windows/Internal-Doors/Fire-Doors/c/1000659",
            "https://www.wickes.co.uk/Products/Doors+Windows/Internal-Doors/Internal-Flush-Doors/c/1061003",
            "https://www.wickes.co.uk/Products/Doors+Windows/Internal-Doors/Bi-Fold-Doors/c/1000658",
            "https://www.wickes.co.uk/Products/Doors+Windows/Internal-Doors/Internal-French-Doors/c/1000662",
            "https://www.wickes.co.uk/Products/Doors+Windows/Internal-Doors/Louvre-Doors/c/1000666",
          
        };
        for (String s : catURL) {
            startLinkScraping(s);
        }
    }

    private static void startLinkScraping(String caturl) {
        String mainURL = caturl;
        String material = StringUtils.substringBetween(caturl, "/Internal-Doors/", "/");
        material = material.replace("-", " ").trim();
        boolean hasNextPage = false;
        int pageno = 0;
        do {
            try {
                String url = mainURL + "?q=%3Arelevance&page=" + pageno;
                System.out.println("" + url);
                Document doc = Jsoup.connect(url)
                        .ignoreContentType(true)
                        .ignoreHttpErrors(true)
                        .timeout(0).get();

                // if (!doc.getElementsByClass("collection-grid").isEmpty()) {
                for (Element a : doc.getElementsByClass("product-card__title")) {
                    String detail = "https://www.wickes.co.uk" + a.attr("href");
                    System.out.println("d:" + detail);
                    saveToDB(detail, material);
                }
                /*  } else {
                    System.out.println("NO products");
                }*/
                if (!doc.getElementsByClass("pagination__btn_next").isEmpty()) {
                    hasNextPage = true;
                    pageno++;
                } else {
                    hasNextPage = false;
                }
            } catch (IOException ex) {
                Logger.getLogger(HomeBaseLinkScraping.class.getName()).log(Level.SEVERE, null, ex);
            }
        } while (hasNextPage);
    }

    private static void saveToDB(String url, String material) {

        String insertQ = "INSERT INTO `ross`.`wickes_link_master`\n"
                + "(\n"
                + "`link`,\n"
                + "`category`\n"
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
