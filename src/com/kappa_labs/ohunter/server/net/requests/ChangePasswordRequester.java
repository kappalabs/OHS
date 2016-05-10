package com.kappa_labs.ohunter.server.net.requests;

import com.kappa_labs.ohunter.lib.net.OHException;
import com.kappa_labs.ohunter.lib.net.Response;
import com.kappa_labs.ohunter.lib.requests.ChangePasswordRequest;
import com.kappa_labs.ohunter.server.database.DatabaseService;

/**
 * Implementation of the ChangePasswordRequest from the OHL.
 */
public class ChangePasswordRequester extends ChangePasswordRequest {

    public ChangePasswordRequester(ChangePasswordRequest request) {
        super(request);
    }

    @Override
    public Response execute() throws OHException {
        DatabaseService ds = new DatabaseService();
        ds.changePassword(player, oldPassword, newPassword);
        Response response = new Response(player);

        return response;
    }

}
