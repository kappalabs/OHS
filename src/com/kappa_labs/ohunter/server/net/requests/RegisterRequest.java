
package com.kappa_labs.ohunter.server.net.requests;

import com.kappa_labs.ohunter.server.database.DatabaseService;
import com.kappa_labs.ohunter.net.OHException;
import com.kappa_labs.ohunter.net.Response;


public class RegisterRequest extends com.kappa_labs.ohunter.requests.RegisterRequest {

    public RegisterRequest(String nickname, String password) {
        super(nickname, password);
    }
    
    @Override
    public Response execute() throws OHException {
        DatabaseService ds = new DatabaseService();
        player = ds.registerPlayer(nickname, password);
        Response response = new Response(player);
        
        return response;
    }

}
