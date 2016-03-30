
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
    private final Photo.DAYTIME daytime;
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
     * @param daytime The preferred daytime of the photos.
     * @param maxThreads The maximum number of threads to retrieve photos for each place.
     */
    public PlaceFiller(Place place, List<Place> places, int width, int height, Photo.DAYTIME daytime, int maxThreads) {
        this.mPlace = place;
        this.mPlaces = places;
        this.width = width;
        this.height = height;
        this.daytime = daytime;
        this.maxThreads = maxThreads;
    }

    @Override
    public void run() {
        List<Photo> photos = PlacesGetter.details(mPlace);
        if (photos == null) {
            return;
        }
        
        /* Retrieve these photos */
        ExecutorService executor = Executors.newFixedThreadPool(maxThreads);        
        photos.stream().forEach((photo) -> {
            executor.execute(new PhotosFiller(mPlace, photo, width, height, daytime));
        });
        executor.shutdown();
        try {
            executor.awaitTermination(MAX_WAIT_TIME, TimeUnit.MINUTES);
        } catch (InterruptedException ex) {
            Logger.getLogger(SearchRequester.class.getName()).log(Level.WARNING, null, ex);
        }
        
        /* Add only places with some photos, other are of no use on the client */
        if (mPlace.getNumberOfPhotos() > 0) {
            mPlaces.add(mPlace);
        }
    }
    
    /**
     * Worker class to retrieve one photo.
     */
    private class PhotosFiller implements Runnable {
        
        private final Place place;
        private final Photo photo;
        private final int width, height;
        private final Photo.DAYTIME daytime;

        public PhotosFiller(Place place, Photo photo, int width, int height, Photo.DAYTIME daytime) {
            this.place = place;
            this.photo = photo;
            this.width = width;
            this.height = height;
            this.daytime = daytime;
        }

        @Override
        public void run() {
            /* Download the image and store it into the photo */
            PlacesGetter.photoRequest(photo, width, height);
            /* Determine whether the image was photographed at night */
            boolean isNight = Analyzer.isNight(photo);
            if (isNight) {
                photo.daytime = Photo.DAYTIME.NIGHT;
            }
            /* Add this photo to the place only if it has requested daytime */
            boolean addPhoto = true;
            switch (daytime) {
                case DAY:
                    addPhoto = !isNight;
                    break;
                case NIGHT:
                    addPhoto = isNight;
                    break;
            }
            if (addPhoto) {
                place.addPhoto(photo);
            }
        }
    
    }

}
