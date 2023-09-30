package insert;

import alns.MyALNSSolution;

import components.Node;
import components.RobotInsert;
import components.Route;
import components.RouteInsert;
import components.RouteNode;

public class BestInsert extends AbstractALNSInsert implements InterfaceALNSInsert {

	public BestInsert(boolean withNoise) {

		if (withNoise) {
			this.name = "Best Insert with Noise";
			enableNoise();
		} else {
			this.name = "Best Insert";
		}
	}

	@Override
	public MyALNSSolution insert(MyALNSSolution toBeFixed) {

		long timeStart = System.currentTimeMillis();

		initialize(toBeFixed);

		double noise = 0.0;

		// insert for all non robot accessible customers
		while (listNRA.size() > 0) {

			RouteInsert routeInsert = new RouteInsert();

			double random = Math.random();

			if (random < 0.5) { // random selection

				int randomindex = (int) (Math.random() * listNRA.size());
				routeInsert.setNodeToBeInserted(listNRA.remove(randomindex));

			} else { // time window priority selection

				int twPrioCandidate = 0; //this is an index
				Node insertCandidate = listNRA.get(twPrioCandidate);

				int position = listNRA.size() - 1;
				while (position > -1) {
					Node n = listNRA.get(position);

					int a = Math.abs(n.getDueTime() - n.getReadyTime());
					int b = Math.abs(insertCandidate.getDueTime() - insertCandidate.getReadyTime());

					if (a < b) {
						twPrioCandidate = position;
					}
					position--;
				}

				routeInsert.setNodeToBeInserted(listNRA.remove(twPrioCandidate));

			}

			// find best insert
			double cheapestInsertCost = Integer.MAX_VALUE;
			double cheapestEstimator = Integer.MAX_VALUE;

			for (Route r : toBeFixed.getRoutes()) {

				if (r.isCapacityAvailable(routeInsert.getNodeToBeInserted())) {

					double costBeforeInsert = routeCosts.get(r.getId());

					Node insertCandidate = routeInsert.getNodeToBeInserted();
					int position = r.getRoute().size();
					while (position > 0) {

						if (r.isTravelTimeAvailabe(routeInsert.getNodeToBeInserted(), position)) {

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
//									r.removeNodeFromRoute(insertCandidate, true);
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
								double currentCost = r.getCost() - costBeforeInsert + noise;
								boolean isInsertFeasible = r.isFeasible();
//								r.removeNodeFromRoute(insertCandidate, true);
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

		// insert for robot accessible customers
		while (listRA.size() > 0) {

			RobotInsert robotInsert = new RobotInsert();

			double random = Math.random();

			if (random < 0.5) { // random selection

				int randomindex = (int) (Math.random() * listRA.size());
				robotInsert.setNodeToBeInserted(listRA.remove(randomindex));

			} else { // time window priority selection

				int twPrioCandidate = 0;
				Node insertCandidate = listRA.get(twPrioCandidate);

				int position = listRA.size() - 1;
				while (position > -1) {
					Node n = listRA.get(position);

					int a = Math.abs(n.getDueTime() - n.getReadyTime());
					int b = Math.abs(insertCandidate.getDueTime() - insertCandidate.getReadyTime());

					if (a < b) {
						twPrioCandidate = position;
					}
					position--;
				}

				robotInsert.setNodeToBeInserted(listRA.remove(twPrioCandidate));

			}

			// find best insert
			double cheapestInsertCost = Integer.MAX_VALUE;

			for (Route r : toBeFixed.getRoutes()) {

				// check capacity constraint
				if (r.isCapacityAvailable(robotInsert.getNodeToBeInserted())) {

					double costBeforeInsert = routeCosts.get(r.getId());

					for (RouteNode rn : r.getRoute()) {

						if (rn.getId() != 0) {

							/*
							 * in here i assume that if theres a feasible robotinsert found it is definitely
							 * cheaper than a route insert which should be a fair assumption to make i think
							 */

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

								if (withNoise) {
									noise = noise(toBeFixed);
								} else {
									noise = 0.0;
								}

								r.addNodeAsRobotVisit(rn.getNode(), robotInsert.getNodeToBeInserted(), false);
								double currentCost = r.getCost() - costBeforeInsert + noise;
								boolean isFeasible = r.isFeasible();
								r.removeFromRobotVisit(robotInsert.getNodeToBeInserted(), true);

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

			// if theres no feasible robot visit insert
			if (robotInsert.getIsFeasibleInsert()) {

				robotInsert.executeInsert();
				robotInsert.getRouteForInsert().updateCost();;
				routeCosts.remove(robotInsert.getRouteForInsert().getId());
				routeCosts.add(robotInsert.getRouteForInsert().getId(), robotInsert.getRouteForInsert().getCost());

			} else {

				RouteInsert routeInsert = new RouteInsert();
				routeInsert.setNodeToBeInserted(robotInsert.getNodeToBeInserted());

				// find best insert
				cheapestInsertCost = Integer.MAX_VALUE;
				double cheapestEstimator = Integer.MAX_VALUE;

				for (Route r : toBeFixed.getRoutes()) {

					if (r.isCapacityAvailable(routeInsert.getNodeToBeInserted())) {

						double costBeforeInsert = routeCosts.get(r.getId());

						int position = r.getRoute().size();
						while (position > 0) {

							if (r.isTravelTimeAvailabe(routeInsert.getNodeToBeInserted(), position)) {

								Node insertCandidate = robotInsert.getNodeToBeInserted();

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

											routeInsert.setInsertPosition(position);
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