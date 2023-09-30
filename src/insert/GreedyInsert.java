package insert;

import alns.MyALNSSolution;
import components.Node;
import components.RobotInsert;
import components.Route;
import components.RouteInsert;
import components.RouteNode;

public class GreedyInsert extends AbstractALNSInsert implements InterfaceALNSInsert {

	public GreedyInsert(boolean withNoise) {

		if (withNoise) {
			this.name = "Greedy Insert With Noise";
			enableNoise();
		} else {
			this.name = "Greedy Insert";
		}
	}

	@Override
	public MyALNSSolution insert(MyALNSSolution toBeFixed) {

		long timeStart = System.currentTimeMillis();

		initialize(toBeFixed);

		double noise = 0.0;

		// insert for non robot accessible customers
		while (listNRA.size() > 0) {

			RouteInsert routeInsert = new RouteInsert();

			double cheapestInsertCost = Integer.MAX_VALUE;
			double cheapestEstimator = Integer.MAX_VALUE;

			int i = listNRA.size() - 1;
			while (i > -1) {

				Node insertCandidate = listNRA.get(i);

				for (Route r : toBeFixed.getRoutes()) {

					// check capacity constraint
					if (r.isCapacityAvailable(insertCandidate)) {

						double costBeforeInsert = routeCosts.get(r.getId());

						int position = r.getRoute().size();
						while (position > 0) {

							if (r.isTravelTimeAvailabe(insertCandidate, position)) {

								if (withNoise) {
									noise = noise(toBeFixed);
								} else {
									noise = 0.0;
								}

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
									double estimatedCost = traveltime + servicetime + noise;

									if (estimatedCost < cheapestEstimator) {

										r.addNodeToRouteAtIndex(insertCandidate, position, false);
										double currentCost = r.getCost() - costBeforeInsert + noise;
										boolean isInsertFeasible = r.isFeasible();
//										r.removeNodeFromRoute(insertCandidate, true);
										r.removeNodeFromRouteAtIndex(insertCandidate, position, true);

										if (currentCost < cheapestInsertCost && isInsertFeasible) {

											cheapestEstimator = estimatedCost;
											cheapestInsertCost = currentCost;

											routeInsert.setRemoveIndex(i);
											routeInsert.setInsertPosition(position);
											routeInsert.setNodeToBeInserted(insertCandidate);
											routeInsert.setRouteForInsert(r);
											routeInsert.setIsFeasibleInsert(true);
										}
									}

								} else {

									r.addNodeToRouteAtIndex(insertCandidate, position, false);
									double currentCost = r.getCost() - costBeforeInsert + noise;
									boolean isInsertFeasible = r.isFeasible();
//									r.removeNodeFromRoute(insertCandidate, true);
									r.removeNodeFromRouteAtIndex(insertCandidate, position, true);

									if (currentCost < cheapestInsertCost && isInsertFeasible) {

										cheapestInsertCost = currentCost;

										routeInsert.setRemoveIndex(i);
										routeInsert.setInsertPosition(position);
										routeInsert.setNodeToBeInserted(insertCandidate);
										routeInsert.setRouteForInsert(r);
										routeInsert.setIsFeasibleInsert(true);
									}

								}
							}
							position--;
						}
					}
				}
				i--;
			}

			// check insert
			if (routeInsert.getIsFeasibleInsert()) {

				listNRA.remove(routeInsert.getRemoveIndex());
				routeInsert.executeInsert();
				routeInsert.getRouteForInsert().updateCost();;
				routeCosts.remove(routeInsert.getRouteForInsert().getId());
				routeCosts.add(routeInsert.getRouteForInsert().getId(), routeInsert.getRouteForInsert().getCost());
			}
		}

