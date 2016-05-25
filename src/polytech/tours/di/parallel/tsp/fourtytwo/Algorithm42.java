package polytech.tours.di.parallel.tsp.fourtytwo;

import java.util.List;
import java.util.ArrayList;
import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import polytech.tours.di.parallel.tsp.Algorithm;
import polytech.tours.di.parallel.tsp.Instance;
import polytech.tours.di.parallel.tsp.InstanceReader;
import polytech.tours.di.parallel.tsp.Solution;

public class Algorithm42 implements Algorithm {
	long seed, timeMax;
	Instance instance;

	@Override
	public Solution run(Properties config) {
		
		//Build the instance from the config file
		InstanceReader ir = new InstanceReader();
		ir.buildInstance(config.getProperty("instance"));
		instance = ir.getInstance();

		seed = Long.valueOf(config.getProperty("seed"));

		//Manage the timing (*1000 to have the time in millisec)
		timeMax = Long.valueOf(config.getProperty("maxcpu"));
		
		return executeThreads();
	}
	
	public Solution executeThreads() {
		
		//TODO: Manage threads interruption
		int nbThreads = Runtime.getRuntime().availableProcessors();
		List<Solution> solutions = new ArrayList<Solution>(nbThreads);
		List<Callable<Object>> threads = new ArrayList<Callable<Object>>(nbThreads);
		final ScheduledExecutorService executor = Executors.newScheduledThreadPool(nbThreads + 1);
		
		//Thread that cancel all the other one after the timeout
		executor.schedule(new Runnable() {
			public void run() {
				executor.shutdownNow();
			}
		}, timeMax, TimeUnit.SECONDS);
		
		long startTime = System.currentTimeMillis();
		
		//Threads that compute
		for(int i = 0; i < nbThreads; i++) {
			Solution currentSolution = new Solution();
			solutions.add(currentSolution);
			
			ThreadedSolutionFinder solutionFinder =
					new ThreadedSolutionFinder(seed, instance, currentSolution);
			
			threads.add(Executors.callable(solutionFinder));
		}
		
		/* Execute and then wait that all threads have finished */
		try {
			executor.invokeAll(threads);

		} catch (InterruptedException e) {
			e.printStackTrace();
			return null;
		}
		
		long time = System.currentTimeMillis() - startTime;
		System.out.println("---------------------------------------");
		System.out.println("All tasks terminated in " + time / 1000.f + "s");
		
		//Iterate over all the found solutions
		Solution bestSolution = solutions.get(0);
		for(int i = 1; i < nbThreads; i++) {
			Solution currentSolution = solutions.get(i);
			if(currentSolution.getOF() < bestSolution.getOF())
				bestSolution = currentSolution;
		}
		
		return bestSolution;
		
	}
}
