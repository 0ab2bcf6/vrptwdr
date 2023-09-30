package components;

import java.util.LinkedList;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.ListIterator;

public class Route {

	private Instance currentInstance;

	private int id;

	private double cost;
	private double capacity;
	private boolean feasible = true;
	private int numberRobots;
	private int size = 0;

	// solomon & homberger use this
	private double vanMaxTraveltime;

	// i just use this for checking if next customer can be reached
	private double vantime = 0;

	// this is to track efficiently which nodes get serviced in the entire route
	private HashSet<Node> setNodesOfRoute;

	// this represents the actual route the van is supposed to drive
	private ArrayList<RouteNode> listRoute;

	// this respresents the customers that get visited by by robot
	private HashMap<RouteNode, LinkedList<Node>> mapRobotVisits;

	// this maps the ID of the Nodes to their routenode objects
	private HashMap<Integer, RouteNode> mapRouteNodes;

	// ignore this
	private boolean consoleLog = true;
	private boolean isRouteDone = false;
	private long totalTimeSpent = 0;

	public Route(int id, Instance currentInstance) {
		this.id = id;
		this.currentInstance = currentInstance;
		capacity = currentInstance.getVanPayload();
		vanMaxTraveltime = currentInstance.getVanMaxTravelTime();
		numberRobots = currentInstance.getNumberRobots();
		listRoute = new ArrayList<>(currentInstance.getNumberCustomers());
		setNodesOfRoute = new HashSet<>();
		mapRobotVisits = new HashMap<>();
		mapRouteNodes = new HashMap<>();
		cost = 0;
		addNodeToRoute(currentInstance.getCustomers(0), false);
	}

	@Override
	public Route clone() {

		Route clone = new Route(id, currentInstance);

		for (RouteNode rn : listRoute) {
			if (rn.getId() != 0) {
				clone.addNodeToRoute(rn.getNode(), false);
				for (Node rv : getRobotVisitsForRouteNode(rn)) {
					clone.addNodeAsRobotVisit(rn.getNode(), rv, false);
				}
			}
		}
		clone.updateCost();

		return clone;
	}

	public boolean isFeasible() {
		return feasible;
	}

	public double getVanTime() {
		return vantime;
	}

	public int getId() {
		return id;
	}

	public int getSize() {
		return size;
	}

	public double getCapacityTravelTime() {
		return vanMaxTraveltime;
	}

	public double getFreeCapacity() {
		return capacity;
	}

	public double getCost() {
		return cost;
	}

	public boolean isTravelTimeAvailabe(Node newNode, int insertedPosition) {

		if (listRoute.get(listRoute.size() -1).getId() == 0) {
			return true;
		}

		Node prev = listRoute.get(insertedPosition - 1).getNode();
		double capTime = vanMaxTraveltime;
		double additionalTime = 0.0;

		if (insertedPosition == listRoute.size()) {

			// if insertPosition is at the end
			additionalTime = currentInstance.getTravelingTimeVehicle(prev.getId(), newNode.getId());

		} else {

			// if insertPosition is in the middle of the route; not at the end
			Node next = listRoute.get(insertedPosition).getNode();
			capTime += currentInstance.getTravelingTimeVehicle(prev.getId(), next.getId());
			additionalTime = currentInstance.getTravelingTimeVehicle(prev.getId(), newNode.getId())
					+ currentInstance.getTravelingTimeVehicle(newNode.getId(), next.getId());

		}

		if (capTime - additionalTime >= 0) {
			return true;
		} else {
			return false;
		}
	}

	public boolean isCapacityAvailable(Node n) {
		if (capacity - n.getDemand() >= 0) {
			return true;
		} else {
			return false;
		}
	}

	public boolean isNodeInRoute(Node n) {
		if (setNodesOfRoute.contains(n)) {
			return true;
		} else {
			return false;
		}
	}

	public boolean isNodeInRouteVisit(Node n) {
		for (RouteNode r : listRoute) {
			if (r.getId() == n.getId()) {
				return true;
			}
		}
		return false;
	}

	public ArrayList<RouteNode> getRoute() {
		return listRoute;
	}

	public RouteNode getLastRouteNodeOfRoute() {
		return listRoute.get(listRoute.size() - 1);
	}

	public LinkedList<Node> getRobotVisitsForRouteNode(RouteNode index) {
		return mapRobotVisits.get(index);
	}

