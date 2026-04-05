package gamersync;

import gamersync.db.DBConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Phase1ConnectionTest {
    public static void main(String[] args) {
        String sql = "SELECT CUST_ID, NAME, PHONE, EMAIL, REGISTERED_DATE FROM CUSTOMER LIMIT 5";

        try (
            Connection con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(sql);
            ResultSet rs = ps.executeQuery()
        ) {
            System.out.println("Connected Successfully");
            System.out.println("Sample rows from CUSTOMER:");

            int count = 0;
            while (rs.next()) {
                count++;
                System.out.printf(
                    "%d | %s | %s | %s | %s%n",
                    rs.getInt("CUST_ID"),
                    rs.getString("NAME"),
                    rs.getString("PHONE"),
                    rs.getString("EMAIL"),
                    rs.getString("REGISTERED_DATE")
                );
            }

            if (count == 0) {
                System.out.println("CUSTOMER table is empty, but DB connection is working.");
            }
        } catch (SQLException e) {
            System.out.println("Connection/Test failed: " + e.getMessage());
            if (e.getCause() != null) {
                System.out.println("Root cause: " + e.getCause().getMessage());
            }
        }
    }
}
