package components;

public class RobotInsert extends AbstractInsertJob {

	private Node indexNode;

	public Node getIndexNode() {
		return indexNode;
	}

	public void setIndexNode(Node indexNode) {
		this.indexNode = indexNode;
	}

	public void executeInsert() {
		toBeInserted.setIgnored(false);
		routeForInsert.addNodeAsRobotVisit(indexNode, toBeInserted, true);
	}
}
