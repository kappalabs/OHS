
package com.kappa_labs.ohunter.server.net.requests;

import com.kappa_labs.ohunter.lib.entities.Player;
import com.kappa_labs.ohunter.lib.net.OHException;
import com.kappa_labs.ohunter.lib.net.Response;
import com.kappa_labs.ohunter.lib.requests.Request;
import com.kappa_labs.ohunter.server.database.DatabaseService;


public class ResetPlayerRequest extends com.kappa_labs.ohunter.lib.requests.ResetPlayerRequest {

    public ResetPlayerRequest(Player player) {
        super(player);
    }
    
    public ResetPlayerRequest(Request r) {
        super((com.kappa_labs.ohunter.lib.requests.ResetPlayerRequest) r);
    }

    @Override
    public Response execute() throws OHException {
        DatabaseService ds = new DatabaseService();
        ds.resetPlayer(player);
        Response response = new Response(player);
        
        return response;
    }

}
