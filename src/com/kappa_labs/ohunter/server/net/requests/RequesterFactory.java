package com.kappa_labs.ohunter.server.net.requests;

import com.kappa_labs.ohunter.lib.net.OHException;
import com.kappa_labs.ohunter.lib.net.Response;
import com.kappa_labs.ohunter.lib.requests.Request;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Factory to create Requester class, implementing execute() method, from
 * recieved data from client.
 */
public class RequesterFactory {

    /**
     * Creates appropriate class from given class skeleten from client.
     *
     * @param rpkg Recieved Request package with data for construction.
     * @return Request implementing the execute() method (Requester class).
     */
    public static Request buildRequester(Request rpkg) {
        String requesterName = rpkg.getClass().getName() + "er";
        requesterName = requesterName.replace("lib", "server.net");
        try {
            Class<Request> clazz = (Class<Request>) Class.forName(requesterName);
            Constructor<Request> constructor = clazz.getConstructor(rpkg.getClass());
            return constructor.newInstance(rpkg);
        } catch (ClassNotFoundException | NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            Logger.getLogger(RequesterFactory.class.getName()).log(Level.SEVERE, null, ex);
        }
        /* This request is unimplemented (or not registered here) on server */
        return new Request() {
            @Override
            public Response execute() throws OHException {
                System.err.println("Wrong implementation of the class " + rpkg.getClass().getName() + " on the server!");
                return null;
            }
        };
    }

}
