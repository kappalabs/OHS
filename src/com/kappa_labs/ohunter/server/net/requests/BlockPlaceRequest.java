
package com.kappa_labs.ohunter.server.net.requests;

import com.kappa_labs.ohunter.lib.entities.Player;
import com.kappa_labs.ohunter.lib.net.OHException;
import com.kappa_labs.ohunter.lib.net.Response;
import com.kappa_labs.ohunter.lib.requests.Request;
import com.kappa_labs.ohunter.server.database.DatabaseService;


public class BlockPlaceRequest extends com.kappa_labs.ohunter.lib.requests.BlockPlaceRequest {

    public BlockPlaceRequest(Player player, String placeID) {
        super(player, placeID);
    }
    
    public BlockPlaceRequest(Request r) {
        super((com.kappa_labs.ohunter.lib.requests.BlockPlaceRequest) r);
    }
    
    @Override
    public Response execute() throws OHException {
        DatabaseService ds = new DatabaseService();
        ds.blockPlace(placeID);
        Response response = new Response(player);
        
        return response;
    }

}
