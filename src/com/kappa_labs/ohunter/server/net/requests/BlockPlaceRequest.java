
package com.kappa_labs.ohunter.server.net.requests;

import com.kappa_labs.ohunter.entities.Player;
import com.kappa_labs.ohunter.net.OHException;
import com.kappa_labs.ohunter.net.Response;
import com.kappa_labs.ohunter.server.database.DatabaseService;


public class BlockPlaceRequest extends com.kappa_labs.ohunter.requests.BlockPlaceRequest {

    public BlockPlaceRequest(Player player, String placeKey) {
        super(player, placeKey);
    }
    
    @Override
    public Response execute() throws OHException {
        DatabaseService ds = new DatabaseService();
        ds.blockPlace(placeKey);
        Response response = new Response(player);
        
        return response;
    }

}
