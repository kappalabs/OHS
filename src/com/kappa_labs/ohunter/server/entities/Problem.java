package com.kappa_labs.ohunter.server.entities;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import scpsolver.problems.LPWizard;
import scpsolver.problems.LPWizardConstraint;
import scpsolver.problems.LinearProgram;

/**
 * Class for representation and translation of the linear problem to language
 * used by LP solvers.
 */
public class Problem {

    /**
     * The distributions, on which the EMP is being computed.
     */
    public List<DistrPair> distr1, distr2;


    /**
     * Saves the problem in MathProg language into a file.
     *
     * @param fname Name of the file.
     */
    public void saveToMOD(String fname) {
        File f = new File(fname);
        try (PrintWriter pw = new PrintWriter(f)) {
            pw.write(toMathProg());
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Problem.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Create an Linear Program for SCPS library from data in this instance.
     *
     * @return The Linear Program for SCPS library.
     */
    public LinearProgram toLinearProgram() {
        LPWizard lpw = new LPWizard();
        StringBuilder resultBuilder = new StringBuilder();
        int numC = 1;

        /* Objective function */
        for (int i = 0; i < distr1.size(); i++) {
            for (int j = 0; j < distr2.size(); j++) {
                resultBuilder.append("f").append(i).append('_').append(j);
                lpw.plus(resultBuilder.toString(), distr1.get(i).vector.distance(distr2.get(j).vector));
                resultBuilder.setLength(0);
            }
        }

        /* Variables >= 0 */
        for (int i = 0; i < distr1.size(); i++) {
            for (int j = 0; j < distr2.size(); j++) {
                resultBuilder.append("f").append(i).append('_').append(j);
                lpw.addConstraint("c" + (numC++), 0, "<=").plus(resultBuilder.toString(), 1);
                resultBuilder.setLength(0);
            }
        }

        /* Main constraints */
        LPWizardConstraint lpwc;
        for (int i = 0; i < distr1.size(); i++) {
            lpwc = lpw.addConstraint("c" + (numC++), distr1.get(i).weight, ">=");
            for (int j = 0; j < distr2.size(); j++) {
                resultBuilder.append("f").append(i).append('_').append(j);
                lpwc.plus(resultBuilder.toString(), j);
                resultBuilder.setLength(0);
            }
        }
        for (int j = 0; j < distr2.size(); j++) {
            lpwc = lpw.addConstraint("c" + (numC++), distr2.get(j).weight, ">=");
            for (int i = 0; i < distr1.size(); i++) {
                resultBuilder.append("f").append(i).append('_').append(j);
                lpwc.plus(resultBuilder.toString(), j);
                resultBuilder.setLength(0);
            }
        }

        /* Sum of all the variables is 1 */
        lpwc = lpw.addConstraint("c" + (numC++), 1, "=");
        for (int i = 0; i < distr1.size(); i++) {
            for (int j = 0; j < distr2.size(); j++) {
                resultBuilder.append("f").append(i).append('_').append(j);
                lpwc.plus(resultBuilder.toString(), 1);
                resultBuilder.setLength(0);
            }
        }

        lpw.setMinProblem(true);
        return lpw.getLP();
    }

    /**
     * Translates the problem into MathProg language.
     *
     * @return The MathProg representation of this LP.
     */
    public String toMathProg() {
        StringBuilder resultBuilder = new StringBuilder();

        /* Variables */
        for (int i = 0; i < distr1.size(); i++) {
            for (int j = 0; j < distr2.size(); j++) {
                resultBuilder.append("var f").append(i).append('_').append(j).append(" >= 0;\n");
            }
        }

        /* Objective function */
        resultBuilder.append("minimize obj: ");
        for (int i = 0; i < distr1.size(); i++) {
            for (int j = 0; j < distr2.size(); j++) {
                resultBuilder.append("f").append(i).append('_').append(j).append(" * ")
                        .append(distr1.get(i).vector.distance(distr2.get(j).vector));
                if (i != distr1.size() - 1 || j != distr2.size() - 1) {
                    resultBuilder.append(" + ");
                }
            }
        }
        resultBuilder.append(";\n");

        /* Conditions */
        int condIndx = 1;
        for (int i = 0; i < distr1.size(); i++) {
            resultBuilder.append("c").append(condIndx++).append(": ");
            for (int j = 0; j < distr2.size(); j++) {
                resultBuilder.append("f").append(i).append('_').append(j);
                if (j != distr2.size() - 1) {
                    resultBuilder.append(" + ");
                }
            }
            resultBuilder.append(" <= ").append(distr1.get(i).weight).append(";\n");
        }
        StringBuilder totalFSum = new StringBuilder();
        /* TODO: popis: Output from from Q */
        for (int j = 0; j < distr2.size(); j++) {
            resultBuilder.append("c").append(condIndx++).append(": ");
            for (int i = 0; i < distr1.size(); i++) {
                resultBuilder.append("f").append(i).append('_').append(j);
                totalFSum.append("f").append(i).append('_').append(j);
                if (i != distr1.size() - 1) {
                    resultBuilder.append(" + ");
                }
                if (i != distr1.size() - 1 || j != distr2.size() - 1) {
                    totalFSum.append(" + ");
                }
            }
            resultBuilder.append(" <= ").append(distr2.get(j).weight).append(";\n");
        }

        resultBuilder.append("c").append(condIndx++).append(": ");
        for (int i = 0; i < distr1.size(); i++) {
            for (int j = 0; j < distr2.size(); j++) {
                resultBuilder.append("f").append(i).append('_').append(j);
                if (i != distr1.size() - 1 || j != distr2.size() - 1) {
                    resultBuilder.append(" + ");
                }
            }
        }
        resultBuilder.append(" = 1;");

        return resultBuilder.toString();
    }

}
