package com.kappa_labs.ohunter.server.net.requests;

import com.kappa_labs.ohunter.lib.entities.Place;
import com.kappa_labs.ohunter.lib.net.OHException;
import com.kappa_labs.ohunter.lib.net.Response;
import com.kappa_labs.ohunter.lib.requests.FillPlacesRequest;
import com.kappa_labs.ohunter.server.utils.PlaceFiller;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Implementation of the FillPlacesRequest from the OHL.
 */
public class FillPlacesRequester extends FillPlacesRequest {

    /**
     * Number of minutes to wait for thread termination.
     */
    private final int MAX_WAIT_TIME = 1;
    /**
     * Number of threads allowed for PlaceFiller thread pool.
     */
    private static final int NUM_FILLER_THREADS = 32;
    /**
     * Number of threads allowed to retrieve photos for each place.
     */
    private static final int NUM_PHOTO_THREADS = 10;


    public FillPlacesRequester(FillPlacesRequest request) {
        super(request);
    }

    @Override
    public Response execute() throws OHException {
        /* Parallel download of the Place Details and Photos */
        List<Place> filledPlaces = new ArrayList<>();
        ExecutorService executor = Executors.newFixedThreadPool(NUM_FILLER_THREADS);
        for (Place place : places) {
            executor.execute(new PlaceFiller(place, filledPlaces, width, height, daytime, NUM_PHOTO_THREADS));
        }
        executor.shutdown();
        try {
            executor.awaitTermination(MAX_WAIT_TIME, TimeUnit.MINUTES);
        } catch (InterruptedException ex) {
            Logger.getLogger(SearchRequester.class.getName()).log(Level.WARNING, null, ex);
        }

        /* Store the data in Response object */
        Response response = new Response(uid);
        response.places = filledPlaces.toArray(new Place[0]);

        System.out.println("FillPlacesRequest: prepared " + filledPlaces.size() + " Places.");

        return response;
    }

}
