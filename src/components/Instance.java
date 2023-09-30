package components;

import parser.*;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;

import java.io.IOException;
import java.util.ArrayList;

public class Instance {

	private String nameInstance = "";
	private String nameDataset = "";
	@SuppressWarnings("unused")
	private boolean consoleLog = false;

	private int numberVans;
	private int numberRobots;

	// the actual data of the dataset
	private int numberCustomers;
	private double vanPayload;
	private double vanMaxTravelTime;
	private double robotMaxRadius;
	private double[][] distanceCustomers;
	private double[][] vanTravelTime;
	private double[][] robotTravelTime;
	private Node[] customers;
	
	private boolean isVRPTWDR = false;

	private double maxTraveltimeBetweenCustomers;
	
	public ArrayList<Node> listCustomers = new ArrayList<Node>();

	// list of all datasets
	private static final String[] CardiffInstanceSets = new String[] { "15", "25", "50", "75", "100", "150", "200" };

	@SuppressWarnings("unused")
	private static final String[] CardiffInstanceSubsets = new String[] { "01", "02", "03", "04", "05", "06", "07",
			"08", "09", "10", "11", "12", "13", "14", "15" };

	private static final String[] SolomonInstanceSets = new String[] { "C101", "C102", "C103", "C104", "C105", "C106",
			"C107", "C108", "C109", "C201", "C202", "C203", "C204", "C205", "C206", "C207", "C208", "R101", "R102",
			"R103", "R104", "R105", "R106", "R107", "R108", "R109", "R110", "R111", "R112", "R201", "R202", "R203",
			"R204", "R205", "R206", "R207", "R208", "R209", "R210", "R211", "RC101", "RC102", "RC103", "RC104", "RC105",
			"RC106", "RC107", "RC108", "RC201", "RC202", "RC203", "RC204", "RC205", "RC206", "RC207", "RC208" };

	@SuppressWarnings("unused")
	private static final String[] SolomonInstanceSubsets = new String[] { "025", "050", "100" };

	private static final String[] HombergerInstanceSets = new String[] { "C1_02", "C1_04", "C1_06", "C1_08", "C1_10",
			"C2_02", "C2_04", "C2_06", "C2_08", "C2_10", "R1_02", "R1_04", "R1_06", "R1_08", "R1_10", "R2_02", "R2_04",
			"R2_06", "R2_08", "R2_10", "RC1_02", "RC1_04", "RC1_06", "RC1_08", "RC1_10", "RC2_02", "RC2_04", "RC2_06",
			"RC2_08", "RC2_10", };

	@SuppressWarnings("unused")
	private static final String[] HombergerInstanceSubsets = new String[] { "01", "02", "03", "04", "05", "06", "07",
			"08", "09", "10" };

	// the path for the instances and results
	private String userDir = System.getProperty("user.dir");
	private String pathInstance = "RPLC3srcRPLC3datasetsRPLC3RPLC0 instancesRPLC3RPLC1";
	private String dirResult = "";
	private String pathResult = "";

