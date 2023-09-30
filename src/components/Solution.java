package components;

import java.util.*;

public class Solution {

	private double objValue;
	private boolean feasible = true;
	private Instance currentInstance;
	private LinkedList<Route> listRoutes;

	private boolean isDone = false;

	private double initialSolutionCost;
	private String initialSolutionToString;

	private long timeForInitialSolution;
	private long timeForImprovedSolution;

	public Solution(Instance currentInstance) {
		this.currentInstance = currentInstance;
		this.listRoutes = new LinkedList<Route>();
		this.objValue = 0;
	}

	public double getInitialSolutionCost() {
		return initialSolutionCost;
	}

	public void setInitialSolutionCost(double initialSolutionCost) {
		this.initialSolutionCost = initialSolutionCost;
	}

	public String getInitialSolutionToString() {
		return initialSolutionToString;
	}

	public void setInitialSolutionToString(String initialSolutionToString) {
		this.initialSolutionToString = initialSolutionToString;
	}

	public void setRoutes(LinkedList<Route> listRoutes) {
		this.listRoutes = new LinkedList<Route>(listRoutes);
	}

	public void addVan(Route newVan) {
		this.listRoutes.add(newVan);
	}

	public LinkedList<Route> getRoutes() {
		return this.listRoutes;
	}
	
	public int getNumberRobotVisits() {
		int result = 0;
		for (Route r : this.listRoutes) {
			for (RouteNode rn : r.getRoute()) {
				if (rn.getId() != 0) {
					for (Node rv : r.getRobotVisitsForRouteNode(rn)) {
						result++;
					}
				}
			}
		}
		return result;
	}

	public Instance getInstance() {
		return this.currentInstance;
	}

	// this is supposed to be console output
	public void printToConsole() {
		System.out.println(printToString());
	}

	public long getTimeForInitialSolution() {
		return this.timeForInitialSolution;
	}

	public long getTimeForImprovedSolution() {
		return this.timeForImprovedSolution;
	}

	public void setTimeInitialSolution(long t) {
		this.timeForInitialSolution = t;
	}

	public void setTimeForImprovedSolution(long t) {
		this.timeForImprovedSolution = t;
	}

	public boolean isFeasible() {
		feasible = true;
		for (Route r : this.listRoutes) {
			feasible = feasible && r.isFeasible();
		}
		return this.feasible;
	}

	// THIS IS FOR THE GUROBI SOLVER ONLY
	public void setFeasible() {
		this.feasible = true;
	}

	// THIS IS FOR FOR THE GUROBI SOLVER ONLY
	public void setCost(double cost) {
		this.objValue = cost;
	}

	// THIS IS FOR CONSOLGE LOGGING ONLY
	public void isDone() {
		for (Route r : this.listRoutes) {
			r.setIsRouteDone();
		}
		this.isDone = true;
	}

	// THIS IS FOR FOR THE GUROBI SOLVER ONLY
	public double grbCost() {
		return this.objValue;
	}
	
	public double getCost() {
		this.objValue = 0;
		for (Route r : this.listRoutes) {
			this.objValue += r.getCost();
		}
		return this.objValue;
	}

	// this gets printed to file
	public String printToString() {

		String result = "";
		int count = 0;

		if (true) {

			result += "Time for Solution (total): "
					+ (this.getTimeForInitialSolution() + this.getTimeForImprovedSolution()) + "\n";
			result += "Time for initial Solution: " + this.getTimeForInitialSolution() + "\n";
			result += "Time for improved Solution: " + this.getTimeForImprovedSolution() + "\n\n";

		}

		result += "Cost: " + this.objValue + "\n";
		result += "Feasibility: " + this.isFeasible() + "\n\n";

		for (Route r : this.listRoutes) {

			int entries = 0;
			result += "Route of Van#" + r.getId() + "\n";

			for (RouteNode rn : r.getRoute()) {

				entries++;

				if (rn.getId() != 0) {
					count++;
					result += "" + rn.getId() + "-{";

					int it = 0;
					for (Node rv : r.getRobotVisitsForRouteNode(rn)) {
						it++;
						if (it == r.getRobotVisitsForRouteNode(rn).size()) {
							result += "" + rv.getId();
						} else {
							result += "" + rv.getId() + ", ";
						}
						count++;
						entries++;
					}

					result += "} -> ";

				} else {
					result += "" + rn.getId() + " -> ";
				}

				if (entries > 12) {
					entries = 0;
					result += "\n";
				}

			}

			result += "0";

			result += "\n";
			result += "Free Payload Capacity: " + r.getFreeCapacity() + "\n";
			result += "Free Time Capacity: " + r.getCapacityTravelTime() + "\n";
			result += "Feasibility: " + r.isFeasible() + "\n";
			result += "Partial Cost: " + r.getCost() + "\n\n";

		}

		result += "Nodes counted: " + count;

		if (isDone) {
			result += "+++++++++++ END IMPROVED SOLUTION +++++++++++\n\n\nInitial Solution: \n\n";
			result += initialSolutionToString;
		}

		return result;
	}
}