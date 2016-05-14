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
    private final Long timeTaken;
    private final Double percentSplits;
    private static Double numProblems = 12.0;
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        
        //format of test problem input:
        //filename, unit clauses, pure variables, branching method          
       
        //old files used, were too quick
       /*String[] fileNames = {"small1", "small2", "small3",
           "small4","small5", "ns1", "er1", "er2", "er3", 
           "erer1", "erer2", "erer3", "TRERE3", "TRE1",
       "TRE1", "TR3", "TR2", "TR1", "TERER3", "TERER2", "TERER1",
       "TER3", "TER2", "TER1", "TE3", "TE2", "TE1", "TRERE2", 
       "TRERE1", "TRE3", "TRE2"};*/
       
       /*String[] fileNames = {"e1", "e2", "e3", "er1", "er2", "er3", "erer1",
           "erer2", "erer3", "r1", "r2", "r3", "re1", "re2", "re3", "rere1",
           "rere2", "rere3"};*/
       
       //files for testing for the report
       String[] fileNames = {"e1", "e2", "e3", "er3", "r1", "re1", "re2", "re3",
            "rere2", "ns1", "rere3", "rere1"};
       
       
       String[] algTypes = {"naive", "UCP", "PVE", "UCP_PVE", "max_active_clauses",
           "max_signed_active_clauses", "weighted_unit", "log_weighted_unit"};
       
       //average run time of time taken by heuristic divided by time taken by naive
       //heuristic on same problem
       Double[] avgAlgRatioTime = new Double[algTypes.length];
       
       //average percent of possible splits done by heuristic divided by same done
       //by naive on same problem
       Double[] avgAlgoPercentSplits = new Double[algTypes.length];
       
       for(int i = 0; i < algTypes.length; i ++){
            avgAlgRatioTime[i] = 0.0;
            avgAlgoPercentSplits[i] = 0.0;
       }
       
       //numProblems = (double)fileNames.length; 
               
        //creates a PrintWriter which will print data to a csv file as we run tests
        try (PrintWriter pw = new PrintWriter(new FileOutputStream(new File("dataForWriteUp.csv"), true))) {

        pw.println(" " + ", " + "seconds taken, UCPS, PVEs, VS, % of VS Possible, Difference from Solution" );
        
        Double naiveTime = 0.0;
        Double naiveSplits = 0.0;
        
        
        //For each of 72 cases, label the data and create test instance of DPLL
        for (int i = fileNames.length-numProblems.intValue(); i < fileNames.length; i++) {
            pw.println("\n" + fileNames[i]);
            System.out.println("\n" + fileNames[i]);
            for (int j = 0; j < algTypes.length; j++) {
               //if (j == 2) { continue; }
               System.out.println(algTypes[j]);
                pw.print(algTypes[j] + ", ");
                Optimization_Spring_2016_Project_1B prob = new Optimization_Spring_2016_Project_1B(fileNames[i], algTypes[j], pw);
                
                if(algTypes[j] == "naive"){
                    naiveTime = prob.timeTaken.doubleValue();
                    naiveSplits = prob.percentSplits;
                }
                
                avgAlgRatioTime[j] += prob.timeTaken.doubleValue() / naiveTime;
                avgAlgoPercentSplits[j] += prob.percentSplits / naiveSplits;
            }
            System.out.println("done\n");
        }
        
        pw.println("\n\n ,average time ratio from naive,average split ratio with naive");
        for(int i = 0; i < algTypes.length; i++) {
            pw.println( algTypes[i] + "," + avgAlgRatioTime[i]/numProblems + "," + avgAlgoPercentSplits[i]/numProblems);
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
            case "naive":
                pureVar = 0;
                unitProp = 0;
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

        this.timeTaken = test.timeTaken;
        this.percentSplits = test.percentSplits;
    
    }
}
