package algorithm;

import java.util.LinkedList;

import components.Instance;
import components.Route;
import components.Solution;

import gurobi.GRB;
import gurobi.GRBEnv;
import gurobi.GRBException;
import gurobi.GRBLinExpr;
import gurobi.GRBModel;
import gurobi.GRBQuadExpr;
import gurobi.GRBVar;

public class GurobiSolver {

	private Instance currentInstance;
	private double objValue = 0;
	private long compTime = 0;

	public GurobiSolver() throws Exception {

	}

	public Solution solve(Instance instance) throws Exception {

		this.currentInstance = instance;
		@SuppressWarnings("unused")
		String pathLog = instance.getPathResult().replaceAll(".txt", ".log");

		int NumberVans = currentInstance.getNumberVans();
		int NumberRobots = currentInstance.getNumberRobots();

		int M = 6000; // Big-M

		int NumberCustomers = currentInstance.getNumberCustomers();
		double VanPayload = currentInstance.getVanPayload();
		double RobotMaxRadius = currentInstance.getRobotMaxRadius();

		/*
		 * to every restriciton of the model (that isnt already been taken care of) by
		 * the variable definitions theres a dedicated commented section. i choose to do
		 * this because it really helps searching, reading and adjusting possible
		 * mistakes.
		 */

		try {

			// Create empty environment, set options, and start
			GRBEnv env = new GRBEnv(true);
			// env.set("logFile", currentInstance.getDatasetName() +
			// currentInstance.getInstanceName());
			env.set("TimeLimit", "1800.0");
			env.start();

			// Create empty model
			GRBModel model = new GRBModel(env);

			// Create variables
			GRBVar[][][] x = new GRBVar[NumberCustomers + 1][NumberCustomers + 1][NumberVans];
			GRBVar[][][][] y = new GRBVar[NumberCustomers + 1][NumberCustomers + 1][NumberVans][NumberRobots];
			GRBVar[][][] p = new GRBVar[NumberCustomers + 1][NumberCustomers + 1][NumberVans];
			GRBVar[] a = new GRBVar[NumberCustomers + 1];
			GRBVar[] b = new GRBVar[NumberCustomers + 1];
			GRBVar[] w = new GRBVar[NumberCustomers + 1];

			for (int i = 0; i <= NumberCustomers; i++) {

				if (i != 0) {
					a[i] = model.addVar(540, currentInstance.getCustomers(i).getDueTime(), -1.0, GRB.CONTINUOUS,
							"a[" + String.valueOf(i) + "]");
					b[i] = model.addVar(currentInstance.getCustomers(i).getReadyTime(),
							currentInstance.getCustomers(i).getDueTime(), 1.0, GRB.CONTINUOUS,
							"b[" + String.valueOf(i) + "]");
					w[i] = model.addVar(0.0, GRB.INFINITY, 1.0, GRB.CONTINUOUS, "w[" + String.valueOf(i) + "]");
				}
				for (int j = 0; j <= NumberCustomers; j++) {
					for (int k = 0; k < NumberVans; k++) {

						x[i][j][k] = model.addVar(0.0, 1.0, 1.0, GRB.BINARY,
								"x[" + String.valueOf(i) + "][" + String.valueOf(j) + "][" + String.valueOf(k) + "]");
						p[i][j][k] = model.addVar(0.0, 1200.0, 0.0, GRB.CONTINUOUS,
								"p[" + String.valueOf(i) + "][" + String.valueOf(j) + "][" + String.valueOf(k) + "]");

						for (int d = 0; d < NumberRobots; d++) {
							if ((i != 0) && (j != 0)) {
								y[i][j][k][d] = model.addVar(0.0, 1.0, 0.0, GRB.BINARY,
										"y[" + String.valueOf(i) + "][" + String.valueOf(j) + "][" + String.valueOf(k)
												+ "][" + String.valueOf(d) + "]");
							}
						}
					}
				}
			}

			/* +++ BEGIN OBJECTIVE FUNCTION +++ */

			GRBLinExpr expr = new GRBLinExpr();
			GRBLinExpr expr2 = new GRBLinExpr();

			for (int i = 0; i <= NumberCustomers; i++) {
				for (int j = 0; j <= NumberCustomers; j++) {
					for (int k = 0; k < NumberVans; k++) {
						expr.addTerm(currentInstance.getTravelingTimeVehicle(i, j), x[i][j][k]);
					}
				}
			}

			for (int i = 1; i <= NumberCustomers; i++) {
				expr.addTerm(1.0, b[i]);
				expr.addTerm(-1.0, a[i]);
				expr.addTerm(1.0, w[i]);
			}

			model.setObjective(expr, GRB.MINIMIZE);

			/* +++ END OBJECTIVE FUNCTION +++ */

			/* +++ BEGIN RESTRICITION (2) +++ */

			for (int j = 1; j <= NumberCustomers; j++) {

				expr = new GRBLinExpr();

				for (int i = 0; i <= NumberCustomers; i++) {
					for (int k = 0; k < NumberVans; k++) {
						expr.addTerm(1.0, x[i][j][k]);
					}
				}

				for (int i = 1; i <= NumberCustomers; i++) {
					for (int k = 0; k < NumberVans; k++) {
						for (int d = 0; d < NumberRobots; d++) {
							expr.addTerm(1.0, y[i][j][k][d]);
						}
					}
				}

				model.addConstr(expr, GRB.EQUAL, 1.0, "c2_j=" + j);
			}

			/* +++ END RESTRICITION (2) +++ */

			/* +++ BEGIN RESTRICITION (3) +++ */

			for (int j = 1; j <= NumberCustomers; j++) {

				expr = new GRBLinExpr();

				for (int i = 1; i <= NumberCustomers; i++) {
					for (int k = 0; k < NumberVans; k++) {
						for (int d = 0; d < NumberRobots; d++) {
							expr.addTerm(1.0, y[i][j][k][d]);
						}
					}
				}

				model.addConstr(expr, GRB.LESS_EQUAL, currentInstance.getCustomers(j).getRobotAccessible(),
						"c3_j=" + j);

			}

			/* +++ END RESTRICITION (3) +++ */

			/* +++ BEGIN RESTRICITION (4) +++ */

			for (int i = 1; i <= NumberCustomers; i++) {
				for (int k = 0; k < NumberVans; k++) {

					expr = new GRBLinExpr();

					for (int j = 1; j <= NumberCustomers; j++) {
						for (int d = 0; d < NumberRobots; d++) {
							expr.addTerm(1.0, y[i][j][k][d]);
						}
					}

					expr2 = new GRBLinExpr();

					for (int j = 0; j <= NumberCustomers; j++) {
						expr2.addTerm(NumberRobots, x[j][i][k]); // FLAG eequivalent to |R|*Sum of x over all j in V?
					}

					model.addConstr(expr, GRB.LESS_EQUAL, expr2, "c4_i=" + i + "_k=" + k);

				}
			}

			/* +++ END RESTRICITION (4) +++ */

			/* +++ BEGIN RESTRICITION (5) +++ */

			for (int i = 1; i <= NumberCustomers; i++) {
				for (int d = 0; d < NumberRobots; d++) {

					expr = new GRBLinExpr();

					for (int j = 1; j <= NumberCustomers; j++) {
						for (int k = 0; k < NumberVans; k++) {
							expr.addTerm(1.0, y[i][j][k][d]);
						}
					}

					model.addConstr(expr, GRB.LESS_EQUAL, 1.0, "c5_i=" + i + "_d=" + d);

				}
			}

			/* +++ END RESTRICITION (5) +++ */

			/* +++ BEGIN RESTRICITION (6) +++ */

			for (int k = 0; k < NumberVans; k++) {

				expr = new GRBLinExpr();

				for (int j = 1; j <= NumberCustomers; j++) {
					expr.addTerm(1.0, x[0][j][k]);
				}

				model.addConstr(expr, GRB.LESS_EQUAL, 1.0, "c6_k=" + k);
			}

			/* +++ END RESTRICITION (6) +++ */

			/* +++ BEGIN RESTRICITION (7) +++ */

			for (int j = 0; j <= NumberCustomers; j++) {
				for (int k = 0; k < NumberVans; k++) {

					expr = new GRBLinExpr();

					for (int i = 0; i <= NumberCustomers; i++) {
						expr.addTerm(1.0, x[i][j][k]);
					}

					expr2 = new GRBLinExpr();

					for (int i = 0; i <= NumberCustomers; i++) {
						expr2.addTerm(1.0, x[j][i][k]);
					}

					model.addConstr(expr, GRB.EQUAL, expr2, "c7_j=" + j + "_k=" + k);
				}
			}

			/* +++ END RESTRICITION (7) +++ */

			/* +++ BEGIN RESTRICITION (8) +++ */

			for (int j = 1; j <= NumberCustomers; j++) {

				expr = new GRBLinExpr();

				for (int i = 0; i <= NumberCustomers; i++) {
					for (int k = 0; k < NumberVans; k++) {
						expr.addTerm(1.0, p[i][j][k]);
					}
				}

				for (int i = 0; i <= NumberCustomers; i++) {
					for (int k = 0; k < NumberVans; k++) {
						expr.addTerm((-1.0), p[j][i][k]);
					}
				}

				expr2 = new GRBLinExpr();

				for (int i = 0; i <= NumberCustomers; i++) {
					for (int k = 0; k < NumberVans; k++) {
						expr2.addTerm(currentInstance.getCustomers(j).getDemand(), x[i][j][k]); // FLAG qj * sum sum x ?
					}
				}

				for (int i = 1; i <= NumberCustomers; i++) {
					for (int k = 0; k < NumberVans; k++) {
						for (int d = 0; d < NumberRobots; d++) {
							expr2.addTerm(currentInstance.getCustomers(i).getDemand(), y[j][i][k][d]);
						}
					}
				}

				model.addConstr(expr, GRB.EQUAL, expr2, "c8_j=" + j);

			}

			/* +++ END RESTRICITION (8) +++ */

			/* +++ BEGIN RESTRICITION (9) +++ */

			GRBQuadExpr expr3 = new GRBQuadExpr();

			for (int i = 1; i <= NumberCustomers; i++) { // for all i in C
				for (int j = 0; j <= NumberCustomers; j++) { // for all j in V
					for (int k = 0; k < NumberVans; k++) { // for all k in K

						expr = new GRBLinExpr();
						expr.addTerm(1.0, p[i][j][k]);

						expr3 = new GRBQuadExpr();
						expr3.addTerm(VanPayload, x[i][j][k]); // Q*x
						expr3.addTerm(-currentInstance.getCustomers(i).getDemand(), x[i][j][k]); // qi*x

						for (int o = 1; o <= NumberCustomers; o++) {
							for (int d = 0; d < NumberRobots; d++) {
								expr3.addTerm(-currentInstance.getCustomers(o).getDemand(), y[i][o][k][d], x[i][j][k]);
							}

						}

						model.addQConstr(expr, GRB.LESS_EQUAL, expr3, "c9");

					}
				}
			}
			/* +++ END RESTRICITION (9) +++ */

			/* +++ BEGIN RESTRICITION (9) +++ */

//			for (int i = 1; i <= NumberCustomers; i++) {
//				for (int j = 0; j <= NumberCustomers; j++) { // for all j in V
//					for (int k = 0; k < NumberVans; k++) { // for all k in K
//
//						expr = new GRBLinExpr();
//						expr.addTerm(1.0, p[i][j][k]);
//
//						expr2 = new GRBLinExpr();
//						expr2.addConstant(VanPayload); // Q*x
//						expr2.addConstant(-currentInstance.getCustomers(i).getDemand()); // qi*x
//
//						for (int o = 1; o <= NumberCustomers; o++) {
//							for (int d = 0; d < NumberRobots; d++) {
//								expr2.addTerm(-currentInstance.getCustomers(o).getDemand(), y[i][o][k][d]);
//							}
//						}
//
//						expr2.addConstant(M);
//						expr2.addTerm(-M, x[i][j][k]);
//
//						model.addConstr(expr, GRB.LESS_EQUAL, expr2, "c9_i=" + i + "_j=" + j + "_k=" + k);
//
//					}
//				}
//			}

			/* +++ END RESTRICTION (9) +++ */

			/* +++ BEGIN RESTRICITION (10) +++ */

			for (int i = 1; i <= NumberCustomers; i++) {
				for (int j = 1; j <= NumberCustomers; j++) {

					expr = new GRBLinExpr();

					expr.addTerm(1.0, b[i]);
					expr.addTerm(-1.0, a[j]);
					expr.addTerm(1.0, w[i]);
					expr.addConstant(currentInstance.getTravelingTimeVehicle(i, j));

					expr2 = new GRBLinExpr();

					expr2.addConstant(M);
					for (int k = 0; k < NumberVans; k++) {
						expr2.addTerm(-M, x[i][j][k]);
					}

					model.addConstr(expr, GRB.LESS_EQUAL, expr2, "c10_i=" + i + "_j=" + j);
				}
			}

			/* +++ END RESTRICITION (10) +++ */

			/* +++ BEGIN RESTRICITION (11) +++ */

			for (int i = 1; i <= NumberCustomers; i++) {
				for (int j = 1; j <= NumberCustomers; j++) {

					expr = new GRBLinExpr();

					expr.addTerm(-1.0, b[i]);
					expr.addTerm(1.0, a[j]);
					expr.addTerm(-1.0, w[i]);
					expr.addConstant(-currentInstance.getTravelingTimeVehicle(i, j));

					expr2 = new GRBLinExpr();

					expr2.addConstant(M);
					for (int k = 0; k < NumberVans; k++) {
						expr2.addTerm(-M, x[i][j][k]);
					}

					model.addConstr(expr, GRB.LESS_EQUAL, expr2, "c11_i=" + i + "_j=" + j);
				}
			}

			/* +++ END RESTRICITION (11) +++ */

			/* +++ BEGIN RESTRICITION (12) +++ */

			for (int i = 1; i <= NumberCustomers; i++) {
				for (int j = 1; j <= NumberCustomers; j++) {

					expr = new GRBLinExpr();

					expr.addTerm(1.0, a[i]);
					expr.addTerm(-1.0, a[j]);
					expr.addConstant(currentInstance.getTravelingTimeRobot(i, j));

					expr2 = new GRBLinExpr();

					expr2.addConstant(M);
					for (int k = 0; k < NumberVans; k++) {
						for (int d = 0; d < NumberRobots; d++) {
							expr2.addTerm(-M, y[i][j][k][d]);
						}
					}

					model.addConstr(expr, GRB.LESS_EQUAL, expr2, "c12_i=" + i + "_j=" + j);
				}
			}

			/* +++ END RESTRICITION (12) +++ */

			/* +++ BEGIN RESTRICITION (13) +++ */

			for (int i = 1; i <= NumberCustomers; i++) {

				expr = new GRBLinExpr();

				expr.addTerm(1.0, w[i]);

				expr2 = new GRBLinExpr();

				for (int j = 0; j <= NumberCustomers; j++) {
					for (int k = 0; k < NumberVans; k++) {
						expr2.addTerm(currentInstance.getCustomers(i).getServiceTimeVehicle(), x[j][i][k]);
					}
				}

				model.addConstr(expr, GRB.GREATER_EQUAL, expr2, "c13_i=" + i);
			}

			/* +++ END RESTRICITION (13) +++ */

			/* +++ BEGIN RESTRICITION (14) +++ */

			for (int i = 1; i <= NumberCustomers; i++) {
				for (int j = 1; j <= NumberCustomers; j++) {

					expr = new GRBLinExpr();

					expr.addTerm(1.0, b[j]);
					expr.addTerm(-1.0, b[i]);
					expr.addConstant(currentInstance.getTravelingTimeRobot(j, i));
					expr.addConstant(currentInstance.getCustomers(j).getServiceTimeRobot());
					expr.addTerm(-1.0, w[i]);

					expr2 = new GRBLinExpr();

					expr2.addConstant(M);
					for (int k = 0; k < NumberVans; k++) {
						for (int d = 0; d < NumberRobots; d++) {
							expr2.addTerm(-M, y[i][j][k][d]);
						}
					}

					model.addConstr(expr, GRB.LESS_EQUAL, expr2, "c14_i=" + i + "_j=" + j);
				}
			}

			/* +++ END RESTRICITION (14) +++ */

			/* +++ BEGIN RESTRICITION (15) +++ */

			for (int i = 1; i <= NumberCustomers; i++) {

				model.addConstr(a[i], GRB.LESS_EQUAL, b[i], "c15_i=" + i);

			}

			/* +++ END RESTRICITION (15) +++ */

			/* +++ BEGIN RESTRICITION (16) +++ */

			for (int i = 1; i <= NumberCustomers; i++) {

				model.addConstr(b[i], GRB.LESS_EQUAL, currentInstance.getCustomers(i).getDueTime(), "c16_1_i=" + i);

			}

			for (int i = 1; i <= NumberCustomers; i++) {

				model.addConstr(b[i], GRB.GREATER_EQUAL, currentInstance.getCustomers(i).getReadyTime(),
						"c16_2_i=" + i);

			}

			/* +++ END RESTRICITION (16) +++ */

			/* +++ BEGIN RESTRICITION (17) +++ */

			for (int k = 0; k < NumberVans; k++) {
				for (int d = 0; d < NumberRobots; d++) {

					expr = new GRBLinExpr();

					for (int i = 1; i <= NumberCustomers; i++) { // for all i in C
						for (int j = 1; j <= NumberCustomers; j++) { // for all j in C
							expr.addTerm(currentInstance.getDistanceCustomers(i, j), y[i][j][k][d]);
						}
					}

					model.addConstr(expr, GRB.LESS_EQUAL, RobotMaxRadius, "c17_k=" + k + "_d=" + d);

				}
			}

			/* +++ END RESTRICITION (17) +++ */

			/* +++ BEGIN RESTRICITION (TRAVELINGTIME) +++ */

			if (currentInstance.getDatasetName() == "Solomon" || currentInstance.getDatasetName() == "Homberger") {

				for (int k = 0; k < NumberVans; k++) {

					expr = new GRBLinExpr();

					for (int i = 0; i <= NumberCustomers; i++) {
						for (int j = 0; j <= NumberCustomers; j++) {
							expr.addTerm(currentInstance.getTravelingTimeVehicle(i, j), x[i][j][k]);
						}
					}

					model.addConstr(expr, GRB.LESS_EQUAL, currentInstance.getVanMaxTravelTime(), "cEXTRA_k=" + k);

				}
			}

			/* +++ END RESTRICITION (TRAVELINGTIME) +++ */

			/* +++ BEGIN RESTRICITION (20,21,22,23) +++ */

			for (int i = 0; i <= NumberCustomers; i++) {
				if (i != 0) {
					model.addConstr(a[i], GRB.GREATER_EQUAL, 0.0, "c21_a[" + i + "]");
				}
			}
			for (int i = 0; i <= NumberCustomers; i++) {
				if (i != 0) {
					model.addConstr(b[i], GRB.GREATER_EQUAL, 0.0, "c22_b[" + i + "]");
				}
			}
			for (int i = 0; i <= NumberCustomers; i++) {
				if (i != 0) {
					model.addConstr(w[i], GRB.GREATER_EQUAL, 0.0, "c23_w[" + i + "]");
				}
			}
			for (int i = 0; i <= NumberCustomers; i++) {
				if (i != 0) {
					for (int j = 0; j <= NumberCustomers; j++) {
						for (int k = 0; k < NumberVans; k++) {
							model.addConstr(p[i][j][k], GRB.GREATER_EQUAL, 0.0,
									"c20_p[" + i + "][" + j + "][" + k + "]");
						}
					}
				}
			}

			/* +++ END RESTRICITION (20,21,22,23) +++ */

			// Optimize model
			long nanoTime = System.currentTimeMillis();

			model.optimize();

			long nanoTime2 = System.currentTimeMillis();
			nanoTime2 = nanoTime2 - nanoTime;

			/* +++ BEGIN: OUTPUT GUROBI SOLUTION TO FILE +++ */
			
//			System.out.println("reached1");
//
			Solution solution = new Solution(instance);
			solution.setTimeForImprovedSolution(nanoTime2);
			solution.setFeasible();
//			
//			System.out.println("Time: " + nanoTime2);
//			System.out.println("Obj: " + model.get(GRB.DoubleAttr.ObjVal));
//
//			Route[] routes = new Route[NumberVans];
//
//			for (int k = 0; k < NumberVans; k++) {
//				routes[k] = new Route(k, instance);
//			}
//
//			for (int i = 0; i <= NumberCustomers; i++) {
//				if (i != 0) {
//					if ((a[i].get(GRB.DoubleAttr.X) != 0) && (b[i].get(GRB.DoubleAttr.X) != 0)) {
//						// System.out.println("" + i + " a: " + a[i].get(GRB.DoubleAttr.X) + " b: " +
//						// b[i].get(GRB.DoubleAttr.X) + " w: " + w[i].get(GRB.DoubleAttr.X));
//					}
//				}
//			}
//			
//			System.out.println("reached1");
//
//			/*
//			 * this next part is completely dedicated to getting the routes of the solution.
//			 * as far as im aware this is the best way to get the decision variables in the
//			 * correct order and without having duplicates
//			 */
//
//			class dmmy {
//				int i;
//				int j;
//				int k;
//
//				public dmmy(int i, int j, int k) {
//					this.i = i;
//					this.j = j;
//					this.k = k;
//				}
//			}
//			
//			System.out.println("reached1");
//
//			LinkedList<dmmy> ll = new LinkedList<dmmy>();
//
//			for (int k = 0; k < NumberVans; k++) {
//				for (int i = 0; i <= NumberCustomers; i++) {
//					for (int j = 0; j <= NumberCustomers; j++) {
//						if (x[i][j][k].get(GRB.DoubleAttr.X) == 1) {
//							ll.add(new dmmy(i, j, k));
//						}
//					}
//				}
//			}
//
//			while (ll.size() > 0) {
//
//				int lastCustomer = 0;
//				for (int k = 0; k < NumberVans; k++) {
//					if (ll.size() > 0) {
//						boolean t = true;
//						while (t) {
//							dmmy dy = ll.remove(0);
//
//							if (dy.i != lastCustomer || dy.k != k) {
//								ll.addLast(dy);
//							} else if (dy.i == lastCustomer && dy.k == k) {
//								if (dy.j != 0) {
//									routes[k].addNodeToRoute(instance.getCustomers(dy.j), true);
//								}
//								lastCustomer = dy.j;
//							}
//
//							int count = 0;
//							for (dmmy d : ll) {
//								if (d.k == k) {
//									count++;
//								}
//							}
//							if (count == 0) {
//								t = false;
//							}
//						}
//					}
//				}
//			}
//			
//			System.out.println("reached1");
//
//			for (int k = 0; k < NumberVans; k++) {
//				for (int i = 0; i <= NumberCustomers; i++) {
//					for (int j = 0; j <= NumberCustomers; j++) {
//						for (int d = 0; d < NumberRobots; d++) {
//							if (i != 0 && j != 0) {
//								if (y[i][j][k][d].get(GRB.DoubleAttr.X) == 1) {
//									routes[k].addNodeAsRobotVisit(currentInstance.getCustomers(i),
//											currentInstance.getCustomers(j), true);
//								}
//							}
//						}
//					}
//				}
//				solution.addVan(routes[k]);
//			}
//			
//			System.out.println("reached1");
//
//			for (Route k : solution.getRoutes()) {
//				k.updateCost();
//			}
//			
//			System.out.println("reached1");
//
//			/*
//			 * the objValue in the printed file will almost certainly be different from the
//			 * sum of partial costs of the routes. i choose to artifially set the objValue
//			 * of the Solution object to see if thers a difference and to potentially spot
//			 * inaccuracy in the way i calculate the cost in the route object
//			 */
//
			
			String checkSolVal = "" + model.get(GRB.DoubleAttr.ObjVal);
			
			if(checkSolVal == "1.0E100") {
				solution.setCost(9999.99);
			} else {
				solution.setCost(model.get(GRB.DoubleAttr.ObjVal));
			}
			
//			System.out.println("modelObjValue: " + model.get(GRB.DoubleAttr.ObjVal));
			
			/* +++ END: OUTPUT GUROBI SOLUTION TO FILE +++ */

			// model.computeIIS();
			// model.write("model.ilp");
			// model.write("model.mps");

			// Dispose of model and environment
			model.dispose();
			env.dispose();

			return solution;

		} catch (GRBException e) {
			System.out.println("Error code: " + e.getErrorCode() + ". " + e.getMessage());
			return null;
		}
	}
}