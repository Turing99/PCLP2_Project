package com.PCLP2.Project.data.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class DBConnection {

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.out.println("Eroare incarcare driver!\n" + e);
        }
    }

    private final static String CONNECTION_STR = "jdbc:mysql://localhost:3306/library_db";
    private final static String DB_USERNAME = "goiac";
    private final static String DB_PASSWORD = "";

    public static Connection openConnection() {
        Connection con = null;

        try {
            con = DriverManager.getConnection(CONNECTION_STR, DB_USERNAME, DB_PASSWORD);
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }

        return con;
    }

    public static void closeConnection(Connection con, PreparedStatement ps, ResultSet rs) {
        if (rs != null) {
            try {
                rs.close();
            } catch (Exception ignored) {
            }
        }

        if(ps != null) {
            try {
                ps.close();
            } catch (Exception ignored) {

            }
        }
        if( con != null) {
            try {
                con.close();
            } catch (Exception ignored) {

            }
        }
    }

}
