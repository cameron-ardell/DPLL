/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package optimization_spring_2016_project_1b;

import java.io.*;
import java.util.*;

/**
 *
 * @author sardell
 */
public class testProblem {
    
    //all variables that will be read in
    private int numClauses;
    private int numVariables;
    //not useful except for knowing how problem was initialized
    private double seed;
     
    /*2D array holding all clauses, along with current information about each 
    clause and variable. Each row*/
    private double[][] allData;
        
    /* To keep track of current problem satisfaction.
    each variable holds the current amount of whatever the label describes*/
    private int active_clauses;
    private int satisfied_clauses;
    private int active_var;
    private int sat_var;
    private int numberOfUnitClauses;
    private int numPureVariables;
    
    private int solutionsFound;
    
    /*
    These hold the amount of aspects that are tracked for each variable
    and clause. Clauses descriptors are in columns to the right of each clause.
    It tracks number of active and satisfied variables in each clause.
    Variable descriptors are in the rows below the clauses, where each col
    corresponds to a different variable. It tracks the number of active clauses
    the literal is positive and negative in, if it has been assigned a value
    (indicated by 1 or -1 depending on if it's negated), the block it is in,
    and the probability it has of being not negated.
    */
    final private int numClauseDescript = 2;
    final private int numVarDescript = 5;
    
    //total rows and columns for 2D array
    private int total_row;
    private int total_col;
    
    //
    //keeps track of indicies where things are stored in array
    //
    //row where how many active clauses a literal is positive in
    private int activePosLitRow;
    //row where how many active clauses a literal is negative in
    private int activeNegLitRow;
    //row that indicates if variables have been assigned, shows +/-. Otherwise, 0
    private int assignedBoolRow;
    //row that indicates which block the variable is in
    private int blockIndexRow;
    //column where active literals in each clause is stored
    private int activeLitCountCol;
    //column where satisfied literals in each clause is stored
    private int satLitCountCol;
    
    //how many blockks of literals there are in the code
    private int numBlocks;

    //what type of splitting algorithm the code will use
    private String splitType;
    
    //probability of satisfaction according to file
    private double fileSolution;
    
    //booleans that indicate if you can use unit clause and/ or pure variable
    //elimination
    private boolean unitClauses;
    private boolean pureVariables;
    
    /*
    Array that holds the first index of variables in each block so you can use
    the current block index to know which variables to run a for loop over
    */
    private int[] blockFirstIndex;

    
    //how long code took to run
    public Long timeTaken;
    //total unit propagations
    public int unitProps;
    //total pure variable elimations
    public int PVEs;
    //number of splits besides UP and PVE
    public int numSplits;
    //relative percent of splits done out of total possible splits
    public double percentSplits;
    
    //variable I used to see if the probability found by code was a different 
    //value than the correct solution
    public double matchSolution;
    
    //the order that the variables came in
    private String pattern;
    
    //constructor for test problems
    //format of test problem input:
    //filename, unit clauses, pure variables, branching method
    testProblem(String fileName, int unit, int pure, String split) {
        
        solutionsFound = 0;
        
        //initializing variables I want to keep track of
        numberOfUnitClauses = 0;
        satisfied_clauses = 0;
        numPureVariables = 0;
        active_var = 0;
        sat_var = 0;
        splitType = split;
        timeTaken = 0l;
        unitProps = 0;
        
        //booleans for whether or not unit clauses and PVE can be used
        unitClauses = (unit == 1) ? true: false;
        pureVariables = (pure == 1) ? true: false;
        
        
        //start timer
        long startTime = System.nanoTime();
        
        
        //grab data from initial file
        readFile(fileName);
        
        //System.out.println(splitType);
        
        //now that large array is created, can now track blocks
        makeBlockFirstIndexArray();
        //System.out.println(blockFirstIndex.length);
        
        //printBlockFirstIndexArray();

        double hmm = solve(-numVariables);
       // System.out.println(solutionsFound);
        //System.out.println(hmm + "\n\n");
        
        matchSolution = fileSolution - hmm;
        
        double totPosSplit = Math.pow(2, numVariables) - 1.0;
        percentSplits = (numSplits * 100.0) / totPosSplit;

        
        //end timer, gives time in seconds
        long timeElapsed = (System.nanoTime() - startTime) / 1000000000;
        timeTaken = timeElapsed;
        
    }
    
