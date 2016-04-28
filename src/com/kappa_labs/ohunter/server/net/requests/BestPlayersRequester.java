package com.kappa_labs.ohunter.server.net.requests;

import com.kappa_labs.ohunter.lib.entities.Player;
import com.kappa_labs.ohunter.lib.net.OHException;
import com.kappa_labs.ohunter.lib.net.Response;
import com.kappa_labs.ohunter.lib.requests.BestPlayersRequest;
import com.kappa_labs.ohunter.server.database.DatabaseService;

/**
 * Implementation of the BestPlayersRequest from the OHL.
 */
public class BestPlayersRequester extends BestPlayersRequest {

    public BestPlayersRequester(Player requester, int count) {
        super(requester, count);
    }

    public BestPlayersRequester(BestPlayersRequest request) {
        super(request);
    }

    @Override
    public Response execute() throws OHException {
        DatabaseService ds = new DatabaseService();
        Response response = new Response(player);
        response.players = ds.getBestPlayers(count);

        return response;
    }

}
