/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.maxmalm.triform.sp;

/**
 *
 * @author max_000
 */
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class JSONUtil {
    
    public static final JSONObject ResultSet2JSONObject(ResultSet rs) {
        JSONObject element = null;
        JSONArray joa = new JSONArray();
        JSONObject jo = new JSONObject();
        int totalLength = 0;
        ResultSetMetaData rsmd = null;
        String columnName = null;
        String columnValue = null;
        try {
            rsmd = rs.getMetaData();
            while (rs.next()) {
                element = new JSONObject();
                for (int i = 0; i < rsmd.getColumnCount(); i++) {
                    columnName = rsmd.getColumnName(i+1);
                    columnValue = rs.getString(columnName);
                    element.accumulate(columnName, columnValue);
                }
                joa.add(element);
                totalLength ++;
            }
            jo.accumulate("result", "success");
            jo.accumulate("rows", totalLength);
            jo.accumulate("data", joa);
        } catch (SQLException e) {
            jo.accumulate("result", "failure");
            jo.accumulate("error", e.getMessage());
        }
        return jo;
    }

    public static final String rs(ResultSet rs) {
        return ResultSet2JSONObject(rs).toString();
    }

    public static final String String2JSON(String str) {
        return "{\"result\":\"success\",\"data\":\""+str+"\"}";
    }

    public static final String String2Error(String str) {
        return "{\"result\":\"error\",\"reason\":\""+str+"\"}";
    }

    public static final String Int2JSON(int id) {
        return "{\"result\":\"success\",\"data\":"+id+"}";
    }
}