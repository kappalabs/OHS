
package com.kappa_labs.ohunter.server.net.requests;

import com.kappa_labs.ohunter.entities.Player;
import com.kappa_labs.ohunter.net.OHException;
import com.kappa_labs.ohunter.net.Response;
import com.kappa_labs.ohunter.server.database.DatabaseService;


public class ChangePasswordRequest extends com.kappa_labs.ohunter.requests.ChangePasswordRequest {

    public ChangePasswordRequest(Player player, String oldPassword, String password) {
        super(player, oldPassword, password);
    }
    
    @Override
    public Response execute() throws OHException {
        DatabaseService ds = new DatabaseService();
        ds.changePassword(player, oldPassword, password);
        Response response = new Response(player);
        
        return response;
    }
    
}
