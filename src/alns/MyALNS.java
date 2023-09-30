package alns;

import java.lang.Math;

import insert.*;
import removal.*;
import config.*;
import components.Instance;
import components.Solution;

public class MyALNS {

	private Instance currentInstance = null;
	private boolean consoleLog = false;

	// parameter
	private final InterfaceALNSConfiguration config;

	// temperature
	private double T;
	// iteration
	private int i = 0;
	// s
	private MyALNSSolution currentSolution = null;
	// s_best
	private MyALNSSolution bestSolution = null;

	// operations
	private final InterfaceALNSRemoval[] removalOperations;
	private final InterfaceALNSInsert[] insertOperations;

	// these are just a dummy variables
	private long timeInitialSolution;
	private String initialSolutionToString;
	private double initialSolutionCost;

	public MyALNS(Solution initialSolution, Instance currentInstance, InterfaceALNSConfiguration config,
			boolean consoleLog) {

		this.consoleLog = consoleLog;

		this.currentInstance = currentInstance;
		this.config = config;

		timeInitialSolution = initialSolution.getTimeForInitialSolution();
		initialSolutionToString = initialSolution.printToString();
		initialSolutionCost = initialSolution.getCost();

		this.currentSolution = new MyALNSSolution(initialSolution);
		this.bestSolution = new MyALNSSolution(currentSolution, 0);
		this.T = config.getEta() * currentSolution.getCost();

		// initialize operations
		removalOperations = new InterfaceALNSRemoval[] { 
				new ShawRemoval(config),
				new ShawRemoval(config.getPhi(), 0, 0, 0), // Distance Removal
				new ShawRemoval(0, config.getChi(), 0, 0), // Time Window Removal
				new WorstRemoval(),
//				new WorstRobotRemoval(),
//				new RandomRemoval(), 
				new RandomRemoval2(), new RouteRemoval() };

		insertOperations = new InterfaceALNSInsert[] { 
				new BestInsert(false), 
				new BestInsert(true), // with Noise
				new GreedyInsert(false), 
				new GreedyInsert(true), 
				new RegretInsert(), // with Noise
		};

		// initialize scores of all heuristics
		for (InterfaceALNSRemoval rmvl : this.removalOperations) {
			rmvl.initialize();
			rmvl.setP(1 / (double) this.removalOperations.length);
		}

		for (InterfaceALNSInsert nsrt : this.insertOperations) {
			nsrt.initialize();
			nsrt.setP(1 / (double) this.insertOperations.length);
		}
	}

