package com.kappa_labs.ohunter.server.net.requests;

import com.kappa_labs.ohunter.server.database.DatabaseService;
import com.kappa_labs.ohunter.server.google_api.PlacesCommunicator;
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
    
    private final SettingsManager settingsManager = SettingsManager.getInstance();

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
        List<Place> allPlaces = PlacesCommunicator.radarSearch(latitude, longitude, radius, "", TYPES);

        /* Filter completed, blocked and rejected ones */
        DatabaseService ds = new DatabaseService();
        try {
            allPlaces = allPlaces.stream().filter((Place place) -> {
                try {
                    return !ds.isCompleted(player, place.getID())
                            && !ds.isBlocked(place.getID())
                            && !ds.isRejected(player, place.getID());
                } catch (OHException ex) {
                    /* NOTE: Cannot throw new OHException(keyWord) from lambda directly */
                    throw new RuntimeException(ex);
                }
            }).collect(Collectors.toCollection(ArrayList::new));
        } catch (RuntimeException ex) {
            if (ex.getCause() instanceof OHException) {
                throw (OHException) ex.getCause();
            }
        }

        /* Reduction of the number of places */
        int size = allPlaces.size();
        int i = size;
        while (--i >= Math.min(size, MAX_PLACES)) {
            allPlaces.remove(i);
        }

        /* Parallel download of the Place Details and Photos */
        List<Place> places = new ArrayList<>();
        ExecutorService executor = Executors.newFixedThreadPool(settingsManager.getFillPoolFillerThreadsNumber());
        for (Place place : allPlaces) {
            executor.submit(new PlaceFiller(place, places, width, height,
                    daytime, settingsManager.getFillPoolPhotoThreadsNumber()));
        }
        executor.shutdown();
        try {
            executor.awaitTermination(settingsManager.getFillPoolMaxWaitTime(), TimeUnit.MINUTES);
        } catch (InterruptedException ex) {
            Logger.getLogger(SearchRequester.class.getName()).log(Level.SEVERE, null, ex);
            throw new OHException("Server too busy now!", OHException.EXType.SERVER_OCCUPIED);
        }

        /* Store the data in Response object */
        Response response = new Response(player);
        response.places = places.toArray(new Place[0]);

        Logger.getLogger(SearchRequester.class.getName()).log(Level.FINE,
                "SearchRequest: prepared {0} Places.", places.size());

        return response;
    }

}
