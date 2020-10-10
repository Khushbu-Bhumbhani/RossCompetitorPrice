/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package oakwooddoors;

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
public class OakWoodDoorsLinkScraping {

    public static void main(String[] args) {
        crawlInternalDoorCategoryLinks();

    }

    private static void crawlInternalDoorCategoryLinks() {
        String catURL[] = {
           // "https://www.oakwooddoors.co.uk/internal-oak-doors",
          /*  "https://www.oakwooddoors.co.uk/internal-doors/solid-oak-internal-doors",
            "https://www.oakwooddoors.co.uk/internal-white-primed-doors",
            "https://www.oakwooddoors.co.uk/internal-doors-walnut",
            "https://www.oakwooddoors.co.uk/internal-doors/internal-white-prefinished-doors",
            "https://www.oakwooddoors.co.uk/internal-doors/suitable-for-trimming-up-to-32mm",
            "https://www.oakwooddoors.co.uk/internal-hardwood-doors",
            "https://www.oakwooddoors.co.uk/internal-doors/internal-laminate-doors",
            "https://www.oakwooddoors.co.uk/cheap-clearance-doors",
            "https://www.oakwooddoors.co.uk/special-offers-internal-doors",
            "https://www.oakwooddoors.co.uk/internal-doors/internal-wenge-doors",
            "https://www.oakwooddoors.co.uk/internal-doors/internal-doors-colours",
            "https://www.oakwooddoors.co.uk/internal-grey-doors",
            "https://www.oakwooddoors.co.uk/internal-glazed-doors/oak-glazed",
            "https://www.oakwooddoors.co.uk/internal-glazed-doors/white-glazed-doors",
            "https://www.oakwooddoors.co.uk/internal-glazed-doors/walnut-glazed",
            "https://www.oakwooddoors.co.uk/internal-doors/internal-enduradoor-composite-doorsets",*/
            "https://www.oakwooddoors.co.uk/internal-semi-solid-core-doors/veneered-moulded-pre-finished-doors",
            "https://www.oakwooddoors.co.uk/internal-doors/suitable-for-trimming-up-to-32mm",
            "https://www.oakwooddoors.co.uk/internal-fire-doors/internal-white-primed-fire-doors",
            "https://www.oakwooddoors.co.uk/fire-doors-with-glass",
            "https://www.oakwooddoors.co.uk/internal-fire-doors/internal-walnut-fire-door",
            "https://www.oakwooddoors.co.uk/internal-fire-doors/internal-wenge-fire-doors",
            "https://www.oakwooddoors.co.uk/internal-fire-doors/internal-fire-door-blanks",
            "https://www.oakwooddoors.co.uk/internal-bifold-doors",
            "https://www.oakwooddoors.co.uk/internal-doors/lpd-spain-supermodel-doors",
            "https://www.oakwooddoors.co.uk/internal-doors/xl-simpli-doorsets-24",
            "https://www.oakwooddoors.co.uk/internal-doors-metric-size-doors",
            "https://www.oakwooddoors.co.uk/white-moulded-doors",
            "https://www.oakwooddoors.co.uk/internal-fire-doors/internal-oak-fire-doors",
            "https://www.oakwooddoors.co.uk/internal-fire-doors/fd60-60-minute-fire-rated-doors",
            "https://www.oakwooddoors.co.uk/internal-doors/internal-pine-doors/interior-doors-clear-pine",
            "https://www.oakwooddoors.co.uk/internal-doors/internal-painted-doors",
            "https://www.oakwooddoors.co.uk/internal-doors/internal-pine-doors/interior-doors-vertical-grain",
            "https://www.oakwooddoors.co.uk/internal-doors/internal-pine-doors/knotty-pine-doors",
            "https://www.oakwooddoors.co.uk/internal-doors/internal-pine-doors/nostalgia-pitch-pine-doors",
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
        do {
            try {
                String url = mainURL + "?p=" + pageno;
                System.out.println("" + url);
                Document doc = Jsoup.connect(url)
                        .ignoreContentType(true)
                        .ignoreHttpErrors(true)
                        .timeout(0).get();

                // if (!doc.getElementsByClass("collection-grid").isEmpty()) {
                for (Element e : doc.getElementsByClass("productItem ")) {
                    Element a = e.getElementsByTag("a").first();

                    String detail = a.attr("href");
                    System.out.println("d:" + detail);
                    saveToDB(detail, material);
                }
                /*  } else {
                    System.out.println("NO products");
                }*/
                if (!doc.getElementsByClass("pages").isEmpty()
                        && !doc.getElementsByClass("pages").first().getElementsByClass("next").isEmpty()) {
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

        String insertQ = "INSERT INTO `ross`.`oakwooddoors_link_master`\n"
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
