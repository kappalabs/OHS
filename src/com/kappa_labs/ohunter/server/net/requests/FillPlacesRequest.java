
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
    private static final int NUM_THREADS = 128;

    
    public FillPlacesRequest(Player player, String[] placeIDs, Photo.DAYTIME daytime, int width, int height) {
        super(player, placeIDs, daytime, width, height);
    }

    public FillPlacesRequest(Request request) {
        super((com.kappa_labs.ohunter.lib.requests.FillPlacesRequest) request);
    }

    @Override
    public Response execute() throws OHException {
        ArrayList<Place> all_places = new ArrayList<>();
        for (String placeID : placeIDs) {
            Place place = new Place();
            place.gfields.put("place_id", placeID);
            all_places.add(place);
        }
        
        /* Parallel download of the Place Details and Photos */
        ArrayList<Place> places = new ArrayList<>();
        ExecutorService executor = Executors.newFixedThreadPool(NUM_THREADS);
        all_places.stream().forEach((place) -> {
            executor.execute(new PlaceFiller(place, places, width, height));
        });
        executor.shutdown();
        try {
            executor.awaitTermination(1, TimeUnit.MINUTES);
        } catch (InterruptedException ex) {
            Logger.getLogger(SearchRequest.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        /* Store the data in Response object */
        Response response = new Response(uid);
        response.places = places.toArray(new Place[0]);
        
        System.out.println("SearchRequest: I've prepared " + places.size() + " Places.");
         
        return response;
    }

}
