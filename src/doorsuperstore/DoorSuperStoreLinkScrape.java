/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package doorsuperstore;

import connectionManager.MyConnection;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import leaderdoors.LeaderDoorsLinkScraping;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

/**
 *
 * @author Khushbu
 */
public class DoorSuperStoreLinkScrape {

    public static void main(String[] args) {
        startcategoryLinkScraping();
    }

    private static void startcategoryLinkScraping() {
        String url[] = {
            "https://www.doorsuperstore.co.uk/browse/internal-doors/pine-internal-doors",
            "https://www.doorsuperstore.co.uk/browse/internal-doors/walnut-internal-doors",
            "https://www.doorsuperstore.co.uk/browse/internal-doors/wenge-internal-doors",
            "https://www.doorsuperstore.co.uk/browse/internal-doors/hardwood-door",
            "https://www.doorsuperstore.co.uk/browse/internal-doors/oak-internal-doors"
        };
        for(String s:url)
        {
            startScrapingLinks(s);
        }
    }

    private static void startScrapingLinks(String Mainurl) {
        int pageno = 1;
        boolean hasNextPage = false;
        String material = StringUtils.substringAfterLast(Mainurl, "/");
        material = material.replace("-", " ").trim();
        //  String Mainurl = "https://www.doorsuperstore.co.uk/browse/internal-doors/";
        do {
            String url = Mainurl + "/page" + pageno + ".html";
            try {
                System.out.println("Getting page:" + url);

                Document doc = Jsoup.connect(url)
                        .ignoreContentType(true)
                        .ignoreHttpErrors(true)
                        .timeout(0).get();
                if (!doc.getElementsByClass("listing").isEmpty()) {
                    Element div = doc.getElementsByClass("listing").first();

                    for (Element e : div.getElementsByClass("listing__body")) {
                        Element a = e.getElementsByClass("js-product-link").first();
                        String detailURL = "https://www.doorsuperstore.co.uk" + a.attr("href");
                        // System.out.println("" + detailURL);
                        String insertQ = "INSERT INTO `ross`.`doorsuperstore_link_master`\n"
                                + "(\n"
                                + "`link`,`material`)\n"
                                + "VALUES\n"
                                + " ("
                                + "'" + detailURL + "',"
                                + "'" + material + "'"
                                + ")";
                        MyConnection.getConnection("ross");
                        MyConnection.insertData(insertQ);
                    }

                } else {
                    System.out.println("NO listing..");
                    hasNextPage = false;
                }
                if (!doc.getElementsByClass("pagination__link--next").isEmpty()) {
                    hasNextPage = true;
                } else {
                    hasNextPage = false;
                }

                pageno++;
            } catch (IOException ex) {
                Logger.getLogger(LeaderDoorsLinkScraping.class.getName()).log(Level.SEVERE, null, ex);
            }
        } while (hasNextPage);
    }

}
