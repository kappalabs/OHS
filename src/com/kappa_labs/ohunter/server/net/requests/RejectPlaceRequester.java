package com.kappa_labs.ohunter.server.net.requests;

import com.kappa_labs.ohunter.lib.net.OHException;
import com.kappa_labs.ohunter.lib.net.Response;
import com.kappa_labs.ohunter.lib.requests.RejectPlaceRequest;
import com.kappa_labs.ohunter.server.database.DatabaseService;

/**
 * Implementation of the RejectPlaceRequest from the OHL.
 */
public class RejectPlaceRequester extends RejectPlaceRequest {

    public RejectPlaceRequester(RejectPlaceRequest request) {
        super(request);
    }

    @Override
    public Response execute() throws OHException {
        DatabaseService ds = new DatabaseService();
        ds.rejectPlace(player, placeID);
        if (loss != 0) {
            player.addScore(-loss);
            ds.updatePlayer(player);
        }
        Response response = new Response(player);

        return response;
    }

}