		// insert for robot accessible customers
		while (listRA.size() > 0) {

			RobotInsert robotInsert = new RobotInsert();

			double cheapestInsertCost = Integer.MAX_VALUE;

			int i = listRA.size() - 1;
			while (i > -1) {

				Node insertCandidate = listRA.get(i);

				for (Route r : toBeFixed.getRoutes()) {

					// check capacity constraint
					if (r.isCapacityAvailable(insertCandidate)) {

						double costBeforeInsert = routeCosts.get(r.getId());

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

								double dummyServiceTimeEPD = insertCandidate.getReadyTime()
										+ currentInstance.getTravelingTimeRobot(rn.getId(), insertCandidate.getId())
										+ insertCandidate.getServiceTimeRobot();

								isReachable = isReachable && (rn.getEPD() >= dummyServiceTimeEPD);
								isReachable = isReachable && (rn.getEPA() < (insertCandidate.getDueTime()
										- currentInstance.getTravelingTimeRobot(rn.getId(), insertCandidate.getId())));

								if (isRobotAvailable && isReachable) {

									if (withNoise) {
										noise = noise(toBeFixed);
									} else {
										noise = 0.0;
									}

									r.addNodeAsRobotVisit(rn.getNode(), insertCandidate, false);
									double currentCost = r.getCost() - costBeforeInsert + noise;
									boolean isFeasible = r.isFeasible();
									r.removeFromRobotVisit(insertCandidate, true);

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

			// check
			if (robotInsert.getIsFeasibleInsert()) {

				listRA.remove(robotInsert.getRemoveIndex());
				robotInsert.executeInsert();
				robotInsert.getRouteForInsert().updateCost();;
				routeCosts.remove(robotInsert.getRouteForInsert().getId());
				routeCosts.add(robotInsert.getRouteForInsert().getId(), robotInsert.getRouteForInsert().getCost());

			} else {

				/*
				 * if theres no feasible insert of a robotvisit found search for the single
				 * cheapest routeinsert
				 */

				// find cheapest route insert
				RouteInsert routeInsert = new RouteInsert();

				cheapestInsertCost = Integer.MAX_VALUE;
				double cheapestEstimator = Integer.MAX_VALUE;

				i = listRA.size() - 1;
				while (i > -1) {

					Node insertCandidate = listRA.get(i);

					for (Route r : toBeFixed.getRoutes()) {

						// check capacity constraint
						if (r.isCapacityAvailable(insertCandidate)) {

							double costBeforeInsert = routeCosts.get(r.getId());

							int position = r.getRoute().size();
							while (position > 0) {

								if (r.isTravelTimeAvailabe(insertCandidate, position)) {

									if (withNoise) {
										noise = noise(toBeFixed);
									} else {
										noise = 0.0;
									}

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
													- currentInstance.getTravelingTimeVehicle(prev.getId(),
															next.getId());
										}

										double servicetime = insertCandidate.getServiceTimeVehicle();
										double estimatedCost = traveltime + servicetime + noise;

										if (estimatedCost < cheapestEstimator) {

											r.addNodeToRouteAtIndex(insertCandidate, position, false);
											double currentCost = r.getCost() - costBeforeInsert + noise;
											boolean isInsertFeasible = r.isFeasible();
//											r.removeNodeFromRoute(insertCandidate, true);
											r.removeNodeFromRouteAtIndex(insertCandidate, position, true);

											if (currentCost < cheapestInsertCost && isInsertFeasible) {

												cheapestEstimator = estimatedCost;
												cheapestInsertCost = currentCost;

												routeInsert.setRemoveIndex(i);
												routeInsert.setInsertPosition(position);
												routeInsert.setNodeToBeInserted(insertCandidate);
												routeInsert.setRouteForInsert(r);
												routeInsert.setIsFeasibleInsert(true);
											}
										}

									} else {

										r.addNodeToRouteAtIndex(insertCandidate, position, false);
										double currentCost = r.getCost() - costBeforeInsert + noise;
										boolean isInsertFeasible = r.isFeasible();
//										r.removeNodeFromRoute(insertCandidate, true);
										r.removeNodeFromRouteAtIndex(insertCandidate, position, true);

										if (currentCost < cheapestInsertCost && isInsertFeasible) {

											cheapestInsertCost = currentCost;

											routeInsert.setRemoveIndex(i);
											routeInsert.setInsertPosition(position);
											routeInsert.setNodeToBeInserted(insertCandidate);
											routeInsert.setRouteForInsert(r);
											routeInsert.setIsFeasibleInsert(true);
										}

									}

								}
								position--;
							}
						}
					}
					i--;
				}

				if (routeInsert.getIsFeasibleInsert()) {
					listRA.remove(routeInsert.getRemoveIndex());
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