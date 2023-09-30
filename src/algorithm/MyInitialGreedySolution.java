package algorithm;

import components.*;

import java.util.LinkedList;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class MyInitialGreedySolution {

	private ArrayList<Node> listCustomers;
	private LinkedList<Route> listVans;

	private Instance currentInstance;

	private boolean consoleLog = false;

	public MyInitialGreedySolution(Instance currentInstance, boolean consoleLog) {

		this.currentInstance = currentInstance;
		this.consoleLog = consoleLog;

		this.listCustomers = currentInstance.getListCustomers();
		this.listVans = new LinkedList<Route>();

		for (int i = 0; i < currentInstance.getNumberVans(); i++) {
			Route r = new Route(i, this.currentInstance);
			this.listVans.add(r);
		}
	}

	public Solution getInitialSolution() {

		if (consoleLog)
			System.out.println("CREATE initial solution...");

		long timeStart = System.currentTimeMillis();

		Solution solution = new Solution(this.currentInstance);

		// first step
		// find customer with highest demand and insert into an empty route
		Collections.sort(listCustomers, new Comparator<Node>() {
			@Override
			public int compare(Node l, Node r) {
				return l.getDemand() < r.getDemand() ? -1 : (l.getDemand() > r.getDemand()) ? 1 : 0;
			}
		});

		this.listVans.get(0).addNodeToRoute(this.listCustomers.remove(listCustomers.size() - 1), false);

		// second step

		/*
		 * i choose to divide the listCustomers into 2 seperate lists the first one is
		 * for NonRobotAccessibles and the second one for RobotAccessible listCustomers
		 */

		ArrayList<Node> listNRA = new ArrayList<Node>(currentInstance.getNumberCustomers());
		ArrayList<Node> listRA = new ArrayList<Node>(currentInstance.getNumberCustomers());

		for (Node n : listCustomers) {
			if (n.getId() != 0) {
				if (n.getRobotAccessible() == 1) {
					listRA.add(n);
				} else {
					listNRA.add(n);
				}
			}
		}

		if (consoleLog) {
			System.out.println("---> Non Robot Accessible Customers: " + listNRA.size());
			System.out.println("---> Robot Accessible Customers: " + listRA.size());
		}

		int prevSize = listNRA.size();
		while (listNRA.size() > 0) {

			RouteInsert routeInsert = new RouteInsert();
			double cheapestInsertCost = Integer.MAX_VALUE;

			int i = listNRA.size() - 1;
			while (i > -1) {

				Node insertCandidate = listNRA.get(i);

				for (Route r : this.listVans) {

					double costBeforeInsert = r.getCost();

					// check capacity constraint
					if (r.isCapacityAvailable(insertCandidate)) {
						if (r.isTravelTimeAvailabe(insertCandidate, r.getRoute().size())) {

							r.addNodeToRoute(insertCandidate, false);
							double currentCost = r.getCost() - costBeforeInsert;
							boolean isInsertFeasible = r.isFeasible();
							r.removeNodeFromRoute(insertCandidate, false);
//							r.removeNodeFromRouteAtIndex(insertCandidate, r.getRoute().size()-1, true);

							if (currentCost < cheapestInsertCost && isInsertFeasible) {

								cheapestInsertCost = currentCost;

								routeInsert.setRemoveIndex(i);
								routeInsert.setInsertPosition(r.getRoute().size());
								routeInsert.setRouteForInsert(r);
								routeInsert.setNodeToBeInserted(insertCandidate);
								routeInsert.setIsFeasibleInsert(true);
							}
						}
					}
				}
				i--;
			}

			if (routeInsert.getIsFeasibleInsert()) {

//				@SuppressWarnings("unused")
//				boolean rem = listNRA.remove(routeInsert.getNodeToBeInserted());
				listNRA.remove(routeInsert.getRemoveIndex());
				routeInsert.executeInsert();

			}
		}

		prevSize = listRA.size();
		while (listRA.size() > 0) {

			// insertjob variable
			RobotInsert robotInsert = new RobotInsert();

			double cheapestInsertCost = Integer.MAX_VALUE;

			int i = listRA.size() - 1;
			while (i > -1) {

				Node insertCandidate = listRA.get(i);

				for (Route r : this.listVans) {

					// check capacity constraint
					if (r.isCapacityAvailable(insertCandidate)) {

						r.updateCost();
						double costBeforeInsert = r.getCost();

						for (RouteNode rn : r.getRoute()) {

							if (rn.getId() != 0) {
								/*
								 * in here i assume that if theres a feasible robotinsert found it is definitely
								 * cheaper than a route insert which should be a fair assumption to make i think
								 */

								double currentDist = currentInstance.getDistanceCustomers(rn.getId(),
										insertCandidate.getId());

								boolean isRobotAvailable = (r.getRobotVisitsForRouteNode(rn).size() < currentInstance
										.getNumberRobots());
								boolean isReachable = (currentInstance.getRobotMaxRadius() >= currentDist);

								if (isRobotAvailable && isReachable) {

									r.addNodeAsRobotVisit(rn.getNode(), insertCandidate, false);
									double currentCost = r.getCost() - costBeforeInsert;
									boolean isFeasible = r.isFeasible();
									r.removeFromRobotVisit(insertCandidate, false);

									if (cheapestInsertCost > currentCost && isFeasible) {

										cheapestInsertCost = currentCost;

										robotInsert.setRemoveIndex(i);
										robotInsert.setIndexNode(rn.getNode());
										robotInsert.setNodeToBeInserted(insertCandidate);
										robotInsert.setRouteForInsert(r);
										robotInsert.setIsFeasibleInsert(true);
									}
								}
							}
						}
					}

				}
				i--;
			}

			// check insert
			if (robotInsert.getIsFeasibleInsert()) {

				// insert cheapest insert
//				@SuppressWarnings("unused")
//				boolean rem = listRA.remove(robotInsert.getNodeToBeInserted());
				listRA.remove(robotInsert.getRemoveIndex());
				robotInsert.executeInsert();
			} else {

				// find single next cheapest route insert
				while (true) {

					RouteInsert routeInsert = new RouteInsert();

					i = listRA.size() - 1;
					while (i > -1) {

						Node insertCandidate = listRA.get(i);

						for (Route r : this.listVans) {

							// check capacity constraint
							if (r.isCapacityAvailable(insertCandidate)) {
								if (r.isTravelTimeAvailabe(insertCandidate, r.getRoute().size())) {

									r.updateCost();
									double costBeforeInsert = r.getCost();

									r.addNodeToRoute(insertCandidate, false);
									double currentCost = r.getCost() - costBeforeInsert;
									boolean isInsertFeasible = r.isFeasible();
									r.removeNodeFromRoute(insertCandidate, false);
//									r.removeNodeFromRouteAtIndex(insertCandidate, r.getRoute().size()-1, true);

									if (currentCost < cheapestInsertCost && isInsertFeasible) {

										cheapestInsertCost = currentCost;

										routeInsert.setRemoveIndex(i);
										routeInsert.setInsertPosition(r.getRoute().size());
										routeInsert.setRouteForInsert(r);
										routeInsert.setNodeToBeInserted(insertCandidate);
										routeInsert.setIsFeasibleInsert(true);
									}
								}
							}
						}
						i--;
					}

					// check insert
					if (routeInsert.getIsFeasibleInsert()) {
						// insert cheapest insert
//						@SuppressWarnings("unused")
//						boolean rem = listRA.remove(routeInsert.getNodeToBeInserted());
						listRA.remove(routeInsert.getRemoveIndex());
						routeInsert.executeInsert();
						break;
					}
				}
			}

			if (prevSize == listRA.size()) {
				System.out.println("RA stuck at" + prevSize);
			}
			prevSize = listRA.size();
		}

		long timeEnd = System.currentTimeMillis();
		long timeTaken = timeEnd - timeStart;

		// assign the finished list to my solution
		solution.setRoutes(this.listVans);
		solution.setTimeInitialSolution(timeTaken);
		solution.setInitialSolutionToString(solution.printToString());
		solution.getCost();
		
		if (consoleLog) {
			System.out.println("FINISHED initial solution!\n");
		}

		return solution;
	}
}