package polytech.tours.di.parallel.tsp.fourtytwo;

import java.util.Collections;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import polytech.tours.di.parallel.tsp.Instance;
import polytech.tours.di.parallel.tsp.Solution;
import polytech.tours.di.parallel.tsp.TSPCostCalculator;

public class ThreadedSolutionFinder implements Runnable {
	private final int workerId;
	private final Instance instance;
	private final boolean isVerbose;
	private final Solution solution;
	
	private Thread thread;
	private Random random;
	
	public ThreadedSolutionFinder(boolean isVerbose, int workerId, Instance instance, Solution solution) {
		this.isVerbose = isVerbose;
		this.workerId = workerId;
		this.instance = instance;
		this.solution = solution;
	}

	@Override
	public void run() {
		thread = Thread.currentThread();
		random = ThreadLocalRandom.current();
		solution.setOF(Double.MAX_VALUE); //No solution so it has the higher cost
		
		do {
			Solution randomSolution = generateRandomSolution();
			Solution localSearchSol = localSearch(randomSolution);
			localSearchSol.setOF(TSPCostCalculator.calcOF(instance, localSearchSol));
			
			if(isVerbose)
				System.out.println("[Worker " + workerId +
						"] " + randomSolution.getOF() + "\t-> " + localSearchSol.getOF());
			
			if(localSearchSol.getOF() < solution.getOF()) {
				//Copy the elements because the solution reference must not change
				solution.clear();
				solution.addAll(localSearchSol);
				solution.setOF(localSearchSol.getOF());
			}

		} while(!thread.isInterrupted());
	}
	
	private Solution generateRandomSolution() {
		Solution solution = new Solution();
		
		/* We add all the city (index) to the solution,
		 * no more things are required. TSPCostCalculator
		 * do the trick */
		for(int i = 0; i < instance.getN(); i++) {
			solution.add(i);
		}
		
		//Randomize the solution indices (cities)
		Collections.shuffle(solution, random);

		//Compute the efficiency of the solution
		solution.setOF(TSPCostCalculator.calcOF(instance, solution));
		
		return solution;
	}

	/* Return the best solution among many changed solution
	 * (local search algorithm)
	 * @param generatedSolution The solution to begin with
	 * @return the best solution found with the algorithm,
	 * null if no better solution
	 */
	private Solution localSearch(Solution solution) {
		boolean continueExploration = true;
		Solution bestSolution = solution;
		
		while(continueExploration && !thread.isInterrupted())
		{
			Solution swapSolution;
			
			swapSolution = exploreNeighborhood(bestSolution);
			if((float)swapSolution.getOF() < (float)bestSolution.getOF())
				bestSolution = swapSolution;
			else
				continueExploration = false;
		}
		
		return bestSolution;
	}

	/* Return the best solution among many changed solution
	 * (local search algorithm)
	 * @param generatedSolution The solution to begin with
	 * @return the best solution found with the algorithm
	 */
	private Solution exploreNeighborhood(Solution solution) {
		Solution bestSolution = solution;
		Solution swapSolution = solution.clone();

		for(int i = 0; i < solution.size() && !thread.isInterrupted(); i++)
		{
			for(int j = i + 1; j < solution.size() && !thread.isInterrupted(); j++)
			{
				double costBefore = swapSolution.getOF();
				double relativeCostBefore = computeSwapCost(swapSolution, i, j);
				swapSolution.swap(i, j);
				double relativeCostAfter = computeSwapCost(swapSolution, i, j);
				double diffCost = relativeCostBefore - relativeCostAfter;
				
				swapSolution.setOF(costBefore - diffCost);
				
				if(swapSolution.getOF() < bestSolution.getOF()) 
					bestSolution = swapSolution.clone();
			}
		}
		
		return bestSolution;
	}


	/* Compute the relative cost of the swap of i and j
	 * @param solution The solution to compute the cost
	 * @param i The index to be swapped with j
	 * @param j The index to be swapped with i
	 * @return the relative cost
	 */
	private double computeSwapCost(Solution solution, int i, int j) {
		//Not accurate, but acceptable for float values
		double cost;
		int locFrom, locTo;
		int n = solution.size();
		
		locFrom = solution.get((i - 1 + n) % n);
		locTo = solution.get(i);
		cost = instance.getDistance(locFrom, locTo);
		
		locFrom = solution.get(i);
		locTo = solution.get(i + 1);
		cost += instance.getDistance(locFrom, locTo);

		if(j - i != 1) {
			locFrom = solution.get(j - 1);
			locTo = solution.get(j);
			cost += instance.getDistance(locFrom, locTo);
		}
		
		locFrom = solution.get(j);
		locTo = solution.get((j + 1) % n);
		cost += instance.getDistance(locFrom, locTo);
		
		return cost;
	}
}
