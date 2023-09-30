package removal;

import operations.InterfaceALNSOperation;
import alns.MyALNSSolution;

public interface InterfaceALNSRemoval extends InterfaceALNSOperation{
	
	//Gamma = number of nodes to be removed
	MyALNSSolution remove(MyALNSSolution toBeDestroyed) throws Exception;

}
