
package com.kappa_labs.ohunter.server.net.requests;

import com.kappa_labs.ohunter.lib.entities.Player;
import com.kappa_labs.ohunter.lib.net.OHException;
import com.kappa_labs.ohunter.lib.net.Response;
import com.kappa_labs.ohunter.lib.requests.Request;
import com.kappa_labs.ohunter.server.database.DatabaseService;


public class RemovePlayerRequest extends com.kappa_labs.ohunter.lib.requests.RemovePlayerRequest {

    public RemovePlayerRequest(Player player) {
        super(player);
    }
    
    public RemovePlayerRequest(Request r) {
        super((com.kappa_labs.ohunter.lib.requests.RemovePlayerRequest) r);
    }
    
    @Override
    public Response execute() throws OHException {
        DatabaseService ds = new DatabaseService();
        ds.removePlayer(player);
        
        return null;
    }

}
