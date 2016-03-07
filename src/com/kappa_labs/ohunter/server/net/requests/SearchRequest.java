
package com.kappa_labs.ohunter.server.net.requests;

import com.kappa_labs.ohunter.server.database.DatabaseService;
import com.kappa_labs.ohunter.server.google_api.PlacesGetter;
import com.kappa_labs.ohunter.lib.entities.Photo;
import com.kappa_labs.ohunter.lib.entities.Place;
import com.kappa_labs.ohunter.lib.entities.Player;
import java.util.ArrayList;
import java.util.stream.Collectors;
import com.kappa_labs.ohunter.lib.net.OHException;
import com.kappa_labs.ohunter.lib.net.Response;
import com.kappa_labs.ohunter.lib.requests.Request;
import static com.kappa_labs.ohunter.lib.requests.RadarSearchRequest.TYPES;
import com.kappa_labs.ohunter.server.utils.PlaceFiller;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;


public class SearchRequest extends com.kappa_labs.ohunter.lib.requests.SearchRequest {
    
    /**
     * Number of threads allowed for PlaceWorkers thread pool.
     */
    private static final int NUM_THREADS = 256;
    /**
     * Maximum number of places that will be send to the client.
     */
    private static final int MAX_PLACES = 30;

    
    public SearchRequest(Player player, double lat, double lng, int radius,
            Photo.DAYTIME daytime, int width, int height) {
        super(player, lat, lng, radius, daytime, width, height);
    }
    
    public SearchRequest(Request r) {
        super((com.kappa_labs.ohunter.lib.requests.SearchRequest) r);
    }

    @Override
    public Response execute() throws OHException {
        System.out.println("SearchRequest on [" + lat + "; " + lng + "]; radius = " + radius);
        /* Retrieve all possible places */
        ArrayList<Place> all_places;
        all_places = PlacesGetter.radarSearch(lat, lng, radius, "", TYPES);
        
        /* Filter completed, blocked and rejected ones */
        DatabaseService ds = new DatabaseService();
        all_places = all_places.stream().filter((Place place) -> {
            try {
                return !ds.isCompleted(player, place.getID())
                        && !ds.isBlocked(place.getID())
                        && !ds.isRejected(player, place.getID());
            } catch (OHException ex) {
                /* NOTE: Cannot throw new OHException(keyWord) from lambda directly */
                throw new RuntimeException(ex);
            }
        }).collect(Collectors.toCollection(ArrayList::new));
        
        /* Reduction of the number of places */
        int size = all_places.size();
        int i = size;
        while (--i >= Math.min(size, MAX_PLACES)) {
            all_places.remove(i);
        }
        
        /* Parallel download of the Place Details and Photos */
        ArrayList<Place> places = new ArrayList<>();
        ExecutorService executor = Executors.newFixedThreadPool(NUM_THREADS);
        for (Place place : all_places) {
            executor.execute(new PlaceFiller(place, places, width, height));
        }
        executor.shutdown();
        try {
            executor.awaitTermination(1, TimeUnit.MINUTES);
        } catch (InterruptedException ex) {
            Logger.getLogger(SearchRequest.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        /* Store the data in Response object */
        Response response = new Response(uid);
        response.places = places.toArray(new Place[0]);
        
        System.out.println("SearchRequest: prepared " + places.size() + " Places.");
         
        return response;
    }
    
}
