# soporte-GeoPackage-herramientas-OEG
### Título del TFG
Actualizar las Herramientas de LinkedData Geográfico utilizadas por el Grupode Ingeniería Ontológica

### Resumen general del trabajo
El Ontology Engineering Group lleva más de una década trabajando con datos geográficos enlazados españoles. En 2010 se definió un caso de uso y en 2019 se refinó el proceso de generación y publicación de los datos abiertos utilizando el dataset BTN100 como caso de estudio. En los últimos años han se han popularizado nuevas herramientas y formatos que ofrecen ventajas no disponibles en las usadas hasta el momento. Entre ellas se encuentran el programa de transformaciones de datos PDI9 Kettle, que reemplaza a GEOKettle; el formato GeoPackage, más práctico que el shapefile; y Apache Maven, más extenso que Apache Ant. Por consiguiente, también será necesario actualizar tripleGeo, el plugin para GEOKettle desarrollado por el OEG, para integrarlo en el nuevo toolbox. 

### Lista de objetivos concretos
1. Replicar la funcionalidad y las transformaciones de GeoKettle + TripleGeo en la nueva suite PDI. 
2. Dar soporte GeoPackage al plugin para transformar a RDF y las transformaciones existentes.
3. Realizar un procesado completo de todos los datos del IGN con las nuevas transformaciones.

### Tutor
OSCAR CORCHO GARCíA - ocorcho@fi.upm.es

### Materiales para el desarrollo
1. Map4RDF
    - [Map4rdf](https://oeg-upm.github.io/map4rdf/)
    - [Source Map4rdf](https://github.com/oeg-upm/map4rdf)
    - [Pagina que implementa Map4rdf](http://certidatos.ign.es/map/) 
    - [Source de la pagina que implementa Map4RDF](https://github.com/oeg-upm/website-geo)  
2. GeoKettle
    - [Wiki TripleGeoKettle](https://github.com/oeg-upm/geo.linkeddata.es-TripleGeoKettle/wiki)
    - [TripleGeoKettle](https://github.com/oeg-upm/geo.linkeddata.es-TripleGeoKettle)  
    - [Imagen docker para desplegar GeoKettle](https://github.com/oeg-upm/docker-geokettle-x3geo)  
3. Procesado de datos
    - [Datos IGN: Base Topográfica Nacional 1:100.000 (BTN100)](http://datos.ign.es/)  

### Documentación
- [Geographical Linked Data: a Spanish Use Case](http://oa.upm.es/6167/1/Geographical_Linked_Data_A_Spanish_Use_Case.pdf)  
- [A sustainable process and toolbox for geographical linked data generation and publication: a case study with BTN100](https://link.springer.com/article/10.1186/s40965-019-0060-4)  
- [Di no al Shapefile y sí al GeoPackage](https://mappinggis.com/2018/08/di-no-al-shapefile-y-si-al-geopackage/)  
