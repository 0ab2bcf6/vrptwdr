package config;

public enum ALNSConfiguration implements InterfaceALNSConfiguration {

	/*
	 * Ropke & Pisinger (2006) The complete parameter tuning resulted in the
	 * following parameter vector (phi, chi, psi, omega, p, p_worst, w, c, sigma1,
	 * sigma2, sigma3, r, eta, xi) = (9, 3, 2, 5, 6, 3, 0.05, 0.99975, 33, 9, 13,
	 * 0.1, 0.025, 0.4)
	 */

	DEFAULT(25000, 2000, 0.1, 33, 9, 13, 0.9975, 1.0, 0.4, 9, 3, 2, 5);

	private final int omega; 	// number of iterations
	private final int tau; 		// number of iterations per segment
	private final double rp; 	// reaction factor
	private final int sigma1; 	// reward score for new global solution
	private final int sigma2; 	// reward score for cost(s_new)>=cost(s_current)
	private final int sigma3; 	// reward score for cost(s_new)<cost(s_current)
	private final double c; 	// cooling rate
	private final double eta; 	// eta for T_start
	private final double xi;	// controls the decrease speed of MaxRemoval

	// these are the parameter for the shaw removal
	private final int phi;
	private final int chi;
	private final int psi;
	private final int smallomega;

	ALNSConfiguration(int omega, int tau, double rp, int sigma1, int sigma2, int sigma3, double c, double eta,
			double xi, int phi, int chi, int psi, int smallomega) {
		this.omega = omega;
		this.tau = tau;
		this.rp = rp;
		this.sigma1 = sigma1;
		this.sigma2 = sigma2;
		this.sigma3 = sigma3;
		this.c = c;
		this.eta = eta;
		this.xi = xi;
		this.psi = psi;
		this.chi = chi;
		this.phi = phi;
		this.smallomega = smallomega;
	}

	@Override
	public int getOmega() {
		return omega;
	}

	@Override
	public int getTau() {
		return tau;
	}

	@Override
	public double getRp() {
		return rp;
	}

	@Override
	public int getSigma1() {
		return sigma1;
	}

	@Override
	public int getSigma2() {
		return sigma2;
	}

	@Override
	public int getSigma3() {
		return sigma3;
	}

	@Override
	public double getC() {
		return c;
	}

	@Override
	public double getEta() {
		return eta;
	}

	@Override
	public int getPhi() {
		return phi;
	}

	@Override
	public int getChi() {
		return chi;
	}

	@Override
	public int getPsi() {
		return psi;
	}

	@Override
	public int getSmallOmega() {
		return smallomega;
	}

	@Override
	public double getXi() {
		return xi;
	}

}
