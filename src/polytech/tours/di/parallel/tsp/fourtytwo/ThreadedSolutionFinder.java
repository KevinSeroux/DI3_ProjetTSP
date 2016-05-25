package polytech.tours.di.parallel.tsp.fourtytwo;

import java.util.Collections;
import java.util.concurrent.ThreadLocalRandom;

import polytech.tours.di.parallel.tsp.Instance;
import polytech.tours.di.parallel.tsp.Solution;
import polytech.tours.di.parallel.tsp.TSPCostCalculator;

public class ThreadedSolutionFinder implements Runnable {
	Thread thread;
	final Instance instance;
	Solution solution;
	
	public ThreadedSolutionFinder(long seed, Instance instance, Solution solution) {
		//ThreadLocalRandom.current().setSeed(seed); TODO
		this.instance = instance;
		this.solution = solution;
	}

	@Override
	public void run() {
		thread = Thread.currentThread();
		solution.setOF(Double.MAX_VALUE); //No solution so it has the higher cost
		
		do {
			Solution randomSolution = generateRandomSolution();
			System.out.println("Random: " + randomSolution);
			Solution currentSolution = localSearch(randomSolution); //TODO: Must be interruptible
			System.out.println("Locally searched solution: " + currentSolution);
			
			if(currentSolution.getOF() < solution.getOF()) {
				//Copy the elements because the solution reference must not change
				solution.clear();
				solution.addAll(currentSolution);
				solution.setOF(currentSolution.getOF());
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
		Collections.shuffle(solution, ThreadLocalRandom.current());

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
			//TODO: Solve this, computeSwapCost is inaccurate
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
				//swapSolution.setOF(TSPCostCalculator.calcOF(instance, swapSolution));
				
				if(swapSolution.getOF() < bestSolution.getOF()) 
					bestSolution = swapSolution.clone();
			}
		}
		
		return bestSolution;
	}
	
	private double computeSwapCost(Solution solution, int i, int j) {
		//TODO: Not accurate
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