	public Solution improveSolution() throws Exception {

		if (consoleLog)
			System.out.println("CREATE improved solution...");

		/*
		 * each iteration consits of 1 removal opertion and 1 insertion operation a new
		 * solution s_new is always approved if cost(s_new) <= cost(s_current) s = s_new
		 * and s_best = s_new
		 */

		long timeStart = System.currentTimeMillis();

		i = 0;

		// calculate ceiling function for removal set
		// int minRemovalNumber = Math.min((int) Math.ceil(0.06 *
		// currentInstance.getNumberCustomers()), 15);
		// int maxRemovalNumber = Math.min((int) Math.ceil(0.30 *
		// currentInstance.getNumberCustomers()), 40);

		int minRemovalNumber = (int) Math.ceil(0.06 * currentInstance.getNumberCustomers());
		int maxRemovalNumber = (int) Math.ceil(0.3 * currentInstance.getNumberCustomers());

		if (consoleLog) {
			System.out.println("ALNS minRemovalNumber: " + minRemovalNumber);
			System.out.println("ALNS maxRemovalNumber: " + maxRemovalNumber);
			System.out.println("");
		}

		while (true) {

			int Gamma = (int) (Math.random() * (maxRemovalNumber - minRemovalNumber + 1) + minRemovalNumber);

			MyALNSSolution cloneCurrentSolution = new MyALNSSolution(currentSolution, Gamma);

			InterfaceALNSRemoval removalOperator = getALNSRemovalOperator();
			InterfaceALNSInsert insertOperator = getALNSInsertOperator();

			// if removaloperator == routeremove then choose GreedyInsert with lesser prob
//			if (removalOperator.getOperatorName() == "Route Removal") {
//				insertOperator = getALNSInsertOperatorLesserProbabilityForGreedy()();
//			} else {
//				insertOperator = getALNSInsertOperator();
//			}

			if (consoleLog) {
				System.out.println("ALNS Temperature: " + T);
				System.out.println("ALNS Iteration: " + i);
				System.out.println("ALNS Gamma: " + Gamma);
				System.out.println("ALNS removalOperator: " + removalOperator.getOperatorName());
				System.out.println("ALNS insertOperator: " + insertOperator.getOperatorName());
				System.out.println("");
			}

			long timeBefore = System.currentTimeMillis();
			MyALNSSolution currentPartial = removalOperator.remove(cloneCurrentSolution);
			long timeAfter = System.currentTimeMillis();
			removalOperator.addToTotalTime(timeAfter - timeBefore);

			timeBefore = System.currentTimeMillis();
			MyALNSSolution newSolution = insertOperator.insert(currentPartial);

			timeAfter = System.currentTimeMillis();
			insertOperator.addToTotalTime(timeAfter - timeBefore);

			// better solution found
			if (newSolution.getCost() < currentSolution.getCost() && newSolution.isFeasible()) {

				if (consoleLog) {
					System.out.println("ALNS better Solution found!");
					System.out.println("ALNS objValue currentSolution: " + currentSolution.getCost());
					System.out.println("ALNS objValue newSolution: " + newSolution.getCost());
					System.out.println("");
				}

				currentSolution = newSolution;

				// new best solution found
				if (newSolution.getCost() < bestSolution.getCost()) {
					
					removalOperator.incThetaImp();
					insertOperator.incThetaImp();

					if (consoleLog) {
						System.out.println("ALNS new best Solution found!");
						System.out.println("ALNS objValue bestSolution: " + bestSolution.getCost());
						System.out.println("ALNS objValue newSolution: " + newSolution.getCost());
						System.out.println("");
					}

					bestSolution = newSolution;

					removalOperator.addToPi(config.getSigma1());
					insertOperator.addToPi(config.getSigma1());

				} else {

					removalOperator.addToPi(config.getSigma2());
					insertOperator.addToPi(config.getSigma2());

				}

			} else {

				if (consoleLog) {
					System.out.println("ALNS worse Solution found!");
					System.out.println("ALNS objValue currentSolution: " + currentSolution.getCost());
					System.out.println("ALNS objValue newSolution: " + newSolution.getCost());
					System.out.println("");
				}

				double acceptanceSA = Math.exp(-(newSolution.getCost() - currentSolution.getCost()) / T);

				if (Math.random() < acceptanceSA && i != config.getOmega()) {

					if (consoleLog) {
						System.out.println("ALNS worse Solution accepted!");
						System.out.println("ALNS objValue currentSolution: " + currentSolution.getCost());
						System.out.println("ALNS objValue (worse) newSolution: " + newSolution.getCost());
						System.out.println("");
					}

					currentSolution = newSolution;
				}

				removalOperator.addToPi(config.getSigma3());
				insertOperator.addToPi(config.getSigma3());
			}

			i++;

			// check if end of segment has been reached
			if (i % config.getTau() == 0 && i > 0) {
				double decreaseSpeed = Math.pow(config.getXi(), i);
				maxRemovalNumber = Math.max(minRemovalNumber,
						(int) Math.ceil(0.3 * currentInstance.getNumberCustomers() * decreaseSpeed));
				long timeEnd = System.currentTimeMillis();
				adaptiveWeightAdjustment();
				if (consoleLog)
					System.out.println(i + " Iterations after " + (timeEnd - timeStart));
			}

			T = config.getC() * T;

			// check if maxNumberOfIterations has been reached
			if (i == config.getOmega() && bestSolution.isFeasible()) {
				break;
			}
		}

		long timeEnd = System.currentTimeMillis();
		long timeSpent = timeEnd - timeStart;

		Solution solution = bestSolution.toSolution();
		solution.setTimeInitialSolution(timeInitialSolution);
		solution.setTimeForImprovedSolution(timeSpent);
		solution.setInitialSolutionCost(initialSolutionCost);
		solution.setInitialSolutionToString(initialSolutionToString);
		solution.isDone();

		currentInstance.printResultToFile(printTimeUsageToString(), "timeusage");

		if (consoleLog)
			System.out.println("FINISHED improved solution!");

		return solution;
	}

	private void adaptiveWeightAdjustment() {

		if (consoleLog)
			System.out.println("ALNS adjust operator weights...");

		double wSum = 0; // to adjust weighted propabilities

		for (InterfaceALNSRemoval rmvl : this.removalOperations) {
			double wPrev = rmvl.getW() * (1 - config.getRp());
			// in case theta_mi = 0
			double piTheta;
			if (rmvl.getTheta() == 0) {
				piTheta = 0;
			} else {
				piTheta = rmvl.getPi() / rmvl.getTheta();
			}
			double wNow = config.getRp() * piTheta + wPrev;
			wSum += wNow;
			rmvl.setW(wNow);
		}

		for (InterfaceALNSRemoval rmvl : this.removalOperations) {
			rmvl.setP(rmvl.getW() / wSum);
		}

		wSum = 0;

		for (InterfaceALNSInsert nsrt : this.insertOperations) {
			double wPrev = nsrt.getW() * (1 - config.getRp());
			// in case theta_mi = 0
			double piTheta;
			if (nsrt.getTheta() == 0) {
				piTheta = 0;
			} else {
				piTheta = nsrt.getPi() / nsrt.getTheta();
			}
			double wNow = config.getRp() * piTheta + wPrev;
			wSum += wNow;
			nsrt.setW(wNow);
		}

		for (InterfaceALNSInsert nsrt : this.insertOperations) {
			nsrt.setP(nsrt.getW() / wSum);
		}
	}

