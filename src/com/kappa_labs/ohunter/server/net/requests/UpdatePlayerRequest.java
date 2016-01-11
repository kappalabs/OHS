
package com.kappa_labs.ohunter.server.net.requests;

import com.kappa_labs.ohunter.lib.entities.Player;
import com.kappa_labs.ohunter.server.database.DatabaseService;
import com.kappa_labs.ohunter.lib.net.OHException;
import com.kappa_labs.ohunter.lib.net.Response;
import com.kappa_labs.ohunter.lib.requests.Request;


public class UpdatePlayerRequest extends com.kappa_labs.ohunter.lib.requests.UpdatePlayerRequest {

    public UpdatePlayerRequest(Player player) {
        super(player);
    }
    
    public UpdatePlayerRequest(Request r) {
        super((com.kappa_labs.ohunter.lib.requests.UpdatePlayerRequest) r);
    }

    @Override
    public Response execute() throws OHException {
        DatabaseService ds = new DatabaseService();
        ds.updatePlayer(player);
        Response response = new Response(player);
        
        return response;
    }

}