	private void updateRouteOnInsert(Node n, int index) {

		// add n into the hashset
		setNodesOfRoute.add(n);

		// increase size of route
		size++;

		// update capacity
		capacity = capacity - n.getDemand();

		// update vanMaxTraveltime
		if (n.getId() == 0) {

			// depot, do nothing

		} else if (index == listRoute.size()) {

			// if insertPosition is at the end
			Node prev = getLastRouteNodeOfRoute().getNode();
			vanMaxTraveltime -= currentInstance.getTravelingTimeVehicle(prev.getId(), n.getId());

		} else if (index == -1) {

			// robotinsert, do nothing

		} else {

//			ListIterator<RouteNode> listItRouteNode = listRoute.listIterator();

			RouteNode nextRNode = listRoute.get(index);
			RouteNode prevRNode = listRoute.get(index-1);

//			nextRNode = listItRouteNode.next();

//			int it = 0;
//			while (listItRouteNode.hasNext()) {
//				it++;
//				prevRNode = nextRNode;
//				nextRNode = listItRouteNode.next();
//				if (it == index) {
//					break;
//				}
//			}

			// if insertPosition is in the middle of the route not at the end
			double additionalTime = 0.0;
			vanMaxTraveltime 
					+= currentInstance.getTravelingTimeVehicle(prevRNode.getId(), nextRNode.getId());
			additionalTime += currentInstance.getTravelingTimeVehicle(prevRNode.getId(), n.getId())
					+ currentInstance.getTravelingTimeVehicle(n.getId(), nextRNode.getId());
			vanMaxTraveltime -= additionalTime;
		}
	}

	private void updateRouteOnRemove(Node n, boolean isQuickRemove) {

		// remove n from the hashset
		setNodesOfRoute.remove(n);

		// decrease size of route
		size--;

		// update capacity
		capacity = capacity + n.getDemand();

		// calculate the new time costs of the route
		if (!isQuickRemove)
			updateCost();
	}

	// adds node to the end of the route
	public void addNodeToRoute(Node n, boolean isQuickInsert) {

		// create new RouteNode n
		RouteNode newRouteNode = new RouteNode(n);

		// map the RouteNode of n to the Node n to access it later
		mapRouteNodes.put(n.getId(), newRouteNode);

		// create a new LinkedList for robot visits from n
		mapRobotVisits.put(newRouteNode, new LinkedList<Node>());

		// update the route
		updateRouteOnInsert(n, listRoute.size());

		// adding n into the vehicle route
		listRoute.add(newRouteNode);

		// calculate the new time costs of the route
		if (!isQuickInsert)
			updateCost();
	}

	// adds node to route at index
	public void addNodeToRouteAtIndex(Node n, int index, boolean isQuickInsert) {

		long timeStart = System.currentTimeMillis();

		// create new RouteNode n
		RouteNode newRouteNode = new RouteNode(n);

		// map the RouteNode of n to the Node n to access it later
		mapRouteNodes.put(n.getId(), newRouteNode);

		// create a new LinkedList for robot visits from n
		mapRobotVisits.put(newRouteNode, new LinkedList<Node>());

		// update the route
		updateRouteOnInsert(n, index);

		// add n to the index
		listRoute.add(index, newRouteNode);

		// calculate the new time costs of the route
		if (!isQuickInsert)
			updateCost();

		long timeEnd = System.currentTimeMillis();
		setTotalTimeSpent(getTotalTimeSpent() + (timeEnd - timeStart));
	}

	public void addNodeAsRobotVisit(Node index, Node n, boolean isQuickInsert) {

		long timeStart = System.currentTimeMillis();

		RouteNode indexRouteNode = mapRouteNodes.get(index.getId());
		if (mapRobotVisits.get(indexRouteNode).size() < numberRobots) {
			mapRobotVisits.get(indexRouteNode).add(n);

			// update the route
			updateRouteOnInsert(n, -1);
		}

		// calculate the new time costs of the route
		if (!isQuickInsert)
			updateCost();

		long timeEnd = System.currentTimeMillis();
		setTotalTimeSpent(getTotalTimeSpent() + (timeEnd - timeStart));
	}

