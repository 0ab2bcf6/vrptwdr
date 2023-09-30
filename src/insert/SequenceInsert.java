package insert;

import alns.MyALNSSolution;
import components.Node;
import components.RouteInsert;

public class SequenceInsert extends AbstractALNSInsert implements InterfaceALNSInsert {
	
	public SequenceInsert() {
		this.name = "Sequence Insert";
	}

	/**
	 *The idea is to insert entire strings of customers, that share 
	 */
	
	@Override
	public MyALNSSolution insert(MyALNSSolution toBeFixed) {
		// TODO Auto-generated method stub
		
		//this is the length of the array of nodes to be inserted
		int lengthInsert = 3;
		//this controls how closely related the nodes should be
		double thresholdSimilarity = 0.0;
		
		long timeStart = System.currentTimeMillis();

		initialize(toBeFixed);
		
		while (listNRA.size() > 0) {

			RouteInsert[] routeInsert = new RouteInsert[lengthInsert];

			double cheapestInsertCost = Integer.MAX_VALUE;
			double cheapestEstimator = Integer.MAX_VALUE;

			int i = listNRA.size() - 1;
			
			//find the lengthInsert's most closely related nodes
			while (i > -1) {

				Node insertCandidate = listNRA.get(i);		
				
			}
			
			//find a route has capacity for the array of nodes
		}
		
		//insert the remaining nodes via greedyinsert
		
				
				
		long timeEnd = System.currentTimeMillis();
		this.addToAlgoTime(timeEnd - timeStart);

		return toBeFixed;
	}

}
