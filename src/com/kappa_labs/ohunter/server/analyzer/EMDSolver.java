package com.kappa_labs.ohunter.server.analyzer;

import com.kappa_labs.ohunter.server.entities.Problem;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import scpsolver.lpsolver.LinearProgramSolver;
import scpsolver.lpsolver.SolverFactory;
import scpsolver.problems.LPSolution;
import scpsolver.problems.LinearProgram;

/**
 * Class performing the EMP by solving the given Problem.
 */
public class EMDSolver {

    public static final String LP_FILE_NAME = "lp_data.mod";
    public static final String LP_LOG_FILE_NAME = "lp_scps.log";

    private final Problem problem;
    private static LinearProgramSolver solver;

    
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
    public double countValue() {
        /* Firstly, redirect the output of the library into a log file */
        PrintStream out_ = System.out;
        PrintStream ps = null;
        try {
            File f = new File(LP_LOG_FILE_NAME);
            f.delete();
            ps = new PrintStream(f);
            System.setOut(ps);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(EMDSolver.class.getName()).log(Level.SEVERE, null, ex);
        }

        /* Create the linear program and find its solution */
        LinearProgram lp = problem.toLinearProgram();
        if (solver == null) {
            solver = SolverFactory.newDefault();
        }
        double[] sol = solver.solve(lp);
        LPSolution lps = new LPSolution(sol, lp);

        /* Redirect the output stream back */
        System.setOut(out_);
        if (ps != null) {
            ps.close();
        }

        return lps.getObjectiveValue();
    }

}
