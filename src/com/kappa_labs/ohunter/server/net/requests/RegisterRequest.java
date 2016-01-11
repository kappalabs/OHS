
package com.kappa_labs.ohunter.server.net.requests;

import com.kappa_labs.ohunter.server.database.DatabaseService;
import com.kappa_labs.ohunter.lib.net.OHException;
import com.kappa_labs.ohunter.lib.net.Response;
import com.kappa_labs.ohunter.lib.requests.Request;


public class RegisterRequest extends com.kappa_labs.ohunter.lib.requests.RegisterRequest {

    public RegisterRequest(String nickname, String password) {
        super(nickname, password);
    }
    
    public RegisterRequest(Request r) {
        super((com.kappa_labs.ohunter.lib.requests.RegisterRequest) r);
    }
    
    @Override
    public Response execute() throws OHException {
        DatabaseService ds = new DatabaseService();
        player = ds.registerPlayer(nickname, password);
        Response response = new Response(player);
        
        return response;
    }

}