 //read file with problem into code       
    public void readFile(String fileName) {
        
        
        try {
            BufferedReader file = new BufferedReader(new FileReader(fileName));
            String buff;
            String stringTemp;
            

            
            //get constants from file
            do {
                //read next line and tokenize it
                buff = file.readLine();         
                
                //skips empty lines
                if(buff.isEmpty()) { continue; }                               
                stringTemp = buff.trim();                                             
                String[] result = stringTemp.split("\\s+");
                
                                
                //get seed (and don't crash code on "variables" line)            
                if(result.length > 1){
                    if (result[1].equals("seed")) {
                        seed = Double.parseDouble(result[3]);
                    }
                    if(result[1].equals("command")) {                       
                        pattern = result[8];
                    }
                }                
                //get number of variables
                if (result[0].equals("v")) {
                    numVariables = Integer.parseInt(result[1]);
                    active_var = numVariables;
                    total_col = numVariables + numClauseDescript - 1;
                    
                    activeLitCountCol = total_col - 1;
                    satLitCountCol = total_col;
                }
                
                //get number of clauses
                if (result[0].equals("c")){
                    numClauses = Integer.parseInt(result[1]);
                    active_clauses = numClauses;
                    total_row = numClauses + numVarDescript - 1;
                    
                    blockIndexRow = total_row - 4;
                    assignedBoolRow = total_row - 3;
                    activePosLitRow = total_row - 2;
                    activeNegLitRow = total_row - 1;
                    
                    allData = new double[total_row + 1][total_col + 1];
                    
                    //fill array with 0's to avoid bugs later
                    for(int i = 0; i <= total_col; i++){
                        Arrays.fill(allData[i], 0);
                    }
                }                
            }while(!buff.equals("variables"));
            
            //get actual variables from file
            for(int i = 0; i < numVariables; i++) {
                buff = file.readLine();
                if(buff.isEmpty()) {continue;}
                stringTemp = buff.trim();
                String[] result = stringTemp.split("\\s+");
                double prob = Double.parseDouble(result[1]);
                //set probabilities
                allData[total_row][i] = prob;
                
            }
            
            //get all the way to the clauses
            do {buff = file.readLine();}while(!buff.equals("clauses"));
            
            //get clauses from file
            int curLiteral = 0;
            for(int i = 0; i < numClauses; i++){
                
                buff = file.readLine();
                if(buff.isEmpty()) {continue;}
                stringTemp = buff.trim();
                String[] result = stringTemp.split("\\s+");
                
                //-1 since don't want to count 0 indicating end of clause
                allData[i][activeLitCountCol] = result.length - 1;
                
                for(int s = 0; s < result.length; s++){
                    curLiteral = Integer.parseInt(result[s]);
                                                         
                    //reached end of clause
                    if (curLiteral == 0.0){
                        //if clause only one item long, means unit clause
                        if (s == 1) {numberOfUnitClauses += 1;}
                        break;
                    } else {
                        //need to adjust since counting from 0
                        int col_index = Math.abs(curLiteral) - 1;
                        
                        
                        /*if literal positive, put 1 in index where indiciating
                        if literal is in the clause. If it's negated, make it -1
                        */
                        if (curLiteral > 0 ) {
                            allData[i][col_index] = 1.0;
                            allData[activePosLitRow][col_index] += 1.0;                            
                        } else {
                            allData[i][col_index] = -1.0;
                            allData[activeNegLitRow][col_index] += 1.0;
                        }
                    }
                }             
            }
            //get all the way to the succes probability, then save it
            do {buff = file.readLine();}while(!buff.startsWith("Success Probability"));
            stringTemp = buff.trim();
            String[] result = stringTemp.split("\\s+");
            fileSolution = Double.parseDouble(result[2]);                       
        } catch (Exception e) {
            System.out.println("Error while reading file: " + e.getMessage());
        }
    }
    
    //function that prints out various values and properties, for testing
    public void testValues() {
        //System.out.println("seed: " + seed);
        //System.out.println("numVar: " + numVariables);
       // System.out.println("num clauses: " + numClauses);
        System.out.println("active clauses: " + active_clauses);
        System.out.println("satisfied clauses: " + satisfied_clauses);
        //System.out.println("satisfied variables: " + );
        System.out.println("unit clauses: " + numberOfUnitClauses);
        System.out.println("pure variables: " + numPureVariables);
//        System.out.println();
    }
    
