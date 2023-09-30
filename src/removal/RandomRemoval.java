package removal;

import java.util.ArrayList;

import alns.MyALNSSolution;
import components.Instance;
import components.Node;
import components.Route;

public class RandomRemoval extends AbstractALNSRemoval implements InterfaceALNSRemoval {

	public RandomRemoval() {
		this.name = "Random Removal";
	}

	@Override
	public MyALNSSolution remove(MyALNSSolution toBeDestroyed) throws Exception {

		Instance currentInstance = toBeDestroyed.getInstance();
		int gamma = toBeDestroyed.getGamma();

		ArrayList<Node> listCustomers = new ArrayList<>(currentInstance.getListCustomers());

		long timeStart = System.currentTimeMillis();

		while (toBeDestroyed.listRemovedNodes.size() < gamma) {

			// choose a customer randomly, +1 so depot is not being chosen
			int customerToBeRemoved = (int) (Math.random() * currentInstance.getNumberCustomers() + 1);
			Node toBeRemoved = currentInstance.getCustomers(customerToBeRemoved);

			for (Route r : toBeDestroyed.getRoutes()) {

				if (r.isNodeInRoute(toBeRemoved)) {
					if (r.isNodeInRouteVisit(toBeRemoved)) {

						for (Node n : r.removeNodeFromRoute(toBeRemoved, true)) {
							toBeDestroyed.getRemovedNodes().add(n);
							listCustomers.remove(n);
						}
						break;

					} else {

						try {
							toBeDestroyed.getRemovedNodes().add(r.removeFromRobotVisit(toBeRemoved, true).get(0));
						} catch (Exception ex) {
							// System.out.println(ex);
						}
						listCustomers.remove(toBeRemoved);
						break;
					}
				}
			}
		}

		long timeEnd = System.currentTimeMillis();
		this.addToAlgoTime(timeEnd - timeStart);

		return toBeDestroyed;
	}

}