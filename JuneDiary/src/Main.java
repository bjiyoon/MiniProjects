import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.*;
import java.util.ArrayList;

public class Main {
    public static final String SQL_DRIVER = "com.mysql.cj.jdbc.Driver";
    public static String sqlServer = "localhost";
    public static String sqlDbase = "";
    public static final String sqlTable = "junediary";
    public static String sqlUser = "";
    public static String sqlPass = "";
    
    public static String SQL_URL = "";
    public static boolean tableReady = false;
    public static long packetSize = 0;
//    public static MainFrame frame;
    public static ArrayList<Integer> idList = new ArrayList<>();
    
    public static void setupUser() {
        sqlUser = sqlDbase;
        SQL_URL = "jdbc:mysql://" + sqlServer + ":3306/" + sqlDbase;
        System.out.println(SQL_URL);
        Connection sqlConn = null;
        try {
            Class.forName(SQL_DRIVER);
            String url = "jdbc:mysql://" + sqlServer + ":3306/mysql";
            sqlConn = DriverManager.getConnection(url, "root", "1010qqww");
            
            String query = "SHOW DATABASES LIKE '" + sqlDbase + "'";
            PreparedStatement pst = sqlConn.prepareStatement(query);
            ResultSet rs = pst.executeQuery(query);
            boolean exists = false;
            while (rs.next()) {
                exists = true;
                
            }
            if (exists) {
                JOptionPane.showMessageDialog(null, "로그인 성공");
            }
            else {
                Statement stmt = sqlConn.createStatement();

                query = "CREATE user '" + sqlUser + "'@'localhost' IDENTIFIED BY '" + sqlPass + "'";
                System.out.println(query);
                stmt.execute(query);

                query = "GRANT ALL PRIVILEGES ON " + sqlUser + ".* TO '" + sqlUser + "'@'localhost'";
                System.out.println(query);
                stmt.execute(query);
                
                query = "CREATE DATABASE " + sqlDbase + " DEFAULT CHARACTER SET utf8 COLLATE utf8_unicode_ci";
                System.out.println(query);
                stmt.execute(query);

                if (createTable())
                    JOptionPane.showMessageDialog(null, "가입 성공");
                else
                    JOptionPane.showMessageDialog(null, "가입 실패");
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public static boolean checkUser() {
        if (sqlUser.isEmpty()) return false;
        if (sqlPass.isEmpty()) return false;
        if (sqlDbase.isEmpty()) return false;
        if (sqlTable.isEmpty()) return false;
        return true;
    }
    public static boolean createTable() {
        System.out.println("createTable()");
        if (!checkUser()) return false;
        boolean result = false;
        Connection sqlConn = null;
        try {
            Class.forName(SQL_DRIVER);
            String url = "jdbc:mysql://localhost:3306/" + sqlDbase;
            sqlConn = DriverManager.getConnection(url, "root", "1010qqww");
            String query = "CREATE TABLE " + sqlTable + "(" +
                    "id INT PRIMARY KEY AUTO_INCREMENT NOT NULL, " +
                    "date DATE NOT NULL, " +
                    "text VARCHAR(20000) NOT NULL, " +
                    "hash VARCHAR(64) DEFAULT '', " +
                    "photo LONGBLOB)";
            System.out.println("SQL> " + query);
            Statement stmt = sqlConn.createStatement();
            stmt.execute(query);
            tableReady = true;
            result = true;
            idList.clear();
        }
        catch (Exception e) {
            JOptionPane.showMessageDialog(null, e);
        }
        if (sqlConn != null) {
            try {
                sqlConn.close();
            }
            catch (Exception e) {
            }
        }
        return result;
    }
    
    public static void main(String[] args) {
        new MainFrame();
    }
}
