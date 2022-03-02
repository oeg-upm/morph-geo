# morph-GEO
Este repositorio es el resultado de varios TFGs de la ETSI Informáticos de la Universidad Politécnica de Madrid, basados en el trabajo previo que el Grupo de Ingeniería Ontológica  ha venido tradicionalmente realizando con instituciones relacionadas con los datos geográficos, y más específicamente con instituciones como el Instituto Geográfico Nacional, para la exportación de algunos de sus datos geográficos a RDF y su publicación como datos enlazados (Linked Data). Un ejemplo de los resultados obtenidos se puede encontrar en https://datos.ign.es/

### TFGs realizados y en progreso (a completar el listado, llegando hasta antes del 2010)
Javier Martínez Cuadrado (2022)

Daniel Jiménez Teomiro (2022)

Beñat Agirre Arruabarrena (2021) Actualizar herramientas de Linked Data geográfico del Grupo de Ingeniería Ontológica. https://oa.upm.es/68125/

### Resumen general (a mejorar)
El código de este repositorio contiene Inicialmente la última versión del plugin original TripleGeoKettle (ver enlace más abajo), que funcionaba como plugin de la herramienta GeoKettle (ya deprecada), adaptado para su uso en la suite Pentaho Data Integration 9 (PDI9).

### Lista de tareas a realizar (a ir actualizando)
1. Analizar la compatibilidad con nuevas versiones de la suite Pentaho Data Integration, y actualizar el plugin en caso de que sea necesario
2. Analizar los cambios realizados en la versión actual de GeoSPARQL1.1 (https://opengeospatial.github.io/ogc-geosparql/geosparql11/spec.html), y permitir la exportación que sea compatible con GeoSPARQL1.0 y con GeoSPARQL1.1
3. Generar todos los datos procedentes del IGN, para actualizarlos en caso de ser necesario.
4. Proponer nuevas formas de xplotar los datos enlazados generados
5. Incluir en el plugin la posibilidad de enlazar con fuentes externas (DBpedia, Wikidata, Geonames)
etc.

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
