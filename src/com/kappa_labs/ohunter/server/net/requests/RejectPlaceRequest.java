
package com.kappa_labs.ohunter.server.net.requests;

import com.kappa_labs.ohunter.lib.entities.Player;
import com.kappa_labs.ohunter.lib.net.OHException;
import com.kappa_labs.ohunter.lib.net.Response;
import com.kappa_labs.ohunter.lib.requests.Request;
import com.kappa_labs.ohunter.server.database.DatabaseService;


public class RejectPlaceRequest extends com.kappa_labs.ohunter.lib.requests.RejectPlaceRequest {

    public RejectPlaceRequest(Player player, String placeID) {
        super(player, placeID);
    }
    
    public RejectPlaceRequest(Request r) {
        super((com.kappa_labs.ohunter.lib.requests.RejectPlaceRequest) r);
    }
    
    @Override
    public Response execute() throws OHException {
        DatabaseService ds = new DatabaseService();
        ds.rejectPlace(player, placeID);
        Response response = new Response(player);
        
        return response;
    }

}