	public LinkedList<Node> removeNodeFromRoute(Node n, boolean isQuickRemove) {

		long timeStart = System.currentTimeMillis();

		LinkedList<Node> listRemovedNodes = new LinkedList<>();

		RouteNode indexRouteNode = mapRouteNodes.get(n.getId());

		if (mapRobotVisits.get(indexRouteNode) != null) {
			if (mapRobotVisits.get(indexRouteNode).size() > 0) {
				listRemovedNodes = removeNodesFromRobotVisit(n);
			}
		}

		ListIterator<RouteNode> listItRouteNode = listRoute.listIterator();

		RouteNode nextRNode = null;
		RouteNode currRNode = null;
		RouteNode prevRNode = null;

		prevRNode = listItRouteNode.next();

		while (listItRouteNode.hasNext()) {

			if (currRNode == null)
				currRNode = listItRouteNode.next();
			
			if(listRoute.size() > 2) {
				nextRNode = listItRouteNode.next();

				if (currRNode.getId() == n.getId()) {
		
					vanMaxTraveltime += currentInstance.getTravelingTimeVehicle(prevRNode.getId(), n.getId())
							+ currentInstance.getTravelingTimeVehicle(n.getId(), nextRNode.getId());
					vanMaxTraveltime -= currentInstance.getTravelingTimeVehicle(prevRNode.getId(), nextRNode.getId());
		
					listItRouteNode.previous();
					listItRouteNode.previous();
					listItRouteNode.remove();
		
					break;

				} else if (nextRNode.getId() == n.getId() && !listItRouteNode.hasNext()) {

					vanMaxTraveltime += currentInstance.getTravelingTimeVehicle(currRNode.getId(), n.getId());
					listItRouteNode.remove();
					break;

				}
			} else {
				//if list has only 2 entries
				vanMaxTraveltime += currentInstance.getTravelingTimeVehicle(prevRNode.getId(), currRNode.getId());
				listItRouteNode.remove();
				break;
			}

			prevRNode = currRNode;
			currRNode = nextRNode;
		}

		mapRobotVisits.remove(indexRouteNode);
		mapRouteNodes.remove(n.getId());
		listRemovedNodes.add(n);

		// update Route
		updateRouteOnRemove(n, isQuickRemove);

		long timeEnd = System.currentTimeMillis();
		setTotalTimeSpent(getTotalTimeSpent() + (timeEnd - timeStart));

		return listRemovedNodes;
	}
	
	public LinkedList<Node> removeNodeFromRouteAtIndex(Node n, int index, boolean isQuickRemove) {
		
		long timeStart = System.currentTimeMillis();

		LinkedList<Node> listRemovedNodes = new LinkedList<>();

		RouteNode indexRouteNode = mapRouteNodes.get(n.getId());

		if (mapRobotVisits.get(indexRouteNode) != null) {
			if (mapRobotVisits.get(indexRouteNode).size() > 0) {
				listRemovedNodes = removeNodesFromRobotVisit(n);
			}
		}
		
		//node is still inserted at this point
		if(index == listRoute.size() - 1) {
			Node prev = listRoute.get(index-1).getNode();
			vanMaxTraveltime += currentInstance.getTravelingTimeVehicle(prev.getId(), n.getId());
		} else {
			Node prev = listRoute.get(index-1).getNode();
			Node next = listRoute.get(index+1).getNode();
			vanMaxTraveltime += currentInstance.getTravelingTimeVehicle(prev.getId(), n.getId());
			vanMaxTraveltime += currentInstance.getTravelingTimeVehicle(n.getId(), next.getId());
			vanMaxTraveltime -= currentInstance.getTravelingTimeVehicle(prev.getId(), next.getId());
		}
		
		listRoute.remove(index);
		
		mapRobotVisits.remove(indexRouteNode);
		mapRouteNodes.remove(n.getId());
		listRemovedNodes.add(n);

		// update Route
		updateRouteOnRemove(n, isQuickRemove);

		long timeEnd = System.currentTimeMillis();
		setTotalTimeSpent(getTotalTimeSpent() + (timeEnd - timeStart));
		
		return listRemovedNodes;
	}

	public void removeNodeFromRobotVisit(Node index, Node n, boolean isQuickRemove) {

		/*
		 * this removes a CERTAIN node given the INDEX node i only use this in the
		 * greedy insert
		 */

		long timeStart = System.currentTimeMillis();

		RouteNode indexRouteNode = mapRouteNodes.get(index.getId());
		mapRobotVisits.get(indexRouteNode).remove(n);

		// update Route
		updateRouteOnRemove(n, isQuickRemove);

		long timeEnd = System.currentTimeMillis();
		setTotalTimeSpent(getTotalTimeSpent() + (timeEnd - timeStart));

	}

	private LinkedList<Node> removeNodesFromRobotVisit(Node n) {

		// this removes ALL robotvisited nodes from routenode
		RouteNode indexRouteNode = mapRouteNodes.get(n.getId());

		LinkedList<Node> listRemovedNodes = new LinkedList<>();
		LinkedList<Node> robotVisits = mapRobotVisits.get(indexRouteNode);

		if (robotVisits != null && robotVisits.size() != 0) {
			for (Node i : mapRobotVisits.get(indexRouteNode)) {
				capacity = capacity + i.getDemand();
				listRemovedNodes.add(i);
				setNodesOfRoute.remove(i);
				size--;
			}
			mapRobotVisits.get(indexRouteNode).remove(n);
		}

		return listRemovedNodes;
	}

