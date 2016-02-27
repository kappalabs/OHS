
package com.kappa_labs.ohunter.server.analyzer;

import com.kappa_labs.ohunter.lib.entities.Photo;
import com.kappa_labs.ohunter.server.entities.SImage;
import com.kappa_labs.ohunter.server.entities.DistrPair;
import com.kappa_labs.ohunter.server.entities.Problem;
import com.kappa_labs.ohunter.server.entities.Segment;
import com.kappa_labs.ohunter.server.entities.Vector;
import com.kappa_labs.ohunter.server.utils.Addterator;
import com.kappa_labs.ohunter.server.utils.CIELab;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Random;
import javax.imageio.ImageIO;

/**
 * Class providing a method for measuring the similarity of two given images.
 */
public class Analyzer {
    
    public static final int DEFAULT_NUM_SAMPLES = 78;
    public static final int DEFAULT_NIGHT_THRESHOLD = 80;
    public static final int OPTIMAL_WIDTH = 256;
    public static final int OPTIMAL_HEIGHT = OPTIMAL_WIDTH;
    
    private Analyzer() {
        /* Analyzer cannot be instantiated from outside of this class */
    }

    /**
     * For given two photos, count their similarity. The result is from interval
     * [0;1], 0 means perfect match, 1 totaly different.
     * 
     * @param ph1 First photo.
     * @param ph2 Second photo.
     * @return Similarity of given photos from interval [0;1];
     */
    public static float computeSimilarity(Photo ph1, Photo ph2) {
        float ret;
        
        /* Scale the images to provide the best results */
        try {
            ph1.image = ph1._image = new SImage(ph1.image);
            ph2.image = ph2._image = new SImage(ph2.image);
            ((SImage)ph1.image).setImage(resize(((SImage)ph1._image).toBufferedImage(), OPTIMAL_WIDTH, OPTIMAL_HEIGHT));
            ((SImage)ph2.image).setImage(resize(((SImage)ph2._image).toBufferedImage(), OPTIMAL_WIDTH, OPTIMAL_HEIGHT));
        } catch(Exception e) {
            System.err.println("Could not acquire photos from client: "+e);
            e.printStackTrace();
            //TODO: lepsi throw nejaka OHException
            return 0;
        }
        
        /* Perform segmentation */
        Segment[] segs1 = Segmenter.segment(ph1);
        Segment[] segs2 = Segmenter.segment(ph2);
//        System.out.println("... mam "+segs1.length+" prvnich a "+segs2.length+" druhych");
        
        /* Create new Problem from given counted segments */
        Problem problem = new Problem();
        prepareDistribution(problem, segs1, ph1, true);
        prepareDistribution(problem, segs2, ph2, false);
        
//        System.out.println("LP:\n"+problem.toMathProg());
        
        /* Solve the EMP linear problem and return the final result */
        EMDSolver empm = new EMDSolver(problem);
        ret = (float) empm.countValue();
        
        return ret * 10f;
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
        int max_dimension = Math.max(photo.getWidth(), photo.getHeight());
        for (Segment seg : segments) {
            DistrPair dp = new DistrPair();
            dp.weight = (double)seg.getSumPixels() / area;
            Vector vect = new Vector(14);
            Addterator<Float> addter = vect.addterator();
            
            /* Color moments - 9 elements in total */
//            float[] mean_hsb = toHSB(seg.getMean());
//            float[] stdev_hsb = toHSB(seg.getStdDeviation());
//            float[] skew_hsb = toHSB(seg.getSkewness());
            float[] mean_hsb = seg.getMean();
            float[] stdev_hsb = seg.getStdDeviation();
            float[] skew_hsb = seg.getSkewness();
            for (int i = 0; i < Segment.MODEL_NUM_ELEMENTS; i++) {
                addter.add(mean_hsb[i]);// * (i == 0 ? 3 : 1));
                addter.add(stdev_hsb[i]);// * (i == 0 ? 3 : 1));
                addter.add(skew_hsb[i]);// * (i == 0 ? 3 : 1));
            }
            /* The other elements - another 5 of them */
            int o_width = seg.getRight() - seg.getLeft();
            o_width = Math.max(o_width, 1);
            int o_height = seg.getBottom() - seg.getTop();
            o_height = Math.max(o_height, 1);
            int o_area = o_width * o_height;
            /* NOTE: experimental, puvodni hodnoty ze specifikace nebyly vhodne,
                pouzitym zpusobem jsem normalizoval kazdou slozku vektoru do intervalu
                [0;1], tzn. L1 metriku staci vydelit poctem slozek vektoru a
                ziskam tak hodnotu podobnosti obrazku z intervalu [0;1]
            */
            /* original - ze specifikace */
            addter.add((float)Math.log((double)o_width / o_height + 1));
            addter.add((float)Math.log(o_area));
            addter.add((float)seg.getSumPixels() / o_area);
//            addter.add((float)seg.getX());
//            addter.add((float)seg.getY());
            /* modified */
//            addter.add(Math.min((float)o_width / o_height, 1.f));
//            addter.add((float)(Math.log(o_area) / Math.log(area)));
//            addter.add((float)seg.getSumPixels() / o_area);
            addter.add((float)seg.getX() / photo.getWidth());
            addter.add((float)seg.getY() / photo.getHeight());
            
//            System.out.println("vect = "+vect);
            
            dp.vector = vect;
            if (isFirst) {
                problem.distr1.add(dp);
            } else {
                problem.distr2.add(dp);
            }
        }
//        System.out.println("--------");
    }
   
    /**
     * Determines if given photo contains night picture.
     * 
     * @param photo The photo to analyze.
     * @return True if the given photo contains night picture, false otherwise.
     */
    public static boolean isNight(Photo photo) {   
        BufferedImage img = ((SImage) photo.image).toBufferedImage();
        Random rand = new Random();
        int x, y, souc = 0;
        for (int i = 0; i < DEFAULT_NUM_SAMPLES; i++) {
            x = rand.nextInt(img.getWidth());
            y = rand.nextInt(img.getHeight());
            souc += argbToByte(img.getRGB(x, y));
        }
        float val = (souc / DEFAULT_NUM_SAMPLES);
//        System.out.println("night value = "+val);
        return val < DEFAULT_NIGHT_THRESHOLD;
    }
    
    /**
     * Convert argb 4-byte color value to one byte value.
     * 
     * @param argb Input 4-byte value.
     * @return 
     */
    private static int argbToByte(int argb) {
        int ret = 0;
        ret += (argb & 0xff) / 3;
        ret += ((argb & 0xff00) >> 8) / 3;
        ret += ((argb & 0xff0000) >> 16) / 3;
        return ret;
    }
    
}
