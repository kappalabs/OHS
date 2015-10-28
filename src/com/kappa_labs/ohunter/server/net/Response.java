
package com.kappa_labs.ohunter.server.net;

import com.kappa_labs.ohunter.server.google_api.Place;
import com.kappa_labs.ohunter.server.entities.Player;
import java.util.ArrayList;

/**
 * Wrapper class for the response to previous Request.
 */
public class Response {
    
    /**
     * Who created the request.
     */
    protected int uid;
    /**
     * Time of creation of the request.
     */
    protected long time;
    
    //NOTE: supported data objects
    public Player player;
    public float similarity;
    public ArrayList<Place> places;
    
    
    /**
     * Create a new response by specifiing the reciever of it.
     * 
     * @param uid The reciever player ID, who requested the command.
     */
    public Response(int uid) {
        this.uid = uid;
        this.time = System.currentTimeMillis();
    }

    /**
     * Create a new response by specifiing the reciever of it.
     * 
     * @param player The reciever player, who requested the command.
     */
    public Response(Player player) {
        this(player.getUID());
        
        this.player = player;
    }
    
    
}
