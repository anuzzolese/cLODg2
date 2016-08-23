@prefix : <${baseURI}> .
@prefix swc: <http://data.semanticweb.org/ns/swc/ontology#> . 
@prefix swrc: <http://swrc.ontoware.org/ontology#> . 
@prefix map: <file:/Users/andrea/Desktop/${confAcronym?upper_case}${year}data/D2RQ/mapping-${confAcronym?lower_case}.ttl#> .
@prefix foaf: <http://xmlns.com/foaf/0.1/> .
@prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .
@prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> .
@prefix owl: <http://www.w3.org/2002/07/owl#> .
@prefix xsd: <http://www.w3.org/2001/XMLSchema#> .
@prefix d2rq: <http://www.wiwiss.fu-berlin.de/suhl/bizer/D2RQ/0.1#> .
@prefix dc: <http://purl.org/dc/elements/1.1/> .
@prefix dcterms: <http://purl.org/dc/terms/> .
@prefix skos: <http://www.w3.org/2004/02/skos/core#> .
@prefix vcard: <http://www.w3.org/2001/vcard-rdf/3.0#> .
@prefix jdbc: <http://d2rq.org/terms/jdbc/> .
@prefix icaltzd: <http://www.w3.org/2002/12/cal/icaltzd#> .

map:database a d2rq:Database;
	d2rq:jdbcDriver "org.hsqldb.jdbcDriver";
	d2rq:jdbcDSN "jdbc:hsqldb:file:${dbAddress}/${dbName}";
	d2rq:username "${dbUser}";
	d2rq:password "${dbPass}" .
	
map:UriTranslator a d2rq:TranslationTable;
	d2rq:javaClass "org.w3id.scholarlydata.clodg.Urifier" . 

# Roles
map:Role a d2rq:ClassMap;
	d2rq:dataStorage map:database;
	d2rq:uriPattern '${baseURI}conference/${confAcronym?lower_case}/${year}/@@ORGANISING.role@@';
	d2rq:join 'ORGANISING.role = SWC_ROLES.name';
	d2rq:translateWith map:UriTranslator;
	d2rq:class swc:Chair .
	
map:role_type a d2rq:PropertyBridge;
	d2rq:belongsToClassMap map:Role;
	d2rq:property rdf:type;
	d2rq:uriColumn 'SWC_ROLES.uri' .
	
map:role_isRoleAt a d2rq:PropertyBridge;
	d2rq:belongsToClassMap map:Role;
	d2rq:property swc:isRoleAt;
	d2rq:uriColumn 'SWC_ROLES.uri' .
	
map:role_label a d2rq:PropertyBridge;
	d2rq:belongsToClassMap map:Role;
	d2rq:property rdfs:label;
	d2rq:pattern "${baseURI}conference/${confAcronym?lower_case}/${year}" .