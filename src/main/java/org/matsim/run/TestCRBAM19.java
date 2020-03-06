package org.matsim.run;

import java.util.LinkedList;

import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.network.NetworkWriter;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.network.io.MatsimNetworkReader;
import org.matsim.core.scenario.ScenarioUtils;

public class TestCRBAM19 {

	public static void main(String[] args) {
		// TODO Auto-generated method stub

		Network network = ScenarioUtils.createScenario(ConfigUtils.createConfig()).getNetwork();
		MatsimNetworkReader reader = new MatsimNetworkReader(network);
		reader.readFile("C:/workAtHome/DTA2020/NetworkAfterRemovingShortOnes/MATSimCopenhagenNetwork_BicyclesOnly.xml.gz");
		
		LinkedList<Link> linksToRemove = new LinkedList<Link>();
		for(Link link : network.getLinks().values()) {
			if(link.getNumberOfLanes() == 1) {
				linksToRemove.add(link);
			}
		}
		for(Link link : linksToRemove) {
			network.removeLink(link.getId());
		}
		
		NetworkWriter writer = new NetworkWriter(network);
		writer.write("C:/workAtHome/DTA2020/NetworkAfterRemovingShortOnes/MATSimCopenhagenNetwork_BicyclesOnly.xml.gz");
			
	}

}
