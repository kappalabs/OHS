package com.kappa_labs.ohunter.server.net.requests;

import com.kappa_labs.ohunter.lib.entities.Player;
import com.kappa_labs.ohunter.lib.net.OHException;
import com.kappa_labs.ohunter.lib.net.Response;
import com.kappa_labs.ohunter.lib.requests.CompletePlaceRequest;
import com.kappa_labs.ohunter.server.database.DatabaseService;

public class CompletePlaceRequester extends com.kappa_labs.ohunter.lib.requests.CompletePlaceRequest {

    public CompletePlaceRequester(Player player, String placeID, String photoReference) {
        super(player, placeID, photoReference);
    }

    public CompletePlaceRequester(Player player, String placeID, String photoReference, int gain) {
        super(player, placeID, photoReference, gain);
    }

    public CompletePlaceRequester(CompletePlaceRequest request) {
        super(request);
    }

    @Override
    public Response execute() throws OHException {
        DatabaseService ds = new DatabaseService();
        ds.completePlace(player, placeID, photoReference, timestamp);
        /* We also need to add some points to this player */
        if (gain != 0) {
            player.addScore(gain);
            ds.updatePlayer(player);
        }
        Response response = new Response(player);

        return response;
    }

}
