/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diy;

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
 * this code is to get links that are generated by searching on website like
 * 'pine door' For category link : add the search link For categoryName: add
 * product urls to diylink master
 *
 * @author Khushbu
 */
public class DiyLinkScrapingBySearchWord {

    public static void main(String[] args) {
        crawlInternalDoorCategoryLinks();

    }

    private static void crawlInternalDoorCategoryLinks() {
        String catURL[] = {
            "https://www.diy.com/search?page=1&term=Pine+Doors",};
        for (String s : catURL) {
            startLinkScraping(s);
        }
    }

    private static void startLinkScraping(String caturl) {
        String mainURL = caturl;
        String material = StringUtils.substringAfter(caturl, "&term=");
        material = material.replace("+", " ").trim();
        boolean hasNextPage = false;
        String url=mainURL;
        int pageno = 1;
        do {
            try {
                
                System.out.println("-->" + url);
                Document doc = Jsoup.connect(url)
                        .ignoreContentType(true)
                        .ignoreHttpErrors(true)
                        .timeout(0).get();

                // if (!doc.getElementsByClass("collection-grid").isEmpty()) {
                for (Element a : doc.getElementsByAttributeValue("data-test-id", "product-panel-main-section")) {
                    String detail = "https://www.diy.com/departments" + a.attr("href");
                    //  System.out.println("d:" + detail);
                    saveToDB(detail, material);
                }
                /*  } else {
                    System.out.println("NO products");
                }*/
                if (!doc.getElementsContainingOwnText("Load More").isEmpty()) {
                    hasNextPage = true;
                    pageno++;
                    url = url.replace("page="+(pageno-1), "page="+pageno);
                } else {
                    hasNextPage = false;
                }
            } catch (IOException ex) {
                Logger.getLogger(HomeBaseLinkScraping.class.getName()).log(Level.SEVERE, null, ex);
            }
        } while (hasNextPage);
    }

    private static void saveToDB(String url, String material) {

        String insertQ = "INSERT INTO `ross`.`diy_link_master`\n"
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