	public Instance(int numberVans, int numberRobots, String instanceSet, String instanceSubset, boolean solvedByGrb, boolean consoleLog)
			throws Exception {
		
		if(consoleLog) System.out.println("parsing Dataset...");
		
		this.consoleLog = consoleLog;

		this.numberVans = numberVans;
		this.numberRobots = numberRobots;

		/*
		 * this is to identify which set we re working with, paired with some ugly code
		 * to construct the path for the dataset, the path for the result and the
		 * directory that needs to be created. i ran into weird issues replacing certain
		 * characters so i defaulted to using RPLC0, RPLC1, RPLC2, RPLC3 this code is
		 * probably insanely redundant but i didnt want to deal with it for too long
		 */
		
		/*
		 * its really really ugly.. dont comment on it please lol i encountered some weird issues
		 * */

		if (checkIfStringInArray(instanceSet, CardiffInstanceSets)) {
			
			this.nameDataset = "Cardiff";

			pathInstance = pathInstance.replaceAll("RPLC0", "Cardiff");
			pathInstance = pathInstance.replaceAll("RPLC1", "data" + instanceSet);
			dirResult = pathInstance + "RPLC3";
			nameInstance = instanceSet + "_" + instanceSubset;
			pathInstance += "RPLC3" + "Cardiff" + instanceSet + "_RPLC2.txt";
			pathInstance = pathInstance.replaceAll("RPLC2", instanceSubset);

			pathResult = pathInstance;
			pathResult = pathResult.replaceAll("srcRPLC3datasets", "results");
			dirResult = dirResult.replaceAll("srcRPLC3datasets", "results");

			pathInstance = pathInstance.replaceAll("RPLC3", "\\\\");
			pathResult = pathResult.replaceAll("RPLC3", "\\\\");
			dirResult = dirResult.replaceAll("RPLC3", "\\\\");

			pathInstance = userDir + pathInstance;
			dirResult = userDir + dirResult;
			pathResult = userDir + pathResult;
			
//			pathResult = pathResult.replaceAll(".txt", "_" + speed + "kmh_" + numberRobots + "r.txt");

			// parse instance
			parseCardiffInstance(pathInstance);

		} else if (checkIfStringInArray(instanceSet, SolomonInstanceSets)) {
			
			this.nameDataset = "Solomon";

			pathInstance = pathInstance.replaceAll("RPLC0", "Solomon");

			if (instanceSet.contains("RC")) {
				pathInstance = pathInstance.replaceAll("RPLC1",
						"solomon-1987-" + instanceSet.substring(0, 3).toLowerCase());
			} else {
				pathInstance = pathInstance.replaceAll("RPLC1",
						"solomon-1987-" + instanceSet.substring(0, 2).toLowerCase());
			}

			nameInstance = instanceSet + "_" + instanceSubset;

			dirResult = pathInstance + "RPLC3";
			pathInstance += "RPLC3" + instanceSet + "_RPLC2.xml";

			pathInstance = pathInstance.replaceAll("RPLC2", instanceSubset);

			pathResult = pathInstance;
			pathResult = pathResult.replaceAll("srcRPLC3datasets", "results");
			dirResult = dirResult.replaceAll("srcRPLC3datasets", "results");

			pathInstance = pathInstance.replaceAll("RPLC3", "\\\\");
			pathResult = pathResult.replaceAll("RPLC3", "\\\\");
			pathResult = pathResult.replaceAll("xml", "txt");
			dirResult = dirResult.replaceAll("RPLC3", "\\\\");

			pathInstance = userDir + pathInstance;
			dirResult = userDir + dirResult;
			pathResult = userDir + pathResult;

			// parse instance
			parseSolomonInstance(pathInstance);

		} else if (checkIfStringInArray(instanceSet, HombergerInstanceSets)) {
			
			this.nameDataset = "Homberger";

			pathInstance = pathInstance.replaceAll("RPLC0", "Homberger");

			if (instanceSet.contains("RC")) {
				pathInstance = pathInstance.replaceAll("RPLC1",
						"gehring-and-homberger-1999-" + instanceSet.substring(0, 3).toLowerCase());
			} else {
				pathInstance = pathInstance.replaceAll("RPLC1",
						"gehring-and-homberger-1999-" + instanceSet.substring(0, 2).toLowerCase());
			}

			nameInstance = instanceSet + "_" + instanceSubset;

			dirResult = pathInstance + "RPLC3";
			pathInstance += "RPLC3" + instanceSet + "_RPLC2.xml";

			pathInstance = pathInstance.replaceAll("RPLC2", instanceSubset);

			pathResult = pathInstance;
			pathResult = pathResult.replaceAll("srcRPLC3datasets", "results");
			dirResult = dirResult.replaceAll("srcRPLC3datasets", "results");

			pathInstance = pathInstance.replaceAll("RPLC3", "\\\\");
			pathResult = pathResult.replaceAll("RPLC3", "\\\\");
			pathResult = pathResult.replaceAll("xml", "txt");
			dirResult = dirResult.replaceAll("RPLC3", "\\\\");

			pathInstance = userDir + pathInstance;
			dirResult = userDir + dirResult;
			pathResult = userDir + pathResult;

			// parse instance
			parseHombergerInstance(pathInstance);
		}
		
		if(consoleLog) {
			System.out.println("Instance: " + this.nameDataset + " " + this.nameInstance);
			System.out.println("-------- Number Customers: " + this.numberCustomers);
			System.out.println("-------- Number Vans: " + this.numberVans);
			System.out.println("-------- Van Payload Capacity: " + this.vanPayload);
			System.out.println("-------- Van Max. Travel Time: " + this.vanMaxTravelTime);
			System.out.println("-------- Number Delivery Robots: " + this.numberRobots);
			System.out.println("-------- Delivery Robot Max. Radius: " + this.robotMaxRadius);
		}
		

		if (solvedByGrb) {
			pathResult = pathResult.replaceAll(".txt", "_GRB.txt");
		}
	}

