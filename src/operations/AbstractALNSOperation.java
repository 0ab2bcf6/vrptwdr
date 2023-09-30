package operations;

public abstract class AbstractALNSOperation implements InterfaceALNSOperation {
	
	protected String name;

    private int pi; 	//quantification of performance of operator m in segment i
    private double p; 	//probability to get drawn, initially 1/#operations
    private int theta; 	//shows how many times the operator has been used in segment i
    private int thetaImp; //increased when an improvement is made to the best solution in an iteration
    private double w; 	//weight, initially 1, w_mi = w_m(i-1) (1-r)+r*pi_mi / theta_mi
    
    private long totalTime = 0;	//i want to track how much time was spent in each operation
    private long algoTime = 0;
    
    @Override
    public void incTheta() {
        this.theta++;
    }
    
    @Override
    public void incThetaImp() {
        this.thetaImp++;
    }
    
    @Override
    public void setThetaImp(int thetaImp) {
        this.thetaImp = thetaImp;
    }
    
    @Override
    public int getThetaImp() {
        return this.thetaImp;
    }
    

    @Override
    public void addToPi(int pi) {
        this.pi += pi;
    }
    
    @Override
    public int getPi() {
        return this.pi;
    }

    @Override
    public void setPi(int pi) {
        this.pi = pi;
    }

    @Override
    public double getP() {
        return this.p;
    }

    @Override
    public void setP(double p) {
        this.p = p;
    }

    @Override
    public int getTheta() {
        return this.theta;
    }

    @Override
    public void setTheta(int t) {
        this.theta = t;
    }

    @Override
    public double getW() {
        return this.w;
    }
    
    @Override
    public void setW(double w) {
        this.w = w;
    }
  
    @Override
    public void addToTotalTime(long t) {
    	this.totalTime += t;
    }
    
    @Override
    public long getTotalTime() {
    	return this.totalTime;
    }
    
    @Override
    public void initialize() {
    	this.setTheta(0);
    	this.setThetaImp(0);
		this.setPi(0);
		this.setW(1.0);
    }
    
    @Override
    public void addToAlgoTime(long t) {
    	this.algoTime += t;
    }
    
    @Override
    public long getAlgoTime() {
    	return this.algoTime;
    }

    @Override
    public String getOperatorName() {
    	return this.name;
    }

}