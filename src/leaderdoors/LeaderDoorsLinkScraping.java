/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package leaderdoors;

import connectionManager.MyConnection;
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
public class LeaderDoorsLinkScraping {

    public static void main(String[] args) {
        startScrapingLinks();
    }

    private static void startScrapingLinks() {
        int pageno = 1;
        int maxPage = 0;
        String url = "https://www.leaderdoors.co.uk/ajax/getProductListings?"
                + "base_url=doors-c14%2Finternal-doors-c111&page_type=productlistings&page_variant=show"
                + "&parent_category_id[]=111&all_upcoming_flag[]=78&keywords=&show=&sort="
                + "&page=" + pageno + "&min_price=35&max_price=1327"
                + "&child_categories[]=111|119|126|398|116|1065|122|997|442|455|121|446|454|456|132|1114|1063|1090|1091|1092|1062|1093|1094|1095|1096|124|1097|1115|1116&transport=html";
        // boolean hasNextPage = false;
        do {
            System.out.println("Getting page:" + pageno);
            try {
                Document doc = Jsoup.connect(url)
                        .ignoreContentType(true)
                        .ignoreHttpErrors(true)
                        .timeout(0).get();

                if (!doc.getElementsByClass("search-results-products").isEmpty()) {
                    Element div = doc.getElementsByClass("search-results-products").first();
                    String insertQ = "INSERT INTO `ross`.`leaderdoors_link_master`\n"
                            + "(\n"
                            + "`link`)\n"
                            + "VALUES\n";
                    for (Element e : div.getElementsByClass("product")) {
                        Element a = e.getElementsByClass("product__details__title").first().getElementsByTag("a").first();
                        String detailURL = "https://www.leaderdoors.co.uk" + a.attr("href");
                        // System.out.println("" + detailURL);

                        insertQ = insertQ + " ("
                                + "'" + detailURL + "'"
                                + "),";
                    }
                    insertQ = insertQ.substring(0, insertQ.length() - 1);
                    MyConnection.getConnection("ross");
                    MyConnection.insertData(insertQ);
                }
                if (maxPage == 0) {
                    String str = doc.getElementsByClass("mobile-pages").first().text();
                    str = StringUtils.substringAfter(str, "of").trim();
                    maxPage = Integer.parseInt(str);
                    System.out.println("Max page:" + maxPage);
                }
                pageno++;
                url = "https://www.leaderdoors.co.uk/ajax/getProductListings?"
                        + "base_url=doors-c14%2Finternal-doors-c111&page_type=productlistings&page_variant=show"
                        + "&parent_category_id[]=111&all_upcoming_flag[]=78&keywords=&show=&sort="
                        + "&page=" + pageno + "&min_price=35&max_price=1327"
                        + "&child_categories[]=111|119|126|398|116|1065|122|997|442|455|121|446|454|456|132|1114|1063|1090|1091|1092|1062|1093|1094|1095|1096|124|1097|1115|1116&transport=html";
            } catch (IOException ex) {
                Logger.getLogger(LeaderDoorsLinkScraping.class.getName()).log(Level.SEVERE, null, ex);
            }
        } while (pageno <= maxPage);
    }
}