    //function that prints the entire array, and test values
    public void printArray() {   
        for(int r = 0; r <= total_row; r++){
            for(int c = 0; c <= total_col; c++){
                System.out.print(String.format("%-20f ", allData[r][c]));
            }
            System.out.println();
        }
        testValues();
    }
    
    //prints out the array that holds start indicies of each block
    public void printTypeArray() {   
        int curVal = -1;
        for(int i = 0; i < numVariables; i++){
            curVal = blockFirstIndex[i];
            //if reached end of array with useful information, don't print it
            if(curVal == 0 && i != 0) {break;}
            System.out.print(curVal + " ");
        }
        System.out.println();
    }
    
        
    public double solve(int curVarIndex) {
        int stoppedAt = numClauses;
        int blockFromUpdate = 0;
                   
        //if not all the way at top of tree, update the array to accomodate
        //assignment of new variable
        if (curVarIndex != -numVariables) {
           // System.out.println(curVarIndex);
            stoppedAt = update(curVarIndex);
            active_var -= 1;
            sat_var += 1;
        }
        int index = (int)Math.sqrt(curVarIndex * curVarIndex);
            
        if (index == numVariables + 1 || index == numVariables) {index = 0;}
        
        blockFromUpdate = (int)allData[blockIndexRow][index];
        
        int sat = checkSatisfaction();
        
        //if update didn't finish, problem can't be satisfied with this literal
        //or if some clauses cannot be satisfied
        if (stoppedAt != numClauses || sat == -1) {                        
            active_var += 1;
            sat_var -= 1;
            backtrack(stoppedAt, curVarIndex);
            return 0.0;
        }          
                
        //if all the clauses are satisfied
        else if (sat == 1) {
            solutionsFound += 1;
            backtrack(stoppedAt, curVarIndex);
            active_var += 1;
            sat_var -= 1;
            
            return 1.0;
        }
       
        //find the next variable naively, then see if unit can find better. then
        //pure can find better within block. Try with heuristic if not
        int naiveSol = naiveSplit();
        
        int newVar[] = unitClauseSplit(); 
        int newVarCol = newVar[0];
        
        boolean pickedByUnit = true;
                
        //no unit clauses found to eliminate
        if(newVarCol == -1){       
            //need to note that nothing was found by UCP
            pickedByUnit = false;
            
            //then need to check for pure variables
            newVar = pureVarSplit(naiveSol);
            newVarCol = newVar[0];
            //then if no pure variables, apply splitting heuritstic
            //if the split type is naive, don't need to run it again
            if(newVarCol == -1){
                
                if (splitType.equals("naive")) {
                    newVarCol = naiveSol;
                    //System.out.print("\nnaive, ");
                } else {
                    newVarCol = split(naiveSol);
                   // System.out.print("\nheuristic, ");
                }
            }
            else {
               //  System.out.print("\npure, ");
            }
        }
        else {
           // System.out.print("\nunit, ");
        }
        double probWeight = allData[total_row][newVarCol];
        
        //to avoid recursion drama with 0 and -0
        if (newVarCol == 0) {
            newVarCol = numVariables + 1;
        }
 
        //check to see if it's a choice variable
        boolean isChoice = (probWeight == -1) ? true : false;
        
        double actual_prob = 0.0;
                
        //if something wasn't picked by unit clauses propogation
        if(pickedByUnit == false){
       
            double truth_prob = solve(newVarCol);
            double false_prob = solve(-newVarCol);
            numSplits += 1;

            if (isChoice == true){
                actual_prob = Math.max(truth_prob, false_prob);               
            } else {
                actual_prob = probWeight * truth_prob + (1.0 - probWeight) * false_prob;
            }
        } else {
            int sign = newVar[1];
            double weight;
            if(isChoice == true) {weight = 1.0;}
            else {weight = (sign > 0) ? probWeight : (1.0 - probWeight);}
            
            actual_prob = solve(newVarCol * sign) * weight;                    
        }


        //if you aren't at the first variable (so the last index to be reached
        //in recursion), then go recurse back down indicies
        if (curVarIndex != -numVariables) {
            backtrack(stoppedAt, curVarIndex);
            active_var += 1;
            sat_var -= 1;
        }
        
        //only return probability after simpler solutions have been given
        return actual_prob;
    }
    
        
    /*change count of active literals and see if any clauses now cannot be
    satisfied. Passes in index of given literal, with sign indicating if it's
    been negated. Returns which clause it stoppped updating on (in case of of
    unsatisfaction).*/
    public int update(int literal){
       
        
        /*once a literal is given a sign there are 5 cases:
        1) the clause was satisfied before and so it still satisfied. If the
            literal assignment has same sign in clause, then update its 
            satisfied var counter by 1. Decrease number of active literals by 1.
            You do not need to change how variables are active in each block.
        2) else, just decrease active literal count by 1.
        3) the clause was not satisfied before and the literal has the same
            assignment as the clause. Decrease number of active literals by 1,
            increase satisfied literals by 1, decrease total active clauses +/-
            by 1 for the literal depending on the sign. Since clause is now
            satisfied, also walk through other literals in clause and depending
            on sign, decrease total active clauses +/- by 1 for them as well.
            Increase satisfiedClauses by 1 and decrease activeClauses by 1.
        4) the clause was not satisfied before and the literal has a different
            assignment than the clause. Decrease number of active literals by 1,
            decrease total active clauses +/- by 1 for the literal depending
            on the sign. Check to see if any active literals left, otherwise
            this assignment makes problemm unsatisfied. In this case, backtrack.
        5) the literal is not in the clause. do nothing.
        
        after every update, check if total active plus satisfied clauses equals
        total number of clauses. If not, then problem is not satisfiable. Then
        backtrack on choice using similar technique as above.*/
        
        int litCol = Math.abs(literal);
        int sign = (literal > 0 ) ? 1 : -1;
        
        //resetting things I did to compensate for sign issues with +-0
        if(literal == numVariables + 1 || literal == -(numVariables + 1)){
            litCol = 0;
        }
        
        //indicates literal is assigned and if it is negated
        allData[assignedBoolRow][litCol] = sign;        
        
        //keeps track of how many clauses were evaluated
        //if doesn't finish with all, then problem unsatisfiable with current values
        int stoppedOnClause = 0;
        
        int curNumUnitClauses = 0;
        
        for(int i = 0; i < numClauses; i++) {
            //case 1
            if(allData[i][satLitCountCol] > 0  && allData[i][litCol] == sign
                    && allData[i][litCol] != 0){
                allData[i][satLitCountCol] += 1;
                allData[i][activeLitCountCol] -= 1;
            }
            //case 2
            else if(allData[i][satLitCountCol] > 0 && allData[i][litCol] !=sign
                    && allData[i][litCol] != 0){
                allData[i][activeLitCountCol] -= 1;
            }
            //case 3
            else if(allData[i][satLitCountCol] == 0 && allData[i][litCol] == sign
                    && allData[i][litCol] != 0){
                allData[i][satLitCountCol] += 1;
                allData[i][activeLitCountCol] -= 1;
                
                int totActiveRow = (sign > 0) ? activePosLitRow : activeNegLitRow;
                allData[totActiveRow][litCol] -= 1;             
                                
                active_clauses -= 1;
                satisfied_clauses += 1;               
                                
                for(int s = 0; s < numVariables; s++){
                    double val = allData[i][s];
                    if(val == 0) { continue; }
                    if(s == litCol) { continue; }
                    if(allData[assignedBoolRow][s] == 1) {continue;}
                       
                    int totActiveRow_ = (val > 0) ? activePosLitRow : activeNegLitRow;
                    allData[totActiveRow_][s] -= 1;
                }
            }
            //case 4
            else if(allData[i][satLitCountCol] == 0 && allData[i][litCol] != sign
                    && allData[i][litCol] != 0){
                
                //means no more active literals, so unsatisfied
                if(allData[i][activeLitCountCol] == 1){
                    break;
                }
                
                allData[i][activeLitCountCol] -= 1;
                //reversing >< since sign has oppositive sign of literal in clause
                int totActiveRow = (sign < 0) ? activePosLitRow : activeNegLitRow;
                allData[totActiveRow][litCol] -= 1;
            }
            
            //case 5
            else if (allData[i][litCol] == 0) {
                //continue;
                //thought I'd keep this case in here, just in case
                //(ha)
            }
            stoppedOnClause += 1;
            
            //check to see if active unit clause, if so, update counter
            if (allData[i][activeLitCountCol] == 1 && allData[i][satLitCountCol] == 0){
                curNumUnitClauses += 1;
            }
        }
        
        if (stoppedOnClause == numClauses) {
            numberOfUnitClauses = curNumUnitClauses;
        }
        
        return stoppedOnClause;                      
    }
 
/*given the clause the code program stopped updating on along with the variable
    that was being inputted, undo all effects it has had on the preceding clauses*/
    public void backtrack(int stoppedOnClause, int curVarIndex){
        int curVarCol = Math.abs(curVarIndex);
        
        //get sign of value
        int sign = (curVarIndex > 0 ) ? 1 : -1;
        
         //resetting things I did to compensate for sign issues with +-0
        if(curVarIndex == numVariables + 1 || curVarIndex == -(numVariables + 1)){
            curVarCol = 0;
        }
        
        //note that is no longer assigned
        allData[assignedBoolRow][curVarCol] = 0;

        //don't need to modify clause stopped on since didn't modify anything in it
        for (int d = stoppedOnClause -1 ; d > 0; d--){

            //case 1 (was satisfied before this round, but literal was in clause)
            if(allData[d][satLitCountCol] > 1  && allData[d][curVarCol] == sign
                    && allData[d][curVarCol] != 0){
                allData[d][satLitCountCol] -= 1;
                allData[d][activeLitCountCol] += 1;
            }
            //case 2 (satisfied before this round and literal was in clause with different sign)
            else if(allData[d][satLitCountCol] > 0 && allData[d][curVarCol] !=sign
                    && allData[d][curVarCol] != 0){
                allData[d][activeLitCountCol] += 1;
            }
            //case 3 (literal had satisfied this clause)
            else if(allData[d][satLitCountCol] == 1 && allData[d][curVarCol] == sign
                    && allData[d][curVarCol] != 0){
                allData[d][satLitCountCol] -= 1;
                allData[d][activeLitCountCol] += 1;

                int totActiveRow = (sign > 0) ? activePosLitRow : activeNegLitRow;
                allData[totActiveRow][curVarCol] += 1;

                active_clauses += 1;
                satisfied_clauses -= 1;

                for(int s = 0; s < numVariables; s++){
                    double val = allData[d][s];
                    if(val == 0) { continue; }
                    if(s == d) { continue; }
                    if(allData[assignedBoolRow][s] == 1) {continue;}

                    int totActiveRow_ = (val > 0) ? activePosLitRow : activeNegLitRow;
                    allData[totActiveRow_][s] += 1;
                }
            }
            //case 4
            else if(allData[d][satLitCountCol] == 0 && allData[d][curVarCol] != sign
                    && allData[d][curVarCol] != 0){

                allData[d][activeLitCountCol] += 1;
                int totActiveRow = (sign < 0) ? activePosLitRow : activeNegLitRow;
                allData[totActiveRow][curVarCol] += 1;
            }

            //case 5
            else if (allData[d][curVarCol] == 0) {
                //continue;
                //thought I'd keep this case in here, just in case
                //(ha)
            }

        }

    }  
    
