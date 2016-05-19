package polytech.tours.di.parallel.tsp.fourtytwo;

import java.util.Properties;

import polytech.tours.di.parallel.tsp.Algorithm;
import polytech.tours.di.parallel.tsp.Instance;
import polytech.tours.di.parallel.tsp.InstanceReader;
import polytech.tours.di.parallel.tsp.Solution;

public class Algorithm42 implements Algorithm {

	@Override
	public Solution run(Properties config) {
		Solution finalSolution = null;
		
		//Build the instance from the config file
		InstanceReader ir = new InstanceReader();
		ir.buildInstance(config.getProperty("instance"));
		Instance instance = ir.getInstance();

		//Manage the timing (*1000 to have the time in millisec)
		long timeMax = Long.valueOf(config.getProperty("maxcpu")) * 1000;
		long startTime = System.currentTimeMillis();
		while((System.currentTimeMillis() - startTime) < timeMax) {
			
			//Here we go
			Solution randomSolution = generateRandomSolution();
			Solution locallySearchedSolution = localSearch(randomSolution);
			
			if(locallySearchedSolution.getOF() > randomSolution.getOF())
				finalSolution = locallySearchedSolution;
			else
				finalSolution = randomSolution;
		}
		
		return finalSolution;
	}
	
	/** TODO: Kevin
	 * Generate and return only one random solution
	 * @return the random solution
	 */
	private Solution generateRandomSolution() {
		
		
		return null;
	}

	/** TODO: Tiffany
	 * Return the best solution among many changed solution
	 * (local search algorithm)
	 * @param generatedSolution The solution to begin with
	 * @return the best solution found with the algorithm
	 */
	private Solution localSearch(Solution generatedSolution) {
		boolean continueExploration = true;
		Solution newSolution = new Solution();
		
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
		Solution newSolution = new Solution();
		Solution swapSolution = new Solution();
		newSolution = solution;
		swapSolution = solution.clone();
		int i, j = 0;

		for(i = 0; i<solution.size(); i++)
		{
			for(j = 0; j<solution.size(); j++)
			{
				swapSolution.swap(i, j); //control special cases (e.g. i=j)

				if(swapSolution.getOF() < newSolution.getOF())
					newSolution = swapSolution;
			}
		}
		
		return newSolution;
	}
}
