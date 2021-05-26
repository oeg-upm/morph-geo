/*
 * ShpToRDF.java	version 1.0   19/03/2016
 *
 * Copyright (C) 2013 Institute for the Management of Information Systems, Athena RC, Greece.
 *
 * This library is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package plugin;



import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;

// RDF4J
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.impl.DynamicModelFactory;
import org.eclipse.rdf4j.model.impl.SimpleIRI;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleLiteral;
import org.eclipse.rdf4j.model.util.ModelBuilder;
import org.eclipse.rdf4j.model.util.Values;
import org.eclipse.rdf4j.model.vocabulary.XSD;
import org.eclipse.rdf4j.model.Namespace;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.RDFS;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.time.LocalDate;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.geotools.geometry.jts.JTS;
import org.opengis.referencing.operation.MathTransform;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;

/**
 * Class to convert shapefiles to RDF.
 *
 * @author jonbaraq
 * initially implemented for geometry2rdf utility (source: https://github.com/boricles/geometry2rdf/tree/master/Geometry2RDF)
 * Modified by: Kostas Patroumpas, 8/2/2013
 * Modified: 6/3/2013, added support for transformation from a given CRS to WGS84
 * Modified: 15/3/2013, added support for exporting custom geometries to (1) Virtuoso RDF and (2) according to WGS84 Geopositioning RDF vocabulary  
 * Modified: 12/6/2013, Kostas Patroumpas
 * Rename ShpConnector.java to ShpToRDF.java
 * Last modified by: Rosangelis Garcia, 19/03/2016
 */
public class ShpToRDF {

	private String attributeName;
	private String phrase;	
	private String feature;
	private String ontologyNS;
	private String ontologyNSPrefix;
	private String resourceNS;
	private String resourceNSPrefix;
	private String language;
	private Boolean flag_csv;
	private ClassesCSV[] classes;
	private CSV csv;		
	private SimpleValueFactory vfact;

	private boolean uuidsActive = false;
	private FieldDefinition[] fields;
	private ColumnDefinition[] columns;

	private Model model_rdf;

	private int posAttribute = -1;
	private int posGeometry = -1;

	private MathTransform transform = null;

	/**
	 * ShpToRDF
	 * @param smi - tripleGEOStepMeta
	 * @param classes 
	 * @param flag_csv 
	 * @param csv 
	 * @param attr
	 * @param phr
	 */	
	public ShpToRDF(tripleGEOStepMeta smi, Boolean flag_csv, ClassesCSV[] classes, CSV csv, String phr, String attr){		
		setAttributeName(attr.toUpperCase());
		setPhrase(phr);
		setFeature(smi.getFeature());
		setOntologyNS(smi.getOntologyNS());
		setOntologyNSPrefix(smi.getOntologyNSPrefix());
		setResourceNS(smi.getResourceNS());
		setResourceNSPrefix(smi.getResourceNSPrefix());
		setLanguage(smi.getLanguage());
		setFlag_csv(flag_csv);
		setClasses(classes);
		setCsv(csv);
		setUuidsActive(smi.isUuidsActive());
		setFields(smi.getFields());
		setColumns(smi.getColumns());
		vfact = SimpleValueFactory.getInstance();
	}
	
	

	/**
	 * Check the prefix and uri
	 * @param modelAux - Model
	 * @param fieldPrefix - Prefix
	 * @param fieldUri - Uri
	 */	
	public void checkPrefixUri(Model modelAux, String fieldPrefix, String fieldUri){
		Set<Namespace> namespaces = modelAux.getNamespaces();
		boolean uriHasPrefix = false;
		for (Namespace e: namespaces) {
			if(e.getName().equals(fieldUri) && e.getPrefix() != null)
				uriHasPrefix = true;
		}
		// if the prefix does not have a URI AND if the URI does not have a prefix, it is new, so set the prefix and URI
		if (modelAux.getNamespace(fieldPrefix) == null && !uriHasPrefix){ // Different				
			modelAux.setNamespace(fieldPrefix, fieldUri);
		// if the prefix has a URI AND the URI does not have a prefix, the URI has changed, so change the URI
		} else if (modelAux.getNamespace(fieldPrefix) != null && !uriHasPrefix){ // Prefix equal, URI different
			modelAux.setNamespace(fieldPrefix + "", fieldUri);
		}
	}


