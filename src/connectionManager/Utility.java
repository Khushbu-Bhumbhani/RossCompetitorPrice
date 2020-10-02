/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package connectionManager;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

/**
 *
 * @author Khushbu
 */
public class Utility {

    public static String html2text(String html) {
        if (html == null) {
            return "";
        }
        if (!html.equals("")) {
            return Jsoup.parse(html).text().trim();
        } else {
            return html;

        }
    }

    public static String prepareString(String str) {
        if (str != null) {
            str = str.replace("'", "''");
            str = str.replace("\"", "\"");
            str = str.replace("\\", "\\\\");
        } else {
            return "";
        }
        return str;
    }

}
