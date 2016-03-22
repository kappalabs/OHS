
package com.kappa_labs.ohunter.server.net.requests;

import com.kappa_labs.ohunter.lib.entities.Player;
import com.kappa_labs.ohunter.lib.net.OHException;
import com.kappa_labs.ohunter.lib.net.Response;
import com.kappa_labs.ohunter.lib.requests.Request;
import com.kappa_labs.ohunter.server.database.DatabaseService;


public class CompletePlaceRequest extends com.kappa_labs.ohunter.lib.requests.CompletePlaceRequest {

    public CompletePlaceRequest(Player player, String placeID, String photoReference) {
        super(player, placeID, photoReference);
    }
    
    public CompletePlaceRequest(Request r) {
        super((com.kappa_labs.ohunter.lib.requests.CompletePlaceRequest) r);
    }
    
    @Override
    public Response execute() throws OHException {
        DatabaseService ds = new DatabaseService();
        ds.completePlace(player, placeID, photoReference, timestamp);
        Response response = new Response(player);
        
        return response;
    }

}