	public String getInstanceName() {
		return this.nameInstance;
	}
	
	public String getDatasetName() {
		return this.nameDataset;
	}

	public int getNumberVans() {
		return this.numberVans;
	}

	public int getNumberCustomers() {
		return this.numberCustomers;
	}

	public int getNumberRobots() {
		return this.numberRobots;
	}

	public double getVanPayload() {
		return this.vanPayload;
	}

	public double getDistanceCustomers(int i, int j) {
		return this.distanceCustomers[i][j];
	}

	public Node getCustomers(int i) {
		return this.customers[i];
	}
	
	public ArrayList<Node> getListCustomers(){
		return this.listCustomers;
	}

	public double getTravelingTimeVehicle(int i, int j) {
		return this.vanTravelTime[i][j];
	}

	public double getTravelingTimeRobot(int i, int j) {
		return robotTravelTime[i][j];
	}

	public double getRobotMaxRadius() {
		return this.robotMaxRadius;
	}

	public double getHighestTravelTime() {
		return maxTraveltimeBetweenCustomers;
	}

	public double getVanMaxTravelTime() {
		return vanMaxTravelTime;
	}

	private boolean checkIfStringInArray(String str, String[] arr) {
		boolean result = false;
		for (int i = 0; i < arr.length; i++) {
			if (str == arr[i]) {
				result = result || true;
			}
		}
		return result;
	}

	public void AllToString() {
		String str = " data [" + numberCustomers + "][" + numberVans + "][" + numberRobots + "][" + vanPayload + "]["
				+ robotMaxRadius + "][" + distanceCustomers[0][5] + "][" + vanTravelTime[0][5] + "]["
				+ robotTravelTime[0][5];
		System.out.println(str);
	}

	private void parseCardiffInstance(String path) throws Exception {

		CardiffTXTParser prsr = new CardiffTXTParser(path);

		this.numberCustomers = prsr.getNumberCustomers();
		this.vanPayload = prsr.getVanPayload();
		this.robotMaxRadius = prsr.getRobotMaxRadius();
		this.vanMaxTravelTime = prsr.getVanMaxTravelTime();
		
		this.isVRPTWDR = true;

		this.customers = new Node[numberCustomers + 1];
		this.distanceCustomers = new double[numberCustomers + 1][numberCustomers + 1];
		this.vanTravelTime = new double[numberCustomers + 1][numberCustomers + 1];
		this.robotTravelTime = new double[numberCustomers + 1][numberCustomers + 1];
		
		listCustomers = new ArrayList<Node>(numberCustomers);

		for (int i = 0; i <= numberCustomers; i++) {

			this.customers[i] = prsr.getCustomers(i);
			if(i!=0)
				listCustomers.add(prsr.getCustomers(i));

			for (int j = 0; j <= numberCustomers; j++) {

				this.distanceCustomers[i][j] = prsr.getDistanceCustomers(i, j);
				this.vanTravelTime[i][j] = prsr.getTravelingTimeVehicle(i, j);
				this.robotTravelTime[i][j] = prsr.getTravelingTimeRobot(i, j);
//				this.robotTravelTime[i][j] = 60 * this.distanceCustomers[i][j] / (speed*1000);
			}
		}

	}

