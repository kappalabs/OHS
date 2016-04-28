package com.kappa_labs.ohunter.server.net.requests;

import com.kappa_labs.ohunter.lib.entities.Player;
import com.kappa_labs.ohunter.lib.net.OHException;
import com.kappa_labs.ohunter.lib.net.Response;
import com.kappa_labs.ohunter.lib.requests.BlockPlaceRequest;
import com.kappa_labs.ohunter.server.database.DatabaseService;

/**
 * Implementation of the BlockPlaceRequest from the OHL.
 */
public class BlockPlaceRequester extends BlockPlaceRequest {

    public BlockPlaceRequester(Player player, String placeID) {
        super(player, placeID);
    }

    public BlockPlaceRequester(BlockPlaceRequest request) {
        super(request);
    }

    @Override
    public Response execute() throws OHException {
        DatabaseService ds = new DatabaseService();
        ds.blockPlace(placeID);
        Response response = new Response(player);

        return response;
    }

}
