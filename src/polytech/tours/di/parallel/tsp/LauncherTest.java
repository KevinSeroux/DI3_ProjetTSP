package polytech.tours.di.parallel.tsp;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Properties;

public class LauncherTest {
	static private final int countTests = 6;
	static private PrintStream outOriginal;
	static private PrintStream outNull;
	static private Properties config;
	static private Algorithm algorithm;

	public static void main(String[] args) {
		//read properties
		config = new Properties();
		try {
			config.loadFromXML(new FileInputStream(args[0]));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		//dynamically load the algorithm class
		algorithm = null;
		try {
			Class<?> c = Class.forName(config.getProperty("algorithm"));
			algorithm = (Algorithm)c.newInstance();
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException e){
			e.printStackTrace();
		}
		
		outOriginal = System.out;
		outNull = new PrintStream(new OutputStream() {
			public void write(int b) {}
		});
		
		//run the test
		executeTest("./data/qa194.tsp.txt", 3);
		executeTest("./data/uy734.tsp.txt", 10);
		executeTest("./data/nu3496.tsp.txt", 50);
	}

	private static void executeTest(String data, int time) {
		config.setProperty("instance", data);
		config.setProperty("maxcpu", Integer.toString(time));
		
		for(int i = 1; i < Runtime.getRuntime().availableProcessors() * 2; i++) {
			config.setProperty("maxthreads", Integer.toString(i));
			long avg = 0;

			System.setOut(outNull);

			//Many tests to have meaning full results
			for(int j = 0; j < countTests; j++) {
				avg += algorithm.run(config).getOF();
			}
			avg /= countTests;

			System.setOut(outOriginal);
			System.out.println("Instance: " + data + "\tThreads: " + i + "\tavg:" + avg);
		}
	}
}