	private void parseSolomonInstance(String path) throws Exception {

		SolomonHombergerXMLParser prsr = new SolomonHombergerXMLParser(path);

		this.numberCustomers = prsr.getNumberCustomers();
		this.numberVans = prsr.getNumberVans();
		this.numberRobots = prsr.getNumberRobots();
		this.robotMaxRadius = prsr.getRobotMaxRadius();
		this.vanMaxTravelTime = prsr.getVanMaxTravelTime();
		
		this.isVRPTWDR = false;

//        System.out.println("SOLOMON Instances requires " + this.numberVans + " Vans!");
//        System.out.println("-> Number Of Vans set to " + this.numberVans);
//        System.out.println("SOLOMON Instances do not use Robots!");

		this.vanPayload = prsr.getVanPayload();
		this.robotMaxRadius = prsr.getRobotMaxRadius();

		this.customers = new Node[numberCustomers + 1];
		this.distanceCustomers = new double[numberCustomers + 1][numberCustomers + 1];
		this.vanTravelTime = new double[numberCustomers + 1][numberCustomers + 1];
		this.robotTravelTime = new double[numberCustomers + 1][numberCustomers + 1];
		
		listCustomers = new ArrayList<Node>(numberCustomers);

		for (int i = 0; i <= numberCustomers; i++) {

			this.customers[i] = prsr.getCustomers(i);
			if(i!=0)
				listCustomers.add(prsr.getCustomers(i));

			for (int j = 0; j <= numberCustomers; j++) {

				this.distanceCustomers[i][j] = prsr.getDistanceCustomers(i, j);
				this.vanTravelTime[i][j] = prsr.getTravelingTimeVehicle(i, j);
				this.robotTravelTime[i][j] = prsr.getTravelingTimeRobot(i, j);
			}
		}
	}

	private void parseHombergerInstance(String path) throws Exception {

		SolomonHombergerXMLParser prsr = new SolomonHombergerXMLParser(path);

		this.numberCustomers = prsr.getNumberCustomers();
		this.numberVans = prsr.getNumberVans();
		this.vanPayload = prsr.getVanPayload();
		this.robotMaxRadius = prsr.getRobotMaxRadius();
		this.vanMaxTravelTime = prsr.getVanMaxTravelTime();
		
		this.isVRPTWDR = false;

//        System.out.println("HOMBERGER Instances requires " + this.numberVans + " Vans!");
//        System.out.println("-> Number Of Vans set to " + this.numberVans);
//        System.out.println("HOMBERGER Instances do not use Robots!");

		this.customers = new Node[numberCustomers + 1];
		this.distanceCustomers = new double[numberCustomers + 1][numberCustomers + 1];
		this.vanTravelTime = new double[numberCustomers + 1][numberCustomers + 1];
		this.robotTravelTime = new double[numberCustomers + 1][numberCustomers + 1];
		
		listCustomers = new ArrayList<Node>(numberCustomers);

		for (int i = 0; i <= numberCustomers; i++) {

			this.customers[i] = prsr.getCustomers(i);
			if(i!=0)
				listCustomers.add(prsr.getCustomers(i));
			
			for (int j = 0; j <= numberCustomers; j++) {

				this.distanceCustomers[i][j] = prsr.getDistanceCustomers(i, j);
				this.vanTravelTime[i][j] = prsr.getTravelingTimeVehicle(i, j);
				this.robotTravelTime[i][j] = prsr.getTravelingTimeRobot(i, j);
			}
		}
	}

	public void printResultToFile(String result) {

		try {
			File directory = new File(dirResult);
			if (!directory.exists()) {
				directory.mkdirs();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		try {
			File file = new File(pathResult);
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

	public void printResultToFile(String result, String name) {

		try {
			File directory = new File(dirResult);
			if (!directory.exists()) {
				directory.mkdirs();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		try {
			File file = new File(dirResult + this.nameInstance + "_timeUsage.txt");
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
	
	public void printResultToPicture() {
		
	}

	public String getPathResult() {
		return this.pathResult;
	}

	public boolean isVRPTWDR() {
		return isVRPTWDR;
	}

	public void setVRPTWDR(boolean isVRPTWDR) {
		this.isVRPTWDR = isVRPTWDR;
	}
}