
package com.kappa_labs.ohunter.server.net.requests;

import com.kappa_labs.ohunter.lib.entities.Player;
import com.kappa_labs.ohunter.lib.net.OHException;
import com.kappa_labs.ohunter.lib.net.Response;
import com.kappa_labs.ohunter.lib.requests.ResetPlayerRequest;
import com.kappa_labs.ohunter.server.database.DatabaseService;


public class ResetPlayerRequester extends com.kappa_labs.ohunter.lib.requests.ResetPlayerRequest {

    public ResetPlayerRequester(Player player) {
        super(player);
    }
    
    public ResetPlayerRequester(ResetPlayerRequest request) {
        super(request);
    }

    @Override
    public Response execute() throws OHException {
        DatabaseService ds = new DatabaseService();
        ds.resetPlayer(player);
        Response response = new Response(player);
        
        return response;
    }

}
