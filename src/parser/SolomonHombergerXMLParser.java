package parser;

import java.io.File;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Element;

import components.Node;

public class SolomonHombergerXMLParser implements InterfaceVRPTWDRDataset {
	
	/*
	 * i didnt bother making it efficient but i want to say that this takes forever
	 * maybe theres a better way to implement this but i sticked to getelementsbytags 
	 * especially for the 1000 customer instances this takes a few minutes to compute
	 */

	private int numberCustomers;
	private int numberVans;
	private int numberRobots;
	private double vanSpeed;
	private double robotSpeed;
	private double vanPayload;
	private double robotMaxRadius;
	private double[][] distanceCustomers;
	private double[][] vanTravelTime;
	private double[][] robotTravelTime;
	private Node[] customers;

	private double maxTraveltimeBetweenCustomers;
	private double vanMaxTravelTime;

	public SolomonHombergerXMLParser(String file) {

		this.numberRobots = 0;
		this.robotSpeed = 1.0;
		this.vanSpeed = 1.0;

		try {

			File inputFile = new File(file);
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(inputFile);
			doc.getDocumentElement().normalize();

			NodeList nList = doc.getElementsByTagName("node");
			NodeList nList2 = doc.getElementsByTagName("request");

			org.w3c.dom.Node nList3 = doc.getElementsByTagName("vehicle_profile").item(0);
			for (int i = 0; i < 1; i++) {
				if (nList3.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE) {
					Element eElement = (Element) nList3;

					this.numberVans = Integer.parseInt(eElement.getAttribute("number"));
					this.vanPayload = Double
							.parseDouble(eElement.getElementsByTagName("capacity").item(0).getTextContent());
					this.vanMaxTravelTime = Double
							.parseDouble(eElement.getElementsByTagName("max_travel_time").item(0).getTextContent());

				}
			}

			this.numberCustomers = nList.getLength() - 1;

			customers = new Node[this.numberCustomers + 1];
			distanceCustomers = new double[this.numberCustomers + 1][this.numberCustomers + 1];
			vanTravelTime = new double[this.numberCustomers + 1][this.numberCustomers + 1];
			robotTravelTime = new double[this.numberCustomers + 1][this.numberCustomers + 1];

			double maxTravel = Integer.MIN_VALUE;

			for (int i = 0; i < nList.getLength(); i++) {

				org.w3c.dom.Node nNode = nList.item(i);

				for (int j = 0; j < nList.getLength(); j++) {

					org.w3c.dom.Node nNode3 = nList.item(j);

					if (nNode.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE
							&& nNode3.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE) {
						Element eElement = (Element) nNode;
						Element eElement1 = (Element) nNode3;

						double x1 = Double.parseDouble(eElement.getElementsByTagName("cx").item(0).getTextContent());
						double y1 = Double.parseDouble(eElement.getElementsByTagName("cy").item(0).getTextContent());

						double x2 = Double.parseDouble(eElement1.getElementsByTagName("cx").item(0).getTextContent());
						double y2 = Double.parseDouble(eElement1.getElementsByTagName("cy").item(0).getTextContent());

						x1 = Math.pow(x1 - x2, 2);
						y1 = Math.pow(y1 - y2, 2);

						distanceCustomers[i][j] = (Math.sqrt(x1 + y1));
						vanTravelTime[i][j] = (Math.sqrt(x1 + y1) / this.vanSpeed);
						robotTravelTime[i][j] = (Math.sqrt(x1 + y1) / this.robotSpeed);

						if (distanceCustomers[i][j] > maxTravel) {
							maxTravel = distanceCustomers[i][j];
						}
					}
				}
			}

			this.maxTraveltimeBetweenCustomers = maxTravel;

			for (int i = 0; i < nList.getLength(); i++) {

				if (i != 0) {
					org.w3c.dom.Node nNode2 = nList2.item(i - 1);
					if (nNode2.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE) {
						Element eElement = (Element) nNode2;

						// Node(int id, String postcode, int isRobotAccessible, int demand, int
						// readyTime, int dueTime, int serviceTimeVehicle, int serviceTimeRobot) {
						customers[i] = new Node(Integer.parseInt(eElement.getAttribute("id")), "789XYZ", 0,
								(int) Double.parseDouble(
										eElement.getElementsByTagName("quantity").item(0).getTextContent()),
								(int) Double
										.parseDouble(eElement.getElementsByTagName("start").item(0).getTextContent()),
								(int) Double.parseDouble(eElement.getElementsByTagName("end").item(0).getTextContent()),
								(int) Double.parseDouble(
										eElement.getElementsByTagName("service_time").item(0).getTextContent()),
								0);
					}

				} else if (i == 0) {
					customers[i] = new Node(0, "123ABC", 0, 0, 0, 10000, 0, 0);
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public int getNumberCustomers() {
		return this.numberCustomers;
	}

	public int getNumberVans() {
		return this.numberVans;
	}

	public int getNumberRobots() {
		return this.numberRobots;
	}

	@Override
	public double getVanMaxTravelTime() {
		return this.vanMaxTravelTime;
	}

	@Override
	public double getVanSpeed() {
		return this.vanSpeed;
	}

	@Override
	public double getVanPayload() {
		return this.vanPayload;
	}

	@Override
	public double getDistanceCustomers(int i, int j) {
		return this.distanceCustomers[i][j];
	}

	@Override
	public Node getCustomers(int i) {
		return this.customers[i];
	}

	@Override
	public double getTravelingTimeVehicle(int i, int j) {
		return vanTravelTime[i][j];
	}

	@Override
	public double getTravelingTimeRobot(int i, int j) {
		return robotTravelTime[i][j];
	}

	@Override
	public double getRobotMaxRadius() {
		return this.robotMaxRadius;
	}

	@Override
	public double getHighestTravelTime() {
		return maxTraveltimeBetweenCustomers;
	}
}