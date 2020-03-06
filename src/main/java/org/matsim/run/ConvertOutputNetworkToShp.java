package org.matsim.run;

import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.network.Network;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.network.io.MatsimNetworkReader;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.utils.gis.matsim2esri.network.FeatureGeneratorBuilderImpl;
import org.matsim.utils.gis.matsim2esri.network.LineStringBasedFeatureGenerator;
import org.matsim.utils.gis.matsim2esri.network.Links2ESRIShape;

public class ConvertOutputNetworkToShp {

	private static final String COORDINATE_SYSTEM = "EPSG:32632";
	private static final String dir = "C:/workAtHome/Berlin/Data/BicycleCopenhagen/full";

	
	public static void main(String[] args) {
		
		Config config = ConfigUtils.createConfig();
		Scenario scenario = ScenarioUtils.createScenario(config);
		Network network = scenario.getNetwork();
		MatsimNetworkReader nr = new MatsimNetworkReader(network);
		nr.readFile(dir + "/output_network.xml.gz");
				
		FeatureGeneratorBuilderImpl featureGeneratorBuilder = new FeatureGeneratorBuilderImpl(network, COORDINATE_SYSTEM);
		featureGeneratorBuilder.setFeatureGeneratorPrototype(LineStringBasedFeatureGenerator.class);
		Links2ESRIShape linksToShape = new Links2ESRIShape(network, dir + "/NetworkUsedInMATSim.shp", featureGeneratorBuilder);
		linksToShape.write();

	}

}
