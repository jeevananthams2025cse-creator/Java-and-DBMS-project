package com.bloodbank.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import javax.sql.DataSource;

public class DatabaseConnection {

    private static volatile DataSource dataSource;

    private static final String URL =
            System.getenv("DB_URL") != null
                    ? System.getenv("DB_URL")
                    : "jdbc:mysql://localhost:3306/blood_bank_management";

    private static final String USER = System.getenv("DB_USER");
    private static final String PASSWORD = System.getenv("DB_PASSWORD");

    public static void setDataSource(DataSource ds) {
        dataSource = ds;
    }

    public static DataSource getDataSource() {
        return dataSource;
    }

    public static Connection getConnection() throws SQLException {
        if (dataSource != null) {
            return dataSource.getConnection();
        }

        if (USER == null || PASSWORD == null) {
            throw new SQLException(
                    "Set DB_USER and DB_PASSWORD environment variables."
            );
        }

        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
