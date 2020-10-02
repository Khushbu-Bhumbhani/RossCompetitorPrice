/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package onlinedoorstore;

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
public class OnlineDoorStoreLinkScraping {

    public static void main(String[] args) {
        crawlInternalDoorCategoryLinks();

    }

    private static void crawlInternalDoorCategoryLinks() {
        String catURL[] = {
            "https://onlinedoorstore.co.uk/product-category/internal-doors/contemporary-internal-doors/",
            "https://onlinedoorstore.co.uk/product-category/internal-doors/primed-doors/",
            "https://onlinedoorstore.co.uk/product-category/internal-doors/oak-doors/",
            "https://onlinedoorstore.co.uk/product-category/internal-doors/1930s-doors/",
            "https://onlinedoorstore.co.uk/product-category/internal-doors/internal-glazed-doors/",
            "https://onlinedoorstore.co.uk/product-category/internal-doors/victorian-doors/",
            "https://onlinedoorstore.co.uk/product-category/internal-doors/moulded-doors/",
            "https://onlinedoorstore.co.uk/product-category/internal-doors/1960s-doors/",
            "https://onlinedoorstore.co.uk/product-category/internal-doors/laminated-doors/",
            "https://onlinedoorstore.co.uk/product-category/internal-doors/victorian-doors/",
            "https://onlinedoorstore.co.uk/product-category/internal-doors/moulded-doors/",
            "https://onlinedoorstore.co.uk/product-category/internal-doors/1960s-doors/",
            "https://onlinedoorstore.co.uk/product-category/internal-doors/bi-fold-doors/",
            "https://onlinedoorstore.co.uk/product-category/internal-doors/pine-doors/",
            "https://onlinedoorstore.co.uk/product-category/internal-doors/pitch-pine-doors/",
            "https://onlinedoorstore.co.uk/product-category/internal-doors/walnut-doors/",
            "https://onlinedoorstore.co.uk/product-category/internal-doors/composite-doors/",
            "https://onlinedoorstore.co.uk/product-category/internal-doors/vestibule-doors/",
            "https://onlinedoorstore.co.uk/product-category/internal-doors/hardwood-doors/",
            "https://onlinedoorstore.co.uk/product-category/internal-doors/georgian-doors/",
            "https://onlinedoorstore.co.uk/product-category/internal-doors/1920s-doors/",
            "https://onlinedoorstore.co.uk/product-category/internal-doors/unglazed-contemporary-doors/",
            "https://onlinedoorstore.co.uk/product-category/internal-doors/flush-doors/",
            "https://onlinedoorstore.co.uk/product-category/internal-doors/panel-doors/",
            "https://onlinedoorstore.co.uk/product-category/internal-doors/white-doors/",
            "https://onlinedoorstore.co.uk/product-category/internal-doors/period-doors/",
            "https://onlinedoorstore.co.uk/product-category/internal-doors/contemporary-doors/",
            "https://onlinedoorstore.co.uk/product-category/internal-doors/lpd-doors/",
            "https://onlinedoorstore.co.uk/product-category/internal-doors/xl-joinery-doors/",
            "https://onlinedoorstore.co.uk/product-category/internal-doors/deanta-doors/",
               
        };
        for (String s : catURL) {
            startLinkScraping(s);
        }
    }

    private static void startLinkScraping(String caturl) {
        String mainURL = caturl;
        String material = StringUtils.substringAfter(caturl, "/internal-doors/");
        material = material.replace("-", " ").trim();
        material = material.replace("/", "").trim();

        boolean hasNextPage = false;
        int pageno = 1;
        do {
            try {
                String url = mainURL + "page/" + pageno + "/";
                System.out.println("" + url);
                Document doc = Jsoup.connect(url)
                        .ignoreContentType(true)
                        .ignoreHttpErrors(true)
                        .timeout(0).get();

                // if (!doc.getElementsByClass("collection-grid").isEmpty()) {
                for (Element e : doc.getElementsByClass("item-col")) {
                    String detail = e.getElementsByTag("span").first().attr("data-gtm4wp_product_url");
                 //   System.out.println("d:" + detail);
                     saveToDB(detail, material);
                }
                /* } else {
                    System.out.println("NO products");
                }*/
                if (!doc.getElementsByClass("woocommerce-pagination").isEmpty()
                        && !doc.getElementsByClass("next").isEmpty()) {
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

        String insertQ = "INSERT INTO `ross`.`onlinedoorstore_link_master`\n"
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
