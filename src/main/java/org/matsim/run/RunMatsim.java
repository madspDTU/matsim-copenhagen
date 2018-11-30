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

import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.network.NetworkWriter;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.controler.Controler;
import org.matsim.core.controler.OutputDirectoryHierarchy.OverwriteFileSetting;
import org.matsim.core.gbl.Gbl;
import org.matsim.core.network.algorithms.NetworkCleaner;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.core.utils.geometry.CoordinateTransformation;
import org.matsim.core.utils.geometry.transformations.TransformationFactory;
import org.matsim.core.utils.io.OsmNetworkReader;

/**
 * @author nagel
 *
 */
public class RunMatsim {

	private static final String osm = "./original-input-data/copenhagen.osm.gz";


	public static void main(String[] args) {
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
				TransformationFactory.WGS84, TransformationFactory.WGS84_UTM33N);
		OsmNetworkReader onr = new OsmNetworkReader(net,ct);
		onr.parse(osm); 
		new NetworkCleaner().run(net);
		new NetworkWriter(net).write("./output-data/MATSimCopenhagenNetwork.xml.gz");

	}

}
