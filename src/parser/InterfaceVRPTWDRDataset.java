package parser;

import components.Node;

public interface InterfaceVRPTWDRDataset {

	public int getNumberCustomers();

	public double getVanSpeed();

	public double getVanPayload();

	public double getDistanceCustomers(int i, int j);

	public Node getCustomers(int i);

	public double getTravelingTimeVehicle(int i, int j);

	public double getTravelingTimeRobot(int i, int j);

	public double getRobotMaxRadius();

	public double getHighestTravelTime();

	public double getVanMaxTravelTime();
}
