package com.kappa_labs.ohunter.server.database;

import com.kappa_labs.ohunter.lib.entities.Player;
import com.kappa_labs.ohunter.server.utils.DBUtils;
import java.io.File;
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

    public static final String TABLE_NAME_PLAYER = "HRAC";
    public static final String TABLE_NAME_REJECTED = "ZAMITNUTE";
    public static final String TABLE_NAME_BLOCKED = "BLOKOVANE";
    public static final String TABLE_NAME_COMPLETED = "SPLNENE";

    private static final String TABLE_COLUMN_PLAYER_ID = "ID_HRACE";
    private static final String TABLE_COLUMN_NICKNAME = "PREZDIVKA";
    private static final String TABLE_COLUMN_SCORE = "SKORE";
    private static final String TABLE_COLUMN_PASSWORD = "HESLO";
    private static final String TABLE_COLUMN_PLACE_ID = "ID_CILE";
    private static final String TABLE_COLUMN_PHOTO_ID = "ID_FOTO";
    private static final String TABLE_COLUMN_TIMESTAMP = "TIMESTAMP";
    private static final String TABLE_COLUMN_DISCOVERY = "OBJEVENI";
    private static final String TABLE_COLUMN_SIMILARITY = "PODOBNOST";
    private static final String TABLE_COLUMN_HUNT_NUMBER = "LOV";

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

    
    private Database() {
        /* Singleton class */
    }

    /**
     * Gets the instance of this singleton class.
     * 
     * @return The instance of this singleton class.
     */
    public static Database getInstance() {
        return DATABASE;
    }

    private static void setDBSystemDir() {
        /* Decide on the db system directory: <userhome>/.<table-name>/ */
        String userHomeDir = System.getProperty("user.home", ".");
        String systemDir = userHomeDir + File.separator + "." + DB_NAME;

        /* Set the db system directory. */
        System.setProperty("derby.system.home", systemDir);
    }

    /**
     * Tries to initialize the database (create its tables). Nothing is made if
     * all the tables exist, otherwise nonexisting tables are created.
     */
    public void tryInitConnection() {
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
            } else {
                System.out.println("table " + TABLE_NAME_PLAYER + " ready");
            }
            DBUtils.closeQuietly(res);
            /* Completed table */
            res = meta.getTables(null, null, TABLE_NAME_COMPLETED, new String[]{"TABLE"});
            if (!res.next()) {
                createCompletedTable(connection);
            } else {
                System.out.println("table " + TABLE_NAME_COMPLETED + " ready");
            }
            DBUtils.closeQuietly(res);
            /* Rejected table */
            res = meta.getTables(null, null, TABLE_NAME_REJECTED, new String[]{"TABLE"});
            if (!res.next()) {
                createRejectedTable(connection);
            } else {
                System.out.println("table " + TABLE_NAME_REJECTED + " ready");
            }
            DBUtils.closeQuietly(res);
            /* Blocked table */
            res = meta.getTables(null, null, TABLE_NAME_BLOCKED, new String[]{"TABLE"});
            if (!res.next()) {
                createBlockedTable(connection);
            } else {
                System.out.println("table " + TABLE_NAME_BLOCKED + " ready");
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
                /* Google Places API unfortunately does not specify the maximum length of this value... */
                + TABLE_COLUMN_PHOTO_ID + " VARCHAR(512) NOT NULL, "
                + TABLE_COLUMN_TIMESTAMP + " TIMESTAMP NOT NULL, "
                + TABLE_COLUMN_DISCOVERY + " INTEGER NOT NULL, "
                + TABLE_COLUMN_SIMILARITY + " INTEGER NOT NULL, "
                + TABLE_COLUMN_HUNT_NUMBER + " INTEGER NOT NULL "
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

    /**
     * Shows the whole content of table specified by its name.
     *
     * @param tableName The name of the table to show.
     */
    public void showTable(String tableName) {
        /* Before doing anything, check (-> instantiate) the DB connector */
        tryInitConnection();

        Statement stmtWholeTable = null;
        ResultSet rsWholeTable = null;
        try {
            stmtWholeTable = connection.createStatement();
            rsWholeTable = stmtWholeTable.executeQuery("SELECT * FROM " + tableName);

            ResultSetMetaData rsmd = rsWholeTable.getMetaData();
            System.out.println("Printing the whole >" + tableName + "< table:");
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

    /**
     * Gets the list of few best players ordered from the ones with best score
     * to worst in the end.
     *
     * @param count Maximum number of best players to return.
     * @return The list of best players ordered from best to worst.
     */
    public Player[] getBestPlayers(int count) {
        /* Before doing anything, check (-> instantiate) the DB connector */
        tryInitConnection();

        Player[] bestPlayers = new Player[count];
        int position = 0;
        PreparedStatement stmtGetBests = null;
        try {
            /* Remove player from Player table */
            stmtGetBests = connection.prepareStatement(
                    "SELECT " + TABLE_COLUMN_NICKNAME + "," + TABLE_COLUMN_SCORE
                    + " FROM " + TABLE_NAME_PLAYER
                    + " ORDER BY " + TABLE_COLUMN_SCORE + " DESC"
                    + " FETCH FIRST ? ROWS ONLY");
            stmtGetBests.setInt(1, count);

            ResultSet results = stmtGetBests.executeQuery();
            while (results.next()) {
                String name = results.getString(TABLE_COLUMN_NICKNAME);
                int score = results.getInt(TABLE_COLUMN_SCORE);
                bestPlayers[position++] = new Player(-1, name, score);
            }
        } catch (SQLException ex) {
            Logger.getLogger(Database.class.getName()).log(Level.WARNING, null, ex);
            closeDatabase();
        } finally {
            DBUtils.closeQuietly(stmtGetBests);
        }
        return bestPlayers;
    }

    /**
     * Removes records of player with given ID from all tables in database.
     *
     * @param ID The unique player ID of the player to remove.
     * @return True on success, false on fail.
     */
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

    /**
     * Edits the player's data. Player must be specified by his ID, other fields
     * will be updated in the table. If the parameter is null, the appropriate
     * value will not be changed.
     *
     * @param id ID number of the player to edit.
     * @param nickname New nickname of the player, null if not changed.
     * @param score New score of the player, null if not changed.
     * @param password New password of the player, null if not changed.
     * @return True on success, false on fail.
     */
    public boolean editPlayer(int id, String nickname, Integer score, String password) {
        /* Before doing anything, check (-> instantiate) the DB connector */
        tryInitConnection();

        boolean bEdited = false;
        PreparedStatement stmtEditPlayer = null;
        try {
            String statement = "UPDATE " + TABLE_NAME_PLAYER + " SET";
            int nicknamePos = 0;
            if (nickname != null) {
                nicknamePos = 1;
                statement += " " + TABLE_COLUMN_NICKNAME + " = ?";
            }
            int scorePos = 0;
            if (score != null) {
                scorePos = nicknamePos + 1;
                statement += ((scorePos > 1) ? ", " : " ") + TABLE_COLUMN_SCORE + " = ?";
            }
            int passwordPos = 0;
            if (password != null) {
                passwordPos = Math.max(nicknamePos, scorePos) + 1;
                statement += ((passwordPos > 1) ? ", " : " ") + TABLE_COLUMN_PASSWORD + " = ?";
            }
            statement += " WHERE " + TABLE_COLUMN_PLAYER_ID + " = ?";
            stmtEditPlayer = connection.prepareStatement(statement);
            if (nicknamePos > 0) {
                stmtEditPlayer.setString(nicknamePos, nickname);
            }
            if (scorePos > 0) {
                stmtEditPlayer.setInt(scorePos, score);
            }
            if (passwordPos > 0) {
                stmtEditPlayer.setString(passwordPos, password);
            }
            stmtEditPlayer.setInt(Math.max(nicknamePos, Math.max(scorePos, passwordPos)) + 1, id);
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

    /**
     * Creates a record for a new player in the database.
     *
     * @param nickname Nickname of the new player.
     * @param score Initial score of the new player.
     * @param password The password hash of the new player.
     * @return Player ID for the new player -1 on fail.
     */
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

    /**
     * Gets the player ID of player specified by his nickname.
     *
     * @param nickname Nickname of the player.
     * @param password Password hash of the player.
     * @param checkPassword Specifies if the password should be checked.
     * @return Player ID of the player with given nickname, -2 if the nickname
     * does not exist in database or when the password is incorrect (if
     * checked), -1 if on fail
     */
    protected int getPlayerID(String nickname, String password, boolean checkPassword) {
        /* Before doing anything, check (-> instantiate) the DB connector */
        tryInitConnection();

        int id = -1;

        PreparedStatement stmtCreatePlayer = null;
        ResultSet resultRow = null;
        try {
            if (checkPassword) {
                stmtCreatePlayer = connection.prepareStatement(
                        "SELECT " + TABLE_COLUMN_PLAYER_ID + "," + TABLE_COLUMN_NICKNAME + " "
                        + "FROM " + TABLE_NAME_PLAYER + " "
                        + "WHERE " + TABLE_COLUMN_NICKNAME + " = ? AND "
                        + TABLE_COLUMN_PASSWORD + " = ?");
                stmtCreatePlayer.setString(1, nickname);
                stmtCreatePlayer.setString(2, password);
            } else {
                stmtCreatePlayer = connection.prepareStatement(
                        "SELECT " + TABLE_COLUMN_PLAYER_ID + "," + TABLE_COLUMN_NICKNAME + " "
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

    /**
     * Retrieves the score of player with given player ID.
     * 
     * @param id The players ID.
     * @return The score of specified player or -1 on fail.
     */
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

    /**
     * Add new completed place to database table.
     *
     * @param playerID ID of the player who completed the place.
     * @param placeID Place unique identifier.
     * @param photoReference Photo reference, from Google Places, of the image
     * that was photographed.
     * @param timestamp Timestamp of the time, when the place was completed.
     * @param discoveryGain Number of points given for finding the target.
     * @param similarityGain Number of points given for photo similarity.
     * @param huntNumber The number of hunt, in which the target was completed.
     * @return True if the record was written succesfully, false otherwise.
     */
    protected boolean addCompleted(int playerID, String placeID, String photoReference, Timestamp timestamp, int discoveryGain, int similarityGain, int huntNumber) {
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
                    + TABLE_COLUMN_TIMESTAMP + ","
                    + TABLE_COLUMN_DISCOVERY + ","
                    + TABLE_COLUMN_SIMILARITY + ","
                    + TABLE_COLUMN_HUNT_NUMBER + ")"
                    + "VALUES (?, ?, ?, ?, ?, ?, ?)");
            stmtAddCompleted.setInt(1, playerID);
            stmtAddCompleted.setString(2, placeID);
            stmtAddCompleted.setString(3, photoReference);
            stmtAddCompleted.setTimestamp(4, timestamp);
            stmtAddCompleted.setInt(5, discoveryGain);
            stmtAddCompleted.setInt(6, similarityGain);
            stmtAddCompleted.setInt(7, huntNumber);
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

    /**
     * Adds a new record to table of rejected places/targets.
     * 
     * @param playerID ID of the player who rejected the place ID.
     * @param placeID Place ID of the rejected place/target.
     * @return True on success, false on fail.
     */
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

    /**
     * Adds a new record to table of blocked places/targets.
     * 
     * @param placeID Place ID of the blocked place/target.
     * @return True on success, false on fail.
     */
    protected boolean addBlocked(String placeID) {
        /* Before doing anything, check (-> instantiate) the DB connector */
        tryInitConnection();

        boolean added = false;
        PreparedStatement stmtAddBlocked = null;
        ResultSet results = null;
        try {
            stmtAddBlocked = connection.prepareStatement(
                    "INSERT INTO " + TABLE_NAME_BLOCKED + " "
                    + "(" + TABLE_COLUMN_PLACE_ID + ") VALUES (?)");
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

    /**
     * Safely close the database connections.
     */
    public void closeDatabase() {
        DBUtils.closeQuietly(connection);
        connection = null;
    }

}
