package com.kappa_labs.ohunter.server.analyzer;

import com.kappa_labs.ohunter.lib.entities.Photo;
import com.kappa_labs.ohunter.lib.net.OHException;
import com.kappa_labs.ohunter.server.entities.SImage;
import com.kappa_labs.ohunter.server.entities.DistrPair;
import com.kappa_labs.ohunter.server.entities.Problem;
import com.kappa_labs.ohunter.server.entities.Segment;
import com.kappa_labs.ohunter.server.entities.Vector;
import com.kappa_labs.ohunter.server.utils.Addterator;
import com.kappa_labs.ohunter.server.utils.CIELab;
import com.kappa_labs.ohunter.server.utils.SettingsManager;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class providing a method for measuring the similarity of two given images.
 */
public class Analyzer {

    private static final Logger LOGGER = Logger.getLogger(Analyzer.class.getName());
    
    private static final SettingsManager settingsManager = SettingsManager.getInstance();

    
    private Analyzer() {
        /* Analyzer cannot be instantiated from outside of this class */
    }

    /**
     * For given two photos, count their similarity. The result is from interval
     * [0;1], 1 means perfect match, 0 totaly different.
     *
     * @param ph1 First photo.
     * @param ph2 Second photo.
     * @return Similarity of given photos from interval [0;1];
     * @throws com.kappa_labs.ohunter.lib.net.OHException When Photos are
     * wrongly set.
     */
    public static float computeSimilarity(Photo ph1, Photo ph2) throws OHException {
        float ret = 0;

        /* Scale the images to provide the best results */
        try {
            ph1.sImage = ph1._sImage = new SImage(ph1.sImage);
            ph2.sImage = ph2._sImage = new SImage(ph2.sImage);
            ((SImage) ph1.sImage).setImage(resize(((SImage) ph1._sImage).toBufferedImage()));
            ((SImage) ph2.sImage).setImage(resize(((SImage) ph2._sImage).toBufferedImage()));
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Could not acquire photos from client: {0}", e);
            throw new OHException("Could not acquire photos!", OHException.EXType.OTHER);
        }

        /* Count average from few attempts */
        final int poc = 5;
        int iteration = poc;
        while (iteration-- > 0) {
            /* Perform segmentation */
            Segment[] segs1 = Segmenter.segment(ph1);
            Segment[] segs2 = Segmenter.segment(ph2);
            LOGGER.log(Level.FINER, "... got {0} segments from first and {1} segments"
                    + " in the second photo", new Object[]{segs1.length, segs2.length});

            /* Create new Problem from given counted segments */
            Problem problem = new Problem();
            prepareDistribution(problem, segs1, ph1, true);
            prepareDistribution(problem, segs2, ph2, false);

            /* Solve the EMP linear problem and return the final result */
            EMDSolver empm = new EMDSolver(problem);
            float act = Math.max(0f, Math.min(1f, (float) empm.countValue()));
            LOGGER.finer(String.format(" - similarity: %.1f%%", 100 - act * 100));
            ret += act;
        }
        ret /= poc;

        return 1f - ret;
    }

    /**
     * Resize input image so that its size matches the optimal image size
     * boundary.
     *
     * @param image The input image to be resized.
     * @return The resized image matching the optimal size boundary.
     */
    public static BufferedImage resize(BufferedImage image) {
        double divider = Math.max(
                (double) image.getWidth() / settingsManager.getOptimalWidth(),
                (double) image.getHeight() / settingsManager.getOptimalHeight());
        return resize(image, (int) (image.getWidth() / divider), (int) (image.getHeight() / divider));
    }

    /**
     * Resize given image to specified dimensions.
     *
     * @param image Image for modification.
     * @param width Desired new image width.
     * @param height Desired new image height.
     * @return Given image scaled to given dimensions.
     */
    public static BufferedImage resize(BufferedImage image, int width, int height) {
        BufferedImage resized = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D _g2d = resized.createGraphics();
        _g2d.drawImage(image, 0, 0, width, height, null);
        _g2d.dispose();

        return resized;
    }

    private static float[] toHSB(float[] moment) {
        float[] rgb = CIELab.getInstance().toRGB(moment);
        return Color.RGBtoHSB((int) (rgb[0] * 255), (int) (rgb[1] * 255), (int) (rgb[2] * 255), null);
    }

    private static void prepareDistribution(Problem problem, Segment[] segments, Photo photo, boolean isFirst) {
        int area = photo.getWidth() * photo.getHeight();
        for (Segment seg : segments) {
            DistrPair dp = new DistrPair();
//            dp.weight = (double)seg.getSumPixels() / area;
            /* Original method uses sqrt */
            dp.weight = Math.sqrt((double) seg.getSumPixels() / area);
            Vector vect = new Vector(14);
            Addterator<Float> addter = vect.addterator();

            /* Color moments - 9 elements in total */
            float[] mean_hsb = seg.getMean();
            float[] stdev_hsb = seg.getStdDeviation();
            float[] skew_hsb = seg.getSkewness();
            for (int i = 0; i < Segment.MODEL_NUM_ELEMENTS; i++) {
                addter.add(mean_hsb[i]);
                addter.add(stdev_hsb[i]);
                /* Skeweness is from range [-1;1] */
//                addter.add(skew_hsb[i] / 2 + 0.5f);
                addter.add(skew_hsb[i]);
            }
            /* The other elements - another 5 of them */
            int o_width = seg.getRight() - seg.getLeft();
            o_width = Math.max(o_width, 1);
            int o_height = seg.getBottom() - seg.getTop();
            o_height = Math.max(o_height, 1);
            int o_area = o_width * o_height;
            /* original - ze specifikace */
//            addter.add((float)Math.log((double)o_width / o_height));
//            addter.add((float)Math.log(o_area));
            /* modified */
            addter.add((float) Math.log((double) o_width / o_height + 1));
            addter.add((float) Math.log((double) o_area / area + 1));
            addter.add((float) seg.getSumPixels() / o_area);
            addter.add((float) seg.getX() / photo.getWidth());
            addter.add((float) seg.getY() / photo.getHeight());

            dp.vector = vect;
            if (isFirst) {
                problem.distr1.add(dp);
            } else {
                problem.distr2.add(dp);
            }
        }
    }

    /**
     * Determines if given photo contains night picture.
     *
     * @param photo The photo to analyze.
     * @return True if the given photo contains night picture, false otherwise.
     */
    public static boolean isNight(Photo photo) {
        BufferedImage img = ((SImage) photo.sImage).toBufferedImage();
        Random rand = new Random();
        int x, y;
        double souc = 0;
        for (int i = 0; i < settingsManager.getRandomPhotoSamplesNumber(); i++) {
            x = rand.nextInt(img.getWidth());
            y = rand.nextInt(img.getHeight());
            souc += argbToIntensity(img.getRGB(x, y));
        }
        double val = (souc / settingsManager.getRandomPhotoSamplesNumber());
        return val < settingsManager.getNightTreshold();
    }

    /**
     * Convert argb 4-byte color value to intensity value.
     *
     * @param argb Input 4-byte value.
     * @return The intensity of given color.
     */
    private static double argbToIntensity(int argb) {
        int ret = 0;
        ret += (argb & 0xff) * 0.0722;
        ret += ((argb & 0xff00) >> 8) * 0.7152;
        ret += ((argb & 0xff0000) >> 16) * 0.226;
        return ret;
    }

}