    /*sends to splitting heuristic that depends on specified split type.
    Will return variable index found to be best guess by heuristic
    within the block of the variable found by the naive search, which is
    what is passed in, since it is the first variable unassigned and so its
    block needs to be addressed first.*/
    public int split(int curCol){
        //initalizes varIndex that will be returned
        int varIndex;
        
        switch(splitType) {
           case "WLU":
               varIndex = powerWeightClauseLengthSplit(curCol);
               break;
           case "WU":
               varIndex = weightedClauseLengthSplit(curCol);
               break;           
           case "MAC":
               varIndex = numActiveClausesSplit(curCol);
               break;
           case "MSAC":
               varIndex = numSignedActiveClausesSplit(curCol);
               break;
            default:
                //if not other type of input, do naive search
                varIndex = naiveSplit();
                break;
        }
        return varIndex;
    }
    

    /*splitting heuristic that chooses variable based on which one in the current
    block is in the most active clauses that are either negated or not (so doesn't
    sum the two, unlike the other number active clauses split) */
    public int numSignedActiveClausesSplit(int curCol) {
        
         //get section of variables to work with
        int beginInd = curCol;
        int endInd = blockFirstIndex[(int)allData[blockIndexRow][curCol] + 1];
        
        //setting baseline amount
        int max_index = beginInd;
        int max_active_clauses = 0;
        
        ///to keep track of current variables amount of clauses it is still active in
        int cur_active_pos_clauses;
        int cur_active_neg_clauses;
        int cur_active_clauses;
        
        //loops through current section of literals
        for(int i = beginInd; i < endInd; i++){
            //if already assigned whether or not negated, don't use it
            if(allData[assignedBoolRow][i] != 0) {continue;}
            
            //calculate current amount of clauses it is active in
            cur_active_pos_clauses = (int)allData[activePosLitRow][i];
            cur_active_neg_clauses = (int)allData[activeNegLitRow][i];
            //pick maximum value
            cur_active_clauses = (cur_active_pos_clauses >= cur_active_neg_clauses) ? cur_active_pos_clauses : cur_active_neg_clauses;
            
            //if there is more active clauses than the previous record, reuse the maximum
            if(cur_active_clauses > max_active_clauses) {
                max_active_clauses = cur_active_clauses;
                max_index = i;
            }   
        }
        return max_index;
    }
    
