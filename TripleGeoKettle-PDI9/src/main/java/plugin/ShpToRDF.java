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


// RDF4J
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.impl.DynamicModelFactory;
import org.eclipse.rdf4j.model.util.ModelBuilder;
import org.eclipse.rdf4j.model.Namespace;



import java.io.IOException;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
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

		builder.setNamespace(this.ontologyNSPrefix, this.ontologyNS.replace(Constants.space,Constants.empty));
		builder.setNamespace(this.resourceNSPrefix, this.resourceNS.replace(Constants.space,Constants.empty));
		builder.setNamespace("geosparql", Constants.NS_GEO);
		builder.setNamespace("sf", Constants.NS_SF);
		builder.setNamespace("dc", Constants.NS_DC);
		builder.setNamespace("xsd", Constants.NS_XSD);		
		builder.setNamespace("rdf", Constants.NS_RDF);
		builder.setNamespace("foaf", Constants.NS_FOAF);
		builder.setNamespace("geo", Constants.NS_WGS84);	
		builder.setNamespace("owl", Constants.NS_OWL);
		builder.setNamespace("rdfs", Constants.NS_RDFS);

		Model modelAux = builder.build();

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
