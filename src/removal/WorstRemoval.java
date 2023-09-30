package removal;

import java.util.LinkedList;

import alns.MyALNSSolution;
import components.Route;
import components.Node;

public class WorstRemoval extends AbstractALNSRemoval implements InterfaceALNSRemoval {

	public WorstRemoval() {
		this.name = "Worst Removal";
	}

	@Override
	public MyALNSSolution remove(MyALNSSolution toBeDestroyed) throws Exception {

		int gamma = toBeDestroyed.getGamma();

		/*
		 * this should actually be very inefficient since i need to iterate gamma*n
		 * times to get my removalset and removing and reinserting it
		 */

		long timeStart = System.currentTimeMillis();

		while (toBeDestroyed.listRemovedNodes.size() <= gamma) {

			Node toBeRemoved = null;
			Route toBeRemovedFrom = null;
			double maxCost = Integer.MIN_VALUE;

			for (Route r : toBeDestroyed.getRoutes()) {

				int position = r.getRoute().size() - 1;
				double costBeforeRemoval = r.getCost();

				while (position > 0) {

					Node removeCandidate = r.getRoute().get(position).getNode();
					LinkedList<Node> listRemovedRobotVisits = new LinkedList<Node>();

					listRemovedRobotVisits = r.removeNodeFromRoute(removeCandidate, false);
					double costAfterRemoval = r.getCost();
					double currCost = (costBeforeRemoval - costAfterRemoval) / r.getSize();

					if (currCost > maxCost) {
						toBeRemoved = removeCandidate;
						toBeRemovedFrom = r;
						maxCost = currCost;
					}

					// reinserting removed previously removed nodes
					r.addNodeToRouteAtIndex(removeCandidate, position, true);
					for (Node m : listRemovedRobotVisits) {
						if (m.getId() != removeCandidate.getId()) {
							r.addNodeAsRobotVisit(removeCandidate, m, true);
						}
					}
					r.updateCost();
					position--;
				}

			}

			if (toBeRemoved != null && toBeRemovedFrom != null) {
				for (Node n : toBeRemovedFrom.removeNodeFromRoute(toBeRemoved, true)) {
					toBeDestroyed.getRemovedNodes().add(n);
				}
			}
		}

		long timeEnd = System.currentTimeMillis();
		this.addToAlgoTime(timeEnd - timeStart);
		
		return toBeDestroyed;
	}

}
