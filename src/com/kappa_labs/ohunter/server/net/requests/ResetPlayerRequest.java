
package com.kappa_labs.ohunter.server.net.requests;

import com.kappa_labs.ohunter.entities.Player;
import com.kappa_labs.ohunter.net.OHException;
import com.kappa_labs.ohunter.net.Response;
import com.kappa_labs.ohunter.server.database.DatabaseService;


public class ResetPlayerRequest extends com.kappa_labs.ohunter.requests.ResetPlayerRequest {

    public ResetPlayerRequest(Player player) {
        super(player);
    }

    @Override
    public Response execute() throws OHException {
        DatabaseService ds = new DatabaseService();
        ds.resetPlayer(player);
        Response response = new Response(player);
        
        return response;
    }

}
