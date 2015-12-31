
package com.kappa_labs.ohunter.server.net.requests;

import com.kappa_labs.ohunter.server.database.DatabaseService;
import com.kappa_labs.ohunter.server.google_api.PlacesGetter;
import com.kappa_labs.ohunter.entities.Photo;
import com.kappa_labs.ohunter.entities.Place;
import com.kappa_labs.ohunter.entities.Player;
import java.util.ArrayList;
import java.util.stream.Collectors;
import com.kappa_labs.ohunter.net.OHException;
import com.kappa_labs.ohunter.net.Response;


public class SearchRequest extends com.kappa_labs.ohunter.requests.SearchRequest {

    public SearchRequest(Player player, double lat, double lng, int radius, int width, int height) {
        super(player, lat, lng, radius, width, height);
    }

    @Override
    public Response execute() throws OHException {
        /* Retrieve all possible places */
        ArrayList<Place> all_places;
        all_places = PlacesGetter.radarSearch(lat, lng, radius, "", TYPES);
        
        DatabaseService ds = new DatabaseService();
        /* Turn them to Photo objects, filter blocked and rejected */
        all_places = all_places.stream().filter((Place place) -> {
            try {
                return !ds.isCompleted(player, place.place_id)
                        && !ds.isBlocked(place.place_id)
                        && !ds.isRejected(player, place.place_id);
            } catch (OHException ex) {
//                    throw new OHException(keyWord); //NOTE: nelze z lambdy, nutno pres RuntimeEx
                throw new RuntimeException(ex);
            }
        }).collect(Collectors.toCollection(ArrayList::new));
        
        ArrayList<Place> places = new ArrayList<>();
        all_places.stream().forEach((place) -> {
            ArrayList<Photo> photos = PlacesGetter.details(place.place_id);
            if (photos == null) {
                return;
            }
            place.photos = photos;
            photos.stream().forEach((photo) -> {
                PlacesGetter.photoRequest(photo, width, height);
            });
            
            places.add(place);
        });
        
        Response response = new Response(uid);
        response.places = places;
        
        return response;
    }

}