    /*splitting heuristic that chooses variable based on which one in current
    block is in the most active clauses, regardless of their negation.*/
    public int numActiveClausesSplit(int curCol) {
        
        //get section of variables to work with
        int beginInd = curCol;
        int endInd = blockFirstIndex[(int)allData[blockIndexRow][curCol] + 1];
        
        //setting baseline amount
        int max_index = beginInd;
        int max_active_clauses = 0;
        
        ///to keep track of current variables amount of clauses it is still active in
        int cur_active_clauses;
        
        //loops through current section of literals
        for(int i = beginInd; i < endInd; i++){
            //if already assigned whether or not negated, don't use it
            if(allData[assignedBoolRow][i] != 0) {continue;}
            
            //calculate current amount of clauses it is active in
            cur_active_clauses = (int)allData[activePosLitRow][i] + (int)allData[activeNegLitRow][i];
            
            //if there is more active clauses than the previous record, reuse the maximum
            if(cur_active_clauses > max_active_clauses) {
                max_active_clauses = cur_active_clauses;
                max_index = i;
            }      
        }                
        return max_index;
    }
    

    /*splitting heuristic that chooses variable based on how many active clauses
    will be shortened and/ or satisfied by it being assigned. The code is heavily
    biased towards clauses of length 2 or 3 (makes greater future likelihood of
    unit clauses).*/
    public int weightedClauseLengthSplit(int curCol) {
        
        //keep track of variable with highest score, and its score
        int maxVarIndex = curCol;
        int maxVarScore = 0;
        
        //get section of variables to work with
        int beginInd = curCol;
        int endInd = blockFirstIndex[(int)allData[blockIndexRow][curCol] + 1];
        
        //initalize score of whatever variable currently checking
        int curVarScore;
        //initalize clause length variable
        int curClauseLen;
        
        //go through rest of literals in block of passed in literal
        for (int i = beginInd; i < endInd; i++) {
            //if already assigned negation (or lack of), skip it
            if (allData[assignedBoolRow][i] != 0.0) { continue; }
            
            //reset curVarScore for eeach variable
            curVarScore = 0;
            
            //go through clauses
            for(int j = 0; j < numClauses; j++){
                //if clause is satisfied, skip it
                if (allData[j][satLitCountCol] != 0.0) { continue; }
                //if variable not in clause, skip it
                if (allData[j][i] == 0.0) { continue; }
                
                //clause length determined by # of active literals in it
                curClauseLen = (int)allData[j][activeLitCountCol];
                
                //update curVarScore with various amounts depending on length
                switch(curClauseLen) {
                    case 2:
                        curVarScore += 4;
                        break;
                    case 3:
                        curVarScore += 2;
                        break;
                    default:
                        curVarScore += 1;
                        break;
                }
            }
            //if stronger score than previous best, replace it
            if (curVarScore > maxVarScore) {
                maxVarScore = curVarScore;
                maxVarIndex = i;
            }
        }         
        return maxVarIndex;
    }
    
