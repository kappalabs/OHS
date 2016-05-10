package com.kappa_labs.ohunter.server.net.requests;

import com.kappa_labs.ohunter.lib.net.OHException;
import com.kappa_labs.ohunter.lib.net.Response;
import com.kappa_labs.ohunter.lib.requests.CompleteTargetRequest;
import com.kappa_labs.ohunter.server.database.DatabaseService;

/**
 * Implementation of the CompleteTargetRequest from the OHL.
 */
public class CompleteTargetRequester extends CompleteTargetRequest {

    public CompleteTargetRequester(CompleteTargetRequest request) {
        super(request);
    }

    @Override
    public Response execute() throws OHException {
        DatabaseService ds = new DatabaseService();
        ds.completeTarget(player, placeID, photoReference, timestamp, discoveryGain, similarityGain, huntNumber);
        /* We also need to add some points to this player */
        int gain = discoveryGain + similarityGain;
        if (gain != 0) {
            player.addScore(gain);
            ds.updatePlayer(player);
        }
        Response response = new Response(player);

        return response;
    }

}
