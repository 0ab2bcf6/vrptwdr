package algorithm;

import alns.MyALNS;
import components.Instance;
import components.Solution;
import config.InterfaceALNSConfiguration;

public class ALNSHeuristicSolver {

	private boolean consoleLog = true;
	
	private int diffRV = 0;
	private int nrRv = 0;

	public ALNSHeuristicSolver() {

	}

	public Solution getInitialSolution(Instance currentInstace) {
		MyInitialGreedySolution greedyInitialSolution = new MyInitialGreedySolution(currentInstace, consoleLog);
		return greedyInitialSolution.getInitialSolution();
	}

	public Solution improveSolution(Solution initialSolution, Instance currentInstace,
			InterfaceALNSConfiguration currentALNSConfig, boolean consoleLog) throws Exception {
		MyALNS ALNSSolution = new MyALNS(initialSolution, currentInstace, currentALNSConfig, consoleLog);
		return ALNSSolution.improveSolution();
	}

	public Solution solve(Instance currentInstace, InterfaceALNSConfiguration currentALNSConfig, boolean consoleLog)
			throws Exception {

		// this is just to have SOME output so the user knows that something is happening
		if (!consoleLog)
			System.out.println("CREATE initial solution...");

		long timeStart = System.currentTimeMillis();

		MyInitialGreedySolution greedyInitialSolution = new MyInitialGreedySolution(currentInstace, consoleLog);
		Solution initialSolution = greedyInitialSolution.getInitialSolution();
		
		long timeEnd = System.currentTimeMillis();

		if (!consoleLog)
			System.out.println("FINISHED initial solution... t: " + ((timeEnd - timeStart) / 1000));

		if (!consoleLog)
			System.out.println("IMPROVE initial solution...");

		timeStart = System.currentTimeMillis();

		MyALNS ALNSSolution = new MyALNS(initialSolution, currentInstace, currentALNSConfig, consoleLog);
		Solution improvedSolution = ALNSSolution.improveSolution();
		timeEnd = System.currentTimeMillis();
		
		this.diffRV = improvedSolution.getNumberRobotVisits() - initialSolution.getNumberRobotVisits();
		this.nrRv = improvedSolution.getNumberRobotVisits();
		
		if (!consoleLog)
			System.out.println("FINISHED improved solution... t: " + ((timeEnd - timeStart) / 1000));

		return improvedSolution;
	}
	
	public int getDiffRV() {
		return this.diffRV;
	}
	
	public int getNrRV() {
		return this.nrRv;
	}
	

}
