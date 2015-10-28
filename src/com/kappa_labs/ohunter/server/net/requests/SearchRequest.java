
package com.kappa_labs.ohunter.server.net.requests;

import com.kappa_labs.ohunter.server.google_api.Photo;
import com.kappa_labs.ohunter.server.google_api.Place;
import com.kappa_labs.ohunter.server.google_api.PlacesGetter;
import com.kappa_labs.ohunter.server.database.DatabaseService;
import com.kappa_labs.ohunter.server.entities.Player;
import com.kappa_labs.ohunter.server.net.OHException;
import com.kappa_labs.ohunter.server.net.Response;
import java.util.ArrayList;
import java.util.stream.Collectors;

/**
 * Request to make a search and return places for given area.
 */
public class SearchRequest extends Request {
    
    /**
     * Types of objects, that will be returned as result photos, supported by GAPI.
     */
    public static final String TYPES = "university|synagogue|city_hall|church|museum|mosque|";

    private double lat;
    private double lng;
    private String keyWord;
    private int radius;
    
    /**
     * Based on Android device, it can request photos with suitable dimensions.
     */
    private int width, height;
    
    
    //NOTE: keyword bude prelozen jiz na Android zarizeni a request tedy bude vzdy prelozen na pozici-polomer
//    /**
//     * Search will be base on given keyWord, which must specify location. The size
//     * of area is defined by radius. The dimension of photos will be adjusted
//     * to suffice the requirements.
//     * 
//     * @param keyWord The keyword which specifies the area.
//     * @param radius The radius of selected area.
//     * @param width The requested width of photos.
//     * @param height The requested height of photos.
//     */
//    public SearchRequest(String keyWord, int radius, int width, int height) {
//        this.keyWord = keyWord;
//        this.radius = radius;
//        this.width = width;
//        this.height = height;
//    }

    /**
     * Search will be based on given location and area radius. The dimension
     * of photos will be adjusted to suffice the requirements.
     * 
     * @param player The player who is requesting the search.
     * @param lat The latitude of location.
     * @param lng The longitude of location.
     * @param radius The radius of selected area.
     * @param width The requested width of photos.
     * @param height The requested height of photos.
     */
    public SearchRequest(Player player, double lat, double lng, int radius, int width, int height) {
        this.player = player;
        this.lat = lat;
        this.lng = lng;
        this.radius = radius;
        this.width = width;
        this.height = height;
        
        this.uid = player.getUID();
        this.time = System.currentTimeMillis();
    }
    
    @Override
    public int getID() {
        return Request.SEARCH;
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

