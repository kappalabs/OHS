package com.kappa_labs.ohunter.server.analyzer;

import com.kappa_labs.ohunter.lib.entities.Photo;
import com.kappa_labs.ohunter.server.entities.Centroid;
import com.kappa_labs.ohunter.server.entities.MyImage;
import com.kappa_labs.ohunter.server.entities.Pixel;
import com.kappa_labs.ohunter.server.entities.SImage;
import com.kappa_labs.ohunter.server.entities.Segment;
import com.kappa_labs.ohunter.server.utils.SettingsManager;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;

/**
 * Class for image segmentation.
 */
public class Segmenter {

    private static final SettingsManager settingsManager = SettingsManager.getInstance();
    
    /**
     * Stop treshold for the convergency speed of the cost function.
     */
    private static final float KMEANS_CONVERG_TRESHOLD = .999f;

    /**
     * Path to debug non-segmented images.
     */
    private static final String NONSEGMENTED_PATH = "results" + File.separator + "_nonsegmented" + File.separator;
    /**
     * Path to debug segmented images.
     */
    private static final String SEGMENTED_PATH = "results" + File.separator + "_segmented" + File.separator;

    
    /**
     * Take the photo and perform segmentation. The number of segments is
     * defined in this class.
     *
     * @param photo The photo, which should be segmented.
     * @return Array of Segments, which contains additional information about
     * every segment, that was found.
     */
    public static Segment[] segment(Photo photo) {
        /* DEBUG: Save the image before segmentation into local file */
        if (settingsManager.getKmeansSaveForDebug()) {
            try {
                File nsDir = new File(NONSEGMENTED_PATH);
                nsDir.mkdirs();
                ImageIO.write(((SImage) photo.sImage).toBufferedImage(), "jpg", new File(nsDir + File.separator + photo.generateName(64) + ".jpg"));
            } catch (IOException ex) {
                Logger.getLogger(Segmenter.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        /* Convert image to custom representation in HSV model */
        Pixel[][] hsvs = new Pixel[photo.getWidth()][photo.getHeight()];
        for (int i = 0; i < photo.getWidth(); i++) {
            for (int j = 0; j < photo.getHeight(); j++) {
                Color col = new Color(((SImage) photo.sImage).toBufferedImage().getRGB(i, j));
                float[] cils = new float[3];
                Color.RGBtoHSB(col.getRed(), col.getGreen(), col.getBlue(), cils);
                hsvs[i][j] = new Pixel(cils, i, j);
            }
        }
        MyImage mi = new MyImage(hsvs, photo.getWidth(), photo.getHeight());
        Pixel[] means = kmeans(mi);

        /* DEBUG: shows kmeans output segments */
        BufferedImage nbi = new BufferedImage(photo.getWidth(), photo.getHeight(), BufferedImage.TYPE_INT_RGB);
        for (int i = 0; i < mi.width; i++) {
            for (int j = 0; j < mi.height; j++) {
                Pixel px = mi.getPixel(i, j);
                if (means[px.centroid] == null) {
                    continue;
                }
                Pixel cpx = means[px.centroid];
                int col = Color.HSBtoRGB(cpx.get(0), cpx.get(1), cpx.get(2));
                nbi.setRGB(i, j, col);
            }
        }
        /* Highlight centroids by red dot */
        for (Pixel px : means) {
            if (px != null) {
                nbi.setRGB(px.x, px.y, 0xFFFF0000);
            }
        }
        /* DEBUG: Save the segmented image into local file */
        if (settingsManager.getKmeansSaveForDebug()) {
            try {
                File sDir = new File(SEGMENTED_PATH);
                sDir.mkdirs();
                ImageIO.write(nbi, "jpg", new File(sDir + File.separator + photo.generateName(64) + ".jpg"));
            } catch (IOException ex) {
                Logger.getLogger(Segmenter.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        /* Save it into a field in the Photo object */
        photo._sImage = new SImage(nbi);

        Segment[] segs = Segment.makeSegments(mi, means);

        return segs;
    }

    /**
     * Firstly find the best starting random pixels, then continue with regular
     * K-Means algorithm, stop it when the cost function starts to 'settle
     * down'.
     *
     * @param img Image, on which the K-Means is performed.
     * @return Pixels, which were computed as the best means.
     */
    private static Pixel[] kmeans(MyImage img) {
        final int num_segments = settingsManager.getKmeansSegmentsNumber();
        Pixel[] means = null;
        Centroid[] centroids = new Centroid[num_segments];
        Random rand = new Random();

        float best_opt = Float.POSITIVE_INFINITY;
        float opt_value, prev_value;

        /* Pick up best random means to start with */
        int repeats = settingsManager.getKmeansInitRepeats();
        Pixel[] _means = null;
        while (repeats-- > 0) {
            if (_means == null) {
                _means = new Pixel[num_segments];
            }
            opt_value = 0;

            /* Randomly initialize guesses for means */
            for (int i = 0; i < _means.length; i++) {
                _means[i] = img.getPixel(rand.nextInt(img.width), rand.nextInt(img.height));
            }

            /* Compute cost function */
            for (Pixel px : img) {
                double dist = Float.MAX_VALUE;
                for (Pixel mean : _means) {
                    if (px.distance(mean) < dist) {
                        dist = px.distance(mean);
                    }
                }
                opt_value += dist;
            }

            if (opt_value < best_opt) {
                best_opt = opt_value;
                means = _means;
                _means = null;
            }
        }

        /**
         * Start the K-means algorithm, stop when the cost function does not
         * change quick enough (treshold), or the number of allowed repeats was
         * exceeded.
         */
        opt_value = 0;
        prev_value = 0;
        int num = settingsManager.getKmeansMaxRepeats();
        while (num-- > 0 && (prev_value == 0 || prev_value * KMEANS_CONVERG_TRESHOLD > opt_value)) {
            prev_value = opt_value;
            opt_value = 0;
            for (int i = 0; i < centroids.length; i++) {
                centroids[i] = new Centroid();
            }
            for (Pixel px : img) {
                for (int i = 0; i < num_segments; i++) {
                    if (means[i] != null && px.distance(means[i]) < px.distance) {
                        px.distance = px.distance(means[i]);
                        px.centroid = i;
                    }
                }
                opt_value += px.distance;
                centroids[px.centroid].addPixel(px);
            }
            for (int i = 0; i < num_segments; i++) {
                means[i] = centroids[i].getMean();
            }
        }

        return means;
    }

}
