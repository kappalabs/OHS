
package com.kappa_labs.ohunter.server.net.requests;

import com.kappa_labs.ohunter.server.database.DatabaseService;
import com.kappa_labs.ohunter.server.entities.Player;
import com.kappa_labs.ohunter.server.net.OHException;
import com.kappa_labs.ohunter.server.net.Response;

/**
 * Request to block a place for all players.
 */
public class BlockPlaceRequest extends Request {
    
    private String placeKey;

    
    /**
     * Creates a new request to block given place for all the players.
     * 
     * @param player The player, who is requesting the blockage.
     * @param placeKey Identifier of the place, that should be blocked.
     */
    public BlockPlaceRequest(Player player, String placeKey) {
        this.player = player;
        this.placeKey = placeKey;
    }

    @Override
    public int getID() {
        return Request.BLOCK_PLACE;
    }

    @Override
    public Response execute() throws OHException {
        DatabaseService ds = new DatabaseService();
        ds.blockPlace(placeKey);
        Response response = new Response(player);
        
        return response;
    }

}
