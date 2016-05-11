package com.kappa_labs.ohunter.server.net.requests;

import com.kappa_labs.ohunter.lib.entities.Place;
import com.kappa_labs.ohunter.lib.net.OHException;
import com.kappa_labs.ohunter.lib.net.Response;
import com.kappa_labs.ohunter.lib.requests.FillPlacesRequest;
import com.kappa_labs.ohunter.server.utils.PlaceFiller;
import com.kappa_labs.ohunter.server.utils.SettingsManager;
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
    
    private final SettingsManager settingsManager = SettingsManager.getInstance();

    
    public FillPlacesRequester(FillPlacesRequest request) {
        super(request);
    }

    @Override
    public Response execute() throws OHException {
        /* Parallel download of the Place Details and Photos */
        List<Place> filledPlaces = new ArrayList<>();
        ExecutorService executor = Executors.newFixedThreadPool(settingsManager.getFillPoolFillerThreadsNumber());
        for (Place place : places) {
            executor.execute(new PlaceFiller(place, filledPlaces, width, height,
                    daytime, settingsManager.getFillPoolPhotoThreadsNumber()));
        }
        executor.shutdown();
        try {
            executor.awaitTermination(settingsManager.getFillPoolMaxWaitTime(), TimeUnit.MINUTES);
        } catch (InterruptedException ex) {
            Logger.getLogger(SearchRequester.class.getName()).log(Level.WARNING, null, ex);
        }

        /* Store the data in Response object */
        Response response = new Response(uid);
        response.places = filledPlaces.toArray(new Place[0]);

        Logger.getLogger(FillPlacesRequester.class.getName()).log(Level.FINE,
                "Prepared {0} Places.", filledPlaces.size());

        return response;
    }

}
