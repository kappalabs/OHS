
package com.kappa_labs.ohunter.server.database;

import com.kappa_labs.ohunter.lib.entities.Player;
import com.kappa_labs.ohunter.lib.net.OHException;
import java.sql.Timestamp;

/**
 * Class providing operations in database.
 */
public class DatabaseService {
    
    private final Database database = Database.getInstance();
    
    /**
     * Default starting score for every new player.
     */
    public static final int DEFAULT_SCORE = 42;
    
    
    /**
     * Try to register a new player with given name and password.
     * Return the Player object on success, otherwise throw OHException.
     * 
     * @param nickname Nickname for this player.
     * @param password Password hash of this player.
     * @return Registered Player object.
     * @throws OHException When player exists or another error arises.
     */
    public Player registerPlayer(String nickname, String password) throws OHException {
        int euid = database.getPlayerID(nickname, null, false);
        if (euid == -1) {
            throw new OHException("Error while registering new player " + nickname + "!",
                    OHException.EXType.DATABASE_ERROR);
        } else if (euid > 0) {
            throw new OHException("Player with this nickname already exists. UID = " + euid,
                    OHException.EXType.DUPLICATE_USER);
        }
        
        int uid = database.createPlayer(nickname, DEFAULT_SCORE, password);
        if (uid == -1) {
            throw new OHException("Error when creating new player " + nickname + "!",
                    OHException.EXType.DATABASE_ERROR);
        }
        Player newPlayer = new Player(uid, nickname, DEFAULT_SCORE);
        
        return newPlayer;
    }
    
    /**
     * Reset all parameters of the given Player in database to default values.
     * If the given player doesn't exist in the database, don't do anything.
     * 
     * @param player The Player, whose parameters should be reset.
     * @throws OHException When exception in the database arises.
     */
    public void resetPlayer(Player player) throws OHException {
        if (database.editPlayer(player.getUID(), player.getNickname(), DEFAULT_SCORE, null, false)) {
            player.setScore(DEFAULT_SCORE);
        } else {
            throw new OHException("Error while editing player score!", OHException.EXType.DATABASE_ERROR);
        }
        
    }
    
    /**
     * Checks if the nickname and given password exists in the database,
     * then creates new Player object from retrieved information.
     * 
     * @param nickname Nickname of the player.
     * @param password Password hash of the player.
     * @return A new Player object representing the given player.
     * @throws OHException When the password is wrong, player does not exist
     *          or another error arises in the database.
     */
    public Player loginPlayer(String nickname, String password) throws OHException {
        int uid = database.getPlayerID(nickname, password, true);
        if (uid == -1) {
            throw new OHException("Error while finding player in DB!",
                    OHException.EXType.DATABASE_ERROR);
        } else if (uid == -2) {
            int _uid = database.getPlayerID(nickname, null, false);
            if (_uid == -1) {
                throw new OHException("Error while finding player in DB!",
                        OHException.EXType.DATABASE_ERROR);
            } else if (_uid < 0) {
                throw new OHException("Player with this nickname does not exist!",
                        OHException.EXType.INCORRECT_USER);
            }
            throw new OHException("Wrong password!",
                    OHException.EXType.INCORRECT_PASSWORD);
        }
        
        int score = database.getPlayerScore(uid);
        if (score == -1) {
            throw new OHException("Score cannot be retrieved!",
                    OHException.EXType.DATABASE_ERROR);
        }
        
        return new Player(uid, nickname, score);
    }
    
    /**
     * Update values of the given player in the database to his current values.
     * In the database, Player is identified by his UID.
     * If the Player is not in the database, no operation is performed.
     * 
     * @param player The Player to be updated in the database.
     * @throws OHException When player information cannot be updated.
     */
    public void updatePlayer(Player player) throws OHException {
        int uid = database.getPlayerID(player.getNickname(), null, false);
        if (uid == -1) {
                throw new OHException("Error while finding player in DB!",
                        OHException.EXType.DATABASE_ERROR);
        }
        if (uid != -2) {
            if (!database.editPlayer(player.getUID(), player.getNickname(), player.getScore(), null, false)) {
                throw new OHException("Error while editing player values!", OHException.EXType.DATABASE_ERROR);
            }
        }
    }
    
