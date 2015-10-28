
package com.kappa_labs.ohunter.server.analyzer;

import com.kappa_labs.ohunter.server.entities.Problem;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;
//import lpsolve.*;
//import org.gnu.glpk.*;

/**
 * Class performing the EMP by solving the given Problem.
 * 
 * TODO: use own implementation of modified simplex algorithm as described
 * for example here: http://www.vision.ucla.edu/~hbling/publication/emd_pami_O.pdf
 * or use different software as Java library.
 */
public class EMDSolver {
    
    public static final String LP_FILE_NAME = "lp_data.mod";
    
    private final Problem problem;

    /**
     * Create new EMPMatch and initialize its Problem, which is LP to be solved.
     * 
     * @param problem LP to be solved by this class.
     */
    public EMDSolver(Problem problem) {
        this.problem = problem;
    }
    
    /**
     * Solve assigned Problem and return the result EMD value.
     * 
     * @return The result optimal EMD value.
     */
    public float countValue() {
        problem.saveToMOD(LP_FILE_NAME);
        
        float similarity = 0;
        Process process;
        try {
            process = new ProcessBuilder("lp_solve", "-S1", "-rxli", "./xli_MathProg", LP_FILE_NAME).start();
            InputStream is = process.getInputStream();
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);
            String line;

            while ((line = br.readLine()) != null) {
//                System.out.println(line);
                if (line.startsWith("Value of objective function: ")) {
                    similarity = Float.parseFloat(line.replaceAll("^[^\\d]*", ""));
                    break;
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(EMDSolver.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        /* NOTE: nepodarilo se mi zprovoznit */
//        try {
//            LpSolve lps = LpSolve.readXLI("xli_MathProg", LP_FILE_NAME, "", "-noint", LpSolve.DETAILED);
////            LpSolve lps = LpSolve.readMps("model.mps", LpSolve.DETAILED);
//            int solution = lps.solve();
//            double[] vars = lps.getPtrVariables();
//            for (double v : vars) {
//                System.out.println(v);
//            }
//            lps.deleteLp();
//        } catch (LpSolveException ex) {
//            Logger.getLogger(EMDSolver.class.getName()).log(Level.SEVERE, null, ex);
//        }
        
//        GlpkSolver solver = GlpkSolver.readModel(problem.toMathProg(), null, null); //GlpkSolver.readMps(LP_FILE_NAME);
//        int retint = solver.simplex();
//        System.out.println("retint = "+retint);
//        double objval = solver.getObjVal();
//        System.out.println("objval = "+objval);
        
        return similarity;
    }
    
    

}
