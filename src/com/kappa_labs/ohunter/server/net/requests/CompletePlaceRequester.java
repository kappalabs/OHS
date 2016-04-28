package com.kappa_labs.ohunter.server.net.requests;

import com.kappa_labs.ohunter.lib.entities.Player;
import com.kappa_labs.ohunter.lib.net.OHException;
import com.kappa_labs.ohunter.lib.net.Response;
import com.kappa_labs.ohunter.lib.requests.CompletePlaceRequest;
import com.kappa_labs.ohunter.server.database.DatabaseService;

/**
 * Implementation of the CompletePlaceRequest from the OHL.
 */
public class CompletePlaceRequester extends CompletePlaceRequest {

    public CompletePlaceRequester(Player player, String placeID, String photoReference) {
        super(player, placeID, photoReference);
    }

    public CompletePlaceRequester(Player player, String placeID, String photoReference, int discoveryGain, int similarityGain) {
        super(player, placeID, photoReference, discoveryGain, similarityGain);
    }

    public CompletePlaceRequester(CompletePlaceRequest request) {
        super(request);
    }

    @Override
    public Response execute() throws OHException {
        DatabaseService ds = new DatabaseService();
        ds.completePlace(player, placeID, photoReference, timestamp);
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
