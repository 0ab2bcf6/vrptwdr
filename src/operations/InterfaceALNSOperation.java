package operations;

public interface InterfaceALNSOperation {

	int getPi(); // quantification of performance operator

	void setPi(int pi);

	void addToPi(int pi);

	double getP();

	void setP(double p);

	double getW(); // score of operator

	void setW(double p);

	void incTheta();

	int getTheta(); // shows how many times the operator has been used in segment i

	void setTheta(int t);

	void addToTotalTime(long t);

	long getTotalTime();
	
	void initialize();

	void addToAlgoTime(long t);

	long getAlgoTime();
	
	void incThetaImp();
	
	void setThetaImp(int thetaImp);
	
	int getThetaImp();

	String getOperatorName();

}
