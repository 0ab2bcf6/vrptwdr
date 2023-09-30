package components;

public class RouteInsert extends AbstractInsertJob {

	private int insertPosition;

	public void setInsertPosition(int insertPosition) {
		this.insertPosition = insertPosition;
	}

	public int getInsertPosition() {
		return this.insertPosition;
	}

	public void executeInsert() {
		if (insertPosition == routeForInsert.getRoute().size()) {
			toBeInserted.setIgnored(false);
			routeForInsert.addNodeToRoute(toBeInserted, true);
		} else {
			toBeInserted.setIgnored(false);
			routeForInsert.addNodeToRouteAtIndex(toBeInserted, insertPosition, true);
		}
	}
}
