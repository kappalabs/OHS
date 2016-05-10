package com.kappa_labs.ohunter.server.net.requests;

import com.kappa_labs.ohunter.lib.entities.Player;
import com.kappa_labs.ohunter.lib.net.OHException;
import com.kappa_labs.ohunter.lib.net.Response;
import com.kappa_labs.ohunter.lib.requests.LoginRequest;
import com.kappa_labs.ohunter.server.database.DatabaseService;

/**
 * Implementation of the LoginRequest from the OHL.
 */
public class LoginRequester extends LoginRequest {

    public LoginRequester(LoginRequest request) {
        super(request);
    }

    @Override
    public Response execute() throws OHException {
        DatabaseService ds = new DatabaseService();
        Player logPlayer = ds.loginPlayer(nickname, password);
        Response response = new Response(logPlayer);

        return response;
    }

}
