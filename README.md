# morph-GEO
Este repositorio es el resultado de varios proyectos de investigación e innovación, así como Trabajos Fin de Grado realizados en la ETSI Informáticos de la Universidad Politécnica de Madrid, basados en el trabajo previo que el Grupo de Ingeniería Ontológica ha venido tradicionalmente realizando con instituciones relacionadas con los datos geográficos, como el Instituto Geográfico Nacional. También se ha utilizado en la transformación de datos geográficos asociados a entidades de transporte (Consorcio Regional de Transportes de Madrid, European Agency for Railways).

Un ejemplo de los resultados obtenidos se puede encontrar en https://datos.ign.es/ (actualmente fuera de servicio, y que transitará a https://geo.linkeddata.es/).

### TFGs realizados y en progreso (a completar el listado, llegando hasta antes del 2010)
Javier Martínez Cuadrado (2022). Actualización de herramientas y aprendizaje en el uso de Spoon PDI para la gestión de datos geográficos. https://oa.upm.es/71567/

Beñat Agirre Arruabarrena (2021) Actualizar herramientas de Linked Data geográfico del Grupo de Ingeniería Ontológica. https://oa.upm.es/68125/

### Resumen general (a mejorar)
El código de este repositorio contiene Inicialmente la última versión del plugin original TripleGeoKettle (ver enlace más abajo), que funcionaba como plugin de la herramienta GeoKettle (ya deprecada), adaptado para su uso en la suite Pentaho Data Integration 9 (PDI9).

### Lista de tareas a realizar (a ir actualizando)
1. Analizar la compatibilidad con nuevas versiones de la suite Pentaho Data Integration, y actualizar el plugin en caso de que sea necesario
2. Analizar los cambios realizados en la versión actual de GeoSPARQL1.1 (https://opengeospatial.github.io/ogc-geosparql/geosparql11/spec.html), y permitir la exportación que sea compatible con GeoSPARQL1.0 y con GeoSPARQL1.1
3. Generar todos los datos procedentes del IGN y de otras fuentes (CRTM, ERA, etc.), para actualizarlos en caso de ser necesario.
4. Proponer nuevas formas de explotar los datos enlazados generados
5. Incluir en el plugin la posibilidad de enlazar con fuentes de datos externas (DBpedia, Wikidata, Geonames)
6. Interfaz HTML para consultas (Geo)SPARQL

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

### Documentación (a completar)
- [Geographical Linked Data: a Spanish Use Case](http://oa.upm.es/6167/1/Geographical_Linked_Data_A_Spanish_Use_Case.pdf)  
- [A sustainable process and toolbox for geographical linked data generation and publication: a case study with BTN100](https://link.springer.com/article/10.1186/s40965-019-0060-4)  
- [Di no al Shapefile y sí al GeoPackage](https://mappinggis.com/2018/08/di-no-al-shapefile-y-si-al-geopackage/)  

### Agradecimientos
Además de varios proyectos previos en los que se hicieron desarrollos que permitieron llegar al estado actual de la tecnología (proyectos nacionales como España Virtual y Datos 4.0, así como contratos financiados por el Instituto Geográfico Nacional), el proyecto más reciente en el que se están realizando transformaciones es: 
* SIGTRANS-UPM. Ayuda PCD2021-120917-C22 financiada por MCIN/AEI/10.13039/501100011033 y por la Unión Europea NextGenerationEU/PRTR