    //choose variable based on the length of clauses it is still active in,
    //prioritizes shorter clauses
    //this is pseudo unit clause propogation/ causes more unit clauses to happen
    public int powerWeightClauseLengthSplit(int curCol) {
        
        //keep track of variable with highest score, and its score
        int maxVarIndex = curCol;
        double maxVarScore = 0;
        
        //get section of variables to work with
        int beginInd = curCol;
        int endInd = blockFirstIndex[(int)allData[blockIndexRow][curCol] + 1];
        
        //initalize score of whatever variable currently checking
        double curVarScore;
        //initalize clause length variable
        double curClauseLen;
        
        for (int i = beginInd; i < endInd; i++) {
            //if already assigned negation (or lack of), skip it
            if (allData[assignedBoolRow][i] != 0.0) { continue; }
            
            curVarScore = 0.0;
            
            //go through clauses
            for(int j = 0; j < numClauses; j++){
                //if clause is satisfied, skip it
                if (allData[j][satLitCountCol] != 0.0) { continue; }
                //if variable is not in clause, skip it
                if (allData[j][i] == 0.0) { continue; }
                
                //clause length determined by # of active literals in it
                curClauseLen = allData[j][activeLitCountCol];
                
                //amount is increasingly small as clause length increases
                curVarScore += Math.pow(2.0, -curClauseLen);
            }
            
            //if stronger score than previous best, replace it
            if (curVarScore > maxVarScore) {
                maxVarScore = curVarScore;
                maxVarIndex = i;
            }
        }
        
        return maxVarIndex;
    }
     
