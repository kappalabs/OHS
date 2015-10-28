
package com.kappa_labs.ohunter.server.entities;

/**
 * Representation of a single user of this application.
 */
public class Player {
    
    /**
     * Unique integer identifier.
     */
    private final int UID;
    /**
     * Unique name.
     */
    private String nickname;
    /**
     * Indicator of success, should always be non-negative.
     */
    private int score;

    
    /**
     * Every player must have UID, nickname and score. These are stored
     * in the database.
     * 
     * @param UID Unique integer identifier given by database.
     * @param nickname Unique string identifier given by player.
     * @param score Current value of success indicator.
     */
    public Player(int UID, String nickname, int score) {
        this.UID = UID;
        this.nickname = nickname;
        this.score = score;
    }
    
    /**
     * Gets the player's UID.
     * 
     * @return The player's UID.
     */
    public int getUID() {
        return UID;
    }

    /**
     * Gets the player's nickname.
     * 
     * @return The player's nickname.
     */
    public String getNickname() {
        return nickname;
    }

    /**
     * Gets the player's score.
     * 
     * @return The player's score.
     */
    public int getScore() {
        return score;
    }

    /**
     * Sets the player's score.
     * 
     * @param score The player's score.
     */
    public void setScore(int score) {
        this.score = score;
    }
    
    @Override
    public String toString() {
        return "Player "+nickname+"["+UID+"] ("+score+"pts)";
    }
    
    
    
}
