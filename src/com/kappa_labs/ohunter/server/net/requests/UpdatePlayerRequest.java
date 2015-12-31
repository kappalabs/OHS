
package com.kappa_labs.ohunter.server.net.requests;

import com.kappa_labs.ohunter.entities.Player;
import com.kappa_labs.ohunter.server.database.DatabaseService;
import com.kappa_labs.ohunter.net.OHException;
import com.kappa_labs.ohunter.net.Response;


public class UpdatePlayerRequest extends com.kappa_labs.ohunter.requests.UpdatePlayerRequest {

    public UpdatePlayerRequest(Player player) {
        super(player);
    }

    @Override
    public Response execute() throws OHException {
        DatabaseService ds = new DatabaseService();
        ds.updatePlayer(player);
        Response response = new Response(player);
        
        return response;
    }

}
