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
 *
 * @author Khushbu
 */
public class DiyLinkScraping {

    public static void main(String[] args) {
        crawlInternalDoorCategoryLinks();

    }

    private static void crawlInternalDoorCategoryLinks() {
        String catURL[] = {
           // "https://www.diy.com/departments/doors-and-windows/internal-doors/oak/_/N-936Z1z11tbb",
        "https://www.diy.com/departments/doors-and-windows/internal-doors/ply/_/N-936Z1z0ljrz",
        "https://www.diy.com/departments/doors-and-windows/internal-doors/walnut/_/N-936Z1z0n6u7",
        "https://www.diy.com/departments/doors-and-windows/internal-doors/primed/_/N-936Z1z0tr3a",
        "https://www.diy.com/departments/doors-and-windows/internal-doors/pre-painted/_/N-936Z1z0tqh8",
        "https://www.diy.com/departments/doors-and-windows/internal-doors/smooth/_/N-936Z1z0tr6j",
        "https://www.diy.com/departments/doors-and-windows/internal-doors/unfinished/_/N-936Z1z0tr3h",
            
        };
        for (String s : catURL) {
            startLinkScraping(s);
        }
    }

    private static void startLinkScraping(String caturl) {
        String mainURL = caturl;
        String material = StringUtils.substringBetween(caturl, "internal-doors/", "/");
        material = material.replace("-", " ").trim();
        boolean hasNextPage = false;
        int pageno = 1;
        do {
            try {
                String url = mainURL + "?page=" + pageno;
                System.out.println("" + url);
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