	private InterfaceALNSInsert getALNSInsertOperator() {

		/*
		 * Ropke&Pisinger(2006): To select the heuristic to use, we assign weights to
		 * the different heuristics and use a roulette wheel selection principle
		 */

		double random = Math.random();
		double aggregatedWeightOfHeuristics = 0.0;
		for (InterfaceALNSInsert nsrt : this.insertOperations) {
			aggregatedWeightOfHeuristics += nsrt.getP();
			if (random <= aggregatedWeightOfHeuristics) {
				nsrt.incTheta();
				return nsrt;
			}
		}

		// this is just to please the compiler
		int index = (int) Math.round(random * (insertOperations.length - 1));
		return this.insertOperations[index];
	}

	// LP = "lesser probability" for choosing the greedy insert
	@SuppressWarnings("unused")
	private InterfaceALNSInsert getALNSInsertOperatorLesserProbabilityForGreedy() {

		double valPenalty = 4;

		double random = Math.random();
		double aggregatedWeightOfHeuristics = 0.0;
		for (InterfaceALNSInsert nsrt : this.insertOperations) {

			double selectChance = nsrt.getP();
			if (nsrt.getOperatorName() == "Greedy Insert" || nsrt.getOperatorName() == "Greedy Insert With Noise")
				selectChance = selectChance / valPenalty; // 1/20 prob for both getting selected, 18/20 left
			else {
				selectChance = selectChance + (18 / (20 * insertOperations.length - 2)); // 1/5 + 18/3 /20 for the rest
																							// getting selected
			}

			aggregatedWeightOfHeuristics += selectChance;
			if (random <= aggregatedWeightOfHeuristics) {
				nsrt.incTheta();
				return nsrt;
			}
		}

		// this is just to please the compiler
		int index = (int) Math.round(random * (insertOperations.length - 1));
		return this.insertOperations[index];
	}

	private InterfaceALNSRemoval getALNSRemovalOperator() {

		double random = Math.random();
		double aggregatedWeightOfHeuristics = 0.0;
		for (InterfaceALNSRemoval rmvl : this.removalOperations) {
			aggregatedWeightOfHeuristics += rmvl.getP();
			if (random <= aggregatedWeightOfHeuristics) {
				rmvl.incTheta();
				return rmvl;
			}
		}

		// this is just to please the compiler
		int index = (int) Math.round(random * (removalOperations.length - 1));
		return this.removalOperations[index];
	}

	private String printTimeUsageToString() {
		
		//i probably shouldve parameterized this for ALNSOperations
		//and get rid of redundant code here

		String result = "ALNS Time Usage Summary: \n\n";

		result += "[ ++++++++++ Removal Operators ++++++++++ ]\n";

		for (InterfaceALNSRemoval rmvl : this.removalOperations) {
			result += rmvl.getOperatorName() + "\n";
			result += "Total Number of Uses: " + rmvl.getTheta() + "\n";
			result += "Total Time spent in Operator: " + rmvl.getTotalTime() + "\n";
			result += "Time spent in the Removal Algorithm: " + rmvl.getAlgoTime() + "\n\n";
			if (rmvl.getTheta() != 0) {
				result += "thetaImp: " + rmvl.getThetaImp() + "\n";
				double ibest = rmvl.getThetaImp() / config.getOmega();
				result += "ibest: " + ibest + "\n";
				double iusage = rmvl.getTheta() / config.getOmega();
				result += "iusage: " + iusage + "\n";
				double ratioBestUsage = ibest / iusage;
				result += "ibest/iusage: " + (ratioBestUsage) + "\n\n";
				double average = rmvl.getTotalTime() / rmvl.getTheta();
				result += "Total Time Average per Use: " + average + "\n";
				average = rmvl.getAlgoTime() / rmvl.getTheta();
				result += "Average Time spent in Algorithm per Use: " + average + "\n\n";
			}
		}

		result += "\n";

		result += "[ ++++++++++ Insert Operators ++++++++++ ]\n";

		for (InterfaceALNSInsert nsrt : this.insertOperations) {
			result += nsrt.getOperatorName() + "\n";
			result += "Total Number of Uses: " + nsrt.getTheta() + "\n";
			result += "Total Time spent in Operator: " + nsrt.getTotalTime() + "\n";
			result += "Time spent in the Insert Algorithm: " + nsrt.getAlgoTime() + "\n\n";
			if (nsrt.getTheta() != 0) {
				result += "thetaImp: " + nsrt.getThetaImp() + "\n";
				double ibest = nsrt.getThetaImp() / config.getOmega();
				result += "ibest: " + ibest + "\n";
				double iusage = nsrt.getTheta() / config.getOmega();
				result += "iusage: " + iusage + "\n";
				double ratioBestUsage = ibest / iusage;
				result += "ibest/iusage: " + (ratioBestUsage) + "\n\n";
				double average = nsrt.getTotalTime() / nsrt.getTheta();
				result += "Total Time Average per Use: " + average + "\n";
				average = nsrt.getAlgoTime() / nsrt.getTheta();
				result += "Average Time spent in Algorithm per Use: " + average + "\n\n";
			}
		}

		return result;
	}

}