	public LinkedList<Node> removeFromRobotVisit(Node n, boolean isQuickRemove) {

		// this removes a single node!
		RouteNode indexRouteNode = null;

		long timeStart = System.currentTimeMillis();

		for (RouteNode rn : listRoute) {
			for (Node i : getRobotVisitsForRouteNode(rn)) {
				if (i.getId() == n.getId()) {
					indexRouteNode = mapRouteNodes.get(rn.getNode().getId());
				}
			}
		}

		LinkedList<Node> listRemovedNodes = new LinkedList<>();
		LinkedList<Node> listRobotVisits = mapRobotVisits.get(indexRouteNode);

		if (listRobotVisits != null && listRobotVisits.size() > 0) {
			listRemovedNodes.add(n);
			mapRobotVisits.get(indexRouteNode).remove(n);

			// update Route
			updateRouteOnRemove(n, isQuickRemove);
		}

		long timeEnd = System.currentTimeMillis();
		setTotalTimeSpent(getTotalTimeSpent() + (timeEnd - timeStart));

		return listRemovedNodes;
	}

	public void setConsoleLog(boolean b) {
		consoleLog = b;
	}

	public void setIsRouteDone() {
		isRouteDone = true;
	}

	public void updateCost() {
		updateCost(consoleLog && isRouteDone);
	}

