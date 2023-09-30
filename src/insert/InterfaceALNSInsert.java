package insert;

import operations.InterfaceALNSOperation;
import alns.MyALNSSolution;

public interface InterfaceALNSInsert extends InterfaceALNSOperation {

    MyALNSSolution insert(MyALNSSolution toBeFixed);	
	
}
