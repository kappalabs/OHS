package com.kappa_labs.ohunter.server.database;

import com.kappa_labs.ohunter.server.utils.DBUtils;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Provides operation directly in the database.
 */
public class Database {

    private static final Database DATABASE;

    private static final String DRIVER = "org.apache.derby.jdbc.EmbeddedDriver";

    private static final String DB_NAME = "oHunterDB";

    private static final String URL = "jdbc:derby:" + Database.DB_NAME + ";create=true";
    private static final String USER = "";
    private static final String PASSWORD = "";

    private static final String TABLE_NAME_PLAYER = "HRAC";
    private static final String TABLE_COLUMN_PLAYER_ID = "ID_HRACE";
    private static final String TABLE_COLUMN_NICKNAME = "NICKNAME";
    private static final String TABLE_COLUMN_SCORE = "SCORE";
    private static final String TABLE_COLUMN_PASSWORD = "PASSWORD";
    private static final String TABLE_NAME_COMPLETED = "SPLNENE";
    private static final String TABLE_COLUMN_PLACE_ID = "ID_MISTA";
    private static final String TABLE_NAME_REJECTED = "ZAMITNUTE";
    private static final String TABLE_NAME_BLOCKED = "BLOKOVANE";
    private static final String TABLE_COLUMN_PHOTO_ID = "ID_FOTO";
    private static final String TABLE_COLUMN_TIMESTAMP = "TIMESTAMP";

    private Connection connection;

