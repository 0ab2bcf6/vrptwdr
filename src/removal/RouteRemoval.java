package removal;

import alns.MyALNSSolution;
import components.Node;
import components.Route;
import components.RouteNode;

import java.util.ArrayList;
import java.util.LinkedList;

public class RouteRemoval extends AbstractALNSRemoval implements InterfaceALNSRemoval {

	public RouteRemoval() {
		this.name = "Route Removal";
	}

	@Override
	public MyALNSSolution remove(MyALNSSolution toBeDestroyed) throws Exception {

		int gamma = toBeDestroyed.getGamma();

		ArrayList<Integer> withinGamma = new ArrayList<>(gamma);
		ArrayList<Integer> nonEmptyRoutes = new ArrayList<>(gamma);

		Route randomRoute = null;
		LinkedList<Node> listRouteNodes = new LinkedList<Node>();

		int intRandom = 0;
		int selectedRoute = 0;

		long timeStart = System.currentTimeMillis();

		for (Route r : toBeDestroyed.getRoutes()) {
			if (r.getSize() <= gamma && r.getSize() > 1) {
				withinGamma.add(r.getId());
			}

			if (r.getSize() > 1) {
				nonEmptyRoutes.add(r.getId());
			}
		}

		if (withinGamma.size() != 0) {

			intRandom = (int) (Math.random() * withinGamma.size());
			selectedRoute = withinGamma.get(intRandom);

			for (Route r : toBeDestroyed.getRoutes()) {
				if (selectedRoute == r.getId()) {
					randomRoute = r;
					for (RouteNode rn : r.getRoute()) {
						if (rn.getId() != 0) {
							listRouteNodes.add(rn.getNode());
						}
					}
				}
			}

		} else {

			intRandom = (int) (Math.random() * nonEmptyRoutes.size());
			selectedRoute = nonEmptyRoutes.get(intRandom);

			for (Route r : toBeDestroyed.getRoutes()) {
				if (selectedRoute == r.getId()) {
					randomRoute = r;
					for (RouteNode rn : r.getRoute()) {
						if (rn.getId() != 0) {
							listRouteNodes.add(rn.getNode());
						}
					}
				}
			}
		}

		for (Node n : listRouteNodes) {
			for (Node m : randomRoute.removeNodeFromRoute(n, true)) {
				toBeDestroyed.getRemovedNodes().add(m);
			}
		}

		long timeEnd = System.currentTimeMillis();
		this.addToAlgoTime(timeEnd - timeStart);

		return toBeDestroyed;
	}
}