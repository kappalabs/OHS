
package com.kappa_labs.ohunter.server.net.requests;

import com.kappa_labs.ohunter.server.database.DatabaseService;
import com.kappa_labs.ohunter.server.entities.Player;
import com.kappa_labs.ohunter.server.net.OHException;
import com.kappa_labs.ohunter.server.net.Response;

/**
 * Request to reject place for given player.
 */
public class RejectPlaceRequest extends Request {
    
    private String placeKey;
    
    
    /**
     * Creates a new request to reject given place, specified by place key
     * for given player.
     * 
     * @param player Player, who is rejecting the place.
     * @param placeKey The place key, that identifies the place to be rejected.
     */
    public RejectPlaceRequest(Player player, String placeKey) {
        this.player = player;
        this.placeKey = placeKey;
    }

    @Override
    public int getID() {
        return Request.REJECT_PLACE;
    }

    @Override
    public Response execute() throws OHException {
        DatabaseService ds = new DatabaseService();
        ds.rejectPlace(player, placeKey);
        Response response = new Response(player);
        
        return response;
    }

}
