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

# Organising
map:OrganisingMember a d2rq:ClassMap;
	d2rq:dataStorage map:database;
	d2rq:uriPattern "${baseURI}person/@@ORGANISING.first name@@-@@ORGANISING.last name@@";
	d2rq:translateWith map:UriTranslator;
	d2rq:class foaf:Person .
	
map:om_given_name a d2rq:PropertyBridge;
	d2rq:belongsToClassMap map:OrganisingMember;
	d2rq:property foaf:givenName;
	d2rq:column "ORGANISING.first name" .
	
map:om_family_name a d2rq:PropertyBridge;
	d2rq:belongsToClassMap map:OrganisingMember;
	d2rq:property foaf:familyName;
	d2rq:column "ORGANISING.last name" .
	
map:om_name a d2rq:PropertyBridge;
	d2rq:belongsToClassMap map:OrganisingMember;
	d2rq:property foaf:name;
	d2rq:property rdfs:label;
	d2rq:pattern "@@ORGANISING.first name@@ @@ORGANISING.last name@@" .

map:om_affiliation a d2rq:PropertyBridge;
	d2rq:belongsToClassMap map:OrganisingMember;
	d2rq:property swrc:affiliation;
	d2rq:uriPattern "${baseURI}organization/@@ORGANISING.organization@@";
	d2rq:translateWith map:UriTranslator .

map:om_homepage a d2rq:PropertyBridge;
	d2rq:belongsToClassMap map:OrganisingMember;
	d2rq:uriColumn "ORGANISING.Web site";
	d2rq:condition "ORGANISING.`Web site` <> ''";
	d2rq:property foaf:homepage .
	
map:om_email a d2rq:PropertyBridge;
	d2rq:belongsToClassMap map:OrganisingMember;
	d2rq:uriPattern "mailto:@@ORGANISING.email@@";
	d2rq:condition "ORGANISING.email <> ''";
	d2rq:property foaf:mbox .
	
map:om_email_sha1 a d2rq:PropertyBridge;
	d2rq:belongsToClassMap map:OrganisingMember;
	d2rq:sqlExpression "SHA1(CONCAT('mailto:', ORGANISING.email))";
	d2rq:condition "ORGANISING.email <> ''";
	d2rq:property foaf:mbox_sha1sum .
	
map:om_role a d2rq:PropertyBridge;
	d2rq:belongsToClassMap map:OrganisingMember;
	d2rq:uriPattern "${baseURI}conference/${confAcronym?lower_case}/${year}/@@ORGANISING.role@@";
	d2rq:translateWith map:UriTranslator;
	d2rq:property swc:holdsRole .