    static {
        DATABASE = new Database();

        setDBSystemDir();
        try {
            Class.forName(DRIVER);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(Database.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    
    private Database() { /* Singleton class */ }

    private static void setDBSystemDir() {
        /* Decide on the db system directory: <userhome>/.<table-name>/ */
        String userHomeDir = System.getProperty("user.home", ".");
        String systemDir = userHomeDir + "/." + DB_NAME;

        /* Set the db system directory. */
        System.setProperty("derby.system.home", systemDir);
    }

    private void tryInitConnection() {
        if (connection == null) {
            System.out.print("Initializing database connection...");

            try {
                connection = DriverManager.getConnection(URL, USER, PASSWORD);
            } catch (SQLException ex) {
                Logger.getLogger(Database.class.getName()).log(Level.SEVERE, null, ex);
            }
            System.out.println("... OK");

            createUnavailableTables(connection);
        }
    }

    private static void createUnavailableTables(Connection connection) {
        try {
            DatabaseMetaData meta = connection.getMetaData();
            ResultSet res;
            /* Player table */
            res = meta.getTables(null, null, TABLE_NAME_PLAYER, new String[]{"TABLE"});
            if (!res.next()) {
                createPlayerTable(connection);
            }
            DBUtils.closeQuietly(res);
            /* Completed table */
            res = meta.getTables(null, null, TABLE_NAME_COMPLETED, new String[]{"TABLE"});
            if (!res.next()) {
                createCompletedTable(connection);
            }
            DBUtils.closeQuietly(res);
            /* Rejected table */
            res = meta.getTables(null, null, TABLE_NAME_REJECTED, new String[]{"TABLE"});
            if (!res.next()) {
                createRejectedTable(connection);
            }
            DBUtils.closeQuietly(res);
            /* Blocked table */
            res = meta.getTables(null, null, TABLE_NAME_BLOCKED, new String[]{"TABLE"});
            if (!res.next()) {
                createBlockedTable(connection);
            }
            DBUtils.closeQuietly(res);
        } catch (SQLException ex) {
            Logger.getLogger(Database.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private static boolean createPlayerTable(Connection connection) {
        final String sqlCreatePlayerTable
                = "CREATE TABLE " + TABLE_NAME_PLAYER + " ( "
                + TABLE_COLUMN_PLAYER_ID + " INTEGER NOT NULL "
                + "PRIMARY KEY GENERATED ALWAYS AS IDENTITY "
                + "(START WITH 1, INCREMENT BY 1), "
                + TABLE_COLUMN_NICKNAME + " VARCHAR(16), "
                + TABLE_COLUMN_SCORE + " INTEGER, "
                + TABLE_COLUMN_PASSWORD + " VARCHAR(64)"
                + ")";
        System.out.println("creating " + TABLE_NAME_PLAYER + " table");
        return execCreateTable(connection, sqlCreatePlayerTable);
    }

    private static boolean createCompletedTable(Connection connection) {
        final String sqlCreateCompletedTable
                = "CREATE TABLE " + TABLE_NAME_COMPLETED + " ( "
                + TABLE_COLUMN_PLAYER_ID + " INTEGER NOT NULL,"
                + TABLE_COLUMN_PLACE_ID + " VARCHAR(64) NOT NULL, "
//                Google Places API unfortunately does not specify the maximum length of this value...
                + TABLE_COLUMN_PHOTO_ID + " VARCHAR(512) NOT NULL, "
                + TABLE_COLUMN_TIMESTAMP + " TIMESTAMP NOT NULL "
                + ")";
        System.out.println("creating " + TABLE_NAME_COMPLETED + " table");
        return execCreateTable(connection, sqlCreateCompletedTable);
    }

    private static boolean createRejectedTable(Connection connection) {
        final String createRejectedTableStatement
                = "CREATE TABLE " + TABLE_NAME_REJECTED + " ( "
                + TABLE_COLUMN_PLAYER_ID + " INTEGER NOT NULL,"
                + TABLE_COLUMN_PLACE_ID + " VARCHAR(64) NOT NULL "
                + ")";
        System.out.println("creating " + TABLE_NAME_REJECTED + " table");
        return execCreateTable(connection, createRejectedTableStatement);
    }

    private static boolean createBlockedTable(Connection connection) {
        final String createBlockedTableStatement
                = "CREATE TABLE " + TABLE_NAME_BLOCKED + " ( "
//                + TABLE_COLUMN_PHOTO_ID + " VARCHAR(64) NOT NULL "
                + TABLE_COLUMN_PLACE_ID + " VARCHAR(64) NOT NULL "
                + ")";
        System.out.println("creating " + TABLE_NAME_BLOCKED + " table");
        return execCreateTable(connection, createBlockedTableStatement);
    }

    private static boolean execCreateTable(Connection connection, String command) {
        boolean bCreatedTables = false;
        Statement stmtCreateTable = null;
        try {
            stmtCreateTable = connection.createStatement();
            stmtCreateTable.execute(command);
            bCreatedTables = true;
        } catch (SQLException ex) {
            Logger.getLogger(Database.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            DBUtils.closeQuietly(stmtCreateTable);
        }

        return bCreatedTables;
    }

    public void showPlayers() {
        /* Before doing anything, check (-> instantiate) the DB connector */
        tryInitConnection();

        Statement stmtWholeTable = null;
        ResultSet rsWholeTable = null;
        try {
            stmtWholeTable = connection.createStatement();
            rsWholeTable = stmtWholeTable.executeQuery("SELECT * FROM " + TABLE_NAME_PLAYER);

            ResultSetMetaData rsmd = rsWholeTable.getMetaData();
            System.out.println("Printing the whole >" + TABLE_NAME_PLAYER + "< table:");
            System.out.println("---------");
            int columnsNumber = rsmd.getColumnCount();
            while (rsWholeTable.next()) {
                for (int i = 1; i <= columnsNumber; i++) {
                    if (i > 1) {
                        System.out.print(",  ");
                    }
                    String columnValue = rsWholeTable.getString(i);
                    System.out.print(rsmd.getColumnName(i) + ": " + columnValue);
                }
                System.out.println();
            }
            System.out.println("---------");
        } catch (SQLException ex) {
            Logger.getLogger(Database.class.getName()).log(Level.WARNING, null, ex);
            closeDatabase();
        } finally {
            DBUtils.closeQuietly(stmtWholeTable);
            DBUtils.closeQuietly(rsWholeTable);
        }
    }

    public boolean removePlayer(int ID) {
        /* Before doing anything, check (-> instantiate) the DB connector */
        tryInitConnection();

        boolean removed = false;
        PreparedStatement stmtRemovePlayer = null;
        try {
            /* Remove player from Player table */
            stmtRemovePlayer = connection.prepareStatement(
                    "DELETE FROM " + TABLE_NAME_PLAYER + " WHERE " + TABLE_COLUMN_PLAYER_ID + " = ?");
            stmtRemovePlayer.setInt(1, ID);
            stmtRemovePlayer.executeUpdate();
            
            /* Remove Player's completed history */
            stmtRemovePlayer = connection.prepareStatement(
                    "DELETE FROM " + TABLE_NAME_COMPLETED + " WHERE " + TABLE_COLUMN_PLAYER_ID + " = ?");
            stmtRemovePlayer.setInt(1, ID);
            stmtRemovePlayer.executeUpdate();
            
            /* Remove Player's rejected history */
            stmtRemovePlayer = connection.prepareStatement(
                    "DELETE FROM " + TABLE_NAME_REJECTED + " WHERE " + TABLE_COLUMN_PLAYER_ID + " = ?");
            stmtRemovePlayer.setInt(1, ID);
            stmtRemovePlayer.executeUpdate();

            removed = true;
        } catch (SQLException ex) {
            Logger.getLogger(Database.class.getName()).log(Level.WARNING, null, ex);
            closeDatabase();
        } finally {
            DBUtils.closeQuietly(stmtRemovePlayer);
        }
        return removed;
    }
    
    protected boolean editPlayer(int id, String nickname, int score, String password, boolean editPassword) {
        /* Before doing anything, check (-> instantiate) the DB connector */
        tryInitConnection();

        boolean bEdited = false;
        PreparedStatement stmtEditPlayer = null;
        try {
            if (editPassword) {
                stmtEditPlayer = connection.prepareStatement(
                        "UPDATE " + TABLE_NAME_PLAYER + " SET "
                        + TABLE_COLUMN_NICKNAME + " = ?,"
                        + TABLE_COLUMN_SCORE + " = ?,"
                        + TABLE_COLUMN_PASSWORD + " = ?,"
                        + "WHERE " + TABLE_COLUMN_PLAYER_ID + " = ?");
                stmtEditPlayer.setString(3, password);
                stmtEditPlayer.setInt(4, id);
            } else {
                stmtEditPlayer = connection.prepareStatement(
                        "UPDATE " + TABLE_NAME_PLAYER + " SET "
                        + TABLE_COLUMN_NICKNAME + " = ?,"
                        + TABLE_COLUMN_SCORE + " = ?,"
                        + "WHERE " + TABLE_COLUMN_PLAYER_ID + " = ?");
                stmtEditPlayer.setInt(3, id);
            }
            stmtEditPlayer.setString(1, nickname);
            stmtEditPlayer.setInt(2, score);
            stmtEditPlayer.executeUpdate();
            bEdited = true;
        } catch (SQLException ex) {
            Logger.getLogger(Database.class.getName()).log(Level.WARNING, null, ex);
            closeDatabase();
        } finally {
            DBUtils.closeQuietly(stmtEditPlayer);
        }
        return bEdited;
    }

    protected int createPlayer(String nickname, int score, String password) {
        /* Before doing anything, check (-> instantiate) the DB connector */
        tryInitConnection();

        int id = -1;
        PreparedStatement stmtCreatePlayer = null;
        ResultSet results = null;
        try {
            stmtCreatePlayer = connection.prepareStatement(
                    "INSERT INTO " + TABLE_NAME_PLAYER + " "
                    + "(" + TABLE_COLUMN_NICKNAME + ","
                    + TABLE_COLUMN_SCORE + ","
                    + TABLE_COLUMN_PASSWORD + ")"
                    + "VALUES (?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS);
            stmtCreatePlayer.setString(1, nickname);
            stmtCreatePlayer.setInt(2, score);
            stmtCreatePlayer.setString(3, password);
            stmtCreatePlayer.executeUpdate();
            results = stmtCreatePlayer.getGeneratedKeys();
            if (results.next()) {
                id = results.getInt(1);
            }
        } catch (SQLException ex) {
            Logger.getLogger(Database.class.getName()).log(Level.WARNING, null, ex);
            closeDatabase();
        } finally {
            DBUtils.closeQuietly(results);
            DBUtils.closeQuietly(stmtCreatePlayer);
        }
        return id;
    }
    
    protected int getPlayerID(String nickname, String password, boolean checkPassword) {
        /* Before doing anything, check (-> instantiate) the DB connector */
        tryInitConnection();

        int id = -1;
        
        PreparedStatement stmtCreatePlayer = null;
        ResultSet resultRow = null;
        try {
            if (checkPassword) {
                stmtCreatePlayer = connection.prepareStatement(
                        "SELECT "+ TABLE_COLUMN_PLAYER_ID + "," + TABLE_COLUMN_NICKNAME + " "
                        + "FROM " + TABLE_NAME_PLAYER + " "
                        + "WHERE " + TABLE_COLUMN_NICKNAME + " = ? AND "
                                + TABLE_COLUMN_PASSWORD + " = ?");
                stmtCreatePlayer.setString(1, nickname);
                stmtCreatePlayer.setString(2, password);
            } else {
                stmtCreatePlayer = connection.prepareStatement(
                        "SELECT "+ TABLE_COLUMN_PLAYER_ID + "," + TABLE_COLUMN_NICKNAME + " "
                        + "FROM " + TABLE_NAME_PLAYER + " "
                        + "WHERE " + TABLE_COLUMN_NICKNAME + " = ?");
                stmtCreatePlayer.setString(1, nickname);
            }
            resultRow = stmtCreatePlayer.executeQuery();
            id = -2;
            if (resultRow.next()) {
                id = resultRow.getInt(1);
            }
        } catch (SQLException ex) {
            Logger.getLogger(Database.class.getName()).log(Level.WARNING, null, ex);
            closeDatabase();
        } finally {
            DBUtils.closeQuietly(resultRow);
            DBUtils.closeQuietly(stmtCreatePlayer);
        }
        return id;
    }
    
    protected int getPlayerScore(int id) {
        /* Before doing anything, check (-> instantiate) the DB connector */
        tryInitConnection();
        
        int score = -1;
        PreparedStatement stmtCreatePlayer = null;
        ResultSet resultRow = null;
        try {
            stmtCreatePlayer = connection.prepareStatement(
                    "SELECT " + TABLE_COLUMN_SCORE + " "
                    + "FROM " + TABLE_NAME_PLAYER + " "
                    + "WHERE " + TABLE_COLUMN_PLAYER_ID + " = ?");
            stmtCreatePlayer.setInt(1, id);
            resultRow = stmtCreatePlayer.executeQuery();
            if (resultRow.next()) {
                score = resultRow.getInt(1);
            }
        } catch (SQLException ex) {
            Logger.getLogger(Database.class.getName()).log(Level.WARNING, null, ex);
            closeDatabase();
        } finally {
            DBUtils.closeQuietly(resultRow);
            DBUtils.closeQuietly(stmtCreatePlayer);
        }
        return score;
    }
    
    protected boolean addCompleted(int playerID, String placeID, String photoReference, Timestamp timestamp) {
        /* Before doing anything, check (-> instantiate) the DB connector */
        tryInitConnection();
        
        boolean added = false;
        PreparedStatement stmtAddCompleted = null;
        ResultSet results = null;
        try {
            stmtAddCompleted = connection.prepareStatement(
                    "INSERT INTO " + TABLE_NAME_COMPLETED + " "
                    + "(" + TABLE_COLUMN_PLAYER_ID + ","
                            + TABLE_COLUMN_PLACE_ID + ","
                            + TABLE_COLUMN_PHOTO_ID + ","
                            + TABLE_COLUMN_TIMESTAMP + ")"
                    + "VALUES (?, ?, ?, ?)");
            stmtAddCompleted.setInt(1, playerID);
            stmtAddCompleted.setString(2, placeID);
            stmtAddCompleted.setString(3, photoReference);
            stmtAddCompleted.setTimestamp(4, timestamp);
            stmtAddCompleted.executeUpdate();
            added = true;
        } catch (SQLException ex) {
            Logger.getLogger(Database.class.getName()).log(Level.WARNING, null, ex);
            closeDatabase();
        } finally {
            DBUtils.closeQuietly(results);
            DBUtils.closeQuietly(stmtAddCompleted);
        }
        return added;
    }
    
    /**
     * Checks if the given pair exists in the database of completed places.
     * 
     * @param playerID ID of the player to check.
     * @param placeID ID of the place to check.
     * @return Number -1 on error, 0 when record does not exist, 1 on success.
     */
    protected int isCompleted(int playerID, String placeID) {
        /* Before doing anything, check (-> instantiate) the DB connector */
        tryInitConnection();
        int ret = -1;
        
        PreparedStatement stmtCreatePlayer = null;
        ResultSet resultRow = null;
        try {
            stmtCreatePlayer = connection.prepareStatement(
                    "SELECT * "
                    + "FROM " + TABLE_NAME_COMPLETED + " "
                    + "WHERE " + TABLE_COLUMN_PLAYER_ID + " = ? AND "
                            + TABLE_COLUMN_PLACE_ID + " = ?");
            stmtCreatePlayer.setInt(1, playerID);
            stmtCreatePlayer.setString(2, placeID);
            resultRow = stmtCreatePlayer.executeQuery();
            ret = 0;
            if (resultRow.next()) {
                ret = 1;
            }
        } catch (SQLException ex) {
            Logger.getLogger(Database.class.getName()).log(Level.WARNING, null, ex);
            closeDatabase();
        } finally {
            DBUtils.closeQuietly(resultRow);
            DBUtils.closeQuietly(stmtCreatePlayer);
        }
        return ret;
    }
    
    protected boolean addRejected(int playerID, String placeID) {
        /* Before doing anything, check (-> instantiate) the DB connector */
        tryInitConnection();
        
        boolean added = false;
        PreparedStatement stmtAddRrejected = null;
        ResultSet results = null;
        try {
            stmtAddRrejected = connection.prepareStatement(
                    "INSERT INTO " + TABLE_NAME_REJECTED + " "
                    + "(" + TABLE_COLUMN_PLAYER_ID + "," + TABLE_COLUMN_PLACE_ID + ")"
                    + "VALUES (?, ?)");
            stmtAddRrejected.setInt(1, playerID);
            stmtAddRrejected.setString(2, placeID);
            stmtAddRrejected.executeUpdate();
            added = true;
        } catch (SQLException ex) {
            Logger.getLogger(Database.class.getName()).log(Level.WARNING, null, ex);
            closeDatabase();
        } finally {
            DBUtils.closeQuietly(results);
            DBUtils.closeQuietly(stmtAddRrejected);
        }
        return added;
    }
    
    /**
     * Checks if the given pair exists in the database of rejected places.
     * 
     * @param playerID ID of the player to check.
     * @param placeID ID of the place to check.
     * @return Number -1 on error, 0 when record does not exist, 1 on success.
     */
    protected int isRejected(int playerID, String placeID) {
        /* Before doing anything, check (-> instantiate) the DB connector */
        tryInitConnection();
        int id = -1;
        
        PreparedStatement stmtCreatePlayer = null;
        ResultSet resultRow = null;
        try {
            stmtCreatePlayer = connection.prepareStatement(
                    "SELECT * "
                    + "FROM " + TABLE_NAME_REJECTED + " "
                    + "WHERE " + TABLE_COLUMN_PLAYER_ID + " = ? AND "
                            + TABLE_COLUMN_PLACE_ID + " = ?");
            stmtCreatePlayer.setInt(1, playerID);
            stmtCreatePlayer.setString(2, placeID);
            resultRow = stmtCreatePlayer.executeQuery();
            id = 0;
            if (resultRow.next()) {
                id = 1;
            }
        } catch (SQLException ex) {
            Logger.getLogger(Database.class.getName()).log(Level.WARNING, null, ex);
            closeDatabase();
        } finally {
            DBUtils.closeQuietly(resultRow);
            DBUtils.closeQuietly(stmtCreatePlayer);
        }
        return id;
    }
    
    protected boolean addBlocked(String placeID) {
        /* Before doing anything, check (-> instantiate) the DB connector */
        tryInitConnection();
        
        boolean added = false;
        PreparedStatement stmtAddBlocked = null;
        ResultSet results = null;
        try {
            stmtAddBlocked = connection.prepareStatement(
                    "INSERT INTO " + TABLE_NAME_BLOCKED + " "
                    + "(" + TABLE_COLUMN_PLACE_ID+ ") VALUES (?)");
            stmtAddBlocked.setString(1, placeID);
            stmtAddBlocked.executeUpdate();
            added = true;
        } catch (SQLException ex) {
            Logger.getLogger(Database.class.getName()).log(Level.WARNING, null, ex);
            closeDatabase();
        } finally {
            DBUtils.closeQuietly(results);
            DBUtils.closeQuietly(stmtAddBlocked);
        }
        return added;
    }
    
    /**
     * Checks if the given key exists in the database of blocked places.
     * 
     * @param placeID ID of the place to check.
     * @return Number -1 on error, 0 when record does not exist, 1 on success.
     */
    protected int isBlocked(String placeID) {
        /* Before doing anything, check (-> instantiate) the DB connector */
        tryInitConnection();
        int id = -1;
        
        PreparedStatement stmtCreatePlayer = null;
        ResultSet resultRow = null;
        try {
            stmtCreatePlayer = connection.prepareStatement(
                    "SELECT * "
                    + "FROM " + TABLE_NAME_BLOCKED + " "
//                    + "WHERE " + TABLE_COLUMN_PHOTO_ID + " = ?");
                    + "WHERE " + TABLE_COLUMN_PLACE_ID + " = ?");
            stmtCreatePlayer.setString(1, placeID);
            resultRow = stmtCreatePlayer.executeQuery();
            id = 0;
            if (resultRow.next()) {
                id = 1;
            }
        } catch (SQLException ex) {
            Logger.getLogger(Database.class.getName()).log(Level.WARNING, null, ex);
            closeDatabase();
        } finally {
            DBUtils.closeQuietly(resultRow);
            DBUtils.closeQuietly(stmtCreatePlayer);
        }
        return id;
    }
    
    
    public void closeDatabase() {
        DBUtils.closeQuietly(connection);
        connection = null;
    }

    public static Database getInstance() {
        return DATABASE;
    }

}
