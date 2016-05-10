package com.kappa_labs.ohunter.server.net.requests;

import com.kappa_labs.ohunter.lib.entities.Photo;
import com.kappa_labs.ohunter.lib.net.OHException;
import com.kappa_labs.ohunter.lib.net.Response;
import com.kappa_labs.ohunter.lib.requests.PhotoRequest;
import com.kappa_labs.ohunter.server.google_api.PlacesGetter;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Implementation of the FillPlacesRequest from the OHL.
 */
public class PhotoRequester extends PhotoRequest {

    /**
     * Number of minutes to wait for thread termination.
     */
    private final int MAX_WAIT_TIME = 1;
    /**
     * Number of threads allowed to retrieve the photos.
     */
    private static final int NUM_PHOTO_THREADS = 10;


    public PhotoRequester(PhotoRequest request) {
        super(request);
    }
    
    @Override
    public Response execute() throws OHException {
        /* Parallel download of the Photos */
        Photo[] photos = new Photo[photoReferences.length];
        ExecutorService executor = Executors.newFixedThreadPool(NUM_PHOTO_THREADS);
        for (int i = 0; i < photos.length; i++) {
            photos[i] = new Photo();
            photos[i].reference = photoReferences[i];
            executor.execute(new PhotoFiller(photos[i], width, height));
        }
        executor.shutdown();
        try {
            executor.awaitTermination(MAX_WAIT_TIME, TimeUnit.MINUTES);
        } catch (InterruptedException ex) {
            Logger.getLogger(SearchRequester.class.getName()).log(Level.WARNING, null, ex);
        }

        /* Store the data in Response object */
        Response response = new Response(uid);
        response.photos = photos;

        return response;
    }

    
    /**
     * Worker class to retrieve one photo.
     */
    private class PhotoFiller implements Runnable {
        
        private final Photo photo;
        private final int width, height;

        /**
         * Creates a new worker to retrieve one photo.
         * 
         * @param photo The photo object that will contain the retrieved photo.
         * @param width Maximum width of the photo.
         * @param height Maximum height of the photo.
         */
        public PhotoFiller(Photo photo, int width, int height) {
            this.photo = photo;
            this.width = width;
            this.height = height;
        }

        @Override
        public void run() {
            /* Download the image and store it into the photo */
            PlacesGetter.photoRequest(photo, width, height);
        }
    
    }
    
}
