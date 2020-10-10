/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package buildingsupplies;

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
public class BuildingSuppliesLinkSraping {

    public static void main(String[] args) {
        crawlInternalDoorCategoryLinks();

    }

    private static void crawlInternalDoorCategoryLinks() {
        String catURL[] = {
        //    "https://www.building-supplies-online.co.uk/doors/internal-doors/oak-internal-doors.html",
            "https://www.building-supplies-online.co.uk/doors/internal-doors/white-internal-doors.html",
            "https://www.building-supplies-online.co.uk/doors/internal-doors/glazed-internal-doors.html",
            "https://www.building-supplies-online.co.uk/doors/internal-doors/grey-doors.html",
            "https://www.building-supplies-online.co.uk/doors/internal-doors/pine-doors.html",
            "https://www.building-supplies-online.co.uk/doors/internal-doors/walnut-doors.html",
            "https://www.building-supplies-online.co.uk/doors/internal-doors/laminate-doors.html",
            "https://www.building-supplies-online.co.uk/doors/internal-doors/hardwood-doors.html",
            "https://www.building-supplies-online.co.uk/doors/internal-doors/flush-doors.html",
            "https://www.building-supplies-online.co.uk/doors/internal-doors/internal-french-doors.html",
            "https://www.building-supplies-online.co.uk/doors/internal-doors/bi-fold-doors.html",
            "https://www.building-supplies-online.co.uk/doors/internal-doors/int-4-panel-doors.html",
            "https://www.building-supplies-online.co.uk/doors/internal-doors/int-6-panel-doors.html",
            "https://www.building-supplies-online.co.uk/doors/internal-doors/panel-doors.html",
            "https://www.building-supplies-online.co.uk/doors/internal-doors/all-internal-doors.html",
            "https://www.building-supplies-online.co.uk/doors/internal-doors/pre-finished-doors-1.html",
            "https://www.building-supplies-online.co.uk/doors/internal-doors/internal-cottage-doors.html",
            "https://www.building-supplies-online.co.uk/doors/internal-doors/black-doors.html",
          //  "",
        };
        for (String s : catURL) {
            startLinkScraping(s);
        }
    }

    private static void startLinkScraping(String caturl) {
        String mainURL = caturl;
        String material = StringUtils.substringAfterLast(caturl, "/");
        material = material.replace("-", " ").trim();
        material = material.replace(".html", " ").trim();
        boolean hasNextPage = false;
        int pageno = 1;

        int maxpage = 1;

        do {
            try {
                String url = mainURL + "?p=" + pageno;
                System.out.println("" + url);
                Document doc = Jsoup.connect(url)
                        .ignoreContentType(true)
                        .ignoreHttpErrors(true)
                        .timeout(0).get();

                // if (!doc.getElementsByClass("collection-grid").isEmpty()) {
                for (Element e : doc.getElementsByClass("product-item-details")) {
                    Element a = e.getElementsByClass("product-item-link").first();

                    String detail = a.attr("href");
                   // System.out.println("d:" + detail);
                    saveToDB(detail, material);
                }
                /*  } else {
                    System.out.println("NO products");
               /* }/*
                if (!doc.getElementsByClass("pages").isEmpty()
                        && !doc.getElementsByClass("pages").first().getElementsByClass("next").isEmpty()) {
                    hasNextPage = true;
                    pageno++;
                } else {
                    hasNextPage = false;
                }*/
                if (!doc.getElementsByClass("pages-items").isEmpty()
                        && !doc.getElementsByClass("pages").first().getElementsByClass("next").isEmpty()) {
                    hasNextPage = true;
                    pageno++;
                } else {
                    hasNextPage = false;
                }
                pageno++;
            } catch (IOException ex) {
                Logger.getLogger(HomeBaseLinkScraping.class.getName()).log(Level.SEVERE, null, ex);
            }

        } while (hasNextPage);
    }

    private static void saveToDB(String url, String material) {

        String insertQ = "INSERT INTO `ross`.`buildingsuppliesonline_link_master`\n"
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
