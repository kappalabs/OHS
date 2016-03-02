
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
import com.kappa_labs.ohunter.server.analyzer.Analyzer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;


public class SearchRequest extends com.kappa_labs.ohunter.lib.requests.SearchRequest {
    
    /**
     * Number of threads allowed for PlaceWorkers thread pool.
     */
    private static final int NUM_THREADS = 128;

    
    public SearchRequest(Player player, double lat, double lng, int radius,
            Photo.DAYTIME daytime, int width, int height) {
        super(player, lat, lng, radius, daytime, width, height);
    }
    
    public SearchRequest(Request r) {
        super((com.kappa_labs.ohunter.lib.requests.SearchRequest) r);
    }

    @Override
    public Response execute() throws OHException {
        System.out.println("search na "+lat+"; "+lng+"; radius = "+radius);
        /* Retrieve all possible places */
        ArrayList<Place> all_places;
        all_places = PlacesGetter.radarSearch(lat, lng, radius, "", TYPES);
        
        // redukce poctu mist!
        int size = all_places.size();
        int i = size;
        // TODO: kam s konstantou?
        while (--i >= Math.min(size, 30)) {
            all_places.remove(i);
        }
        
        DatabaseService ds = new DatabaseService();
        /* Turn them to Photo objects, filter blocked and rejected */
        all_places = all_places.stream().filter((Place place) -> {
            try {
                return !ds.isCompleted(player, place.getID())
                        && !ds.isBlocked(place.getID())
                        && !ds.isRejected(player, place.getID());
            } catch (OHException ex) {
//                    throw new OHException(keyWord); //NOTE: nelze z lambdy, nutno pres RuntimeEx
                throw new RuntimeException(ex);
            }
        }).collect(Collectors.toCollection(ArrayList::new));
        
        ArrayList<Place> places = new ArrayList<>();
        ExecutorService executor = Executors.newFixedThreadPool(NUM_THREADS);
        for (Place place : all_places) {
            executor.execute(new PlaceFiller(place, places));
        }
        executor.shutdown();
        try {
            executor.awaitTermination(1, TimeUnit.MINUTES);
        } catch (InterruptedException ex) {
            Logger.getLogger(SearchRequest.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        Response response = new Response(uid);
        response.places = places;
        
        System.out.println("SearchRequest: I've prepared " + places.size() + " Places.");
         
        return response;
    }

    /**
     * Worker class for filling up given Place object with information and photos.
     */
    private class PlaceFiller implements Runnable {

        private final Place mPlace;
        private final ArrayList<Place> mPlaces;

        
        public PlaceFiller(Place place, ArrayList<Place> places) {
            this.mPlace = place;
            this.mPlaces = places;
        }
        
        @Override
        public void run() {
            ArrayList<Photo> photos = PlacesGetter.details(mPlace);
            if (photos == null) {
                return;
            }
            mPlace.photos = photos;
            photos.stream().forEach((photo) -> {
                PlacesGetter.photoRequest(photo, width, height);
                if (Analyzer.isNight(photo)) {
                    photo.daytime = Photo.DAYTIME.NIGHT;
                }
            });
            
            mPlaces.add(mPlace);
        }
        
    }
    
}
