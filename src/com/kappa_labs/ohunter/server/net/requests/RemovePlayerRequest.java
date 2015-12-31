
package com.kappa_labs.ohunter.server.net.requests;

import com.kappa_labs.ohunter.entities.Player;
import com.kappa_labs.ohunter.net.OHException;
import com.kappa_labs.ohunter.net.Response;
import com.kappa_labs.ohunter.server.database.DatabaseService;


public class RemovePlayerRequest extends com.kappa_labs.ohunter.requests.RemovePlayerRequest {

    public RemovePlayerRequest(Player player) {
        super(player);
    }
    
    @Override
    public Response execute() throws OHException {
        DatabaseService ds = new DatabaseService();
        ds.removePlayer(player);
        
        return null;
    }

}
