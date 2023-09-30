package removal;

import alns.MyALNSSolution;
import components.Node;
import components.Instance;
import components.Route;
import config.InterfaceALNSConfiguration;

import java.util.HashSet;

public class ShawRemoval extends AbstractALNSRemoval implements InterfaceALNSRemoval {

	private int phi = 9;
	private int chi = 2;
	private int psi = 3;
	private int omega = 5;

	public ShawRemoval(InterfaceALNSConfiguration config) {
		this.name = "Shaw Removal";
		this.phi = config.getPhi();
		this.chi = config.getChi();
		this.psi = config.getPsi();
		this.omega = config.getSmallOmega();
	}

	public ShawRemoval(int phi, int chi, int psi, int omega) {
		if (phi == 0 && chi != 0 && psi == 0 && omega == 0) {
			this.name = "Time Window Removal";
			this.phi = 0;
			this.chi = chi;
			this.psi = 0;
			this.omega = 0;
		} else if (phi != 0 && chi == 0 && psi == 0 && omega == 0) {
			this.name = "Distance Removal";
			this.phi = phi;
			this.chi = 0;
			this.psi = 0;
			this.omega = 0;
		} else {
			this.name = "Shaw Removal";
			this.phi = phi;
			this.chi = chi;
			this.psi = psi;
			this.omega = omega;
		}
	}

	@Override
	public MyALNSSolution remove(MyALNSSolution toBeDestroyed) throws Exception {

		Instance currentInstance = toBeDestroyed.getInstance();
		int gamma = toBeDestroyed.getGamma();

		HashSet<Node> alreadyChosen = new HashSet<>();

		long timeStart = System.currentTimeMillis();

		// choose a customer randomly, +1 so depot is not being chosen
		int removedFromRouteId = 0;
		Node firstCustomerRemove = currentInstance
				.getCustomers((int) (Math.random() * currentInstance.getNumberCustomers() + 1));

		for (Route r : toBeDestroyed.getRoutes()) {
			if (r.isNodeInRoute(firstCustomerRemove)) {
				removedFromRouteId = r.getId();
				if (r.isNodeInRouteVisit(firstCustomerRemove)) {
					for (Node n : r.removeNodeFromRoute(firstCustomerRemove, true)) {
						toBeDestroyed.getRemovedNodes().add(n);
						alreadyChosen.add(n);
					}
					break;
				} else {
					toBeDestroyed.getRemovedNodes().add(r.removeFromRobotVisit(firstCustomerRemove, true).get(0));
					alreadyChosen.add(firstCustomerRemove);
					break;
				}
			}
		}

		// find related nodes
		while (toBeDestroyed.getRemovedNodes().size() <= gamma) {

			double minRelated = Integer.MAX_VALUE;
			Node toBeRemoved = null;

			for (Node i : toBeDestroyed.getRemovedNodes()) {
				for (Node j : currentInstance.getListCustomers()) {

					if (i.getId() != j.getId() && !alreadyChosen.contains(j)) {

						double twRel = Math.abs(i.getReadyTime() - j.getReadyTime())
								+ Math.abs(i.getDueTime() - j.getDueTime());
						double distRel = currentInstance.getDistanceCustomers(i.getId(), j.getId());
						double demRel = Math.abs(i.getDemand() - j.getDemand());
						int sr = 0;

						for (Route r : toBeDestroyed.getRoutes()) {
							if (r.isNodeInRoute(j)) {
								if (r.getId() == removedFromRouteId) {
									sr = -1;
								}
								break;
							}
						}

						double currRel = phi * distRel + chi * twRel + psi * demRel + omega * sr;

						if (currRel < minRelated) {
							toBeRemoved = j;
							minRelated = currRel;
						}

					}
				}
			}

			// remove nodes
			for (Route r : toBeDestroyed.getRoutes()) {
				if (r.isNodeInRoute(toBeRemoved)) {
					removedFromRouteId = r.getId();
					if (r.isNodeInRouteVisit(toBeRemoved)) {
						for (Node n : r.removeNodeFromRoute(toBeRemoved, true)) {
							toBeDestroyed.getRemovedNodes().add(n);
							alreadyChosen.add(n);
						}
						break;
					} else {
						toBeDestroyed.getRemovedNodes().add(r.removeFromRobotVisit(toBeRemoved, true).get(0));
						alreadyChosen.add(toBeRemoved);
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