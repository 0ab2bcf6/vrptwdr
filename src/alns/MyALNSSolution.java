package alns;

import java.util.ArrayList;
import java.util.LinkedList;

import components.Node;
import components.Route;
import components.RouteNode;
import components.Instance;
import components.Solution;

public class MyALNSSolution {

	private double objValue;
	private boolean feasible = true;
	private LinkedList<Route> listRoutes;
	private Instance currentInstance;

	public ArrayList<Node> listRemovedNodes;
	private int gamma = 0;

	public MyALNSSolution(Solution solution) {

		this.currentInstance = solution.getInstance();
		this.listRoutes = new LinkedList<Route>();
		this.listRemovedNodes = new ArrayList<Node>();

		for (Route r : solution.getRoutes()) {

			Route newRoute = new Route(r.getId(), solution.getInstance());

			for (RouteNode rn : r.getRoute()) {
				if (rn.getId() != 0) {
					newRoute.addNodeToRoute(rn.getNode(), true);
					for (Node rv : r.getRobotVisitsForRouteNode(rn)) {
						newRoute.addNodeAsRobotVisit(rn.getNode(), rv, true);
					}
				}
			}

			newRoute.setTotalTimeSpent(r.getTotalTimeSpent());
			newRoute.updateCost();

			this.listRoutes.add(newRoute);

		}

		this.objValue = solution.getCost();
		this.feasible = solution.isFeasible();
	}

	public MyALNSSolution(MyALNSSolution solution, int gamma) {

		this.currentInstance = solution.getInstance();

		this.listRoutes = new LinkedList<Route>();
		this.listRemovedNodes = new ArrayList<Node>(currentInstance.getNumberCustomers());
		this.gamma = gamma;
		this.objValue = solution.getCost();
		this.feasible = solution.isFeasible();

		/*
		 * rather than using the implemented method this.listRoutes.add(r) to copy lists
		 * i use this because with 200customers and 5000 iterations in the alns this
		 * safes up to 20 seconds of 50 seconds in improved solution building
		 */

		for (Route r : solution.getRoutes()) {

			Route newRoute = new Route(r.getId(), solution.getInstance());

			for (RouteNode rn : r.getRoute()) {
				if (rn.getId() != 0) {
					newRoute.addNodeToRoute(rn.getNode(), true);
					for (Node rv : r.getRobotVisitsForRouteNode(rn)) {
						newRoute.addNodeAsRobotVisit(rn.getNode(), rv, true);
					}
				}
			}
			newRoute.setTotalTimeSpent(r.getTotalTimeSpent());
			newRoute.updateCost();
			this.listRoutes.add(newRoute);
		}
	}

	public MyALNSSolution() {

	}

	public MyALNSSolution clone() {

		MyALNSSolution clone = new MyALNSSolution();

		clone.currentInstance = this.currentInstance;

		clone.listRoutes = new LinkedList<Route>();
		clone.listRemovedNodes = new ArrayList<Node>(currentInstance.getNumberCustomers());
		clone.objValue = this.objValue;
		clone.feasible = this.feasible;

		for (Route r : this.listRoutes) {

			Route newRoute = new Route(r.getId(), this.currentInstance);

			for (RouteNode rn : r.getRoute()) {
				if (rn.getId() != 0) {
					newRoute.addNodeToRoute(rn.getNode(), true);
					for (Node rv : r.getRobotVisitsForRouteNode(rn)) {
						newRoute.addNodeAsRobotVisit(rn.getNode(), rv, true);
					}
				}
			}
			newRoute.setTotalTimeSpent(r.getTotalTimeSpent());
			newRoute.updateCost();
			clone.listRoutes.add(newRoute);
		}

		return clone;
	}

	public Instance getInstance() {
		return this.currentInstance;
	}

	public LinkedList<Route> getRoutes() {
		return this.listRoutes;
	}

	public ArrayList<Node> getRemovedNodes() {
		return this.listRemovedNodes;
	}

	public boolean isFeasible() {
		this.feasible = true;
		for (Route r : this.listRoutes) {
			feasible = feasible && r.isFeasible();
		}
		return this.feasible;
	}

	public double getCost() {
		this.objValue = 0;
		for (Route r : this.listRoutes) {
			objValue += r.getCost();
		}
		return this.objValue;
	}

	public Solution toSolution() {

		Solution result = new Solution(currentInstance);

		for (Route r : this.listRoutes) {

			Route newRoute = new Route(r.getId(), this.currentInstance);

			for (RouteNode rn : r.getRoute()) {
				if (rn.getId() != 0) {
					newRoute.addNodeToRoute(rn.getNode(), true);
					for (Node rv : r.getRobotVisitsForRouteNode(rn)) {
						newRoute.addNodeAsRobotVisit(rn.getNode(), rv, true);
					}
				}
			}
			newRoute.setTotalTimeSpent(r.getTotalTimeSpent());
			newRoute.updateCost();
			result.addVan(newRoute);

		}

		result.setCost(objValue);
		return result;
	}

	public int getGamma() {
		return gamma;
	}
}