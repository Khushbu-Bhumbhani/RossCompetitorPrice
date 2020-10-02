/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package directdoors;

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
public class DirectDoorsLinkScraping {

    public static void main(String[] args) {
        crawlInternalDoorCategoryLinks();

    }

    private static void crawlInternalDoorCategoryLinks() {
        String catURL[] = {
            "https://www.directdoors.com/collections/white-internal-doors",
            "https://www.directdoors.com/collections/internal-white-primed-doors",
            "https://www.directdoors.com/collections/grey-internal-doors",
            "https://www.directdoors.com/collections/black-internal-doors",
            "https://www.directdoors.com/collections/coloured-internal-doors",
            "https://www.directdoors.com/collections/pre-finished-internal-doors",
            "https://www.directdoors.com/collections/oak-internal-doors",
            "https://www.directdoors.com/collections/walnut-internal-doors",
            "https://www.directdoors.com/collections/mahogany-internal-doors",
            "https://www.directdoors.com/collections/pine-internal-doors"

        };
        for (String s : catURL) {
            startLinkScraping(s);
        }
    }

    private static void startLinkScraping(String caturl) {
        String mainURL = caturl;
        String material = StringUtils.substringAfterLast(caturl, "/");
        material = material.replace("-", " ").trim();
        boolean hasNextPage = false;
        int pageno = 0;
        do {
            try {
                String url = mainURL + "?page=" + pageno;
                Document doc = Jsoup.connect(url)
                        .ignoreContentType(true)
                        .ignoreHttpErrors(true)
                        .timeout(0).get();

                if (!doc.getElementsByClass("collection-grid").isEmpty()) {
                    for (Element a : doc.getElementsByClass("collection-grid").first().getElementsByClass("product-card")) {
                        String detail = "https://www.directdoors.com" + a.attr("href");
                        System.out.println("d:" + detail);
                        saveToDB(detail, material);
                    }
                } else {
                    System.out.println("NO products");
                }
                if (!doc.getElementsContainingOwnText("Next").isEmpty()) {
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

        String insertQ = "INSERT INTO `ross`.`directdoors_link_master`\n"
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
