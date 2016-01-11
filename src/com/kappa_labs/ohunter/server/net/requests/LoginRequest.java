
package com.kappa_labs.ohunter.server.net.requests;

import com.kappa_labs.ohunter.lib.net.OHException;
import com.kappa_labs.ohunter.lib.net.Response;
import com.kappa_labs.ohunter.lib.requests.Request;
import com.kappa_labs.ohunter.server.database.DatabaseService;


public class LoginRequest extends com.kappa_labs.ohunter.lib.requests.LoginRequest {

    public LoginRequest(String nickname, String password) {
        super(nickname, password);
    }
    
    public LoginRequest(Request r) {
        super((com.kappa_labs.ohunter.lib.requests.LoginRequest) r);
    }
    
    @Override
    public Response execute() throws OHException {
        DatabaseService ds = new DatabaseService();
        player = ds.loginPlayer(nickname, password);
        Response response = new Response(player);
        
        return response;
    }
    
}
