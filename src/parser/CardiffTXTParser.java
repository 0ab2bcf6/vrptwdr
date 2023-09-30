package parser;

import java.io.FileReader;
import java.util.Scanner;

import components.Node;

public class CardiffTXTParser implements InterfaceVRPTWDRDataset {

	private int numberCustomers;

	private double vanSpeed;
	private double robotSpeed;
	private double vanPayload;
	private double vanMaxTravelTime;
	private double robotMaxRadius;
	private double[][] distanceCustomers;
	private double[][] vanTravelTime;
	private double[][] robotTravelTime;
	private Node[] customers;

	private double maxTraveltimeBetweenCustomers;

	public CardiffTXTParser(String path) throws Exception {

		// not in the Cardiff set
		this.vanMaxTravelTime = 1000.0;

		// Open the file.
		FileReader fr = new FileReader(path);
		Scanner inFile = new Scanner(fr);

		this.numberCustomers = Integer.parseInt(inFile.nextLine());

		inFile.nextLine(); // blank line

		this.vanPayload = Double.parseDouble(inFile.nextLine());
		this.vanSpeed = Double.parseDouble(inFile.nextLine());
		this.robotSpeed = Double.parseDouble(inFile.nextLine());
		this.robotMaxRadius = Double.parseDouble(inFile.nextLine());

		inFile.nextLine(); // blank line

		int m = numberCustomers;
		int n = numberCustomers;

		this.distanceCustomers = new double[m + 1][n + 1];
		for (int i = 0; i <= m; i++) {

			String str = inFile.nextLine();
			String[] split = str.split("	");

			for (int j = 0; j <= n; j++) {
				this.distanceCustomers[i][j] = Double.parseDouble(split[j]);
			}
		}

		inFile.nextLine(); // blank line
		inFile.nextLine(); // blank line

		this.customers = new Node[numberCustomers + 1];

		for (int i = 0; i <= numberCustomers; i++) {

			String str = inFile.nextLine();
			str = str.replaceAll("\\s+", ";");
			String[] split = str.split(";");

			customers[i] = new Node(Integer.parseInt(split[0]), split[1], Integer.parseInt(split[2]),
					Integer.parseInt(split[3]), Integer.parseInt(split[4]), Integer.parseInt(split[5]),
					Integer.parseInt(split[6]), Integer.parseInt(split[7]));

			if (i == 0) {
				inFile.nextLine(); // blank line
			}
		}

		inFile.close();

		double[][] s_v = new double[numberCustomers + 1][numberCustomers + 1];
		double[][] s_d = new double[numberCustomers + 1][numberCustomers + 1];

		maxTraveltimeBetweenCustomers = Integer.MIN_VALUE;

		for (int i = 0; i <= numberCustomers; i++) {
			for (int j = 0; j <= numberCustomers; j++) {

				double val = 60 * distanceCustomers[i][j] / (vanSpeed * 1000);

				s_v[i][j] = val;

				val = 60 * distanceCustomers[i][j] / (robotSpeed * 1000);

				s_d[i][j] = val;

				if (maxTraveltimeBetweenCustomers < s_v[i][j]) {
					maxTraveltimeBetweenCustomers = s_v[i][j];
				}
			}
		}

		this.vanTravelTime = s_v;
		this.robotTravelTime = s_d;

	}

	@Override
	public int getNumberCustomers() {
		return this.numberCustomers;
	}

	@Override
	public double getVanMaxTravelTime() {
		return this.vanMaxTravelTime;
	}

	@Override
	public double getVanSpeed() {
		return this.vanSpeed;
	}

	@Override
	public double getVanPayload() {
		return this.vanPayload;
	}

	@Override
	public double getDistanceCustomers(int i, int j) {
		return this.distanceCustomers[i][j];
	}

	@Override
	public Node getCustomers(int i) {
		return this.customers[i];
	}

	@Override
	public double getTravelingTimeVehicle(int i, int j) {
		return vanTravelTime[i][j];
	}

	@Override
	public double getTravelingTimeRobot(int i, int j) {
		return robotTravelTime[i][j];
	}

	@Override
	public double getRobotMaxRadius() {
		return this.robotMaxRadius;
	}

	@Override
	public double getHighestTravelTime() {
		return maxTraveltimeBetweenCustomers;
	}

}