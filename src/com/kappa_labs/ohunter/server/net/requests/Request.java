
package com.kappa_labs.ohunter.server.net.requests;

import com.kappa_labs.ohunter.server.entities.Player;
import com.kappa_labs.ohunter.server.net.OHException;
import com.kappa_labs.ohunter.server.net.Response;

/**
 * Class to provide interface for command pattern.
 */
abstract public class Request {
    
    //NOTE: mozna nebude potreba - zamysleno k identifikaci requestu
    public static final int UNKNOWN = -1;
    public static final int LOGIN = 1;
    public static final int REGISTER = 2;
    public static final int COMPARE = 3;
    public static final int SEARCH = 4;
    public static final int REMOVE_PLAYER = 5;
    public static final int RESET_PLAYER = 6;
    public static final int UPDATE_PLAYER = 7;
    public static final int CHANGE_PASSWORD = 8;
    public static final int COMPLETE_PLACE = 9;
    public static final int REJECT_PLACE = 10;
    public static final int BLOCK_PLACE = 11;

    /**
     * Who created the request.
     */
    protected int uid;
    /**
     * Time of creation of the request.
     */
    protected long time;
    protected Player player;

    
    /**
     * Creates a new request, makes a timestamp.
     */
    public Request() {
        this.time = System.currentTimeMillis();
    }
    
    /**
     * Gets the ID of request.
     * 
     * @return The ID of request.
     */
    public int getID() {
        return UNKNOWN;
    }
    
    /**
     * Execute the request (done on server side) and return the result.
     * 
     * @return The result of the request command.
     * @throws OHException When error arises during the execution.
     */
    abstract public Response execute() throws OHException;
    
}
