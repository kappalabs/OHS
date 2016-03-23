package com.kappa_labs.ohunter.server.net.requests;

import com.kappa_labs.ohunter.lib.entities.Player;
import com.kappa_labs.ohunter.server.database.DatabaseService;
import com.kappa_labs.ohunter.lib.net.OHException;
import com.kappa_labs.ohunter.lib.net.Response;
import com.kappa_labs.ohunter.lib.requests.RegisterRequest;

public class RegisterRequester extends com.kappa_labs.ohunter.lib.requests.RegisterRequest {

    public RegisterRequester(String nickname, String password) {
        super(nickname, password);
    }

    public RegisterRequester(RegisterRequest request) {
        super(request);
    }

    @Override
    public Response execute() throws OHException {
        DatabaseService ds = new DatabaseService();
        Player regPlayer = ds.registerPlayer(nickname, password);
        Response response = new Response(regPlayer);

        return response;
    }

}
