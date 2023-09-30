package parser;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import java.awt.Color;
import java.awt.Font;

import components.*;

public class MyGraphicsTool {

	Solution currSol;
	String instanceName;
	double instanceCost;

	public MyGraphicsTool(Solution currSol, String name) {

		this.currSol = currSol;
		this.instanceName = name;
		this.instanceCost = currSol.getCost();

		String filePath = System.getProperty("user.dir") + "\\" + name + ".png";

		try {
			BufferedImage bi = createSolutionImage(); // retrieve image
			File outputfile = new File(filePath);
			ImageIO.write(bi, "png", outputfile);
		} catch (IOException e) {
			System.out.print(e);
		}

	}

	private BufferedImage createSolutionImage() {

		int sizeWidth = 1400;
		int sizeHeight = 1400;

		BufferedImage img = new BufferedImage(sizeWidth, sizeHeight, BufferedImage.TYPE_INT_RGB);
		Graphics g = img.getGraphics();

		Graphics2D g2d = (Graphics2D) g;

		int nodeSize = 24;
		int fontSize = 18;

		g.setFont(new Font("TimesNewRoman", Font.PLAIN, fontSize));

		g.setColor(Color.WHITE);
		g.fillRect(0, 0, sizeWidth, sizeHeight);

		g.setColor(Color.BLACK);
		g.drawString("Instance: " + instanceName + " Solution Cost: " + instanceCost, nodeSize, nodeSize);

		// count all nonempty routes
		int countNonEmptyRoutes = 0;

		for (Route r : currSol.getRoutes()) {
			if (r.getLastRouteNodeOfRoute().getId() != 0) {
				countNonEmptyRoutes++;
			}
		}

		// calculate centerpoint depot
		int cx = sizeWidth / 2;
		int cy = sizeHeight / 2;

		// set the distance of the routes to the centerpoint
		int cDistance = 320;

		// set the distance of the routenodes to the centerpoint of the route
		int rnDistance = 168;

		// calculate the angle of each route
		double angleRoutes = 2 * Math.PI / countNonEmptyRoutes;

		// draw the vehicles
		int routecount = 0;
		for (Route r : currSol.getRoutes()) {
			g.setFont(new Font("TimesNewRoman", Font.PLAIN, fontSize));
			if (r.getLastRouteNodeOfRoute().getId() != 0) {

				Double xpos = Math.cos(angleRoutes * routecount) * cDistance;
				Double ypos = Math.sin(angleRoutes * routecount) * cDistance;

				int x = cx + xpos.intValue();
				int y = cy + ypos.intValue();

				g.setColor(Color.BLUE);
				g.drawRect(x, y, nodeSize, nodeSize);
				g.setColor(Color.BLACK);
				g.drawString("" + r.getId(), x, y + nodeSize);

				// arrange all routenodes around that centerpoint equidistant
				int countRouteNodes = 0;
				for (RouteNode rn : r.getRoute()) {
					if (rn.getId() != 0) {
						countRouteNodes++;
					}
				}

				double angleRoutenodes = ((2 * Math.PI) / countRouteNodes);

				countRouteNodes = 0;
				int lastRnId = 1;
				for (RouteNode rn : r.getRoute()) {

					if (rn.getId() != 0) {
						g.setFont(new Font("TimesNewRoman", Font.PLAIN, fontSize));

//						Double xpos2 = (Math
//								.cos((angleRoutenodes) * countRouteNodes + Math.PI + angleRoutes * routecount))
//								* rnDistance;
//						Double ypos2 = (Math
//								.sin((angleRoutenodes) * countRouteNodes + Math.PI + angleRoutes * routecount))
//								* rnDistance;

						Double xpos2 = (Math.cos((angleRoutenodes) * countRouteNodes)) * rnDistance;
						Double ypos2 = (Math.sin((angleRoutenodes) * countRouteNodes)) * rnDistance;

						int rx = x + xpos2.intValue();
						int ry = y + ypos2.intValue();

						if (lastRnId == 0) {
							g.setColor(Color.BLACK);
							g.drawLine(rx, ry, cx, cy);
						}

						g.setColor(Color.RED);
						g.fillRect(rx, ry, nodeSize, nodeSize);
						g.setColor(Color.BLACK);
						
						String ndtxt = "" + rn.getId();
						if (lastRnId == 0) {
							ndtxt = "^" + rn.getId() + "^";
						}
						g.drawString(ndtxt, rx, ry + nodeSize);

						if (rn.getId() != 0 && r.getRobotVisitsForRouteNode(rn).size() != 0) {

							int rvCount = 0;

							Double xpos3 = Math.cos(angleRoutenodes * countRouteNodes) * 0.5 * rnDistance;
							Double ypos3 = Math.sin(angleRoutenodes * countRouteNodes) * 0.5 * rnDistance;

							int rvx = rx + xpos3.intValue();
							int rvy = ry + ypos3.intValue();

							for (Node rv : r.getRobotVisitsForRouteNode(rn)) {
								g.setFont(new Font("TimesNewRoman", Font.PLAIN, 12));
								if (rvCount == 0) {
									g.setColor(Color.BLACK);
									g.drawLine(rx, ry, rvx, rvy);
								}
								g.setColor(Color.MAGENTA);
								g.drawRect(rvx + nodeSize * rvCount, rvy, nodeSize, nodeSize);
								g.setColor(Color.BLACK);
								g.drawString("" + rv.getId(), rvx + nodeSize * rvCount, rvy + nodeSize);
								rvCount++;
							}

						}

						countRouteNodes++;
					}
					lastRnId = rn.getId();

				}

				routecount++;
			}
		}

		g.setColor(Color.GREEN);
		g.fillRect(cx, cy, nodeSize, nodeSize);
		g.setColor(Color.BLACK);
		g.drawString("0", cx + nodeSize, cy + nodeSize);

		return img;
	}

}
