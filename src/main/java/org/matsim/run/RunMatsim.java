/* *********************************************************************** *
 * project: org.matsim.*												   *
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2008 by the members listed in the COPYING,        *
 *                   LICENSE and WARRANTY file.                            *
 * email           : info at matsim dot org                                *
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *   See also COPYING, LICENSE and WARRANTY file                           *
 *                                                                         *
 * *********************************************************************** */
package org.matsim.run;

import java.util.LinkedList;

import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.network.NetworkWriter;
import org.matsim.api.core.v01.network.Node;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.controler.Controler;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.network.io.MatsimNetworkReader;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.core.utils.geometry.CoordinateTransformation;
import org.matsim.core.utils.geometry.transformations.TransformationFactory;
import org.matsim.core.utils.io.FFFOsmNetworkReader;
import org.matsim.utils.gis.matsim2esri.network.FeatureGeneratorBuilderImpl;
import org.matsim.utils.gis.matsim2esri.network.LineStringBasedFeatureGenerator;
import org.matsim.utils.gis.matsim2esri.network.Links2ESRIShape;

/**
 * @author nagel
 *
 */
public class RunMatsim {

	private static final String osm = "./original-input-data/EasternDenmark.osm.gz";
	private static final String COORDINATE_SYSTEM = "EPSG:32632";


	public static void main(String[] args) {
		
/*
		Config config = ConfigUtils.createConfig();
		Scenario scenario = ScenarioUtils.createScenario(config);
		MatsimNetworkReader networkReader = new MatsimNetworkReader(scenario.getNetwork());
		networkReader.readFile("./output-data/MATSimCopenhagenNetwork.xml.gz");
		
		FeatureGeneratorBuilderImpl featureGeneratorBuilder = new FeatureGeneratorBuilderImpl(scenario.getNetwork(), "WGS84_UTM33N");
		featureGeneratorBuilder.setFeatureGeneratorPrototype(LineStringBasedFeatureGenerator.class);
		Links2ESRIShape linksToShape = new Links2ESRIShape(scenario.getNetwork(),"./output-data/OSMNetwork.shp", featureGeneratorBuilder);
		
		
		linksToShape.write();
		*/
		run(ConfigUtils.createConfig());
		// makes some sense to not modify the config here but in the run method to help  with regression testing.
	}

	static void run(Config config) {

		
		String urlForOSMExtract = "https://overpass-api.de/api/map?bbox=10.838,54.555,12.690,56.140";  //Should be created on-the-go in the future.
		//Don't know if this can be used directly from Java. 
		
		// possibly modify config here

		// ---

		Scenario scenario = ScenarioUtils.loadScenario(config) ;

		writeCopenhagenFromOSM(scenario.getNetwork());

		// possibly modify scenario here

		// ---

		Controler controler = new Controler( scenario ) ;
 
		// possibly modify controler here

		// ---

	//	controler.run();
	}


