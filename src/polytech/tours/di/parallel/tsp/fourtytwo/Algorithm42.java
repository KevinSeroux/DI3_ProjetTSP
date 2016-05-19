package polytech.tours.di.parallel.tsp.fourtytwo;

import java.util.Properties;

import polytech.tours.di.parallel.tsp.Algorithm;
import polytech.tours.di.parallel.tsp.Solution;

public class Algorithm42 implements Algorithm {

	@Override
	public Solution run(Properties config) {
		return null;
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
		return generatedSolution;
	}

	/** TODO: Tiffany
	 * Return the best solution among many changed solution
	 * (local search algorithm)
	 * @param generatedSolution The solution to begin with
	 * @return the best solution found with the algorithm
	 */
	private Solution exploreNeightborhood(Solution solution) {
		return solution;
	}
}
