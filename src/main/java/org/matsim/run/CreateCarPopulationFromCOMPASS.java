package org.matsim.run;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Random;

import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.population.Activity;
import org.matsim.api.core.v01.population.Leg;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.Plan;
import org.matsim.api.core.v01.population.PopulationFactory;
import org.matsim.api.core.v01.population.PopulationWriter;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.scenario.ScenarioUtils;


public class CreateCarPopulationFromCOMPASS {

	private final static String ZONE_TYPE = "Micro";
	private final static String COORDS_FILE = "c:/workAtHome/Daysim_2019/Coords_" + ZONE_TYPE + ".csv";
	private final static String DEMAND_FILE = "c:/workAtHome/Daysim_2019/DaySimTrips_" + ZONE_TYPE + "_full.csv";  
	private final static String POPULATION_FILE = "c:/workAtHome/Daysim_2019/CarPopulation2019_" + ZONE_TYPE + "_full.xml.gz";
	
	private final static long RANDOM_SEED = 60190;
	

	
	public static void main(String[] args) {
		Config config = ConfigUtils.createConfig();
		Scenario scenario = ScenarioUtils.createScenario(config);
			
		HashMap<Integer,LinkedList<Coord>> coords = new HashMap<Integer, LinkedList<Coord>>();
		
		try {
			BufferedReader br = new BufferedReader(new FileReader(COORDS_FILE));
			br.readLine();
			String readLine;
			while((readLine = br.readLine()) != null) {
				String[] rowArray = readLine.split(";");
				int zoneId = Integer.parseInt(rowArray[0]);
				if(!coords.containsKey(zoneId)) {
					coords.put(zoneId, new LinkedList<Coord>());
				}
				double x = Double.parseDouble(rowArray[1]);
				double y = Double.parseDouble(rowArray[2]);
				Coord coord = new Coord(x, y);
				coords.get(zoneId).addLast(coord);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
	
		PopulationFactory factory = scenario.getPopulation().getFactory();
		Random random = new Random(RANDOM_SEED);
		try {
			int idNumber = 1;
			BufferedReader br = new BufferedReader(new FileReader(DEMAND_FILE));
			br.readLine();
			String readLine;
			while((readLine = br.readLine()) != null) {
				String[] rowArray = readLine.split(";");
				int fromZoneId = Integer.parseInt(rowArray[0]);
				int toZoneId = Integer.parseInt(rowArray[1]);
				int depTmRaw = Integer.parseInt(rowArray[3]);
				double depTm = depTmRaw * 60. + random.nextDouble()*60.;
				
				Person person = factory.createPerson(Id.create(idNumber + "_Person", Person.class));
				Plan plan = factory.createPlan();
			
				Coord coord1 = coords.get(fromZoneId).pollFirst();
				Activity act1 = factory.createActivityFromCoord("missing", coord1);
				act1.setEndTime(depTm);

				Leg leg = factory.createLeg(TransportMode.car);
				
				Coord coord2 = coords.get(toZoneId).pollFirst();
				Activity act2 = factory.createActivityFromCoord("missing", coord2);
				act2.setEndTime(Double.POSITIVE_INFINITY);
				
				plan.addActivity(act1);
				plan.addLeg(leg);
				plan.addActivity(act2);
			
				plan.setPerson(person);
				person.addPlan(plan);
				person.setSelectedPlan(plan);
				
				scenario.getPopulation().addPerson(person);
				
				idNumber++;
			}
			
			PopulationWriter writer = new PopulationWriter(scenario.getPopulation());
			writer.write(POPULATION_FILE);
			System.out.println("Car population created :-)");
		} catch (IOException e) {
			e.printStackTrace();
		}		
	}

}