    /**
     * Change password for given player in the database.
     * In the database, Player is identified by his UID.
     * 
     * @param player The Player, who wants to change the password.
     * @param oldPassword The current password for given player.
     * @param newPassword The new password for given player.
     * @throws OHException When the player does not exist or the old password is wrong.
     */
    public void changePassword(Player player, String oldPassword, String newPassword) throws OHException {
        int uid = database.getPlayerID(player.getNickname(), oldPassword, true);
        if (uid == -1) {
            throw new OHException("Error while finding player in DB!",
                    OHException.EXType.DATABASE_ERROR);
        } else if (uid == -2) {
            int _uid = database.getPlayerID(player.getNickname(), null, false);
            if (_uid == -1) {
                throw new OHException("Error while finding player in DB!",
                        OHException.EXType.DATABASE_ERROR);
            } else if (_uid < 0) {
                throw new OHException("Player with this nickname does not exist!",
                        OHException.EXType.INCORRECT_USER);
            }
            throw new OHException("Wrong old password!",
                    OHException.EXType.INCORRECT_PASSWORD);
        }
        
        if (!database.editPlayer(player.getUID(), player.getNickname(),
                player.getScore(), newPassword, true)) {
            throw new OHException("Error while changing the password!",
                    OHException.EXType.DATABASE_ERROR);
        }
    }
    
    /**
     * Remove given Player from the database records.
     * In database, Player is identified by his UID.
     * 
     * @param player The Player to be removed from database records.
     * @throws OHException When given player is not in the database.
     */
    public void removePlayer(Player player) throws OHException {
        int uid = database.getPlayerID(player.getNickname(), null, false);
        if (uid == -1) {
            throw new OHException("Error while finding player in DB!",
                    OHException.EXType.DATABASE_ERROR);
        } else if (uid == -2) {
            throw new OHException("Given Player does not exist!",
                    OHException.EXType.INCORRECT_USER);
        }
        
        if (!database.removePlayer(uid)) {
            throw new OHException("Error while removing player from DB!",
                    OHException.EXType.DATABASE_ERROR);
        }
    }
    
    /**
     * Add new completed place to database for given player.
     * 
     * @param player Who completed the place.
     * @param placeID Place unique identifier.
     * @param photoReference Photo reference, from Google Places, of the image that was photographed.
     * @param timestamp Timestamp of the time, when the place was completed.
     * @throws OHException When place cannot be add to database.
     */
    public void completePlace(Player player, String placeID, String photoReference, Timestamp timestamp) throws OHException {
        if (!database.addCompleted(player.getUID(), placeID, photoReference, timestamp)) {
            throw new OHException("Error while adding to completed!", OHException.EXType.DATABASE_ERROR);
        }
    }
    
    /**
     * Check if the given player has the place among his completed ones.
     * 
     * @param player The player, who should be checked.
     * @param placeKey The place, which should be checked.
     * @return True if the place is completed by this player, false if not.
     * @throws OHException When error arises in the database.
     */
    public boolean isCompleted(Player player, String placeKey) throws OHException {
        int ret = database.isCompleted(player.getUID(), placeKey);
        if (ret == 1) {
            return true;
        } else if (ret == 0) {
            return false;
        }
        throw new OHException("Cannot check, database error!", OHException.EXType.DATABASE_ERROR);
    }
    
    /**
     * Add new rejected place to database for given player.
     * 
     * @param player Who is rejecting the place.
     * @param placeKey Place unique identifier.
     * @throws OHException When rejected place cannot be add to database.
     */
    public void rejectPlace(Player player, String placeKey) throws OHException {
        if (!database.addRejected(player.getUID(), placeKey)) {
            throw new OHException("Error while adding to rejected", OHException.EXType.DATABASE_ERROR);
        }
    }
    
    /**
     * Check if the given player has the place among his rejected ones.
     * 
     * @param player The player, who should be checked.
     * @param placeKey The place, which should be checked.
     * @return True if the place is rejected by this player, false if not.
     * @throws OHException When error arises in the database.
     */
    public boolean isRejected(Player player, String placeKey) throws OHException {
        int ret = database.isRejected(player.getUID(), placeKey);
        if (ret == 1) {
            return true;
        } else if (ret == 0) {
            return false;
        }
        throw new OHException("Cannot check, database error!", OHException.EXType.DATABASE_ERROR);
    }
    
    /**
     * Add new place to be blocked into the database.
     * 
     * @param placeKey Place unique identifier.
     * @throws OHException When the new record cannot be add to database.
     */
    public void blockPlace(String placeKey) throws OHException {
        if (!database.addBlocked(placeKey)) {
            throw new OHException("Error while adding toblocked! ("+placeKey+")", OHException.EXType.DATABASE_ERROR);
        }
    }
    
    /**
     * Check if the given place is blocked.
     * 
     * @param placeKey The place, which should be checked.
     * @return True if the place is blocked, false if not.
     * @throws OHException When error arises in the database.
     */
    public boolean isBlocked(String placeKey) throws OHException {
        int ret = database.isBlocked(placeKey);
        if (ret == 1) {
            return true;
        } else if (ret == 0) {
            return false;
        }
        throw new OHException("Cannot check, database error!", OHException.EXType.DATABASE_ERROR);
    }
}
