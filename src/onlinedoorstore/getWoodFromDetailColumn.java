/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package onlinedoorstore;

import connectionManager.MyConnection;
import connectionManager.Utility;
import homebase.HomeBaseDetailScraping;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author Khushbu
 */
public class getWoodFromDetailColumn {

    public static void main(String[] args) {
        getWood();
    }

    private static void getWood() {
        String selectQ = "select detail_id,details from ross.onlinedoorstore_detail_master where wood is null";
        MyConnection.getConnection("ross");
        ResultSet rs = MyConnection.getResultSet(selectQ);
        try {
            while (rs.next()) {

                int id = rs.getInt("detail_id");
                String wood = StringUtils.substringBetween(rs.getString("details"), " Wood:", "|");
                String updateQ = "update ross.onlinedoorstore_detail_master set wood='" + Utility.prepareString(wood) + "' where detail_id=" + id;
                MyConnection.insertData(updateQ);
            }
        } catch (SQLException ex) {
            Logger.getLogger(HomeBaseDetailScraping.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
