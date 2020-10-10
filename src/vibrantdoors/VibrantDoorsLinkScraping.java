/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package vibrantdoors;

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
public class VibrantDoorsLinkScraping {

    public static void main(String[] args) {
        crawlInternalDoorCategoryLinks();

    }

    private static void crawlInternalDoorCategoryLinks() {
        String catURL[] = {
           // "https://www.vibrantdoors.co.uk/choose-your-door/internal/fire-doors",
            "https://www.vibrantdoors.co.uk/choose-your-door/internal/glazed-doors",
            "https://www.vibrantdoors.co.uk/choose-your-door/internal/grey-doors",
            "https://www.vibrantdoors.co.uk/choose-your-door/internal/hardwood-doors",
            "https://www.vibrantdoors.co.uk/choose-your-door/internal/laminated",
            "https://www.vibrantdoors.co.uk/choose-your-door/internal/oak-doors",
            "https://www.vibrantdoors.co.uk/choose-your-door/internal/panel-doors",
            "https://www.vibrantdoors.co.uk/choose-your-door/internal/flush-doors",
            "https://www.vibrantdoors.co.uk/choose-your-door/internal/pine-doors",
            "https://www.vibrantdoors.co.uk/choose-your-door/internal/planked-doors",
            "https://www.vibrantdoors.co.uk/choose-your-door/internal/pre-finished-oak-doors",
            "https://www.vibrantdoors.co.uk/choose-your-door/internal/rustic-oak-doors",
            "https://www.vibrantdoors.co.uk/choose-your-door/internal/solid-white-primed-doors",
            "https://www.vibrantdoors.co.uk/choose-your-door/internal/walnut-doors",
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
        int pageno = 1;

        int maxpage = 1;

        do {
            try {
                String url = mainURL + "?page=" + pageno;
                System.out.println("" + url);
                Document doc = Jsoup.connect(url)
                        .ignoreContentType(true)
                        .ignoreHttpErrors(true)
                        .timeout(0).get();

                // if (!doc.getElementsByClass("collection-grid").isEmpty()) {
                for (Element e : doc.getElementsByClass("c_33 m_50")) {
                    Element a = e.getElementsByAttributeValue("itemprop", "url").first();

                    String detail = a.attr("content");
                    System.out.println("d:" + detail);
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
                if (maxpage == 1) {
                    if (!doc.getElementsContainingOwnText("Show All Doors").isEmpty()) {
                        Element e = doc.getElementsContainingOwnText("Show All Doors").first().previousElementSibling();
                        maxpage = Integer.parseInt(e.text());
                        System.out.println("Max page:" + e.text());
                    }
                }
            } catch (IOException ex) {
                Logger.getLogger(HomeBaseLinkScraping.class.getName()).log(Level.SEVERE, null, ex);
            }

            pageno++;

        } while (pageno <= maxpage);
    }

    private static void saveToDB(String url, String material) {

        String insertQ = "INSERT INTO `ross`.`vibrantdoors_link_master`\n"
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
