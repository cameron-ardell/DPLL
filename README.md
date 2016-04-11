# DPLL
First project for Optimization and Uncertainty Spring 2016

Changed how blocks are tracked so now instead of having a block array that is actively updated every time something changes, when selecting the next variable, the code first checks for unit clauses and if one is not found, it naively chooses the next available literal. It then first tries to see if there are any pure variables within the block of this literal, and if not, applies whatever splitting heuristic is selected to see if can find a better variable in the block than what the naive search came up with. All small tests work now without bugs for all heuristics applied so far, but there seems to be a bug for all large files the code is tested on for the split based on how many active variables there are for both signed and unsigned sums.

There is something preventing MSAC and MAC from exploring all of the the possible assignments, and I'm not sure what it is. I think they happened to get lucky finding the optimal solution in the small problems, but since so many more things can go wrong in the bigger ones, it becomes more obvious that something isn't updating quite correctly. I am going to try to pass the naive solution into the heuristic, as that implies that all literals before it have been assigned a value, and you only need to check the remainder of the block. I do not think this is where the bug is coming from, but it will make the code run more efficiently.

I just realized that when you check for satisfaction, things don't automatically come back unsatisfied based on the principle that the clause has 0 active literals and 0 satisfied literals. The code worked when I added a check for the global of active variables being 0 and not having all clauses satisfied. This makes me think that the active variables tracker for each clause is not working properly.