    /*creates the array that holds the first index of each block
    e.g. second block starts at 3, which you can find out by checking
        blockFirstIndex[1]*/
    public void makeBlockFirstIndexArray(){
        //gets rid of any white space in the ER-type format read in
        String stringTemp = pattern.trim();
        //puts pattern into array so can loop through each item
        String[] result = stringTemp.split("");
        
        //global that tracks how many total blocks there are
        numBlocks = 1;
        
        //size of block array, which I know is potentially too big
        //just didn't know how to shrink any extra stuff after...
        int size = result.length;
        //initalize global array that will hold these indicies
        blockFirstIndex = new int[size +1];
        
        //given that first block index will always start at 0
        blockFirstIndex[0] = 0;
        //tracks which block index we are adding to
        int curIndex = 1;
        
        //we know there is at least 1 item in the first block, which means
        //that the next block doesn't start until at least 1
        blockFirstIndex[curIndex] = 1;
        
        //loop through all variables after the first one
        for (int i = 1; i < numVariables; i++){            
            //if the current variable is the same type as the last variable
            if(result[i].equals(result[i-1])){
                //just need to say the next index starts one more away than previously thought
                blockFirstIndex[curIndex] += 1;
            }
            //otherwise we've moved onto the next block, so increase index by 1
            else {
                curIndex += 1;
                //therefore, next block after that is at least 1 more than current
                //index
                blockFirstIndex[curIndex] = 1 + blockFirstIndex[curIndex-1];
                //increase total blocks by 1
                numBlocks += 1;               
            }
            //save which block each variable corresponds to in array
            allData[blockIndexRow][i] = numBlocks -1;
        } 
    }
    
    //prints array that holds first index of each block
    public void printBlockFirstIndexArray() {
        for(int i = 0; i < blockFirstIndex.length; i++){
            System.out.print(blockFirstIndex[i] + " ");
        }
        System.out.println("");
    }
    
    //splitting heuristic that just selects the first unset literal
    public int naiveSplit(){
        //index to return if nothing is unset
        int index = numVariables - 1;
        
        for(int i = 0; i < numVariables; i++){
            //if literal already assigned, skip it
            if(allData[assignedBoolRow][i] != 0) { continue; }
            
            //if got to this part of the code, then means var unset and code should stop
            index = i;
            break;
        }
        
        return index;
    }
    
