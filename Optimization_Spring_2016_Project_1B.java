/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package optimization_spring_2016_project_1b;

import java.util.*;
import java.io.*;

/**
 *
 * @author sardell
 */
public class Optimization_Spring_2016_Project_1B {

    private final String fileType;
    private final String algoType;
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        
        //format of test problem input:
        //filename, unit clauses, pure variables, branching method          
       
       String[] fileNames = {"small1", "small2", "small3",
           "small4","small5", "er1", "er2", "er3",
           "erer1", "erer2", "erer3", "ns1"};
       
       String[] algTypes = {"UCP", "PVE", "UCP_PVE", "max_active_clauses",
           "max_signed_active_clauses", "weighted_unit", "log_weighted_unit"};
       
       
       
        //creates a PrintWriter which will print data to a csv file as we run tests
        try (PrintWriter pw = new PrintWriter(new FileOutputStream(new File("DPLL.csv"), true))) {

        pw.println(" " + ", " + "seconds taken, UCPS, PVEs, VS, % of VS Possible, Difference from Solution" );
        
        
        //For each of 72 cases, label the data and create test instance of DPLL
        for (int i = 5; i < 6; i++) {
            pw.println("\n" + fileNames[i]);
            //System.out.println("\n" + fileNames[i]);
            for (int j = 0; j < 5; j++) {
               //if (j ==1 || j == 2) { continue; }
                pw.print(algTypes[j] + ", ");
                    Optimization_Spring_2016_Project_1B prob = new Optimization_Spring_2016_Project_1B(fileNames[i], algTypes[j], pw);

            }
          
        }
 

        } catch (IOException ioe) {
        //If something goes wrong, throw exception
        System.err.println("IOException: " + ioe.getMessage());
        }                        
    }
    
     //Runs a PSO test
    public Optimization_Spring_2016_Project_1B(String fileName, String  algType, PrintWriter pw) {
        this.fileType = fileName + ".ssat";
        this.algoType = algType;
        
        int unitProp = 1;
        int pureVar = 1;
        String split = "naive";
        
        switch(this.algoType){
            case "UCP":
                pureVar = 0;
                break;
            case "PVE":
                unitProp = 0;
                break;
            case "weighted_unit":
                split = "WU";
                break;
            case "log_weighted_unit":
                split = "WLU";
                break;
            case "max_active_clauses":
                split = "MAC";
                break;
            case "max_signed_active_clauses":
                split = "MSAC";
            default:
                break;
        }
        
            
        testProblem test = new testProblem(fileType,unitProp, pureVar, split);
        //after each run, retrieve the data, so we can print it

        pw.print(test.timeTaken + ", " + test.unitProps + ", " + test.PVEs + ", "
            + test.numSplits + ", " + test.percentSplits +", " + test.matchSolution);
        
        pw.println(" ");

    
    }
}
