/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package leaderdoors;

import connectionManager.MyConnection;
import connectionManager.Utility;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
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
public class LeaderDoorsDetailScrape {

    public static void main(String[] args) {
        startDetailScrape();
        /* String str[] = {
            "https://www.leaderdoors.co.uk/doors-c14/internal-doors-c111/oak-internal-doors-c119/internal-rustic-oak-fully-finished-grange-ledged-solid-plank-door-holb-p54148",
            "https://www.leaderdoors.co.uk/doors-c14/internal-doors-c111/coloured-internal-doors-c398/white-internal-doors-c124/internal-white-primed-trent-3p-door-iwptre-p51782",
            "https://www.leaderdoors.co.uk/doors-c14/internal-doors-c111/oak-internal-doors-c119/internal-rustic-oak-fully-finished-grange-ledged-solid-plank-door-holb-p54148",
            "https://www.leaderdoors.co.uk/doors-c14/internal-doors-c111/coloured-internal-doors-c398/white-internal-doors-c124/internal-white-primed-toledo-3p-door-iwpwor-p51788",
            "https://www.leaderdoors.co.uk/doors-c14/internal-doors-c111/oak-internal-doors-c119/internal-oak-unfinished-pattern-10-1p-door-iopat10-p51732",
            "https://www.leaderdoors.co.uk/doors-c14/internal-doors-c111/coloured-internal-doors-c398/white-internal-doors-c124/internal-white-primed-victorian-shaker-2p-2l-clear-glass-flat-door-iwpmalcg-p51765",
            "https://www.leaderdoors.co.uk/doors-c14/internal-doors-c111/coloured-internal-doors-c398/white-internal-doors-c124/internal-white-primed-pedrena-1l-clear-glass-door-iwppedcg-p57566",
            "https://www.leaderdoors.co.uk/doors-c14/internal-doors-c111/oak-internal-doors-c119/internal-oak-unfinished-malaga-3l-obscure-glass-door-iomalobg-p57557",
            "https://www.leaderdoors.co.uk/doors-c14/internal-doors-c111/coloured-internal-doors-c398/white-internal-doors-c124/internal-white-primed-toledo-3l-clear-glass-door-iwpworcg-p51789",
        };
        for (String s : str) {
            scrapeDetails(s);
        }*/
    }

    private static void startDetailScrape() {

        String selectQ = "SELECT link_id,link FROM ross.leaderdoors_link_master where is_scraped=0";

        MyConnection.getConnection("ross");
        ResultSet rs = MyConnection.getResultSet(selectQ);

        try {
            while (rs.next()) {
                String link = rs.getString("link");
                int id = rs.getInt("link_id");
                scrapeDetails(link, id);

            }
        } catch (SQLException ex) {
            Logger.getLogger(LeaderDoorsDetailScrape.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private static void scrapeDetails(String url, int id) {
        try {
            Document doc = Jsoup.connect(url)
                    .ignoreContentType(true)
                    .ignoreHttpErrors(true)
                    .timeout(0).get();
            String title = "";
            String price = "";
            String desc = "";
            //String size = "";
            String material = "";
            // String delivery = "";

            title = doc.getElementById("js-product-title").text();
            price = doc.getElementById("js-product-price").getElementsByClass("GBP").first().text();
            desc = StringUtils.substringBetween(doc.html(), "Door Information", "</table>");
            if (desc != null) {
                desc = desc.replace("</tr>", " | ");
                desc = desc.replace("<td style=\"width: 65%; padding: 7px; border: 1px solid #ffffff;\">", " :  ");
                material = StringUtils.substringBetween(doc.html(), "<strong>Material", "</tr");
            } else {
                desc = StringUtils.substringBetween(doc.html(), "General Information", "</table>");
                if (desc != null) {
                    desc = desc.replace("</li>", " | ");
                    material = StringUtils.substringBetween(doc.html(), "<strong>Material:", "</li");
                }
            }
            desc = Utility.html2text(desc);
            if (!desc.equals("")) {
                desc = desc.substring(0, desc.length() - 1).trim();
            }
            //
            material = Utility.html2text(material);

            Element table = doc.getElementById("js-product-price-breaks-table");
            boolean isFirst = true;
            if (table != null) {
                for (Element tr : table.getElementsByTag("tr")) {
                    if (isFirst) {
                        isFirst = false;
                        continue;
                    }
                    if (tr.getElementsByTag("td").size() >= 6) {
                        String size1 = tr.getElementsByTag("td").get(3).text();
                        String price1 = tr.getElementsByTag("td").get(6).getElementsByClass("GBP").first().text();
                        // System.out.println("" + title + ";" + price + ";" + size1 + ";" + price1 + ";" + desc + ";" + url);
                        String insertQ = "INSERT INTO `ross`.`leaderdoors_detail_master`\n"
                                + "(\n"
                                + "`title`,\n"
                                + "`price`,\n"
                                + "`size`,\n"
                                + "`price_per_given_size`,\n"
                                + "`material`,\n"
                                + "`desc`,\n"
                                + "`link_id`)\n"
                                + "VALUES\n"
                                + "("
                                + "'" + Utility.prepareString(title) + "',"
                                + "'" + Utility.prepareString(price) + "',"
                                + "'" + Utility.prepareString(size1) + "',"
                                + "'" + Utility.prepareString(price1) + "',"
                                + "'" + Utility.prepareString(material) + "',"
                                + "'" + Utility.prepareString(desc) + "',"
                                + id + ""
                                + ")";
                        MyConnection.getConnection("ross");
                        if (MyConnection.insertData(insertQ)) {
                            String update = "update leaderdoors_link_master set is_scraped=1 where link_id=" + id;
                            MyConnection.insertData(update);
                        }
                        System.out.println("Inserted " + id);
                    }
                }
            } else {
                String insertQ = "INSERT INTO `ross`.`leaderdoors_detail_master`\n"
                        + "(\n"
                        + "`title`,\n"
                        + "`price`,\n"
                        + "`material`,\n"
                        + "`desc`,\n"
                        + "`link_id`)\n"
                        + "VALUES\n"
                        + "("
                        + "'" + Utility.prepareString(title) + "',"
                        + "'" + Utility.prepareString(price) + "',"
                        + "'" + Utility.prepareString(material) + "',"
                        + "'" + Utility.prepareString(desc) + "',"
                        + id + ""
                        + ")";
                MyConnection.getConnection("ross");
                if (MyConnection.insertData(insertQ)) {
                    String update = "update leaderdoors_link_master set is_scraped=1 where link_id=" + id;
                    MyConnection.insertData(update);
                }
            }
            // System.out.println("" + title + ";" + price + ";" + desc);
        } catch (IOException ex) {
            Logger.getLogger(LeaderDoorsDetailScrape.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

}
