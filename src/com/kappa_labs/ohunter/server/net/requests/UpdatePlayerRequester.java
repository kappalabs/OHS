package com.kappa_labs.ohunter.server.net.requests;

import com.kappa_labs.ohunter.lib.entities.Player;
import com.kappa_labs.ohunter.server.database.DatabaseService;
import com.kappa_labs.ohunter.lib.net.OHException;
import com.kappa_labs.ohunter.lib.net.Response;
import com.kappa_labs.ohunter.lib.requests.UpdatePlayerRequest;

public class UpdatePlayerRequester extends com.kappa_labs.ohunter.lib.requests.UpdatePlayerRequest {

    public UpdatePlayerRequester(Player player) {
        super(player);
    }

    public UpdatePlayerRequester(UpdatePlayerRequest request) {
        super(request);
    }

    @Override
    public Response execute() throws OHException {
        DatabaseService ds = new DatabaseService();
        ds.updatePlayer(player);
        Response response = new Response(player);

        return response;
    }

}
