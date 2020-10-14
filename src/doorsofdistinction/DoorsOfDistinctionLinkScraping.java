/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package doorsofdistinction;

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
public class DoorsOfDistinctionLinkScraping {

    public static void main(String[] args) {
        crawlInternalDoorCategoryLinks();

    }

    private static void crawlInternalDoorCategoryLinks() {
        String catURL[] = {
            // "https://www.doorsofdistinction.co.uk/index.int.po.grooved.html",
            /*   "https://www.doorsofdistinction.co.uk/index.int.po.oak.html",
            "https://www.doorsofdistinction.co.uk/index.int.po.grooved.html",
            "https://www.doorsofdistinction.co.uk/index.int.cp.std.html",
            "https://www.doorsofdistinction.co.uk/index.int.oak.trad.html",
            "https://www.doorsofdistinction.co.uk/index.int.oak.grooved.html",
            "https://www.doorsofdistinction.co.uk/index.int.flush.ftre.html",
            "https://www.doorsofdistinction.co.uk/index.int.flat.po.oak.html",
            "https://www.doorsofdistinction.co.uk/index.int.pw.walnut.html",
            "https://www.doorsofdistinction.co.uk/index.interior-oak-flat-panel_doors.html",
            "https://www.doorsofdistinction.co.uk/index.int.pr.europa.html",
            "https://www.doorsofdistinction.co.uk/index.int.w.flat.html",
            "https://www.doorsofdistinction.co.uk/index.int.w.grooved.html",
            "https://www.doorsofdistinction.co.uk/index.int.w.trad.html",
            "https://www.doorsofdistinction.co.uk/index.int.w.smooth.html",*/
            "https://www.doorsofdistinction.co.uk/Index.Deanta-White-doors.html",
            "https://www.doorsofdistinction.co.uk/index.int.w.textured.html",
            "https://www.doorsofdistinction.co.uk/index.int.flush.std.html",
            "https://www.doorsofdistinction.co.uk/index.int.pairs.html",
            "https://www.doorsofdistinction.co.uk/index.int.portfolio.html",
            "https://www.doorsofdistinction.co.uk/index.int.combinations.html",
            "https://www.doorsofdistinction.co.uk/index.int.freefolds.html",
            "https://www.doorsofdistinction.co.uk/InternalPocketDoors.html",
            "https://www.doorsofdistinction.co.uk/index.interior-fire-doors.html",
            "https://www.doorsofdistinction.co.uk/index.internal-glass-fire_doors.html",
            "https://www.doorsofdistinction.co.uk/index.interior-bi-folding_doors.html",
            "https://www.doorsofdistinction.co.uk/index.interior-timber-bi-folding_doors.html",
            "https://www.doorsofdistinction.co.uk/index.int.narrow_doors.html",
            "https://www.doorsofdistinction.co.uk/index.internal-narrow-veneered_doors.html",
            "https://www.doorsofdistinction.co.uk/index.all-narrow_doors.html",
            "https://www.doorsofdistinction.co.uk/index.int.frames.html",
            "https://www.doorsofdistinction.co.uk/index.interior-door-sets1.html",
            "https://www.doorsofdistinction.co.uk/index.XL-Grooved-Oak-Doors.html",
            "https://www.doorsofdistinction.co.uk/index.XL-Pine%20Doors.html",
            "https://www.doorsofdistinction.co.uk/index.interior-door-sets2.html",
            "https://www.doorsofdistinction.co.uk/index.interior-door-sets3.html",};
        for (String s : catURL) {
            startLinkScraping(s);
        }
    }

    private static void startLinkScraping(String url) {
        //   String mainURL = caturl;
        //material = material.replace("-", " ").trim();
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

            String material = "";
            if (!doc.getElementsByClass("index-title").isEmpty()) {
                material = doc.getElementsByClass("index-title").first().text();
            } else {
                material =StringUtils.substringAfter(url, "https://www.doorsofdistinction.co.uk/index.").trim().replace(".html", "");
            }
            material = StringUtils.substringAfter(material, "-").trim();

            // if (!doc.getElementsByClass("collection-grid").isEmpty()) {
            //for (Element a : doc.select(".//a[starts-with(@href, 'interior_doors/')]")) {
            for (Element a : doc.select("a[href^=interior_doors/]")) {

                String detail = "https://www.doorsofdistinction.co.uk/" + a.attr("href");
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

        String insertQ = "INSERT INTO `ross`.`doorsofdistinction_link_master`\n"
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
