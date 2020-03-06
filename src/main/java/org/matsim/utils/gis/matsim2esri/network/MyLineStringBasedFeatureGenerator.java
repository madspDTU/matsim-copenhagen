package org.matsim.utils.gis.matsim2esri.network;

import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.matsim.api.core.v01.network.Link;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.utils.geometry.geotools.MGC;
import org.matsim.core.utils.io.FFFOsmNetworkReader;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

public class MyLineStringBasedFeatureGenerator implements FeatureGenerator{

	private final WidthCalculator widthCalculator;
	private SimpleFeatureBuilder builder;
	private final CoordinateReferenceSystem crs;
	private final GeometryFactory geofac;


	public MyLineStringBasedFeatureGenerator(final WidthCalculator widthCalculator, final CoordinateReferenceSystem crs) {
		this.widthCalculator = widthCalculator;
		this.crs = crs;
		this.geofac = new GeometryFactory();
		initFeatureType();
	}


	private void initFeatureType() {
		SimpleFeatureTypeBuilder typeBuilder = new SimpleFeatureTypeBuilder();
		typeBuilder.setName("link");
		typeBuilder.setCRS(this.crs);
		typeBuilder.add("the_geom", LineString.class);
		typeBuilder.add("ID", String.class);
		typeBuilder.add("fromID", String.class);
		typeBuilder.add("toID", String.class);
		typeBuilder.add("length", Double.class);
		typeBuilder.add("freespeed", Double.class);
		typeBuilder.add("capacity", Double.class);
		typeBuilder.add("lanes", Double.class);
		typeBuilder.add("visWidth", Double.class);
		typeBuilder.add("type", String.class);
		typeBuilder.add("surface", String.class);
		typeBuilder.add("name", String.class);
		typeBuilder.add("OSM_Id", Long.class);
		

		this.builder = new SimpleFeatureBuilder(typeBuilder.buildFeatureType());
	}


	@Override
	public SimpleFeature getFeature(final Link link) {
		double width = this.widthCalculator.getWidth(link);
		
		
		Coordinate[] internalNodes = FFFOsmNetworkReader.nodesMap.get(link.getId());
		LineString ls = this.geofac.createLineString(internalNodes);

		Object [] attribs = new Object[13];
		attribs[0] = ls;
		attribs[1] = link.getId().toString();
		attribs[2] = link.getFromNode().getId().toString();
		attribs[3] = link.getToNode().getId().toString();
		attribs[4] = link.getLength();
		attribs[5] = link.getFreespeed();
		attribs[6] = link.getCapacity();
		attribs[7] = link.getNumberOfLanes();
		attribs[8] = width;
		attribs[9] = link.getAttributes().getAttribute(FFFOsmNetworkReader.TAG_HIGHWAY);
		attribs[10] = link.getAttributes().getAttribute(FFFOsmNetworkReader.TAG_SURFACE);
		attribs[11] = link.getAttributes().getAttribute(FFFOsmNetworkReader.TAG_NAME);
		attribs[12] = link.getAttributes().getAttribute(FFFOsmNetworkReader.TAG_OSM_ID);
		
		try {
			return this.builder.buildFeature(null, attribs);
		} catch (IllegalArgumentException e) {
			throw new RuntimeException(e);
		}

	}

}
