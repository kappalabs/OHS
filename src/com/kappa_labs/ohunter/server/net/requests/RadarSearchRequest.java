
package com.kappa_labs.ohunter.server.net.requests;

import com.kappa_labs.ohunter.lib.entities.Place;
import com.kappa_labs.ohunter.lib.entities.Player;
import com.kappa_labs.ohunter.lib.net.OHException;
import com.kappa_labs.ohunter.lib.net.Response;
import com.kappa_labs.ohunter.lib.requests.Request;
import com.kappa_labs.ohunter.server.database.DatabaseService;
import com.kappa_labs.ohunter.server.google_api.PlacesGetter;
import java.util.ArrayList;
import java.util.stream.Collectors;


public class RadarSearchRequest extends com.kappa_labs.ohunter.lib.requests.RadarSearchRequest {

    public RadarSearchRequest(Player player, double lat, double lng, int radius) {
        super(player, lat, lng, radius);
    }

    public RadarSearchRequest(Request request) {
        super((com.kappa_labs.ohunter.lib.requests.RadarSearchRequest) request);
    }

    @Override
    public Response execute() throws OHException {
        System.out.println("RadarSearchRequest on [" + lat + "; " + lng + "]; radius = " + radius);
        /* Retrieve all possible places */
        ArrayList<Place> places = PlacesGetter.radarSearch(lat, lng, radius, "", TYPES);
        
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
        
        System.out.println("RadarSearchRequest: prepared " + places.size() + " Places.");
        
        return response;
    }

}
