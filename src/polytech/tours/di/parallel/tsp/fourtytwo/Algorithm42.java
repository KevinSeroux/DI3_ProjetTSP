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
		Solution inLoopBestSolution;
		
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
			Solution randomSolution = generateRandomSolution();
			Solution locallySearchedSolution = localSearch(randomSolution);
			
			if(finalSolution == null)
				finalSolution = randomSolution;
			
			//Test which solution is better
			if(locallySearchedSolution.getOF() < randomSolution.getOF()) {
				System.out.println("Random: " + randomSolution);
				System.out.println("[OP] Locally searched solution: " + locallySearchedSolution);
				inLoopBestSolution = locallySearchedSolution;
			}
			else {
				System.out.println("[OP] Random: " + randomSolution);
				System.out.println("Locally searched solution: " + locallySearchedSolution);
				inLoopBestSolution = randomSolution;
			}
			
			if(inLoopBestSolution.getOF() < finalSolution.getOF())
				finalSolution = inLoopBestSolution;
			
			System.out.println("---------------------------------------");
		}
		
		System.out.println("=======================================");
		
		return finalSolution;
	}
	
	/** TODO: Kevin
	 * Generate and return only one random solution
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

	/** TODO: Tiffany
	 * Return the best solution among many changed solution
	 * (local search algorithm)
	 * @param generatedSolution The solution to begin with
	 * @return the best solution found with the algorithm
	 */
	private Solution localSearch(Solution generatedSolution) {
		boolean continueExploration = true;
		Solution newSolution = null;
		
		while(continueExploration)
		{
			newSolution = exploreNeighborhood(generatedSolution);
			if(newSolution.getOF() < generatedSolution.getOF())
				generatedSolution = newSolution;
			else
				continueExploration = false;
		}
		return generatedSolution;
	}

	/** TODO: Tiffany
	 * Return the best solution among many changed solution
	 * (local search algorithm)
	 * @param generatedSolution The solution to begin with
	 * @return the best solution found with the algorithm
	 */
	private Solution exploreNeighborhood(Solution solution) {
		Solution newSolution = solution;
		Solution swapSolution = solution.clone();

		for(int i = 0; i < solution.size(); i++)
		{
			for(int j = 0; j < solution.size(); j++)
			{
				if(i != j) {
					swapSolution.swap(i, j);
					swapSolution.setOF(TSPCostCalculator.calcOF(instance, swapSolution));
	
					if(swapSolution.getOF() < newSolution.getOF())
						newSolution = swapSolution;
				}
			}
		}
		
		return newSolution;
	}
}
