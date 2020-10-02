/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package homebase;

import connectionManager.MyConnection;
import connectionManager.Utility;
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
public class HomeBaseLinkScraping {

    public static void main(String[] args) {
        getCategoryLinks();
    }

    private static void getCategoryLinks() {
        String url = "https://www.homebase.co.uk/our-range/building-and-hardware/doors/internal-doors";
        try {
            Document doc = Jsoup.connect(url)
                    .ignoreContentType(true)
                    .ignoreHttpErrors(true)
                    .timeout(0).get();

            if (!doc.getElementsByClass("article__content").isEmpty()) {
                for (Element a : doc.getElementsByClass("article__content").first().getElementsByTag("a")) {
                    String categoryUrl = "https://www.homebase.co.uk" + a.attr("href");
                    System.out.println("Category:" + categoryUrl);
                    scrapeLinks(categoryUrl);
                    System.out.println("Sleeping..");
                    Thread.sleep(3000);
                }
            } else {
                System.out.println("NO Category..");
            }
        } catch (IOException ex) {
            Logger.getLogger(HomeBaseLinkScraping.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InterruptedException ex) {
            Logger.getLogger(HomeBaseLinkScraping.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private static void scrapeLinks(String categoryUrl) {
        int pageno = 1;
        int totalPage = 1;
        String mainURL = categoryUrl;
        String category = StringUtils.substringAfterLast(categoryUrl, "/");
        categoryUrl = categoryUrl + "?page=" + pageno;
        try {

            boolean hasNextPage = true;

            do {
                System.out.println("Getting..." + categoryUrl);
                Document doc = Jsoup.connect(categoryUrl)
                        .ignoreContentType(true)
                        .ignoreHttpErrors(true)
                        .timeout(0).get();

                if (!doc.getElementsByClass("product-list-group").isEmpty()
                        && doc.getElementsByClass("product-list-group").first().getElementsByTag("a").size() != 0) {
                    Element div = doc.getElementsByClass("product-list-group").first();
                    for (Element a : div.getElementsByTag("a")) {
                        if (!a.attr("href").equals("/") && a.hasAttr("data-product-id")) {

                            String detailURL = "https://www.homebase.co.uk" + a.attr("href");
                            System.out.println("D:" + detailURL);

                            String insertQ = "INSERT INTO `ross`.`homebase_link_master`\n"
                                    + "(\n"
                                    + "`link`,\n"
                                    + "`category`\n"
                                    + ")\n"
                                    + "VALUES\n"
                                    + "("
                                    + "'" + Utility.prepareString(detailURL) + "','" + Utility.prepareString(category) + "'"
                                    + ")";

                            MyConnection.getConnection("ross");
                            MyConnection.insertData(insertQ);

                        }
                    }
                } else {
                    hasNextPage = false;
                }
                if (totalPage == 1) {
                    if (!doc.getElementsByClass("count-block").isEmpty()) {
                        totalPage = Integer.parseInt(doc.getElementsByClass("count-block").first().text());
                        totalPage = Math.round(totalPage / 48) + 1;
                        System.out.println("Total Pages:" + totalPage);
                    }
                }
                pageno++;
                categoryUrl = mainURL + "?page=" + pageno;
                System.out.println("Sleeping..");
                Thread.sleep(3000);
            } while (pageno <= totalPage);

        } catch (IOException ex) {
            Logger.getLogger(HomeBaseLinkScraping.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InterruptedException ex) {
            Logger.getLogger(HomeBaseLinkScraping.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
}
