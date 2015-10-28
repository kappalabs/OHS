
package com.kappa_labs.ohunter.server.net.requests;

import com.kappa_labs.ohunter.server.database.DatabaseService;
import com.kappa_labs.ohunter.server.entities.Player;
import com.kappa_labs.ohunter.server.net.OHException;
import com.kappa_labs.ohunter.server.net.Response;

/**
 * Request to change password of existing player.
 */
public class ChangePasswordRequest extends Request {
    
    private String nickname;
    private String password;
    private String oldPassword;

    
    /**
     * Creates a new request to change password hash of given
     * player to given password hash.
     * 
     * @param player The Player, whose password will be changed.
     * @param oldPassword
     * @param password The password hash.
     */
    public ChangePasswordRequest(Player player, String oldPassword, String password) {
        this.player = player;
        this.oldPassword = oldPassword;
        this.password = password;
    }

    @Override
    public int getID() {
        return Request.CHANGE_PASSWORD;
    }

    @Override
    public Response execute() throws OHException {
        DatabaseService ds = new DatabaseService();
        ds.changePassword(player, oldPassword, password);
        Response response = new Response(player);
        
        return response;
    }
    
    
}
