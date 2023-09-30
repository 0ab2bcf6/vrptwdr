
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import algorithm.ALNSHeuristicSolver;
import algorithm.GurobiSolver;
import algorithm.MyInitialGreedySolution;
import components.Instance;
import components.Node;
import components.Route;
import components.RouteNode;
import components.Solution;

import config.ALNSConfiguration;
import parser.MyGraphicsTool;

@SuppressWarnings("unused")
public class Main {

	private static final String[] CardiffInstanceSets = new String[] { "15", "25", "50", "75", "100", "150", "200" };

	private static final String[] CardiffInstanceSubsets = new String[] { "01", "02", "03", "04", "05", "06", "07",
			"08", "09", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20" };

	private static final String[] SolomonInstanceSets = new String[] { "C101", "C102", "C103", "C104", "C105", "C106",
			"C107", "C108", "C109", "C201", "C202", "C203", "C204", "C205", "C206", "C207", "C208", "R101", "R102",
			"R103", "R104", "R105", "R106", "R107", "R108", "R109", "R110", "R111", "R112", "R201", "R202", "R203",
			"R204", "R205", "R206", "R207", "R208", "R209", "R210", "R211", "RC101", "RC102", "RC103", "RC104", "RC105",
			"RC106", "RC107", "RC108", "RC201", "RC202", "RC203", "RC204", "RC205", "RC206", "RC207", "RC208" };

	private static final String[] SolomonInstanceSubsets = new String[] { "025", "050", "100" };

	private static final String[] HombergerInstanceSets = new String[] { "C1_02", "C1_04", "C1_06", "C1_08", "C1_10",
			"C2_02", "C2_04", "C2_06", "C2_08", "C2_10", "R1_02", "R1_04", "R1_06", "R1_08", "R1_10", "R2_02", "R2_04",
			"R2_06", "R2_08", "R2_10", "RC1_02", "RC1_04", "RC1_06", "RC1_08", "RC1_10", "RC2_02", "RC2_04", "RC2_06",
			"RC2_08", "RC2_10", };

	private static final String[] HombergerInstanceSubsets = new String[] { "01", "02", "03", "04", "05", "06", "07",
			"08", "09", "10" };

	private static final int numberRobots = 4;

	private static final int[] numberVans = new int[] { 3, 3, 5, 5, 10, 10, 10};

	// this is to log whats happening in the ALNS Process
	static boolean consoleLog = false;

	/*
	 * for the cardiff instances i manually wrote an array to provide a sufficient
	 * amount of vans for each set of customers the solomon and homberger instances
	 * are not designed for robotvisits and the fleet of vans is given so no matter
	 * what you feed to the constructor the fleet in the set determines the amount
	 * of vans used in that instance
	 * 
	 * to differenciate between simplex solved solutions and heuristically solved
	 * solutions i added a boolean to the instance constructor. the results will be
	 * printed to the /src/results/ directory in the subdirectory of the specified
	 * set. heuristically solved solutions have the same name as the dataset file,
	 * simplex solved solutions end with "_GRB.txt". the objValue in the simplex
	 * solved solutions might vary from the sum or partial costs. in doubt the
	 * objValue is to be believed. to track the heuristics algorithm performance
	 * there will also be a file in the same directory that ends of "_timeUsage.txt"
	 * in which theres every ALNSOperation listed with its time usage and averages
	 */

	public static void main(String[] args) throws Exception {
		cardiffExp();
	}

	private static void solveCardiffGurobi() throws Exception{

		for (int i = 0; i < 4; i++) {
			
			String strInitImpOverview = "";
			
			for (int j = 0; j < CardiffInstanceSubsets.length; j++) {						
				
				GurobiSolver myGurobiSolver = new GurobiSolver();
				
				Instance instanceGRB = new Instance(numberVans[i], 4, CardiffInstanceSets[i], CardiffInstanceSubsets[j], true, consoleLog);
				
				Solution mySol = myGurobiSolver.solve(instanceGRB);
//				instance.printResultToFile(mySol.printToString());
				
				System.out.println("" + mySol.grbCost() + " " + mySol.getTimeForImprovedSolution());
				strInitImpOverview += "" + mySol.grbCost() + " " + mySol.getTimeForImprovedSolution() + "\n";
				
			}
			
			printResultToFile(strInitImpOverview, "" + CardiffInstanceSets[i] + "_GRB");
			
		}

	}

	private static void analyzeCustomerSets() throws Exception {

		/**
		 * check for each instance how many customers are robotvisitable and how wide
		 * the timewindows are
		 */

		/**
		 * for each instance set we create an overview of the form C=15 (NRV)
		 */

		String result = "";

		for (int i = 0; i < CardiffInstanceSets.length; i++) {

			result += "C=" + CardiffInstanceSets[i] + "\n";

			int avg = 0;
			for (int j = 0; j < CardiffInstanceSubsets.length; j++) {

				Instance instance = new Instance(5, 4, CardiffInstanceSets[i], CardiffInstanceSubsets[j], false,
						consoleLog);

				int count = 0;
				for (int k = 1; k < CardiffInstanceSets.length + 1; k++) {
					if (instance.getCustomers(k).getRobotAccessible() == 1) {
						count++;
					}
				}

				avg += count;
				result += "" + count + "\n";
			}

			double realavg = avg / 20;

			result += "" + realavg + "\n\n";

		}
		// printResultToFile(result);
	}

	private static void cardiffExp() throws Exception {

		/**
		 * create file for each subset that tracks initial solution improved solution
		 **/

		String strInitImpOverview = "";
		String strNextSolution = "";

		for (int i = 0; i < CardiffInstanceSets.length; i++) {
			
			strInitImpOverview = "";
			
			for (int j = 0; j < CardiffInstanceSubsets.length; j++) {

				int k = 0;
				int nRv = 0;
				int diff = 0;
				double impSol = Double.MAX_VALUE;
				double initSol = Double.MAX_VALUE;
				long timeforCompl = 100000000;

				while (k < 4) {

					ALNSHeuristicSolver myALNSHeuristicSolver = new ALNSHeuristicSolver();

					Instance instance = new Instance(numberVans[i], numberRobots, CardiffInstanceSets[i],
							CardiffInstanceSubsets[j], false, consoleLog);

					Solution myImprovedSolution = myALNSHeuristicSolver.solve(instance, ALNSConfiguration.DEFAULT,
							consoleLog);
					if (myImprovedSolution.getCost() < impSol) {
						initSol = myImprovedSolution.getInitialSolutionCost();
						impSol = myImprovedSolution.getCost();
						instance.printResultToFile(myImprovedSolution.printToString());
						MyGraphicsTool myGr = new MyGraphicsTool(myImprovedSolution, CardiffInstanceSets[i] + "_" + CardiffInstanceSubsets[j]);
						timeforCompl = myImprovedSolution.getTimeForImprovedSolution() + myImprovedSolution.getTimeForInitialSolution();
						nRv = myALNSHeuristicSolver.getNrRV();
						diff = myALNSHeuristicSolver.getDiffRV();
					}
					k++;
				}
				strInitImpOverview += initSol + " " + impSol + " " + nRv + " " + diff + " " + timeforCompl + "\n";
			}
			printResultToFile(strInitImpOverview, "maincase" + CardiffInstanceSets[i]);
		}

	}

	private static void printResultToFile(String result, String name) {

		String filePath = System.getProperty("user.dir") + "\\initImpDiff" + name + ".txt";

		try {
			File file = new File(filePath);
			file.createNewFile();

			try (FileOutputStream fos = new FileOutputStream(file);
					BufferedOutputStream bos = new BufferedOutputStream(fos)) {
				// convert string to byte array
				byte[] bytes = result.getBytes();
				// write byte array to file
				bos.write(bytes);
				bos.close();
				fos.close();
			} catch (IOException e) {
				e.printStackTrace();
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void solveCardiffExample(String s) throws Exception {

		// create Instance(20 vans max, 4 robots per van, set 200 (from the cardiff
		// set), subset 07, not solved with a Simplex Solver)
//		Instance instance = new Instance(5, 4, "50", s, false, consoleLog);
//
//		// create a HeuristicSolver Object
//		ALNSHeuristicSolver myALNSHeuristicSolver = new ALNSHeuristicSolver();
//
//		// create a Solution Object
//		Solution myImprovedSolution = myALNSHeuristicSolver.solve(instance, ALNSConfiguration.DEFAULT, consoleLog);
//
//		// print the Solution to a file
//		instance.printResultToFile(myImprovedSolution.printToString());
	}

	private static void solveHomExample() throws Exception {

		// create Instance(number of vans is predefined in the set, no robots allowed,
		// set "R1_04" (from th homberger set), subset "01", not solved with a Simplex
		// Solver)
//		Instance instance = new Instance(0, 0, "RC2_04", "10", false, consoleLog);
//
//		// create a HeuristicSolver Object
//		ALNSHeuristicSolver myALNSHeuristicSolver = new ALNSHeuristicSolver();
//
//		// create a Solution Object
//		Solution myImprovedSolution = myALNSHeuristicSolver.solve(instance, ALNSConfiguration.DEFAULT, consoleLog);
//
//		// print the Solution to a file
//		instance.printResultToFile(myImprovedSolution.printToString());
	}

	private static void solveSolExample() throws Exception {

		// create Instance(number of vans is predefined in the set, no robots allowed,
		// set "R1_04" (from th homberger set), subset "01", not solved with a Simplex
		// Solver)
//		Instance instance = new Instance(0, 0, "RC104", "100", false, consoleLog);
//
//		// create a HeuristicSolver Object
//		ALNSHeuristicSolver myALNSHeuristicSolver = new ALNSHeuristicSolver();
//
//		// create a Solution Object
//		Solution myImprovedSolution = myALNSHeuristicSolver.solve(instance, ALNSConfiguration.DEFAULT, consoleLog);
//
//		// print the Solution to a file
//		instance.printResultToFile(myImprovedSolution.printToString());
	}

	private static void solveAllCardiffInstances() throws Exception {

//		String result = "";
//		String instanceRes = "";
//
//		for (int j = 4; j < CardiffInstanceSubsets.length; j++) {
//
//			GurobiSolver myGurobiSolver = new GurobiSolver();
//			Instance instance = new Instance(2, 4, "50", CardiffInstanceSubsets[j], true, consoleLog);
//
//			Solution myGurobiSolution = myGurobiSolver.solve(instance);
//			instance.printResultToFile(myGurobiSolution.printToString());
//
//		}

	}

	private static void solveAllSolomonInstances() throws Exception {

		ALNSHeuristicSolver myALNSHeuristicSolver = new ALNSHeuristicSolver();
		GurobiSolver myGurobiSolver = new GurobiSolver();

//		for (int i = 0; i < SolomonInstanceSets.length; i++) {
//			for (int j = 0; j < SolomonInstanceSubsets.length; j++) {
//
//				Instance instance = new Instance(0, 0, SolomonInstanceSets[i], SolomonInstanceSubsets[j], false,
//						consoleLog);
//
//				Solution myImprovedSolution = myALNSHeuristicSolver.solve(instance, ALNSConfiguration.DEFAULT,
//						consoleLog);
//				instance.printResultToFile(myImprovedSolution.printToString());
//
//				instance = new Instance(0, 0, SolomonInstanceSets[i], SolomonInstanceSubsets[j], true, consoleLog);
//
//				Solution myGurobiSolution = myGurobiSolver.solve(instance);
//				instance.printResultToFile(myGurobiSolution.printToString());
//			}
//		}

	}

	private static void solveAllHombergerInstances() throws Exception {

//		ALNSHeuristicSolver myALNSHeuristicSolver = new ALNSHeuristicSolver();
//		GurobiSolver myGurobiSolver = new GurobiSolver();

//		for (int j = 2; j < SolomonInstanceSubsets.length; j++) {
//
//			String strInitImpOverview = "";
//			String strNextSolution = "";
//
//			for (int i = 0; i < SolomonInstanceSets.length; i++) {
//
//				System.out.println(
//						"Solomon " + SolomonInstanceSets[i] + "_" + SolomonInstanceSubsets[j] + " instanciated!");
//
//				if (SolomonInstanceSets[i] != "R101" || SolomonInstanceSets[i] != "R102") {
//					int k = 0;
//					int nRv = 0;
//					double impSol = Double.MAX_VALUE;
//					double initSol = Double.MAX_VALUE;
//
//					while (k < 4) {
//
//						Instance instance = new Instance(0, 0, SolomonInstanceSets[i], SolomonInstanceSubsets[j], false,
//								consoleLog);
//
//						ALNSHeuristicSolver myALNSHeuristicSolver = new ALNSHeuristicSolver();
//
//						Solution myImprovedSolution = myALNSHeuristicSolver.solve(instance, ALNSConfiguration.DEFAULT,
//								consoleLog);
//						instance.printResultToFile(myImprovedSolution.printToString());
//
//						if (myImprovedSolution.getCost() < impSol) {
//							initSol = myImprovedSolution.getInitialSolutionCost();
//							impSol = myImprovedSolution.getCost();
//							instance.printResultToFile(myImprovedSolution.printToString());
//							nRv = 0;
//							// MyGraphicsTool mgt = new MyGraphicsTool(myImprovedSolution,
//							// CardiffInstanceSets[i] + "_" + CardiffInstanceSubsets[j]);
//						}
//						k++;
//					}
//					strInitImpOverview += initSol + " " + impSol + " " + SolomonInstanceSets[i] + "_"
//							+ SolomonInstanceSubsets[j] + "\n";
//				}
//			}
//
//			printResultToFile(strInitImpOverview, SolomonInstanceSubsets[j]);
//		}

//				instance = new Instance(0, 0, HombergerInstanceSets[i], HombergerInstanceSubsets[j], true, consoleLog);
//
//				Solution myGurobiSolution = myGurobiSolver.solve(instance);
//				instance.printResultToFile(myGurobiSolution.printToString());

	}

}