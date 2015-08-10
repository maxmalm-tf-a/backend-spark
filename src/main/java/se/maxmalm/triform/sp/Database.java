/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.maxmalm.triform.sp;

import java.sql.*;
import java.util.UUID;
import org.mindrot.jbcrypt.BCrypt;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

/**
 *
 * @author max_000
 */
public class Database {
    
    Connection connection = null;
    
    Database() {
        try {
            Class.forName("org.postgresql.Driver");
        }
        catch(ClassNotFoundException e) {
            System.out.println("Driver not found");
            e.printStackTrace();
        }
        
        try {
            connection = DriverManager.getConnection(
                    "jdbc:postgresql://localhost/test", 
                    "test",
                    "test");
        }
        catch (SQLException e) {
            System.out.println("Connection failed");
            e.printStackTrace();
        }
    }
        
    private String createToken(int userId) {
        String token = UUID.randomUUID().toString();
        try {
            PreparedStatement query = connection.prepareStatement(
                "INSERT INTO tokens VALUES (?, now() + '30 minutes', ?) LIMIT 1"
            );
            query.setString(1, token);
            query.setInt(2, userId);
            query.executeUpdate(); // http://stackoverflow.com/a/21276130
        }
        catch(SQLException e) {
            System.out.print("Failed");
            e.printStackTrace();
        }
        return token;
    }
    
    public int checkToken(String token) {
        try {
            PreparedStatement query = connection.prepareStatement(
                "SELECT * FROM tokens WHERE token = ? LIMIT 1"
            );
            query.setString(1, token);
            ResultSet rs = query.executeQuery(); // http://stackoverflow.com/a/21276130
            if (!rs.isBeforeFirst() ) {
                System.out.println("No token found");
                throw new IllegalArgumentException("No token found");
            }
            rs.next();
            java.util.Date date= new java.util.Date();
            if(new Timestamp(date.getTime()).before(rs.getTimestamp("validuntil"))) {
                System.out.println("Token valid");
                extendToken(token); // extend the life of the token for 30 minutes
                return rs.getInt("userid");
            }
            else {
                System.out.println("Token expired");
                throw new IllegalArgumentException("Token expired");
            }
        }
        catch(SQLException e) {
            System.out.print("Failed");
            e.printStackTrace();
        }
        return 0;
    }
    
    public void extendToken(String token) {
        try {
            PreparedStatement query = connection.prepareStatement(
                "UPDATE tokens SET validuntil = now() + '30 minutes' WHERE token = ?"
            );
            query.setString(1, token);
            query.executeUpdate();
        }
        catch(SQLException e) {
            System.out.print("Failed");
            e.printStackTrace();
        }
        
    }
    
    public String verifyCredentials(String username, String password) {
        String token = null;
        String dbPassword = null;
        int dbUserId = 0;
        int rowCount = 0;
        try {
            PreparedStatement query = connection.prepareStatement(
                "SELECT email, password, userid FROM users WHERE email = ? LIMIT 1"
            );
            query.setString(1, username);
            ResultSet rs = query.executeQuery();
            
            while(rs.next()) {
                rowCount++;
                dbPassword = rs.getString("password");
                dbUserId = rs.getInt("userid");
            }
            if(rowCount == 0) {
                throw new IllegalArgumentException("No user found");
            }
            if (BCrypt.checkpw(password, dbPassword)) {
                System.out.println("Correct password!");
                token = createToken(dbUserId);
            }
            else {
                throw new IllegalArgumentException("Invalid credentials");
            }
            
        }
        catch(SQLException e) {
            System.out.println("Failed");
            e.printStackTrace();
        }
        return token;
    }
    
    public String createUser(String username, String password) {
        String hashed = BCrypt.hashpw(password, BCrypt.gensalt(12));
        String token = null;
        
        try {
            PreparedStatement query = connection.prepareStatement(
                "INSERT INTO users (email, password) VALUES (?, ?) LIMIT 1 RETURNING userid, email"
            );
            query.setString(1, username);
            query.setString(2, hashed);
            ResultSet rs = query.executeQuery(); // http://stackoverflow.com/a/21276130
            rs.next();
            token = createToken(rs.getInt("userid"));
            System.out.println(token);
        }
        catch(SQLException e) {
            throw new IllegalArgumentException("Invalid credentials");
        }
        return token;
    }
    
    public boolean checkUserFree(String username) {
        boolean free = false;
        try {
            PreparedStatement query = connection.prepareStatement(
                "SELECT count(*) AS sum FROM users WHERE email = ?"
            );
            query.setString(1, username);
            ResultSet rs = query.executeQuery();
            rs.next();
            if(rs.getInt("sum") == 0) {
                free = true;
                System.out.println("free!");
            }
            
        }
        catch(SQLException e) {
            System.out.println("Failed");
            e.printStackTrace();
        }
        return free;
    }
    
    public ResultSet getTransactions(String token) {
        int userid = checkToken(token);
        ResultSet rs = null;
        if(userid > 0) {
            try {
                PreparedStatement query = connection.prepareStatement(
                    "SELECT text, value, sum, date, category FROM transactions WHERE userid = ? ORDER BY transactionid Desc"
                );
                query.setInt(1, userid);
                rs = query.executeQuery(); // http://stackoverflow.com/a/21276130
            }
            catch(SQLException e) {
                System.out.print("Failed");
                e.printStackTrace();
            }
        }
        else {
            throw new IllegalArgumentException("Token invalid");
        }
        return rs;
    }
    
    public void clearTransactions(String token) {
        int userid = checkToken(token);
        if(userid > 0) {
            try {
                PreparedStatement query = connection.prepareStatement(
                    "DELETE FROM transactions WHERE userid = ?"
                );
                query.setInt(1, userid);
                query.executeUpdate(); // http://stackoverflow.com/a/21276130
            }
            catch(SQLException e) {
                System.out.print("Failed");
                e.printStackTrace();
            }
        }
        else {
            throw new IllegalArgumentException("Token invalid");
        }
    }
    
    public void insertTransactions(String token, String text, int value, String category, int daysago) {
        int userid = checkToken(token);
        ResultSet rs;
        int sum = 0;
        
        if(userid > 0) {
            try {
                PreparedStatement query = connection.prepareStatement(
                    "SELECT sum FROM transactions WHERE userid = ? ORDER BY transactionid Desc LIMIT 1"
                );
                query.setInt(1, userid);
                rs = query.executeQuery(); // http://stackoverflow.com/a/21276130
                while(rs.next()) {
                    System.out.println("yolo");
                    System.out.println(rs.getInt("sum"));
                    sum = rs.getInt("sum");
                }
                System.out.println(sum);
                PreparedStatement insert = connection.prepareStatement(
                    "INSERT INTO transactions (userid, text, value, sum, category, date) VALUES (?, ?, ?, ?, ?, ?) LIMIT 1"
                );
                insert.setInt(1, userid);
                insert.setString(2, text);
                insert.setInt(3, value);
                insert.setInt(4, sum + value);
                insert.setString(5, category);
                insert.setString(6, getDate(daysago));
                insert.executeUpdate(); 
            }
            catch(SQLException e) {
                System.out.println("Failed insert");
                e.printStackTrace();
            }
            System.out.println("added");
            
        }
        else {
            throw new IllegalArgumentException("Token invalid");
        }
    }
    
    public String getDate(int daysago) {
        DateTime date = new DateTime();
        DateTimeFormatter fmt = DateTimeFormat.forPattern("yyyy-MM-dd");
        date = date.minusDays(daysago);
        return fmt.print(date);
    }
}
