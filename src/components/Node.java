package components;

public class Node {

	private int id;
	private String postcode;
	private int isRobotAccessible;
	private int demand;

	private int readyTime;
	private int dueTime;

	private int serviceTimeVehicle;
	private int serviceTimeRobot;

	private double regret = 0;
	private double removalCost = 0;
	
	private double timeArrival;
	private double timeService;
	
	private boolean isIgnored = false;

	public Node() {

	}

	public Node(Node n) {
		this.id = n.id;
		this.postcode = n.postcode;
		this.isRobotAccessible = n.isRobotAccessible;
		this.demand = n.demand;
		this.readyTime = n.readyTime;
		this.dueTime = n.dueTime;
		this.serviceTimeVehicle = n.serviceTimeVehicle;
		this.serviceTimeRobot = n.serviceTimeRobot;
	}

	public Node(int id, String postcode, int isRobotAccessible, int demand, int readyTime, int dueTime,
			int serviceTimeVehicle, int serviceTimeRobot) {
		this.id = id;
		this.postcode = postcode;
		this.isRobotAccessible = isRobotAccessible;
		this.demand = demand;
		this.readyTime = readyTime;
		this.dueTime = dueTime;
		this.serviceTimeVehicle = serviceTimeVehicle;
		this.serviceTimeRobot = serviceTimeRobot;
	}

	public int getId() {
		return id;
	}

	public String getPostcode() {
		return postcode;
	}

	public int getRobotAccessible() {
		return isRobotAccessible;
	}

	public int getDemand() {
		return demand;
	}

	public int getReadyTime() {
		return readyTime;
	}

	public double getRemovalCost() {
		return this.removalCost;
	}

	public void setRemovalCost(double r) {
		this.removalCost = r;
	}

	public double getRegret() {
		return this.regret;
	}

	public void setRegret(double r) {
		this.regret = r;
	}

	public int getDueTime() {
		return dueTime;
	}

	public int getServiceTimeVehicle() {
		return serviceTimeVehicle;
	}

	public int getServiceTimeRobot() {
		return serviceTimeRobot;
	}

	public String toString() {
		return "" + this.getId() + " " + this.getPostcode() + " " + this.getRobotAccessible() + " " + this.getDemand()
				+ " " + this.getReadyTime() + " " + this.getDueTime() + " " + this.getServiceTimeVehicle() + " "
				+ this.getServiceTimeRobot();
	}

	public double getTimeArrival() {
		return timeArrival;
	}

	public void setTimeArrival(double timeArrival) {
		this.timeArrival = timeArrival;
	}

	public double getTimeService() {
		return timeService;
	}

	public void setTimeService(double timeService) {
		this.timeService = timeService;
	}

	public boolean isIgnored() {
		return isIgnored;
	}

	public void setIgnored(boolean isIgnored) {
		this.isIgnored = isIgnored;
	}
}
