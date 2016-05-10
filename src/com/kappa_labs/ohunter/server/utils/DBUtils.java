package com.kappa_labs.ohunter.server.utils;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * Utilities to simplify operations on database.
 */
public class DBUtils {

    public static void closeQuietly(Connection connection) {
        try {
            connection.close();
        } catch (Exception ex) {
            /* Empty body - quiet */
        }
    }

    public static void closeQuietly(Statement statement) {
        try {
            statement.close();
        } catch (Exception ex) {
            /* Empty body - quiet */
        }
    }

    public static void closeQuietly(ResultSet resultSet) {
        try {
            resultSet.close();
        } catch (Exception ex) {
            /* Empty body - quiet */
        }
    }

}
