
package com.kappa_labs.ohunter.server.net.requests;

import com.kappa_labs.ohunter.lib.entities.Photo;
import com.kappa_labs.ohunter.lib.entities.Place;
import com.kappa_labs.ohunter.lib.entities.Player;
import com.kappa_labs.ohunter.lib.net.OHException;
import com.kappa_labs.ohunter.lib.net.Response;
import com.kappa_labs.ohunter.lib.requests.Request;
import com.kappa_labs.ohunter.server.utils.PlaceFiller;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;


public class FillPlacesRequest extends com.kappa_labs.ohunter.lib.requests.FillPlacesRequest {

    /**
     * Number of threads allowed for PlaceWorkers thread pool.
     */
    private static final int NUM_THREADS = 256;

    
    public FillPlacesRequest(Player player, String[] placeIDs, Photo.DAYTIME daytime, int width, int height) {
        super(player, placeIDs, daytime, width, height);
    }

    public FillPlacesRequest(Request request) {
        super((com.kappa_labs.ohunter.lib.requests.FillPlacesRequest) request);
    }

    @Override
    public Response execute() throws OHException {
        /* Parallel download of the Place Details and Photos */
        ArrayList<Place> filledPlaces = new ArrayList<>();
        ExecutorService executor = Executors.newFixedThreadPool(NUM_THREADS);
        for (Place place : places) {
            executor.execute(new PlaceFiller(place, filledPlaces, width, height));
        }
        executor.shutdown();
        try {
            executor.awaitTermination(1, TimeUnit.MINUTES);
        } catch (InterruptedException ex) {
            Logger.getLogger(SearchRequest.class.getName()).log(Level.WARNING, null, ex);
        }
        
        /* Store the data in Response object */
        Response response = new Response(uid);
        response.places = filledPlaces.toArray(new Place[0]);
        
        System.out.println("FillPlacesRequest: prepared " + filledPlaces.size() + " Places.");
         
        return response;
    }

}
