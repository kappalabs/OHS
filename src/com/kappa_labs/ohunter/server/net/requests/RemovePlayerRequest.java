
package com.kappa_labs.ohunter.server.net.requests;

import com.kappa_labs.ohunter.server.database.DatabaseService;
import com.kappa_labs.ohunter.server.entities.Player;
import com.kappa_labs.ohunter.server.net.OHException;
import com.kappa_labs.ohunter.server.net.Response;

/**
 * Request to remove player from database.
 */
public class RemovePlayerRequest extends Request {

    /**
     * Creates a new request to remove given player from the database.
     * 
     * @param player Player, who should be removed from the database.
     */
    public RemovePlayerRequest(Player player) {
        this.player = player;
    }
    
    @Override
    public int getID() {
        return Request.REMOVE_PLAYER;
    }
    
    @Override
    public Response execute() throws OHException {
        DatabaseService ds = new DatabaseService();
        ds.removePlayer(player);
        
        return null;
    }

}
