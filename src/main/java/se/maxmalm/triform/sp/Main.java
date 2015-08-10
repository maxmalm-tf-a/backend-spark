/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.maxmalm.triform.sp;

import java.sql.ResultSet;
import spark.Filter;
import spark.Request;
import spark.Response;
import static spark.Spark.*;

public class Main {
    public static void main(String[] args) {
        Database db = new Database ();
        enableCORS("*", "*", "*");
        
        get("/", (request, response) -> {
            return JSONUtil.String2JSON("works");
        });
        
        // account
        get("/account", (req, res) -> {
            String username = req.queryParams("username");
            String password = req.queryParams("password");
            String token = null;
            
            try {
                token = db.verifyCredentials(username, password);
            }
            catch(IllegalArgumentException e) {
                halt(401, JSONUtil.String2Error(e.getMessage()));
            }
            return JSONUtil.String2JSON(token);
        });
        post("/account", (req, res) -> {
            String username = req.queryParams("username");
            String password = req.queryParams("password");
            String token = null;

            if(db.checkUserFree(username)) {
                try {
                    token = db.createUser(username, password);
                }
                catch(IllegalArgumentException e) {
                    halt(401, JSONUtil.String2Error(e.getMessage()));
                }
                
            }
            else {
                halt(401, JSONUtil.String2Error("Username taken"));
            }
            return JSONUtil.String2JSON(token);
        });
        
        // token verification
        get("/verifytoken", (req, res) -> {
            
            String token = req.queryParams("token");
            int userid = 0;
            try {
                userid = db.checkToken(token);
                System.out.println(userid);
            }
            catch(IllegalArgumentException e) {
                halt(401, JSONUtil.String2Error(e.getMessage()));
            }
            return JSONUtil.Int2JSON(userid);
        });
        
        // transactions
        get("/transactions", (req, res) -> {
            String token = req.queryParams("token");
            ResultSet rs = null;
            try {
                rs = db.getTransactions(token);
            }
            catch(IllegalArgumentException e) {
                halt(401, JSONUtil.String2Error(e.getMessage()));
            }
            String result = JSONUtil.rs(rs);
            return result;
        });
        
        // seed database
        post("/seed", (req, res) -> {
            String token = req.queryParams("token");
            int userId;
            try {
                userId = db.checkToken(token);
            }
            catch(IllegalArgumentException e) {
                halt(401, JSONUtil.String2Error(e.getMessage()));
            }
            db.clearTransactions(token);
            db.insertTransactions(token, "L�n", 40000, "Inkomst", 30);
            db.insertTransactions(token, "Kaffe", -40, "N�je", 29);
            db.insertTransactions(token, "Hyra", -5000, "Bostad", 28);
            db.insertTransactions(token, "Avanza", -2000, "Sparande", 27);
            db.insertTransactions(token, "Lego", -400, "N�je", 26);
            db.insertTransactions(token, "Skatte�terb�ring", 500, "N�je", 25);
            db.insertTransactions(token, "Kaffe", -40, "N�je", 24);
            db.insertTransactions(token, "Kaffe", -40, "N�je", 23);
            db.insertTransactions(token, "Kaffe", -40, "N�je", 22);
            db.insertTransactions(token, "Kaffe", -40, "N�je", 21);
            db.insertTransactions(token, "Kaffe", -40, "N�je", 20);
            db.insertTransactions(token, "Kaffe", -40, "N�je", 19);
            db.insertTransactions(token, "Kaffe", -40, "N�je", 18);
            db.insertTransactions(token, "Kaffe", -40, "N�je", 17);
            db.insertTransactions(token, "Systembolaget", -493, "N�je", 16);
            db.insertTransactions(token, "Kaffe", -40, "N�je", 15);
            db.insertTransactions(token, "Kaffe", -40, "N�je", 14);
            db.insertTransactions(token, "Kaffe", -40, "N�je", 13);
            db.insertTransactions(token, "Kaffe", -40, "N�je", 12);
            db.insertTransactions(token, "Kaffe", -40, "N�je", 11);
            db.insertTransactions(token, "Kaffe", -40, "N�je", 9);
            db.insertTransactions(token, "Kaffe", -40, "N�je", 8);
            db.insertTransactions(token, "Kaffe", -40, "N�je", 7);
            db.insertTransactions(token, "Trissvinst", 25, "N�je", 6);
            db.insertTransactions(token, "Kaffe", -40, "N�je", 5);
            db.insertTransactions(token, "Kaffe", -40, "N�je", 4);
            db.insertTransactions(token, "Kaffe", -40, "N�je", 3);
            db.insertTransactions(token, "Kaffe", -40, "N�je", 2);
            db.insertTransactions(token, "Kaffe", -40, "N�je", 1);

            return JSONUtil.String2JSON("nice");
        });
        
    }
    
    
    private static void enableCORS(final String origin, final String methods, final String headers) {
        before(new Filter() {
            @Override
            public void handle(Request request, Response response) {
                response.header("Access-Control-Allow-Origin", origin);
                response.header("Access-Control-Request-Method", methods);
                response.header("Access-Control-Allow-Headers", headers);
            }
        });
    }
}