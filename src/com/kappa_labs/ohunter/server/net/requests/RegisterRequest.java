
package com.kappa_labs.ohunter.server.net.requests;

import com.kappa_labs.ohunter.server.database.DatabaseService;
import com.kappa_labs.ohunter.server.net.OHException;
import com.kappa_labs.ohunter.server.net.Response;

/**
 * Request to register a new player.
 */
public class RegisterRequest extends Request {
    
    private String nickname;
    private String password;

    
    /**
     * Creates a new request to register a new player with given nickname
     * and password hash.
     * 
     * @param nickname The nickname of the player.
     * @param password The password hash.
     */
    public RegisterRequest(String nickname, String password) {
        this.nickname = nickname;
        this.password = password;
    }
    
    @Override
    public int getID() {
        return Request.REGISTER;
    }

    @Override
    public Response execute() throws OHException {
        DatabaseService ds = new DatabaseService();
        player = ds.registerPlayer(nickname, password);
        Response response = new Response(player);
        
        return response;
    }

}
