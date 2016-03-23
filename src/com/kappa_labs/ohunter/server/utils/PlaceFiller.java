
package com.kappa_labs.ohunter.server.utils;

import com.kappa_labs.ohunter.lib.entities.Photo;
import com.kappa_labs.ohunter.lib.entities.Place;
import com.kappa_labs.ohunter.server.analyzer.Analyzer;
import com.kappa_labs.ohunter.server.google_api.PlacesGetter;
import com.kappa_labs.ohunter.server.net.requests.SearchRequester;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * Worker class for filling up given Place object with information and photos.
 */
public class PlaceFiller implements Runnable {

    /**
     * Number of minutes to wait for thread termination.
     */
    private final int MAX_WAIT_TIME = 1;
    
    private final Place mPlace;
    private final List<Place> mPlaces;
    private final int width, height;
    private final int maxThreads;


    /**
     * Creates a new Place filler to fill the detail information and photos
     * into given place. The result Place is added into the List only when no error
     * occures and at least one photo is available for that place.
     * 
     * @param place The place whose details will be retrieved.
     * @param places The List, where the valid result should be placed.
     * @param width The maximum width of photos for given place.
     * @param height The maximum height of photos for given place.
     * @param maxThreads The maximum number of threads to retrieve photos for each place.
     */
    public PlaceFiller(Place place, List<Place> places, int width, int height, int maxThreads) {
        this.mPlace = place;
        this.mPlaces = places;
        this.width = width;
        this.height = height;
        this.maxThreads = maxThreads;
    }

    @Override
    public void run() {
        List<Photo> photos = PlacesGetter.details(mPlace);
        if (photos == null) {
            return;
        }
        mPlace.addPhotos(photos);
        
        /* Retrieve these photos */
        ExecutorService executor = Executors.newFixedThreadPool(maxThreads);        
        photos.stream().forEach((photo) -> {
            executor.execute(new PhotosFiller(photo, width, height));
        });
        executor.shutdown();
        try {
            executor.awaitTermination(MAX_WAIT_TIME, TimeUnit.MINUTES);
        } catch (InterruptedException ex) {
            Logger.getLogger(SearchRequester.class.getName()).log(Level.WARNING, null, ex);
        }
        
        mPlaces.add(mPlace);
    }
    
    /**
     * Worker class to retrieve one photo.
     */
    private class PhotosFiller implements Runnable {
        
        private Photo photo;
        private final int width, height;

        public PhotosFiller(Photo photo, int width, int height) {
            this.photo = photo;
            this.width = width;
            this.height = height;
        }

        @Override
        public void run() {
            photo = PlacesGetter.photoRequest(photo, width, height);
            if (Analyzer.isNight(photo)) {
                photo.daytime = Photo.DAYTIME.NIGHT;
            }
        }
    
    }

}
