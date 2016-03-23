package com.kappa_labs.ohunter.server.net.requests;

import com.kappa_labs.ohunter.lib.net.OHException;
import com.kappa_labs.ohunter.lib.net.Response;
import com.kappa_labs.ohunter.lib.requests.*;
import com.kappa_labs.ohunter.lib.requests.Request;

/**
 * Factory to create Requester class, implementing execute() method, from
 * recieved data from client.
 */
public class RequesterFactory {

    /**
     * Creates appropriate class from given class skeleten from client.
     *
     * @param rpkg Recieved Request package with data for construction.
     * @return Request if possible (data package is complete and consitent),
     * null otherwise.
     */
    public static Request buildRequester(Request rpkg) {
        if (rpkg instanceof BlockPlaceRequest) {
            return new BlockPlaceRequester((BlockPlaceRequest) rpkg);
        }
        if (rpkg instanceof ChangePasswordRequest) {
            return new ChangePasswordRequester((ChangePasswordRequest) rpkg);
        }
        if (rpkg instanceof CompareRequest) {
            return new CompareRequester((CompareRequest) rpkg);
        }
        if (rpkg instanceof CompletePlaceRequest) {
            return new CompletePlaceRequester((CompletePlaceRequest) rpkg);
        }
        if (rpkg instanceof FillPlacesRequest) {
            return new FillPlacesRequester((FillPlacesRequest) rpkg);
        }
        if (rpkg instanceof LoginRequest) {
            return new LoginRequester((LoginRequest) rpkg);
        }
        if (rpkg instanceof RadarSearchRequest) {
            return new RadarSearchRequester((RadarSearchRequest) rpkg);
        }
        if (rpkg instanceof RegisterRequest) {
            return new RegisterRequester((RegisterRequest) rpkg);
        }
        if (rpkg instanceof RejectPlaceRequest) {
            return new RejectPlaceRequester((RejectPlaceRequest) rpkg);
        }
        if (rpkg instanceof RemovePlayerRequest) {
            return new RemovePlayerRequester((RemovePlayerRequest) rpkg);
        }
        if (rpkg instanceof ResetPlayerRequest) {
            return new ResetPlayerRequester((ResetPlayerRequest) rpkg);
        }
        if (rpkg instanceof SearchRequest) {
            return new SearchRequester((SearchRequest) rpkg);
        }
        if (rpkg instanceof UpdatePlayerRequest) {
            return new UpdatePlayerRequester((UpdatePlayerRequest) rpkg);
        }
        if (rpkg == null) {
            return new Request() {
                @Override
                public Response execute() throws OHException {
                    throw new OHException("Serialization version is incompatible!",
                            OHException.EXType.SERIALIZATION_INCOMPATIBLE);
                }
            };
        }
        /* This request is unimplemented (or not registered here) on server */
        return new Request() {
        };
    }

}
