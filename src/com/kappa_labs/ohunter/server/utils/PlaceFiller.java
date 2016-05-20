package com.kappa_labs.ohunter.server.utils;

import com.kappa_labs.ohunter.lib.entities.Photo;
import com.kappa_labs.ohunter.lib.entities.Place;
import com.kappa_labs.ohunter.server.analyzer.Analyzer;
import com.kappa_labs.ohunter.server.google_api.PlacesCommunicator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Worker class for filling up given Place object with information and photos.
 */
public class PlaceFiller implements Callable<Void> {

    private final Place mPlace;
    private final List<Place> mPlaces;
    private final int width, height;
    private final Photo.DAYTIME daytime;
    private final int maxThreads;

    
    /**
     * Creates a new Place filler to fill the detail information and photos into
     * given place. The result Place is added into the List only when no error
     * occures and at least one photo is available for that place.
     *
     * @param place The place whose placeDetails will be retrieved.
     * @param places The List, where the valid result should be placed.
     * @param width The maximum width of photos for given place.
     * @param height The maximum height of photos for given place.
     * @param daytime The preferred daytime of the photos.
     * @param maxThreads The maximum number of threads to retrieve photos for
     * each place.
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
    public Void call() throws Exception {
        List<Photo> photos = PlacesCommunicator.placeDetails(mPlace);
        if (photos == null) {
            return null;
        }

        /* Retrieve these photos */
        ExecutorService executor = Executors.newFixedThreadPool(maxThreads);
        photos.stream().forEach((photo) -> {
            executor.submit(new PhotosFiller(mPlace, photo, width, height, daytime));
        });
        executor.shutdown();
        try {
            executor.awaitTermination(SettingsManager.getInstance().getFillPoolMaxWaitTime(), TimeUnit.MINUTES);
        } catch (InterruptedException ex) {
            Logger.getLogger(PlaceFiller.class.getName()).log(Level.WARNING, null, ex);
        }

        /* Add only places with some photos, other are of no use on the client */
        if (mPlace.getNumberOfPhotos() > 0) {
            mPlaces.add(mPlace);
        }
        return null;
    }

    /**
     * Worker class to retrieve one photo.
     */
    private class PhotosFiller implements Callable<Void> {

        private final Place place;
        private final Photo photo;
        private final int width, height;
        private final Photo.DAYTIME daytime;

        /**
         * Creates a new worker to retrieve one photo and set its daytime.
         *
         * @param place The place whose photo this should be.
         * @param photo The photo object that will contain the retrieved photo.
         * @param width Maximum width of the photo.
         * @param height Maximum height of the photo.
         * @param daytime The preffered daytime of photos.
         */
        public PhotosFiller(Place place, Photo photo, int width, int height, Photo.DAYTIME daytime) {
            this.place = place;
            this.photo = photo;
            this.width = width;
            this.height = height;
            this.daytime = daytime;
        }

        @Override
        public Void call() throws Exception {
            /* Download the image and store it into the photo */
            PlacesCommunicator.photoRequest(photo, width, height);
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
            return null;
        }

    }

}
