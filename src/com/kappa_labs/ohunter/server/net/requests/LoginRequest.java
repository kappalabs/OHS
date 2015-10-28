
package com.kappa_labs.ohunter.server.net.requests;

import com.kappa_labs.ohunter.server.database.DatabaseService;
import com.kappa_labs.ohunter.server.net.OHException;
import com.kappa_labs.ohunter.server.net.Response;

/**
 * Request to login a player into the application.
 */
public class LoginRequest extends Request {
    
    private String nickname;
    private String password;

    
    /**
     * Creates a new request for comparsion between given photos.
     * 
     * @param nickname The nickname of the player.
     * @param password The password hash.
     */
    public LoginRequest(String nickname, String password) {
        this.nickname = nickname;
        this.password = password;
    }
    
    @Override
    public int getID() {
        return Request.LOGIN;
    }
    
    @Override
    public Response execute() throws OHException {
        DatabaseService ds = new DatabaseService();
        player = ds.loginPlayer(nickname, password);
        Response response = new Response(player);
        
        return response;
    }
}
