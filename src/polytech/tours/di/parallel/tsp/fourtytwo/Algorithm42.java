package polytech.tours.di.parallel.tsp.fourtytwo;

import java.util.Collections;
import java.util.Properties;
import java.util.Random;

import polytech.tours.di.parallel.tsp.Algorithm;
import polytech.tours.di.parallel.tsp.Instance;
import polytech.tours.di.parallel.tsp.InstanceReader;
import polytech.tours.di.parallel.tsp.Solution;
import polytech.tours.di.parallel.tsp.TSPCostCalculator;

public class Algorithm42 implements Algorithm {
	private Random randomizer; //TODO: Use parallel version
	private Instance instance;

	@Override
	public Solution run(Properties config) {
		Solution finalSolution = null;
		
		//Get some parameters
		randomizer = new Random(Long.valueOf(config.getProperty("seed")));
		
		//Build the instance from the config file
		InstanceReader ir = new InstanceReader();
		ir.buildInstance(config.getProperty("instance"));
		instance = ir.getInstance();

		//Manage the timing (*1000 to have the time in millisec)
		long timeMax = Long.valueOf(config.getProperty("maxcpu")) * 1000;
		long startTime = System.currentTimeMillis();
		while((System.currentTimeMillis() - startTime) < timeMax) {
			
			//Find the solutions
			Solution generatedSolution = generateRandomSolution();
			System.out.println("Random: " + generatedSolution);
			
			Solution locallySearchedSolution = localSearch(generatedSolution);
			System.out.println("Locally searched solution: " + locallySearchedSolution);
			
			if(finalSolution == null)
				finalSolution = generatedSolution;
			
			if(locallySearchedSolution.getOF() < finalSolution.getOF())
				finalSolution = locallySearchedSolution;
			
			System.out.println("---------------------------------------");
		}
		
		System.out.println("---------------------------------------");
		
		return finalSolution;
	}
	
	/** Generate and return only one random solution
	 * @return the random solution
	 */
	private Solution generateRandomSolution() {
		Solution solution = new Solution();
		
		/* We add all the city (index) to the solution,
		 * no more things are required. TSPCostCalculator
		 * do the trick */
		for(int i = 0; i < instance.getN(); i++) {
			solution.add(i);
		}
		
		//Randomize the solution indices (cities)
		Collections.shuffle(solution, randomizer);

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
		
		while(continueExploration)
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

		for(int i = 0; i < solution.size(); i++)
		{
			for(int j = i + 1; j < solution.size(); j++)
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