	public static void writeCopenhagenFromOSM(Network net){

		CoordinateTransformation ct = TransformationFactory.getCoordinateTransformation(
				TransformationFactory.WGS84,COORDINATE_SYSTEM);
		FFFOsmNetworkReader onr = new FFFOsmNetworkReader(net,ct,true);
		onr.setMemoryOptimization(true);
		onr.parse(osm); 
		onr.printType();
		
		
		NetworkCleaner nc = new NetworkCleaner();
		
		String fileBoth = "./output-data/MATSimCopenhagenNetwork_WithBicycleInfrastructure.xml.gz";
		new NetworkWriter(net).write(fileBoth);
		clearNetwork(net);
		nc.run(fileBoth, fileBoth);
		MatsimNetworkReader nrBoth = new MatsimNetworkReader(net);
		nrBoth.readFile(fileBoth);
		
		Network networkBike = extractModeSpecificNetwork(net, TransportMode.bike);
		String fileBike  = "./output-data/MATSimCopenhagenNetwork_BicyclesOnly.xml.gz";
		new NetworkWriter(networkBike).write(fileBike);
		clearNetwork(networkBike);
		nc.run(fileBike,fileBike);
		MatsimNetworkReader nrBike = new MatsimNetworkReader(networkBike);
		nrBike.readFile(fileBike);
				
		clearNetwork(net);
		nrBoth.readFile(fileBoth);
		
		Network networkCar = extractModeSpecificNetwork(net, TransportMode.car);
		String fileCar = "./output-data/MATSimCopenhagenNetwork_CarsOnly.xml.gz";
		new NetworkWriter(networkCar).write(fileCar);
		clearNetwork(networkCar);
		nc.run(fileCar, fileCar);
		MatsimNetworkReader nrCar = new MatsimNetworkReader(networkCar);
		nrCar.readFile(fileCar);
		
		
		clearNetwork(net);
		nrBoth.readFile(fileBoth);
		
		

		
		
		
			
		
		FeatureGeneratorBuilderImpl featureGeneratorBuilderBoth = new FeatureGeneratorBuilderImpl(net, COORDINATE_SYSTEM);
		featureGeneratorBuilderBoth.setFeatureGeneratorPrototype(LineStringBasedFeatureGenerator.class);
		Links2ESRIShape linksToShapeBoth = new Links2ESRIShape(net,"./output-data/OSMNetworkCombined.shp", featureGeneratorBuilderBoth);
		linksToShapeBoth.write();
		
		FeatureGeneratorBuilderImpl featureGeneratorBuilderBike = new FeatureGeneratorBuilderImpl(networkBike, COORDINATE_SYSTEM);
		featureGeneratorBuilderBike.setFeatureGeneratorPrototype(LineStringBasedFeatureGenerator.class);
		Links2ESRIShape linksToShapeBike = new Links2ESRIShape(networkBike,"./output-data/OSMNetwork_BicyclesOnly.shp", featureGeneratorBuilderBike);
		linksToShapeBike.write();
	
		FeatureGeneratorBuilderImpl featureGeneratorBuilderCar = new FeatureGeneratorBuilderImpl(networkCar, COORDINATE_SYSTEM);
		featureGeneratorBuilderCar.setFeatureGeneratorPrototype(LineStringBasedFeatureGenerator.class);
		Links2ESRIShape linksToShapeCar = new Links2ESRIShape(networkCar,"./output-data/OSMNetwork_CarsOnly.shp", featureGeneratorBuilderCar);
		linksToShapeCar.write();
		
	
		
		//Creating a bicycle network with only 1 lane.
		for(Link link : networkBike.getLinks().values()){
			link.setNumberOfLanes(1);
		}
		new NetworkWriter(networkBike).write("./output-data/MATSimCopenhagenNetwork_BicyclesOnly_1Lane.xml.gz");
		
		
		///Cleaning!
		
		//If two links between the same two nodes allow bicycles, keep only one (with maximum number of lanes).
		
		//For both modes:
			// If a node only has one inlink and one outlink (per mode), and the two links have the same number of lanes, then remove the node and merge the two links.

	}
	
	
	
	
	
	private static void clearNetwork(Network network){
		LinkedList<Link> linksToBeRemoved = new LinkedList<Link>();
		LinkedList<Node> nodesToBeRemoved = new LinkedList<Node>();
		for(Link link : network.getLinks().values()){
			linksToBeRemoved.add(link);
		}
		for(Node node : network.getNodes().values()){
			nodesToBeRemoved.add(node);
		}
		for(Link link : linksToBeRemoved){
			network.removeLink(link.getId());
		}
		for(Node node : nodesToBeRemoved){
			network.removeNode(node.getId());
		}
	}
	
	private static Network extractModeSpecificNetwork(Network network, String mode){
		Config newConfig = ConfigUtils.createConfig();
		Scenario newScenario = ScenarioUtils.createScenario(newConfig);
		Network newNetwork = newScenario.getNetwork();
		
		for(Node node : network.getNodes().values()){
			Node newNode = NetworkUtils.createNode(node.getId(), node.getCoord());
			newNetwork.addNode(newNode);
		}
		for(Link link : network.getLinks().values()){
			if(link.getAllowedModes().contains(mode)){
				newNetwork.addLink(link);
			} 
		}
		LinkedList<Node> nodesToBeRemoved = new LinkedList<Node>();
		for(Node node : network.getNodes().values()){
			if( node.getInLinks().isEmpty() && node.getOutLinks().isEmpty()){
				nodesToBeRemoved.add(node);
			}
		}
		for(Node node : nodesToBeRemoved){
			network.removeNode(node.getId());
		}
		return newNetwork;
	}
}