    /*searches rest of block of passed in variable to see if there are any
    pure variables, which means variables that are only all pos or neg in 
    active clauses. It will return the index of first one it findsm along with
    its sign. If it does not find any (or they aren't allowed), then it will
    return a -1 for the index and a 0 for the "purity", which is the sign
    of the pure variable.*/ 
    public int[] pureVarSplit(int curCol) {
        int index = -1;
        int purity = 0;
        
        //not a choice variable, or no pure variables allowed, can't find
        //pure variable then
        if((allData[total_row][curCol] != -1.0) ||
                pureVariables == false) {
            return new int[]{index, purity};
        }
        
        /*can just loop through all variables to see if any pure. Seems fastest
        to do it here since will be at most v time and can't update var easily
        in regular update function since rest of code runs by clause*/
        
        int beginInd = curCol;
        int endInd = blockFirstIndex[(int)allData[blockIndexRow][curCol] + 1];
        
        for(int i = beginInd; i < endInd; i++){
            
            //if already assigned a value, continue
            if(allData[assignedBoolRow][i] != 0) {continue;}
            
            purity = checkPureVariable(i);
            
            //not pure variable, then skip it
            if(purity == 0) {continue;}
            
            //otherwise, var is pure & code should return it instead of continuing
            index = i;
            //increase number of PVE used for split by 1
            PVEs += 1;
            return new int[]{index, purity};
        }
        return new int[]{index, purity};
    }
    
    
    /*searchse the entire list of active variables, regardless of block, to see
    if there are any unit clauses, which are clauses that only have one active
    variable and so require that variable to have a particular assignment. It
    will return the index of the first one it finds, along with its sign. If it
    does not find any (or they aren't allowed), then it will return a -1 for the
    index and a 0 for the sign.*/
    public int[] unitClauseSplit() {
        
        int index = -1;
        int sign = 0;
         
        //no unit clauses
        if (numberOfUnitClauses == 0) {
            return new int[]{index, sign};
        }
        //code not allowed to use unit clauses
        else if (unitClauses == false){
            return new int[]{index, sign};
        }else{
            //find first unit clause, then get var that's in it
            for(int i = 0; i < numClauses; i++){
                //skip this clause if it's not a unit clause
                if(checkUnitClause(i) == false) { continue; }
                //skip if the clause is satisfied
                if(allData[i][satLitCountCol] != 0) {  continue; }
                
                //for first found unit clause, get var that is still active,
                //then stop loop (so doesn't run more than c + v)
                for(int v = 0; v < numVariables; v++){

                    //if literal in clause and not assigned a boolean yet, then
                    //it is active in the clause
                    if(allData[i][v] != 0 && allData[assignedBoolRow][v] == 0) {
                        index = v; 
                        //get sign of this literal in this clause
                        sign = (int)allData[i][v];
                        //increase the number of UCP used for split by 1
                        unitProps += 1;
                        return new int[]{index, sign};
                    }  
                }
            }
        }        
        return new int[]{index, sign};
    }
    
    //checks if given clause is a unit clause, returns a boolean
    public boolean checkUnitClause(int clauseRowIndex){
        
        //if only one active literal left in unsatisfied clause, it's a unit clause
        return allData[clauseRowIndex][activeLitCountCol] == 1
                && allData[clauseRowIndex][satLitCountCol] == 0;
        
    }
    
    //prints variable assignments for clauses as they currently stand
    public void printSolution() {
        for (int i = 0; i < numVariables; i++) {   
            //want to print the index + 1 (since started at 0, unlike clauses
            //multiply it by whatever boolean it's been assigned to indicate negation
            int val = (int)allData[assignedBoolRow][i] * (i+1);           
            System.out.print(val + " ");
        }
        System.out.println();
    }
    
    //prints all active clauses
    public void printActiveClauses() {
        for (int i = 0; i < numClauses; i++){
            //print all clauses that have no satisfied variables
            if(allData[i][satLitCountCol] == 0){
                System.out.print(i + ": ");
                printClause(i);
            }
        }
    }
    
    //prints a clause given the index of a clause
    public void printClause(int clauseRow){
        for(int i = 0; i  < numVariables; i++){
            System.out.print(allData[clauseRow][i] + " ");
        }
        System.out.println();
    }
    
    /*literal is a pure variable if remaining active variables are all either 
    negated or not negated. Returns 1 if not negated, -1 all negated, 0 if mix
    (so not a pure variable)*/
    public int checkPureVariable(int variableIndex) {
        //only negated var left
        if (allData[activePosLitRow][variableIndex] == 0 && allData[activeNegLitRow][variableIndex] != 0){
            return -1;
        }
        //only not negated var left
        else if (allData[activePosLitRow][variableIndex] != 0 && allData[activeNegLitRow][variableIndex] == 0) {
            return 1;
        }
        //both negated and not negated var left (or both are 0)
        return 0;
    }
    
    
    //checks if problem is satisfied, not satisfied (yet?), or unsatisfiable
    //with current variable assignments
    public int checkSatisfaction() {
        //count number of satisfied clauses
        int numSat = 0;

        //tracks num satisfied and active variables for each clause
        int sat;
        int active;
        
        //go through all the clauses
        for(int i = 0; i < numClauses; i++){
            sat = (int)allData[i][satLitCountCol];
            active = (int)allData[i][activeLitCountCol];
            
            //stop immediately if there is a clause with no active literals and unsatisfied
            if(sat == 0 && active == 0) {
                return -1;
            }
            //if clause is satisfied, add it to the count
            if (sat > 0) {
                numSat += 1;
            }
        }
        //if all clauses satisfied
        if(numSat == numClauses) { return 1; }
        //otherwise, carry on
        else { return 0; }    
    }
}