	/**
	 * Returns a RDF4J RDF model populated with the params from the configuration.
	 * @throws IOException 
	 */
	public void getModelFromConfiguration() throws IOException {	
		//Model modelAux = ModelFactory.createOntologyModel(OntModelSpec.RDFS_MEM);
		ModelBuilder builder = new ModelBuilder();

		Model modelAux = builder.setNamespace(this.ontologyNSPrefix, this.ontologyNS.replace(Constants.space,Constants.empty))
		.setNamespace(this.resourceNSPrefix, this.resourceNS.replace(Constants.space,Constants.empty))
		.setNamespace("geosparql", Constants.NS_GEO)
		.setNamespace("sf", Constants.NS_SF)
		.setNamespace("dc", Constants.NS_DC)
		.setNamespace("xsd", Constants.NS_XSD)	
		.setNamespace("rdf", Constants.NS_RDF)
		.setNamespace("foaf", Constants.NS_FOAF)
		.setNamespace("geo", Constants.NS_WGS84)
		.setNamespace("owl", Constants.NS_OWL)
		.setNamespace("rdfs", Constants.NS_RDFS).build();

		// Inserts the column's prefix
		if (this.columns != null) {
			for (ColumnDefinition col : this.columns) {
				if (col.getShow().equalsIgnoreCase("YES") 
						&& col.getUri() != null 
						&& col.getPrefix() != null 					
						&& !col.getColumn_shp().equalsIgnoreCase(this.attributeName)){	
					checkPrefixUri(modelAux,col.getPrefix(),
							col.getUri().replace(Constants.space,Constants.empty));
				}
				if (col.getColumn_shp().equalsIgnoreCase(this.attributeName) 
						&& col.getPrefix() != null && col.getUri() != null){
					checkPrefixUri(modelAux,col.getPrefix(),
							col.getUri().replace(Constants.space,Constants.empty));
				}
			}
		}

		// Inserts other prefixes
		if (this.fields != null) {
			for (FieldDefinition field : this.fields) {
				if (field.getPrefix() != null && field.getUri() != null){					
					checkPrefixUri(modelAux,field.getPrefix(),
							field.getUri().replace(Constants.space,Constants.empty));			
				}
			}
		}		

		setModel_rdf(modelAux);	

		// Helps the garbage collector
		this.fields = null;
		modelAux = null;


	}

