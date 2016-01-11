
package com.kappa_labs.ohunter.server.net.requests;

import com.kappa_labs.ohunter.lib.requests.Request;


/**
 * Factory to create Request class from recieved data from client.
 */
public class RequestFactory {

    /**
     * Creates appropriate class from given class skeleten from client.
     * @param rpkg Recieved Request package with data for construction.
     * @return Request if possible (data package is complete and consitent), null otherwise.
     */
    public static Request buildRequest(Request rpkg) {
        if (rpkg instanceof com.kappa_labs.ohunter.lib.requests.BlockPlaceRequest) {
            return new BlockPlaceRequest(rpkg);
        }
        if (rpkg instanceof com.kappa_labs.ohunter.lib.requests.ChangePasswordRequest) {
            return new ChangePasswordRequest(rpkg);
        }
        if (rpkg instanceof com.kappa_labs.ohunter.lib.requests.CompareRequest) {
            return new CompareRequest(rpkg);
        }
        if (rpkg instanceof com.kappa_labs.ohunter.lib.requests.CompletePlaceRequest) {
            return new CompletePlaceRequest(rpkg);
        }
        if (rpkg instanceof com.kappa_labs.ohunter.lib.requests.LoginRequest) {
            return new LoginRequest(rpkg);
        }
        if (rpkg instanceof com.kappa_labs.ohunter.lib.requests.RegisterRequest) {
            return new RegisterRequest(rpkg);
        }
        if (rpkg instanceof com.kappa_labs.ohunter.lib.requests.RejectPlaceRequest) {
            return new RejectPlaceRequest(rpkg);
        }
        if (rpkg instanceof com.kappa_labs.ohunter.lib.requests.RemovePlayerRequest) {
            return new RemovePlayerRequest(rpkg);
        }
        if (rpkg instanceof com.kappa_labs.ohunter.lib.requests.ResetPlayerRequest) {
            return new ResetPlayerRequest(rpkg);
        }
        if (rpkg instanceof com.kappa_labs.ohunter.lib.requests.SearchRequest) {
            return new SearchRequest(rpkg);
        }
        if (rpkg instanceof com.kappa_labs.ohunter.lib.requests.UpdatePlayerRequest) {
            return new UpdatePlayerRequest(rpkg);
        }
        return null;
    }
}
