
package com.kappa_labs.ohunter.server.entities;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class for representation and translation of the linear problem to language
 * used by LP solvers.
 */
public class Problem {
    
    /**
     * The distributions, on which the EMP is being computed.
     */
    public ArrayList<DistrPair> distr1, distr2;
    
    
    /**
     * Create a new problem, instantiate the distributions.
     */
    public Problem() {
        distr1 = new ArrayList<>();
        distr2 = new ArrayList<>();
    }
    
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

        /* NOTE: not needed, sum through weights is always 1 */
//        /* Assign minimum */
//        resultBuilder.append("c").append(condIndx++).append(": ");
//        for (int i = 0; i < distr1.size(); i++) {
//            for (int j = 0; j < distr2.size(); j++) {
//                resultBuilder.append("f").append(i).append('_').append(j);
//                if (i != distr1.size() - 1 || j != distr2.size() - 1) {
//                    resultBuilder.append(" + ");
//                }
//            }
//        }
//        resultBuilder.append(" = min(");
//        
//        for (int i = 0; i < distr1.size(); i++) {
//            resultBuilder.append(distr1.get(i).weight);
//            if (i != distr1.size() - 1) {
//                resultBuilder.append(" + ");
//            }
//        }
//        resultBuilder.append(", ");
//        
//        for (int j = 0; j < distr2.size(); j++) {
//            resultBuilder.append(distr2.get(j).weight);
//            if (j != distr2.size() - 1) {
//                resultBuilder.append(" + ");
//            }
//        }
//        resultBuilder.append(");\n");