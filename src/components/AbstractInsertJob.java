package components;

public abstract class AbstractInsertJob {

	protected Node toBeInserted;
	protected Route routeForInsert;
	
	protected int removeIndex;

	protected boolean isFeasibleInsert = false;;

	public void setNodeToBeInserted(Node n) {
		this.toBeInserted = n;
	}

	public Node getNodeToBeInserted() {
		return this.toBeInserted;
	}

	public void setRouteForInsert(Route r) {
		this.routeForInsert = r;
	}

	public Route getRouteForInsert() {
		return this.routeForInsert;
	}

	public void setIsFeasibleInsert(boolean isFeasibleInsert) {
		this.isFeasibleInsert = isFeasibleInsert;
	}

	public boolean getIsFeasibleInsert() {
		return this.isFeasibleInsert;
	}
	
	public int getRemoveIndex() {
		return removeIndex;
	}

	public void setRemoveIndex(int removeIndex) {
		this.removeIndex = removeIndex;
	}
}
