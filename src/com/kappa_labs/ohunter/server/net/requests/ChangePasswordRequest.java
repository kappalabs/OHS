
package com.kappa_labs.ohunter.server.net.requests;

import com.kappa_labs.ohunter.lib.entities.Player;
import com.kappa_labs.ohunter.lib.net.OHException;
import com.kappa_labs.ohunter.lib.net.Response;
import com.kappa_labs.ohunter.lib.requests.Request;
import com.kappa_labs.ohunter.server.database.DatabaseService;


public class ChangePasswordRequest extends com.kappa_labs.ohunter.lib.requests.ChangePasswordRequest {

    public ChangePasswordRequest(Player player, String oldPassword, String password) {
        super(player, oldPassword, password);
    }
    
    public ChangePasswordRequest(Request r) {
        super((com.kappa_labs.ohunter.lib.requests.ChangePasswordRequest) r);
    }
    
    @Override
    public Response execute() throws OHException {
        DatabaseService ds = new DatabaseService();
        ds.changePassword(player, oldPassword, password);
        Response response = new Response(player);
        
        return response;
    }
    
}
