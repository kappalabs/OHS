package com.kappa_labs.ohunter.server.net.requests;

import com.kappa_labs.ohunter.lib.entities.Player;
import com.kappa_labs.ohunter.lib.net.OHException;
import com.kappa_labs.ohunter.lib.net.Response;
import com.kappa_labs.ohunter.lib.requests.RemovePlayerRequest;
import com.kappa_labs.ohunter.server.database.DatabaseService;

/**
 * Implementation of the RemovePlayerRequest from the OHL.
 */
public class RemovePlayerRequester extends RemovePlayerRequest {

    public RemovePlayerRequester(Player player) {
        super(player);
    }

    public RemovePlayerRequester(RemovePlayerRequest request) {
        super(request);
    }

    @Override
    public Response execute() throws OHException {
        DatabaseService ds = new DatabaseService();
        ds.removePlayer(player);

        return null;
    }

}
