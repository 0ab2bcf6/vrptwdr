package insert;

import java.util.ArrayList;

import alns.MyALNSSolution;
import components.Node;
import components.Route;
import components.Instance;
import operations.AbstractALNSOperation;

public abstract class AbstractALNSInsert extends AbstractALNSOperation {

	protected boolean withNoise = false;
	protected Instance currentInstance;
	protected ArrayList<Node> listNRA;
	protected ArrayList<Node> listRA;
	protected ArrayList<Double> routeCosts = new ArrayList<>();

	protected boolean useCostEstimator = true;

	protected void enableNoise() {
		this.withNoise = true;
	}

	protected double noise(MyALNSSolution toBeFixed) {

		double droof = toBeFixed.getInstance().getHighestTravelTime();
		double mu = 0.5;
		double epsilon = Math.random() * 2 - 1;
		double result = droof * mu * epsilon;

		return result;
	}

	protected void initialize(MyALNSSolution toBeFixed) {

		currentInstance = toBeFixed.getInstance();
		int gamma = toBeFixed.getRemovedNodes().size();

		// new list for non robot accessible customers
		listNRA = new ArrayList<Node>(gamma);

		/*
		 * if i have to deal with a VRPTW rather than a VRPTWDR then i skip this step
		 * since i have to copy the entire removal set every time
		 */

		int i = toBeFixed.getRemovedNodes().size() - 1;
		while (i > -1) {
			if (toBeFixed.getRemovedNodes().get(i).getRobotAccessible() == 0) {
				listNRA.add(toBeFixed.getRemovedNodes().remove(i));
			}
			i--;
		}
		listRA = toBeFixed.getRemovedNodes();

		for (Route r : toBeFixed.getRoutes()) {
			r.updateCost();
			double cost = r.getCost();
			routeCosts.add(r.getId(), cost);
		}
	}
}
