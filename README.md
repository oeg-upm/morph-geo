# actualizar-herramientas-linked-data-geografico-OEG
### Título del TFG
Actualizar herramientas de Linked Data geográfico del Grupo de Ingeniería Ontológica (inicialmente el objetivo era el de introducir soporte al formato GeoPackage en herramientas de Linked Data Geográfico desarrolladas por el Grupo de Ingeniería Ontológica)

### Resumen general del trabajo
En el Grupo de Ingeniería Ontológica se ha venido tradicionalmente trabajando con el Instituto Geográfico Nacional para la exportación de algunos de sus datos geográficos a formato Linked Data. Un ejemplo se puede encontrar en https://datos.ign.es/

Inicialmente, se planteó la posibilidad de realizar la adaptación de herramientas como el plugin TripleGeoKettle para que usaran el formato GeoPackage. Sin embargo, tras un estudio inicial sobre este formato y sobre las actualizaciones necesarias en el plugin, se decidió actualizar este plugin para su uso en la suite Pentaho Data Integration 9 (PDI9).

### Lista de objetivos concretos (iniciales - no cubiertos finalmente)
1. Dar soporte GeoPackage a la herramienta Map4RDF
2. Dar soporte GeoPackage a la herramienta GeoKettle y su plugin para transformar a RDF
3. Realizar un procesado completo de todos los datos del IGN para generar este tipo de formato.

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
