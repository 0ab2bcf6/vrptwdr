package components;

public class RouteNode {

	/*
	 * this class purely exists because i need to be able to distinguish between
	 * nodes that build my route and notes that are being visited by robots for
	 * which im not using a designated class. by the time im commenting this i
	 * actually forgot when it was apparent that i needed some other class but it
	 * was something within the route object that i was coding
	 */

	double arrivaltime;
	double deliverytime;
	double servicetime;
	double idletime = 0.0;

	double earliestPossibleArrival;
	double latestPossibleArrival;
	double earliestPossibleDeparture;
	double latestPossibleDeparture;
	
	double timeSlack;
	
	private boolean isIgnored = false;

	private Node n; // the actual node in the dataset

	public RouteNode(Node n) {
		this.isIgnored = n.isIgnored();
		this.n = n;
		this.servicetime = n.getServiceTimeVehicle();
	}

	public Node getNode() {
		return this.n;
	}

	public double getReadyTime() {
		return this.n.getReadyTime();
	}

	public double getDueTime() {
		return this.n.getDueTime();
	}

	public int getId() {
		return this.n.getId();
	}

	public double getServiceTime() {
		return this.servicetime;
	}

	public double getArrivalTime() {
		return this.arrivaltime;
	}

	public void setArrivalTime(double a) {
		this.arrivaltime = a;
	}

	public double getDeliveryTime() {
		return this.deliverytime;
	}

	public void setDeliveryTime(double a) {
		this.deliverytime = a;
	}

	public double getIdleTime() {
		return this.idletime;
	}

	public void setIdleTime(double a) {
		this.idletime = a;
	}

	public double getEPA() {
		return this.earliestPossibleArrival;
	}

	public void setEPA(double a) {
		this.earliestPossibleArrival = a;
	}

	public double getLPA() {
		return this.latestPossibleArrival;
	}

	public void setLPA(double a) {
		this.latestPossibleArrival = a;
	}

	public double getEPD() {
		return this.earliestPossibleDeparture;
	}

	public void setEPD(double a) {
		this.earliestPossibleDeparture = a;
	}

	public double getLPD() {
		return this.latestPossibleDeparture;
	}

	public void setLPD(double a) {
		this.latestPossibleDeparture = a;
	}
	
	public double getTimeSlack() {
		return this.timeSlack;
	}

	public void setTimeSlack(double a) {
		this.timeSlack = a;
	}

	public boolean isIgnored() {
		return isIgnored;
	}

	public void setIgnored(boolean isIgnored) {
		this.isIgnored = isIgnored;
	}
}
