# DPLL
First project for Optimization and Uncertainty Spring 2016

Changed how blocks are tracked so now instead of having a block array that is actively updated every time something changes, when selecting the next variable, the code first checks for unit clauses and if one is not found, it naively chooses the next available literal. It then first tries to see if there are any pure variables within the block of this literal, and if not, applies whatever splitting heuristic is selected to see if can find a better variable in the block than what the naive search came up with. All small tests work now without bugs for all heuristics applied so far, but there seems to be a bug for all large files the code is tested on for the split based on how many active variables there are for both signed and unsigned sums.

Tested amount of solutions found in ER1.ssat by just Unit Clause Propogation (which works perfectly), and got 35742 in 1 second. Using the max_active_clauses splitting heuristic, the code took 44 seconds and only found 699 solutions.