	public void updateCost(boolean debug) {

		feasible = true;

		if (getLastRouteNodeOfRoute().getId() == 0) {

			cost = 0;
			vantime = getLastRouteNodeOfRoute().getReadyTime();

		} else if ((listRoute.size() > 1)) {

			ListIterator<RouteNode> listItRouteNode = listRoute.listIterator();

			RouteNode currRNode = null;
			RouteNode prevRNode = null;

			while (listItRouteNode.hasNext()) {

				currRNode = listItRouteNode.next();

				double nodeTravelTime = 0;
				if (currRNode.getId() == 0) {

					double lowerBound = currRNode.getReadyTime();
					double upperBound = currRNode.getDueTime();

					double earliestDeparture = currRNode.getReadyTime() + currRNode.getServiceTime();
					double latestDeparture = currRNode.getDueTime() + currRNode.getServiceTime();

					currRNode.setEPA(lowerBound);
					currRNode.setLPA(upperBound);
					currRNode.setEPD(earliestDeparture);
					currRNode.setLPD(latestDeparture);

					prevRNode = currRNode;

				} else {

					nodeTravelTime = currentInstance.getTravelingTimeVehicle(prevRNode.getId(), currRNode.getId());
					
					if(prevRNode.getEPD() + nodeTravelTime > currRNode.getDueTime()) {
						this.feasible = false;
					}

					double earliestPossibleArrival = Math.max(
							prevRNode.getEPD()
									+ currentInstance.getTravelingTimeVehicle(prevRNode.getId(), currRNode.getId()),
							currRNode.getReadyTime());

					double latestPossibleArrival = Math.max(Math.min(
							prevRNode.getLPD()
									+ currentInstance.getTravelingTimeVehicle(prevRNode.getId(), currRNode.getId()),
							currRNode.getDueTime()), currRNode.getReadyTime());

					currRNode.setEPA(earliestPossibleArrival);
					currRNode.setLPA(latestPossibleArrival);

					// TBD fix service time calculation
					double earliestDeparture = Math.max(currRNode.getReadyTime(), earliestPossibleArrival)
							+ currRNode.getServiceTime();
					double latestDeparture = latestPossibleArrival + currRNode.getServiceTime();

					for (Node rv : mapRobotVisits.get(currRNode)) {

						double robotTravelTime = currentInstance.getTravelingTimeRobot(rv.getId(), currRNode.getId());
						// dummy timewindow
						double rvLowerBound = rv.getReadyTime() - robotTravelTime;
						double rvUpperBound = rv.getDueTime() - robotTravelTime;

						if (rvUpperBound < earliestPossibleArrival) {
							this.feasible = false;
						}

						earliestPossibleArrival = Math.min(earliestPossibleArrival, rvUpperBound);
						latestPossibleArrival = Math.min(latestPossibleArrival, rvUpperBound);

						// earliest possible departure from ROUTENODE after serving RV
						double rvEarliestDep = Math.max(earliestPossibleArrival, rvLowerBound)
								+ rv.getServiceTimeRobot()
								+ 2 * currentInstance.getTravelingTimeRobot(rv.getId(), currRNode.getId());
						earliestDeparture = Math.max(rvEarliestDep, earliestDeparture);

						// latest possible departure from ROUTENODE after serving RV
						double rvLatestDep = rvUpperBound + rv.getServiceTimeRobot()
								+ 2 * currentInstance.getTravelingTimeRobot(rv.getId(), currRNode.getId());
						latestDeparture = Math.max(rvLatestDep, latestDeparture);

//						System.out.println("\t\tArrival Robot: " + rvUpperBound);
//						System.out.println("\t\tRobot: " + rvEarliestDep + " / " + rvLatestDep);
					}

					// TODO readd with fixed formula
					/*
					 * if (prevRNode.getEPD() +
					 * currentInstance.getTravelingTimeVehicle(prevRNode.getId(), currRNode.getId())
					 * > upperBound) { this.feasible = false; System.out.println("NOT FEASIBLE");
					 * return; }
					 */

					currRNode.setEPD(earliestDeparture);
					currRNode.setLPD(latestDeparture);

					prevRNode = currRNode;
				}

				if (debug) {
					System.out.println(currRNode.getId() + ": \n\tTimeWindow: " + currRNode.getReadyTime() + " / "
							+ currRNode.getDueTime() + "; \n\tArrival: " + currRNode.getEPA() + "/" + currRNode.getLPA()
							+ " ;\n\t Departures: " + currRNode.getEPD() + " / " + currRNode.getLPD()
							+ ", Service Time: " + currRNode.getServiceTime());
					System.out.println("\t Travel time: " + nodeTravelTime);
				}
			}

			double earliestStopTime = listRoute.get(listRoute.size() - 1).getEPD();
			double earliestCurrentTime = earliestStopTime;

			double latestStopTime = listRoute.get(listRoute.size() - 1).getLPD();
			double latestCurrentTime = latestStopTime;

			this.cost = currentInstance.getTravelingTimeVehicle(getLastRouteNodeOfRoute().getId(),
					listRoute.get(0).getId());

			RouteNode currNode = listItRouteNode.previous();
			RouteNode prevNode = null;

			while (listItRouteNode.hasPrevious()) {

				prevNode = listItRouteNode.previous();

				double earliestDepartureTimeAtPrevNode = calculateBackwards(currNode, prevNode, earliestCurrentTime);
				double latestDepartureTimeAtPrevNode = calculateBackwards(currNode, prevNode, latestCurrentTime);

				earliestCurrentTime = Math.min(earliestDepartureTimeAtPrevNode, prevNode.getLPD());
				latestCurrentTime = Math.min(latestDepartureTimeAtPrevNode, prevNode.getLPD());

				currNode = prevNode;
			}

			double earliestCost = earliestStopTime - earliestCurrentTime;
			double latestCost = latestStopTime - latestCurrentTime;
			this.cost += Math.min(earliestCost, latestCost);

//			System.out.println("Best: " + ((earliestCost < latestCost) ? "Earliest" : "Latest"));
//			System.out.println("Departure time from depot: " + earliestCurrentTime);
//			System.out.println("Departure time from depot: " + latestCurrentTime);
//			System.out.println("Cost: " + this.cost);
		}
	}

	private double calculateBackwards(RouteNode currNode, RouteNode prevNode, double currentTime) {
		double earliestArrivalTimeAtCurrNode = Math.min(currentTime - currNode.getServiceTime(), currNode.getDueTime());

		for (Node j : this.mapRobotVisits.get(currNode)) {
			double robotTravelTime = currentInstance.getTravelingTimeRobot(currNode.getId(), j.getId());

			double timeReturnedToRouteNode = Math.min(currentTime - robotTravelTime,
					j.getDueTime() + j.getServiceTimeRobot());
			timeReturnedToRouteNode -= j.getServiceTimeRobot();
			timeReturnedToRouteNode -= robotTravelTime;
			earliestArrivalTimeAtCurrNode = Math.min(earliestArrivalTimeAtCurrNode, timeReturnedToRouteNode);
		}

		double travelTime = currentInstance.getTravelingTimeVehicle(currNode.getId(), prevNode.getId());
		double earliestDepartureTimeAtPrevNode = earliestArrivalTimeAtCurrNode - travelTime;
		return earliestDepartureTimeAtPrevNode;
	}

	public long getTotalTimeSpent() {
		return totalTimeSpent;
	}

	public void setTotalTimeSpent(long totalTimeSpent) {
		this.totalTimeSpent = totalTimeSpent;
	}

}