	/**
	 * Writes the RDF model into a file
	 * @param row - Row
	 * @param outputRowMeta
	 * @throws UnsupportedEncodingException
	 */
	public void writeRdfModel(Object[] row, RowMetaInterface outputRowMeta) throws UnsupportedEncodingException {
		String featureAttribute = null;

		if (this.posAttribute > 0){
			if (row[this.posAttribute] != null) {
				featureAttribute = row[this.posAttribute].toString();
			}
		}

		String label = featureAttribute;
		featureAttribute = repeatedCharacters(removeSpecialCharacter(featureAttribute));
		String encodingResource = null;       

		if (this.uuidsActive){	
			// Generate UUIDs (Universally Unique Identifiers)
			encodingResource = UUID.nameUUIDFromBytes(featureAttribute.getBytes()).toString();
			if (encodingResource.substring(0, 1).matches("-?\\d+(\\.\\d+)?")){
				encodingResource = "" + encodingResource;
			}
		} else {
			//encodingResource = repeatedCharacters(URLEncoder.encode(featureAttribute.toLowerCase(), Constants.UTF_8)
			//		.replace(Constants.STRING_TO_REPLACE,Constants.SEPARATOR));		

			encodingResource = repeatedCharacters(URLEncoder.encode(featureAttribute, Constants.UTF_8)
					.replace(Constants.STRING_TO_REPLACE,Constants.empty));	

			if (featureAttribute.length() > 1){
				if (featureAttribute.substring(0, 1).matches("-?\\d+(\\.\\d+)?")){
					encodingResource = "" + encodingResource;
				}
			} else if (featureAttribute.length() == 1){
				if (featureAttribute.matches("-?\\d+(\\.\\d+)?")){
					encodingResource = "" + encodingResource;
				}
			}			
		}

		// Type according to GeoSPARQL feature		
		Object row_csv = null;
		Boolean fl = false;

		// Check if the CSV file exist
		if (getFlag_csv()){ 
			int pos_csv = 0;
			for (ValueMetaInterface vmeta : outputRowMeta.getValueMetaList()) {

				if (vmeta.getName().equalsIgnoreCase(this.csv.getAttribute())){
					fl = true;
					row_csv = row[pos_csv];
					break;
				}
				pos_csv++;
			}

			// Check if the attribute in the CSV file exist			
			boolean flag_classe = true;
			if (fl){			
				for (ClassesCSV classe : classes) {
					if (classe.getColumn().equalsIgnoreCase(row_csv.toString())){						
						String feature_csv = repeatedCharacters(removeSpecialCharacter(classe.getValue()));						
						//String feature = repeatedCharacters(URLEncoder.encode(feature_csv.toLowerCase(), Constants.UTF_8)
						//		.replace(Constants.STRING_TO_REPLACE,Constants.SEPARATOR));
						String feature = repeatedCharacters(URLEncoder.encode(feature_csv, Constants.UTF_8)
								.replace(Constants.STRING_TO_REPLACE,Constants.empty));

						insertResourceTypeResource(this.resourceNS.replace(Constants.space,Constants.empty) 
								+ encodingResource,
								this.ontologyNS.replace(Constants.space,Constants.empty) + feature );
						flag_classe = false;
						break;
					}
				}

				// If do not exist this class in the CSV file
				if (flag_classe) {
					String feature_ = repeatedCharacters(removeSpecialCharacter(this.feature));						
					//String feature = repeatedCharacters(URLEncoder.encode(feature_.toLowerCase(), Constants.UTF_8)
					//		.replace(Constants.STRING_TO_REPLACE,Constants.SEPARATOR));	
					String feature = repeatedCharacters(URLEncoder.encode(feature_, Constants.UTF_8)
							.replace(Constants.STRING_TO_REPLACE,Constants.empty));	
					insertResourceTypeResource(this.resourceNS.replace(Constants.space,Constants.empty)
							+ encodingResource,
							this.ontologyNS.replace(Constants.space,Constants.empty) + feature );					
				}

			} else {
				String feature_ = repeatedCharacters(removeSpecialCharacter(this.feature));						
				//String feature = repeatedCharacters(URLEncoder.encode(feature_.toLowerCase(), Constants.UTF_8)
				//		.replace(Constants.STRING_TO_REPLACE,Constants.SEPARATOR));	
				String feature = repeatedCharacters(URLEncoder.encode(feature_, Constants.UTF_8)
						.replace(Constants.STRING_TO_REPLACE,Constants.empty));	
				insertResourceTypeResource(this.resourceNS.replace(Constants.space,Constants.empty)
						+ encodingResource,this.ontologyNS.replace(Constants.space,Constants.empty) + feature );
			} 			
		} else {
			String feature_ = repeatedCharacters(removeSpecialCharacter(this.feature));						
			//String feature = repeatedCharacters(URLEncoder.encode(feature_.toLowerCase(), Constants.UTF_8)
			//		.replace(Constants.STRING_TO_REPLACE,Constants.SEPARATOR));	
			String feature = repeatedCharacters(URLEncoder.encode(feature_, Constants.UTF_8)
					.replace(Constants.STRING_TO_REPLACE,Constants.empty));	
			insertResourceTypeResource(this.resourceNS.replace(Constants.space,Constants.empty)
					+ encodingResource,this.ontologyNS.replace(Constants.space,Constants.empty) + feature );
		}	

		// Label with special characters
		insertLabelResource(this.resourceNS.replace(Constants.space,Constants.empty)
				+ encodingResource, label, this.language);

		// Columns of the shapefile
		int pos = 0;
		if (this.columns == null) {
			for (ValueMetaInterface vmeta : outputRowMeta.getValueMetaList()) {   			
				if (!vmeta.getName().equalsIgnoreCase(Constants.the_geom) 
						&& !vmeta.getName().equalsIgnoreCase(this.attributeName) 
						&& row[pos] != null){					
					if (!row[pos].toString().matches(Constants.empty) && !row[pos].toString().matches("0"))
						addColumns(encodingResource,vmeta.getName(),row[pos],
								this.resourceNS.replace(Constants.space,Constants.empty), outputRowMeta.getValueMeta(pos));
				}		
				pos++;
			}
		} else {
			for (ColumnDefinition col : this.columns) {
				if (col.getColumn_shp().equalsIgnoreCase(this.attributeName)){
					if (col.getPrefix() != null && col.getUri() != null){
						if (!row[pos].toString().matches(Constants.empty) && !row[pos].toString().matches("0"))
							addColumns(encodingResource,col.getColumn(),row[pos],
									col.getUri().replace(Constants.space,Constants.empty),outputRowMeta.getValueMeta(pos));
					}
				}
				
				if (col.getShow().equalsIgnoreCase("YES")
						&& !col.getColumn_shp().equalsIgnoreCase(this.attributeName)
						&& !col.getColumn().equalsIgnoreCase(Constants.the_geom)
						&& row[pos] != null){					
					if (col.getUri() != null && col.getPrefix() != null){						
						if (!row[pos].toString().matches(Constants.empty) && !row[pos].toString().matches("0"))
							addColumns(encodingResource,col.getColumn(),row[pos],
									col.getUri().replace(Constants.space,Constants.empty), outputRowMeta.getValueMeta(pos));
					} else {
						if (!row[pos].toString().matches(Constants.empty) && !row[pos].toString().matches("0"))
							addColumns(encodingResource,col.getColumn(),row[pos],
									this.resourceNS.replace(Constants.space,Constants.empty), outputRowMeta.getValueMeta(pos));
					}					
				}
				pos++;
			}
		}

		Geometry geometry = null;
		// GEOMETRY
		try {
			geometry = (Geometry) row[this.posGeometry];
		} catch (Exception e1) {
			WKTReader reader = new WKTReader();
			try {
				geometry = reader.read((String) row[this.posGeometry]);
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}

		// Attempt to transform geometry into the target CRS
		if (transform != null) {
			try {
				geometry = JTS.transform(geometry,transform);
			} catch (Exception e) {
				e.printStackTrace();
			}		
		}	

		if (this.columns == null) {
			addGeometry(encodingResource,geometry);
		} else {
			pos = 0;
			for (ColumnDefinition col : this.columns) {				
				if (col.getShow().equalsIgnoreCase("YES")
						&& col.getColumn().equalsIgnoreCase(Constants.the_geom)
						&& row[pos] != null){
					addGeometry(encodingResource,geometry);
					break;
				}
				pos++;
			}
		}

		geometry = null; // Helps the garbage collector
	}  
		
	/**
	 * Adds columns in the model 
	 * @param encodingResource - The attribute
	 * @param column - Columns's name
	 * @param object - Value of the column
	 * @param propertyNS - Property
	 * @param valueMeta - Meta-information of the given column
	 */	
	private void addColumns(String encodingResource, String column, Object object, String propertyNS, ValueMetaInterface valueMeta){
		Resource resource = vfact.createIRI(this.resourceNS.replace(Constants.space,Constants.empty) + encodingResource);		
		IRI property = vfact.createIRI(propertyNS + column);
		ModelBuilder builder = new ModelBuilder(this.model_rdf);
		builder.subject(resource);

		if (object.toString().matches(".*\\d.*")) { // Object is a number
			if (object.toString().matches("-?\\d+(\\.\\d+)?")){
				builder.add(property, vfact.createLiteral(object.toString()));
			} else {
				if (object.getClass().getName().equalsIgnoreCase("java.util.Date")){
					builder.add(property, vfact.createLiteral(LocalDate.parse(object.toString())));
				} else {
					if (this.language.equalsIgnoreCase(Constants.null_)){
						builder.add(property, vfact.createLiteral(object.toString()));
					} else {
						builder.add(property, vfact.createLiteral(object.toString(), this.language));

					}
				}
			}						
		} else if(object.toString().matches("^<http(.*)>$")){ // Object is an url resource
			builder.add(property, vfact.createLiteral(object.toString().replace(">", "").replace("<", "")));
		} else if (object.toString().equals(Constants.empty)) {
			builder.add(property, vfact.createLiteral(object.toString()));
		} else {
			if (this.language.equalsIgnoreCase(Constants.null_)){
				if (object.getClass().getName().equalsIgnoreCase("java.util.Date")){
					builder.add(property, vfact.createLiteral(LocalDate.parse(object.toString())));
				} else {
					builder.add(property, vfact.createLiteral(object.toString(),""));
				}
			} else {
				if (object.getClass().getName().equalsIgnoreCase("java.util.Date")){
					builder.add(property, vfact.createLiteral(LocalDate.parse(object.toString())));
				} else {
					builder.add(property, vfact.createLiteral(object.toString(), this.language));
				}
			}
		}
		builder.build();
	}

	/**
	 * Adds the geometry in the model
	 * @param encodingResource - The attribute
	 * @param geometry - The geometry
	 */	
	private void addGeometry(String encodingResource, Geometry geometry){
		Resource resourceGeometry = vfact.createIRI(this.resourceNS.replace(Constants.space,Constants.empty) + encodingResource);		
		IRI property = vfact.createIRI(Constants.NS_GEO + "hasGeometry");
		Resource resourceGeometry2 = vfact.createIRI(this.resourceNS.replace(Constants.space,Constants.empty) + encodingResource + Constants.GEOMETRY);		
		ModelBuilder builder = new ModelBuilder(this.model_rdf);
		builder.add(resourceGeometry, property, resourceGeometry2);

		String geo = encodingResource + Constants.GEOMETRY;
		if (geometry.getGeometryType().equals(Constants.POINT)) {
			insertPoint(geo, geometry);
		} else if (geometry.getGeometryType().equals(Constants.LINE_STRING)) {
			insertLineString(geo, geometry);
		} else if (geometry.getGeometryType().equals(Constants.POLYGON)) {
			insertPolygon(geo, geometry);
		} else if (geometry.getGeometryType().equals(Constants.MULTI_POLYGON)) {
			if (geometry.getNumGeometries() == 1){
				Geometry tmpGeometry = geometry.getGeometryN(0);
				if (tmpGeometry.getGeometryType().equals(Constants.POLYGON)) {
					insertPolygon(geo, tmpGeometry);
				} else if (tmpGeometry.getGeometryType().equals(Constants.LINE_STRING)) {
					insertLineString(geo, tmpGeometry);
				} else if (tmpGeometry.getGeometryType().equals(Constants.POINT)) {
					insertPoint(geo, tmpGeometry);
				}	
			} else {
				insertMultiPolygon(geo, geometry);
			}	
		} else if (geometry.getGeometryType().equals(Constants.MULTI_LINE_STRING)) {
			if (geometry.getNumGeometries() == 1){
				Geometry tmpGeometry = geometry.getGeometryN(0);
				if (tmpGeometry.getGeometryType().equals(Constants.POLYGON)) {
					insertPolygon(geo, tmpGeometry);
				} else if (tmpGeometry.getGeometryType().equals(Constants.LINE_STRING)) {
					insertLineString(geo, tmpGeometry);
				} else if (tmpGeometry.getGeometryType().equals(Constants.POINT)) {
					insertPoint(geo, tmpGeometry);
				}	
			} else {
				insertMultiLineString(geo, geometry);
			}		
		}		
	}	

	/**
	 * Handle Polyline geometry according to GeoSPARQL standard
	 * @param resource - Attribute
	 * @param geo - Geometry
	 */
	private void insertLineString(String resource, Geometry geo) {          
		insertResourceTypeResource(this.resourceNS.replace(Constants.space,Constants.empty) + resource, 
				Constants.NS_SF + Constants.LINE_STRING);	
		insertLiteralTriplet(this.resourceNS.replace(Constants.space,Constants.empty) + resource, 
				Constants.NS_GEO + Constants.WKT, geo.toText(), 
				Constants.NS_GEO + Constants.WKTLiteral);
	}

	/**
	 * Handle Polygon geometry according to GeoSPARQL standard
	 * @param resource - Attribute
	 * @param geo - Geometry
	 */
	private void insertPolygon(String resource, Geometry geo) {		
		insertResourceTypeResource(this.resourceNS.replace(Constants.space,Constants.empty) + resource, 
				Constants.NS_SF + Constants.POLYGON);		    
		insertLiteralTriplet(this.resourceNS.replace(Constants.space,Constants.empty) + resource,
				Constants.NS_GEO + Constants.WKT, geo.toText(),
				Constants.NS_GEO +  Constants.WKTLiteral);
	}

	/**
	 * Handle MultiPolygon geometry according to GeoSPARQL standard
	 * @param resource - Attribute
	 * @param geo - Geometry
	 */
	private void insertMultiPolygon(String resource, Geometry geo) {	
		insertResourceTypeResource(this.resourceNS.replace(Constants.space,Constants.empty) + resource, 
				Constants.NS_SF + Constants.MULTI_POLYGON);		    
		insertLiteralTriplet(this.resourceNS.replace(Constants.space,Constants.empty) + resource,
				Constants.NS_GEO + Constants.WKT, geo.toText(),
				Constants.NS_GEO +  Constants.WKTLiteral);
	}

	/**
	 * Handle MultiPolyline geometry according to GeoSPARQL standard
	 * @param resource - Attribute
	 * @param geo - Geometry
	 */
	private void insertMultiLineString(String resource, Geometry geo) {          
		insertResourceTypeResource(this.resourceNS.replace(Constants.space,Constants.empty) + resource, 
				Constants.NS_SF + Constants.MULTI_LINE_STRING);	
		insertLiteralTriplet(this.resourceNS.replace(Constants.space,Constants.empty) + resource, 
				Constants.NS_GEO + Constants.WKT, geo.toText(), 
				Constants.NS_GEO + Constants.WKTLiteral);
	}

	/**
	 * Handle resource type
	 * @param r1 - Attribute 1
	 * @param r2 - Attribute 2
	 */
	private void insertResourceTypeResource(String r1, String r2) {
		this.model_rdf.add(vfact.createIRI(r1), RDF.TYPE,  vfact.createIRI(r2));
	}

	/**
	 * Handle triples for string literals
	 * @param s - Literals
	 * @param p - Literals
	 * @param o - Literals
	 * @param x - Literals
	 */
	private void insertLiteralTriplet(String s, String p, String o, String x) {
		Resource resourceGeometry = vfact.createIRI(s);
		IRI property = vfact.createIRI(p);
			this.model_rdf.add(resourceGeometry, property, vfact.createLiteral(o));		
	}

	/**
	 * Handle label triples
	 * @param resource - Attribute
	 * @param label - Label
	 * @param lang
	 */
	private void insertLabelResource(String resource, String label, String lang) {				
		Resource resource1 = vfact.createIRI(resource);
		label = label.replaceAll("\\s+$", "");
		if (this.phrase != null && this.phrase != ""){
			
			label = this.phrase + " " + label.replaceAll("\\s+","");
		}
		
		if (label.toString().matches(".*\\d.*")){ // label is a number
			this.model_rdf.add(resource1, RDFS.LABEL, vfact.createLiteral(label,Constants.empty));									
		} else if (label.toString().equals(Constants.empty)) {
			this.model_rdf.add(resource1, RDFS.LABEL, vfact.createLiteral(label,Constants.empty));
		} else { 
			if (this.language.equalsIgnoreCase(Constants.null_)){
				this.model_rdf.add(resource1, RDFS.LABEL, vfact.createLiteral(label, Constants.empty));
			} else {
				this.model_rdf.add(resource1, RDFS.LABEL, vfact.createLiteral(label, lang));
			}
		}	
	}

	/**
	 * Point geometry according to GeoSPARQL standard
	 * @param resource - Attribute
	 * @param geo - Geometry
	 */
	private void insertPoint(String resource, Geometry geo) {    
		insertResourceTypeResource(this.resourceNS.replace(Constants.space,Constants.empty) + resource,
				Constants.NS_SF + Constants.POINT);	    
		insertLiteralTriplet(this.resourceNS.replace(Constants.space,Constants.empty) + resource,
				Constants.NS_GEO + Constants.WKT,geo.toText(),
				Constants.NS_GEO + Constants.WKTLiteral);
	}

	/**
	 * Remove special character from String.
	 * @param input - String
	 * @return String without special characters.
	 */	
	public static String removeSpecialCharacter(String input) {
		if (input == null)
			return null;

		for (int i = 0; i < Constants.SPECIAL_CHARACTER.length(); i++)
			input = input.replace(Constants.SPECIAL_CHARACTER.charAt(i), Constants.ASCII.charAt(i));

		for (int i = 0; i < Constants.SYMBOLS.length(); i++)
			input = input.replace(Constants.SYMBOLS.charAt(i), '-');

		return input;
	}

	/**
	 * Replaces the sequence of specific repeated character.
	 * @param input - String
	 * @return String without specific repeated character.
	 */	
	public static String repeatedCharacters(String input) {
		String s = input + Constants.space;
		String newString = Constants.empty;
		char char1, char2;

		for (int i = 0; i < (s.length() - 1); i++) {			
			char1 = s.charAt(i);
			char2 = s.charAt(i+1);				
			if (char1 == '-'){ 
				if (char1 != char2){
					newString = newString + char1;
				}
			} else {
				newString = newString + char1;
			}
		}

		if (newString.equals(Constants.empty)){
			return input;
		}

		if (newString.charAt(newString.length()-1) == '-'){
			newString = newString.substring(0, newString.length()-1); 
		}		

		return newString;
	}

	/**
	 * Get the RDF Model
	 * @param model - RDF Model
	 * @return String with the RDF Model
	 */
	private String getRdfModel(Model model) {
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		Rio.write(this.model_rdf, stream, RDFFormat.TURTLE);	

		// Helps the garbage collector
		this.columns = null; 
		this.model_rdf = null;

		return new String(stream.toByteArray());
	}	

	public String getAttributeName() { return this.attributeName; }
	public void setAttributeName(String attributeName) { this.attributeName = attributeName; }

	public String getFeature() { return this.feature; }
	public void setFeature(String feature) { this.feature = feature; }

	public String getOntologyNS() { return this.ontologyNS; }
	public void setOntologyNS(String ontologyNS) { this.ontologyNS = ontologyNS; }

	public String getOntologyNSPrefix() { return this.ontologyNSPrefix; }
	public void setOntologyNSPrefix(String ontologyNSPrefix) { this.ontologyNSPrefix = ontologyNSPrefix; }

	public String getResourceNS() { return this.resourceNS;}
	public void setResourceNS(String resourceNS) { this.resourceNS = resourceNS; }

	public String getResourceNSPrefix() { return this.resourceNSPrefix; }
	public void setResourceNSPrefix(String resourceNSPrefix) { this.resourceNSPrefix = resourceNSPrefix; }

	public String getLanguage() { return this.language; }
	public void setLanguage(String language) { this.language = language; }

	public Boolean getFlag_csv() { return this.flag_csv; }
	public void setFlag_csv(Boolean flag_csv) { this.flag_csv = flag_csv; }

	public ClassesCSV[] getClasses() { return this.classes; }
	public void setClasses(ClassesCSV[] classes) { this.classes = classes; }

	public CSV getCsv() { return this.csv; }
	public void setCsv(CSV csv) { this.csv = csv; }	

	public boolean isUuidsActive() { return this.uuidsActive; }
	public void setUuidsActive(boolean uuidsActive) { this.uuidsActive = uuidsActive; }

	public String getModel_rdf() { return getRdfModel(this.model_rdf); }
	public void setModel_rdf(Model model_rdf) { this.model_rdf = model_rdf; }		

	public int getPosAttribute(){ return this.posAttribute; }	  
	public void setPosAttribute(int posAttribute){ this.posAttribute = posAttribute; }

	public int getPosGeometry(){ return this.posGeometry; }	  
	public void setPosGeometry(int posGeometry){ this.posGeometry = posGeometry; }

	public FieldDefinition[] getFields() { return this.fields; }
	public void setFields(FieldDefinition[] fields) { this.fields = fields; }	

	public ColumnDefinition[] getColumns() { return this.columns; }
	public void setColumns(ColumnDefinition[] columns) { this.columns = columns; }
	
	public String getPhrase() { return phrase; }
	public void setPhrase(String phrase) { this.phrase = phrase; }

}

// END ShpToRDF.java
