package com.kappa_labs.ohunter.server.net.requests;

import com.kappa_labs.ohunter.server.database.DatabaseService;
import com.kappa_labs.ohunter.server.google_api.PlacesGetter;
import com.kappa_labs.ohunter.lib.entities.Place;
import java.util.ArrayList;
import java.util.stream.Collectors;
import com.kappa_labs.ohunter.lib.net.OHException;
import com.kappa_labs.ohunter.lib.net.Response;
import com.kappa_labs.ohunter.lib.requests.SearchRequest;
import static com.kappa_labs.ohunter.server.net.requests.RadarSearchRequester.TYPES;
import com.kappa_labs.ohunter.server.utils.PlaceFiller;
import com.kappa_labs.ohunter.server.utils.SettingsManager;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Implementation of the SearchRequest from the OHL.
 */
public class SearchRequester extends SearchRequest {

    /**
     * Maximum number of places that will be send to the client.
     */
    private static final int MAX_PLACES = 30;

    
    public SearchRequester(SearchRequest request) {
        super(request);
    }

    @Override
    public Response execute() throws OHException {
        Logger.getLogger(SearchRequester.class.getName()).log(Level.FINE,
                "SearchRequest on [{0}; {1}]; radius = {2}", new Object[]{latitude, longitude, radius});
        /* Retrieve all possible places */
        List<Place> all_places;
        all_places = PlacesGetter.radarSearch(latitude, longitude, radius, "", TYPES);

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
        List<Place> places = new ArrayList<>();
        ExecutorService executor = Executors.newFixedThreadPool(SettingsManager.getFillPoolFillerThreadsNumber());
        for (Place place : all_places) {
            executor.execute(new PlaceFiller(place, places, width, height,
                    daytime, SettingsManager.getFillPoolPhotoThreadsNumber()));
        }
        executor.shutdown();
        try {
            executor.awaitTermination(SettingsManager.getFillPoolMaxWaitTime(), TimeUnit.MINUTES);
        } catch (InterruptedException ex) {
            Logger.getLogger(SearchRequester.class.getName()).log(Level.SEVERE, null, ex);
        }

        /* Store the data in Response object */
        Response response = new Response(uid);
        response.places = places.toArray(new Place[0]);

        Logger.getLogger(SearchRequester.class.getName()).log(Level.FINE,
                "SearchRequest: prepared {0} Places.", places.size());

        return response;
    }

}
