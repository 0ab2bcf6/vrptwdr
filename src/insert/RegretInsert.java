package insert;

import java.util.Collections;
import java.util.Comparator;
import java.util.PriorityQueue;

import alns.MyALNSSolution;
import components.Instance;
import components.Node;
import components.RobotInsert;
import components.Route;
import components.RouteInsert;
import components.RouteNode;

public class RegretInsert extends AbstractALNSInsert implements InterfaceALNSInsert {

	public RegretInsert() {
		this.name = "Regret Insert";
	}

	@Override
	public MyALNSSolution insert(MyALNSSolution toBeFixed) {

		long timeStart = System.currentTimeMillis();
		
		for (Route r : toBeFixed.getRoutes()) {
			r.updateCost();
			double cost = r.getCost();
			routeCosts.add(r.getId(), cost);
		}

		Instance currentInstance = toBeFixed.getInstance();

		Collections.sort(toBeFixed.getRemovedNodes(), new Comparator<Node>() {
			@Override
			public int compare(Node l, Node r) {
				return l.getRobotAccessible() < r.getRobotAccessible() ? -1
						: (l.getRobotAccessible() > r.getRobotAccessible()) ? 1 : 0;
			}
		});

		PriorityQueue<Node> prioQueue = new PriorityQueue<Node>(new Comparator<Node>() {
			@Override
			public int compare(Node l, Node r) {
				return l.getRegret() > r.getRegret() ? -1 : (l.getRegret() < r.getRegret()) ? 1 : 0;
			}
		});

		// calculate regret
		while (toBeFixed.getRemovedNodes().size() > 0 && toBeFixed.getRemovedNodes().get(0).getRobotAccessible() == 0) {

			Node toBeInserted = toBeFixed.getRemovedNodes().remove(0);
			toBeInserted.setRegret(0);

			double cheapestInsert = Integer.MAX_VALUE;
			double secondCheapestInsert = Integer.MAX_VALUE;
			double cheapestEstimator = Integer.MAX_VALUE;

			/*
			 * i assume that theres at least one feasible insert for the node which is fair
			 * assuming we always have a sufficient fleet of vans
			 */

			for (Route r : toBeFixed.getRoutes()) {

				if (r.isCapacityAvailable(toBeInserted)) {

					double costBeforeInsert = routeCosts.get(r.getId());

					int position = r.getRoute().size();

					while (position > 0) {

						if (r.isTravelTimeAvailabe(toBeInserted, position)) {

							if (useCostEstimator) {

								Node prev = r.getRoute().get(position - 1).getNode();
								double traveltime = 0.0;

								if (position == r.getRoute().size()) {

									traveltime = currentInstance.getTravelingTimeVehicle(prev.getId(),
											toBeInserted.getId());

								} else {
									Node next = r.getRoute().get(position).getNode();
									traveltime = currentInstance.getTravelingTimeVehicle(prev.getId(),
											toBeInserted.getId())
											+ currentInstance.getTravelingTimeVehicle(toBeInserted.getId(),
													next.getId())
											- currentInstance.getTravelingTimeVehicle(prev.getId(), next.getId());
								}

								double servicetime = toBeInserted.getServiceTimeVehicle();
								double estimatedCost = traveltime + servicetime;

								if (estimatedCost < cheapestEstimator) {

									r.addNodeToRouteAtIndex(toBeInserted, position, false);
									double costAfterInsert = r.getCost();
									double currentCost = costAfterInsert - costBeforeInsert;
									boolean isInsertFeasible = r.isFeasible();
//									r.removeNodeFromRoute(toBeInserted, true);
									r.removeNodeFromRouteAtIndex(toBeInserted, position, true);

									if (currentCost < cheapestInsert && isInsertFeasible) {

										cheapestEstimator = estimatedCost;

										secondCheapestInsert = cheapestInsert;
										cheapestInsert = currentCost;
									}
								}

							} else {

								r.addNodeToRouteAtIndex(toBeInserted, position, false);
								double costAfterInsert = r.getCost();
								double currentCost = costAfterInsert - costBeforeInsert;
								boolean isInsertFeasible = r.isFeasible();
//								r.removeNodeFromRoute(toBeInserted, true);
								r.removeNodeFromRouteAtIndex(toBeInserted, position, true);

								if (currentCost < cheapestInsert && isInsertFeasible) {
									secondCheapestInsert = cheapestInsert;
									cheapestInsert = currentCost;
								}

							}
						}

						position--;
					}

				}
			}

			toBeInserted.setRegret(secondCheapestInsert - cheapestInsert);
			prioQueue.add(toBeInserted);
		}

		// insert
		while (prioQueue.size() > 0) {

			RouteInsert routeInsert = new RouteInsert();

			routeInsert.setNodeToBeInserted(prioQueue.poll());

			double cheapestInsert = Integer.MAX_VALUE;
			double cheapestEstimator = Integer.MAX_VALUE;

			/*
			 * this might reach completly different inserts than assumed by cheapeast and
			 * second cheapest we assumed earlier since there might be capacity violations
			 * and shift time windows as im adding nodes to the actual routes. that also
			 * kinda forces me to iterate to the route once again. very inefficient
			 */

			for (Route r : toBeFixed.getRoutes()) {

				if (r.isCapacityAvailable(routeInsert.getNodeToBeInserted())) {

					r.updateCost();
					double costBeforeInsert = r.getCost();

					int position = r.getRoute().size();
					while (position > 0) {

						if (r.isTravelTimeAvailabe(routeInsert.getNodeToBeInserted(), position)) {
							
							Node toBeInserted = routeInsert.getNodeToBeInserted();
							
							if (useCostEstimator) {

								Node prev = r.getRoute().get(position - 1).getNode();
								double traveltime = 0.0;

								if (position == r.getRoute().size()) {

									traveltime = currentInstance.getTravelingTimeVehicle(prev.getId(),
											toBeInserted.getId());

								} else {
									Node next = r.getRoute().get(position).getNode();
									traveltime = currentInstance.getTravelingTimeVehicle(prev.getId(),
											toBeInserted.getId())
											+ currentInstance.getTravelingTimeVehicle(toBeInserted.getId(),
													next.getId())
											- currentInstance.getTravelingTimeVehicle(prev.getId(), next.getId());
								}

								double servicetime = toBeInserted.getServiceTimeVehicle();
								double estimatedCost = traveltime + servicetime;

								if (estimatedCost < cheapestEstimator) {

									r.addNodeToRouteAtIndex(toBeInserted, position, false);
									double costAfterInsert = r.getCost();
									double currentCost = costAfterInsert - costBeforeInsert;
									boolean isInsertFeasible = r.isFeasible();
//									r.removeNodeFromRoute(toBeInserted, true);
									r.removeNodeFromRouteAtIndex(toBeInserted, position, true);

									if (currentCost < cheapestInsert && isInsertFeasible) {

										cheapestEstimator = estimatedCost;

										cheapestInsert = currentCost;
									}
								}

							} else {

								r.addNodeToRouteAtIndex(toBeInserted, position, false);
								double costAfterInsert = r.getCost();
								double currentCost = costAfterInsert - costBeforeInsert;
								boolean isInsertFeasible = r.isFeasible();
//								r.removeNodeFromRoute(toBeInserted, true);
								r.removeNodeFromRouteAtIndex(toBeInserted, position, true);

								if (currentCost < cheapestInsert && isInsertFeasible) {
									cheapestInsert = currentCost;
								}

							}
						}
						position--;
					}
				}
			}

			if (routeInsert.getIsFeasibleInsert()) {
				routeInsert.executeInsert();
				routeInsert.getRouteForInsert().updateCost();;
				routeCosts.remove(routeInsert.getRouteForInsert().getId());
				routeCosts.add(routeInsert.getRouteForInsert().getId(), routeInsert.getRouteForInsert().getCost());
			
			} else {
				toBeFixed.getRemovedNodes().add(routeInsert.getNodeToBeInserted());
			}
		}

		// calculate regret
		while (toBeFixed.getRemovedNodes().size() > 0) {

			Node toBeInserted = toBeFixed.getRemovedNodes().remove(0);
			toBeInserted.setRegret(0);

			double cheapestInsert = Integer.MAX_VALUE;
			double secondCheapestInsert = Integer.MAX_VALUE;
			
			double cheapestEstimator = Integer.MAX_VALUE;

			for (Route r : toBeFixed.getRoutes()) {

				if (r.isCapacityAvailable(toBeInserted) && toBeInserted.getRobotAccessible() == 1) {

					double costBeforeInsert = routeCosts.get(r.getId());

					for (RouteNode rn : r.getRoute()) {
						if (rn.getId() != 0) {

							boolean isRobotAvailable = (r.getRobotVisitsForRouteNode(rn).size() < currentInstance
									.getNumberRobots());
							boolean isReachable = (currentInstance.getRobotMaxRadius() >= currentInstance
									.getDistanceCustomers(toBeInserted.getId(), rn.getNode().getId()));

							if (isReachable && isRobotAvailable) {

								// calculate cost for robotvisitinsert at rn
								r.addNodeAsRobotVisit(rn.getNode(), toBeInserted, false);

								double costAfterInsert = r.getCost();
								double currentCost = costAfterInsert - costBeforeInsert;
								boolean isInsertFeasible = r.isFeasible();
								r.removeNodeFromRobotVisit(rn.getNode(), toBeInserted, true);

								if (currentCost < cheapestInsert && isInsertFeasible) {
									secondCheapestInsert = cheapestInsert;
									cheapestInsert = currentCost;
								}
							}
						}

					}

					// in case theres no feasible robotinsert found
					if (cheapestInsert == Integer.MAX_VALUE) {

						int position = r.getRoute().size();

						while (position > 0) {

							if (r.isTravelTimeAvailabe(toBeInserted, position)) {
								
								if (useCostEstimator) {

									Node prev = r.getRoute().get(position - 1).getNode();
									double traveltime = 0.0;

									if (position == r.getRoute().size()) {

										traveltime = currentInstance.getTravelingTimeVehicle(prev.getId(),
												toBeInserted.getId());

									} else {
										Node next = r.getRoute().get(position).getNode();
										traveltime = currentInstance.getTravelingTimeVehicle(prev.getId(),
												toBeInserted.getId())
												+ currentInstance.getTravelingTimeVehicle(toBeInserted.getId(),
														next.getId())
												- currentInstance.getTravelingTimeVehicle(prev.getId(), next.getId());
									}

									double servicetime = toBeInserted.getServiceTimeVehicle();
									double estimatedCost = traveltime + servicetime;

									if (estimatedCost < cheapestEstimator) {

										r.addNodeToRouteAtIndex(toBeInserted, position, false);
										double costAfterInsert = r.getCost();
										double currentCost = costAfterInsert - costBeforeInsert;
										boolean isInsertFeasible = r.isFeasible();
//										r.removeNodeFromRoute(toBeInserted, true);
										r.removeNodeFromRouteAtIndex(toBeInserted, position, true);

										if (currentCost < cheapestInsert && isInsertFeasible) {

											cheapestEstimator = estimatedCost;
											
											secondCheapestInsert = cheapestInsert;
											cheapestInsert = currentCost;
										}
									}

								} else {

									r.addNodeToRouteAtIndex(toBeInserted, position, false);
									double costAfterInsert = r.getCost();
									double currentCost = costAfterInsert - costBeforeInsert;
									boolean isInsertFeasible = r.isFeasible();
//									r.removeNodeFromRoute(toBeInserted, true);
									r.removeNodeFromRouteAtIndex(toBeInserted, position, true);

									if (currentCost < cheapestInsert && isInsertFeasible) {
										secondCheapestInsert = cheapestInsert;
										cheapestInsert = currentCost;
									}
								}
							}
							position--;
						}

						// in case theres only 1 feasible robot insert found
					} else if (secondCheapestInsert == Integer.MAX_VALUE) {

						int position = r.getRoute().size();

						while (position > 0) {

							if (r.isTravelTimeAvailabe(toBeInserted, position)) {
								
								if (useCostEstimator) {

									Node prev = r.getRoute().get(position - 1).getNode();
									double traveltime = 0.0;

									if (position == r.getRoute().size()) {

										traveltime = currentInstance.getTravelingTimeVehicle(prev.getId(),
												toBeInserted.getId());

									} else {
										Node next = r.getRoute().get(position).getNode();
										traveltime = currentInstance.getTravelingTimeVehicle(prev.getId(),
												toBeInserted.getId())
												+ currentInstance.getTravelingTimeVehicle(toBeInserted.getId(),
														next.getId())
												- currentInstance.getTravelingTimeVehicle(prev.getId(), next.getId());
									}

									double servicetime = toBeInserted.getServiceTimeVehicle();
									double estimatedCost = traveltime + servicetime;

									if (estimatedCost < cheapestEstimator) {

										r.addNodeToRouteAtIndex(toBeInserted, position, false);
										double costAfterInsert = r.getCost();
										double currentCost = costAfterInsert - costBeforeInsert;
										boolean isInsertFeasible = r.isFeasible();
//										r.removeNodeFromRoute(toBeInserted, true);
										r.removeNodeFromRouteAtIndex(toBeInserted, position, true);

										if (currentCost < cheapestInsert && isInsertFeasible) {

											cheapestEstimator = estimatedCost;

											secondCheapestInsert = currentCost;
										}
									}

								} else {

									r.addNodeToRouteAtIndex(toBeInserted, position, false);
									double costAfterInsert = r.getCost();
									double currentCost = costAfterInsert - costBeforeInsert;
									boolean isInsertFeasible = r.isFeasible();
//									r.removeNodeFromRoute(toBeInserted, true);
									r.removeNodeFromRouteAtIndex(toBeInserted, position, true);

									if (currentCost < cheapestInsert && isInsertFeasible) {
										secondCheapestInsert = currentCost;
									}

								}
								
							}
							position--;
						}
					}
				}
			}

			toBeInserted.setRegret(secondCheapestInsert - cheapestInsert);
			prioQueue.add(toBeInserted);
		}

		// insert
		while (prioQueue.size() > 0) {

			RobotInsert robotInsert = new RobotInsert();
			robotInsert.setNodeToBeInserted(prioQueue.poll());

			double cheapestInsertCost = Integer.MAX_VALUE;

			for (Route r : toBeFixed.getRoutes()) {

				if (r.isCapacityAvailable(robotInsert.getNodeToBeInserted())
						&& robotInsert.getNodeToBeInserted().getRobotAccessible() == 1) {

					r.updateCost();
					double costBeforeInsert = r.getCost();

					for (RouteNode rn : r.getRoute()) {

						if (rn.getId() != 0) {

							Node insertCandidate = robotInsert.getNodeToBeInserted();

							double currentDist = currentInstance.getDistanceCustomers(rn.getId(),
									insertCandidate.getId());

							boolean isRobotAvailable = (r.getRobotVisitsForRouteNode(rn).size() < currentInstance
									.getNumberRobots());
							boolean isReachable = (currentInstance.getRobotMaxRadius() >= currentDist);

							double dummyServiceTimeEPD = insertCandidate.getReadyTime()
									+ currentInstance.getTravelingTimeRobot(rn.getId(), insertCandidate.getId())
									+ insertCandidate.getServiceTimeRobot();

							isReachable = isReachable && (rn.getEPD() >= dummyServiceTimeEPD);
							isReachable = isReachable && (rn.getEPA() < (insertCandidate.getDueTime()
									- currentInstance.getTravelingTimeRobot(rn.getId(), insertCandidate.getId())));

							if (isRobotAvailable && isReachable) {

								r.addNodeAsRobotVisit(rn.getNode(), robotInsert.getNodeToBeInserted(), false);
								double currentCost = r.getCost() - costBeforeInsert;
								boolean isFeasible = r.isFeasible();
								r.removeNodeFromRobotVisit(rn.getNode(), robotInsert.getNodeToBeInserted(), true);

								if (cheapestInsertCost > currentCost && isFeasible) {

									cheapestInsertCost = currentCost;

									robotInsert.setIndexNode(rn.getNode());
									robotInsert.setRouteForInsert(r);
									robotInsert.setIsFeasibleInsert(true);
								}
							}

						}
					}
				}

			}

			if (robotInsert.getIsFeasibleInsert()) {

				robotInsert.executeInsert();
				robotInsert.getRouteForInsert().updateCost();;
				routeCosts.remove(robotInsert.getRouteForInsert().getId());
				routeCosts.add(robotInsert.getRouteForInsert().getId(), robotInsert.getRouteForInsert().getCost());
			

			} else {

				/*
				 * if theres no feasible insert of a robotinsert found search for the next
				 * cheapest routeinsert
				 */

				RouteInsert routeInsert = new RouteInsert();
				routeInsert.setNodeToBeInserted(robotInsert.getNodeToBeInserted());

				cheapestInsertCost = Integer.MAX_VALUE;
				double cheapestEstimator = Integer.MAX_VALUE;

				for (Route r : toBeFixed.getRoutes()) {
					if (r.isCapacityAvailable(routeInsert.getNodeToBeInserted())) {

						double costBeforeInsert = routeCosts.get(r.getId());

						int position = r.getRoute().size();
						while (position > 0) {

							if (r.isTravelTimeAvailabe(routeInsert.getNodeToBeInserted(), position)) {

								Node insertCandidate = routeInsert.getNodeToBeInserted();

								if (useCostEstimator) {

									Node prev = r.getRoute().get(position - 1).getNode();
									double traveltime = 0.0;

									if (position == r.getRoute().size()) {

										traveltime = currentInstance.getTravelingTimeVehicle(prev.getId(),
												insertCandidate.getId());

									} else {
										Node next = r.getRoute().get(position).getNode();
										traveltime = currentInstance.getTravelingTimeVehicle(prev.getId(),
												insertCandidate.getId())
												+ currentInstance.getTravelingTimeVehicle(insertCandidate.getId(),
														next.getId())
												- currentInstance.getTravelingTimeVehicle(prev.getId(), next.getId());
									}

									double servicetime = insertCandidate.getServiceTimeVehicle();
									double estimatedCost = traveltime + servicetime;

									if (estimatedCost < cheapestEstimator) {

										r.addNodeToRouteAtIndex(insertCandidate, position, false);
										double currentCost = r.getCost() - costBeforeInsert;
										boolean isInsertFeasible = r.isFeasible();
//										r.removeNodeFromRoute(insertCandidate, true);
										r.removeNodeFromRouteAtIndex(insertCandidate, position, true);

										if (currentCost < cheapestInsertCost && isInsertFeasible) {

											cheapestEstimator = estimatedCost;
											cheapestInsertCost = currentCost;

											routeInsert.setInsertPosition(position);
											routeInsert.setRouteForInsert(r);
											routeInsert.setIsFeasibleInsert(true);
										}
									}

								} else {

									r.addNodeToRouteAtIndex(insertCandidate, position, false);
									double currentCost = r.getCost() - costBeforeInsert;
									boolean isInsertFeasible = r.isFeasible();
//									r.removeNodeFromRoute(insertCandidate, true);
									r.removeNodeFromRouteAtIndex(insertCandidate, position, true);

									if (currentCost < cheapestInsertCost && isInsertFeasible) {

										cheapestInsertCost = currentCost;

										routeInsert.setInsertPosition(position);
										routeInsert.setRouteForInsert(r);
										routeInsert.setIsFeasibleInsert(true);
									}

								}
							}
							position--;
						}
					}
				}

				if (routeInsert.getIsFeasibleInsert()) {
					routeInsert.executeInsert();
					routeInsert.getRouteForInsert().updateCost();;
					routeCosts.remove(routeInsert.getRouteForInsert().getId());
					routeCosts.add(routeInsert.getRouteForInsert().getId(), routeInsert.getRouteForInsert().getCost());
				}
			}

		}

		long timeEnd = System.currentTimeMillis();
		this.addToAlgoTime(timeEnd - timeStart);

		return toBeFixed;
	}

}