package parser;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import components.Node;

public class SolomonHombergerXMLParser implements InterfaceVRPTWDRDataset {

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

            try (var inputStream = new FileInputStream(inputFile)) {
                Document doc = dBuilder.parse(inputStream);
                doc.getDocumentElement().normalize();

                NodeList nList = doc.getElementsByTagName("node");
                NodeList nList2 = doc.getElementsByTagName("request");

                Element vehicleProfileElement = (Element) doc.getElementsByTagName("vehicle_profile").item(0);
                int vanNumber = Integer.parseInt(vehicleProfileElement.getAttribute("number"));
                double vanPayload = Double.parseDouble(vehicleProfileElement.getElementsByTagName("capacity").item(0).getTextContent());
                double vanMaxTravelTime = Double.parseDouble(vehicleProfileElement.getElementsByTagName("max_travel_time").item(0).getTextContent());

                this.numberVans = vanNumber;
                this.vanPayload = vanPayload;
                this.vanMaxTravelTime = vanMaxTravelTime;

                this.numberCustomers = nList.getLength() - 1;
                customers = new Node[this.numberCustomers + 1];
                distanceCustomers = new double[this.numberCustomers + 1][this.numberCustomers + 1];
                vanTravelTime = new double[this.numberCustomers + 1][this.numberCustomers + 1];
                robotTravelTime = new double[this.numberCustomers + 1][this.numberCustomers + 1];

                double maxTravel = Double.MIN_VALUE;

                HashMap<Integer, Element> nodeElementsMap = new HashMap<>();
                for (int i = 0; i < nList.getLength(); i++) {
                    Element nodeElement = (Element) nList.item(i);
                    int nodeId = Integer.parseInt(nodeElement.getAttribute("id"));
                    nodeElementsMap.put(nodeId, nodeElement);
                }

                for (int i = 0; i < nList.getLength(); i++) {
                    Element nodeElement = (Element) nList.item(i);
                    int nodeId = Integer.parseInt(nodeElement.getAttribute("id"));
                    double cx1 = Double.parseDouble(nodeElement.getElementsByTagName("cx").item(0).getTextContent());
                    double cy1 = Double.parseDouble(nodeElement.getElementsByTagName("cy").item(0).getTextContent());

                    for (int j = 0; j < nList.getLength(); j++) {
                        Element otherNodeElement = (Element) nList.item(j);
                        int otherNodeId = Integer.parseInt(otherNodeElement.getAttribute("id"));
                        double cx2 = Double.parseDouble(otherNodeElement.getElementsByTagName("cx").item(0).getTextContent());
                        double cy2 = Double.parseDouble(otherNodeElement.getElementsByTagName("cy").item(0).getTextContent());

                        double x1 = Math.pow(cx1 - cx2, 2);
                        double y1 = Math.pow(cy1 - cy2, 2);

                        distanceCustomers[nodeId][otherNodeId] = Math.sqrt(x1 + y1);
                        vanTravelTime[nodeId][otherNodeId] = calculateTravelTime(distanceCustomers[nodeId][otherNodeId], this.vanSpeed);
                        robotTravelTime[nodeId][otherNodeId] = calculateTravelTime(distanceCustomers[nodeId][otherNodeId], this.robotSpeed);

                        if (distanceCustomers[nodeId][otherNodeId] > maxTravel) {
                            maxTravel = distanceCustomers[nodeId][otherNodeId];
                        }
                    }
                }

                this.maxTraveltimeBetweenCustomers = maxTravel;

                for (int i = 0; i < nList2.getLength(); i++) {
                    Element requestElement = (Element) nList2.item(i);
                    int id = Integer.parseInt(requestElement.getAttribute("id"));
                    int quantity = Integer.parseInt(requestElement.getElementsByTagName("quantity").item(0).getTextContent());
                    int start = Integer.parseInt(requestElement.getElementsByTagName("start").item(0).getTextContent());
                    int end = Integer.parseInt(requestElement.getElementsByTagName("end").item(0).getTextContent());
                    int serviceTime = Integer.parseInt(requestElement.getElementsByTagName("service_time").item(0).getTextContent());

                    customers[id] = new Node(id, "789XYZ", 0, quantity, start, end, serviceTime, 0);
                }

            } catch (IOException e) {
                e.printStackTrace();
            }

        } catch (ParserConfigurationException | SAXException e) {
            e.printStackTrace();
        }
    }

    private double calculateTravelTime(double distance, double speed) {
        return distance / speed;
    }

    @Override
    public int getNumberCustomers() { return this.numberCustomers; }

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
    public Node getCustomers(int i) { return this.customers[i]; }

    @Override
    public double getTravelingTimeVehicle(int i, int j) {
        return vanTravelTime[i][j];
    }

    @Override
    public double getTravelingTimeRobot(int i, int j) {
        return robotTravelTime[i][j];
    }

    @Override
    public double getRobotMaxRadius() { return this.robotMaxRadius; }

    @Override
    public double getHighestTravelTime() {
        return maxTraveltimeBetweenCustomers;
    }
}