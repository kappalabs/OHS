package com.kappa_labs.ohunter.server.net.requests;

import com.kappa_labs.ohunter.lib.entities.Place;
import com.kappa_labs.ohunter.lib.net.OHException;
import com.kappa_labs.ohunter.lib.net.Response;
import com.kappa_labs.ohunter.lib.requests.RadarSearchRequest;
import com.kappa_labs.ohunter.server.database.DatabaseService;
import com.kappa_labs.ohunter.server.google_api.PlacesGetter;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Implementation of the RadarSearchRequest from the OHL.
 */
public class RadarSearchRequester extends RadarSearchRequest {

    /**
     * Types of objects, that will be returned as result photos, supported by
     * Google Place API.
     */
    public static final String TYPES = "university|synagogue|city_hall|church|museum|mosque|";


    public RadarSearchRequester(RadarSearchRequest request) {
        super(request);
    }

    @Override
    public Response execute() throws OHException {
        Logger.getLogger(RadarSearchRequester.class.getName()).log(Level.FINE,
                "RadarSearchRequest on [{0}; {1}]; radius = {2}", new Object[]{latitude, longitude, radius});
        /* Retrieve all possible places */
        List<Place> places = PlacesGetter.radarSearch(latitude, longitude, radius, "", TYPES);

        DatabaseService ds = new DatabaseService();
        /* Turn them to Photo objects, filter blocked and rejected */
        places = places.stream().filter((Place place) -> {
            try {
                return !ds.isCompleted(player, place.getID())
                        && !ds.isBlocked(place.getID())
                        && !ds.isRejected(player, place.getID());
            } catch (OHException ex) {
                /* NOTE: Cannot throw OHException from lambda */
                throw new RuntimeException(ex);
            }
        }).collect(Collectors.toCollection(ArrayList::new));

        /* Store the data in Response object */
        Response response = new Response(uid);
        response.places = places.toArray(new Place[0]);

        Logger.getLogger(RadarSearchRequester.class.getName()).log(Level.FINE,
                "Prepared {0} Places.", places.size());

        return response;
    }

}
