
package com.kappa_labs.ohunter.server.net.requests;

import com.kappa_labs.ohunter.entities.Photo;
import com.kappa_labs.ohunter.entities.Player;
import com.kappa_labs.ohunter.requests.Request;
import com.kappa_labs.ohunter.requests.RequestPkg;


/**
 * Factory to create Request class from recieved data from client.
 */
public class RequestFactory {

    /**
     * Creates appropriate class from given class skeleten from client.
     * @param rpkg Recieved Request package with data for construction.
     * @return Request if possible (data package is complete and consitent), null otherwise.
     */
    public static Request buildRequest(RequestPkg rpkg) {
        switch (rpkg.getType()) {
            case LOGIN:
                return new LoginRequest(
                        rpkg.getParam(0, String.class), rpkg.getParam(1, String.class));
            case REGISTER:
                return new RegisterRequest(
                        rpkg.getParam(0, String.class), rpkg.getParam(1, String.class));
            case COMPARE:
                return new CompareRequest(
                        rpkg.getParam(0, Player.class),
                        rpkg.getParam(1, Photo.class), rpkg.getParam(2, Photo.class));
            case SEARCH:
                return new SearchRequest(
                        rpkg.getParam(0, Player.class), rpkg.getParam(1, double.class),
                        rpkg.getParam(2, double.class), rpkg.getParam(3, int.class),
                        rpkg.getParam(4, int.class), rpkg.getParam(5, int.class));
            case REMOVE_PLAYER:
                return new RemovePlayerRequest(
                        rpkg.getParam(0, Player.class));
            case RESET_PLAYER:
                return new ResetPlayerRequest(
                        rpkg.getParam(0, Player.class));
            case UPDATE_PLAYER:
                return new UpdatePlayerRequest(
                        rpkg.getParam(0, Player.class));
            case CHANGE_PASSWORD:
                return new ChangePasswordRequest(rpkg.getParam(0, Player.class),
                        rpkg.getParam(1, String.class), rpkg.getParam(2, String.class));
            case COMPLETE_PLACE:
                return new CompletePlaceRequest(
                        rpkg.getParam(0, Player.class), rpkg.getParam(1, String.class));
            case REJECT_PLACE:
                return new RejectPlaceRequest(
                        rpkg.getParam(0, Player.class), rpkg.getParam(1, String.class));
            case BLOCK_PLACE:
                return new BlockPlaceRequest(
                        rpkg.getParam(0, Player.class), rpkg.getParam(1, String.class));
            default:
                return null;
        }
    }
}
