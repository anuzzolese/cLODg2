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

# Session

map:Session a d2rq:ClassMap;
	d2rq:dataStorage map:database;
	d2rq:uriPattern "${baseURI}conference/${confAcronym?lower_case}/${year}/@@SESSION.id@@";
	d2rq:class icaltzd:Vevent .
	
map:session_vevent a d2rq:PropertyBridge;
	d2rq:belongsToClassMap map:Session;
	d2rq:property rdf:type;
	d2rq:uriPattern "http://data.semanticweb.org/ns/swc/ontology#@@SESSION.type@@" .
	
map:session_start a d2rq:PropertyBridge;
	d2rq:belongsToClassMap map:Session;
	d2rq:property icaltzd:dtstart;
	d2rq:pattern "@@SESSION.date@@T@@SESSION.start@@" .
	
map:session_end a d2rq:PropertyBridge;
	d2rq:belongsToClassMap map:Session;
	d2rq:property icaltzd:dtend;
	d2rq:pattern "@@SESSION.date@@T@@SESSION.end@@" .
	
map:session_title a d2rq:PropertyBridge;
	d2rq:belongsToClassMap map:Session;
	d2rq:property icaltzd:description;
	d2rq:property rdfs:label;
	d2rq:pattern "@@SESSION.title@@" .
	
map:session_summary a d2rq:PropertyBridge;
	d2rq:belongsToClassMap map:Session;
	d2rq:property icaltzd:summary;
	d2rq:pattern "@@SESSION.description@@" .
	
map:session_location a d2rq:PropertyBridge;
	d2rq:belongsToClassMap map:Session;
	d2rq:condition "SESSION.location <> ''";
	d2rq:property icaltzd:location;
	d2rq:pattern "@@SESSION.location@@" .
	
map:session_track a d2rq:PropertyBridge;
	d2rq:belongsToClassMap map:Session;
	d2rq:condition "SESSION.track <> ''";
	d2rq:property swc:isSubEventOf;
	d2rq:uriPattern "${baseURI}conference/${confAcronym?lower_case}/${year}/@@SESSION.track@@" .

map:session_no_track a d2rq:PropertyBridge;
	d2rq:belongsToClassMap map:Session;
	d2rq:condition "SESSION.track = ''";
	d2rq:property swc:isSubEventOf;
	d2rq:uriPattern "${baseURI}conference/${confAcronym?lower_case}${year}" .
	
# Tracks and Sessions 
	
map:Track_sessions a d2rq:ClassMap;
	d2rq:dataStorage map:database;
	d2rq:uriPattern "${baseURI}conference/${confAcronym?lower_case}/${year}/@@TRACK.name@@";
	d2rq:join "TRACK.name = SESSION.track";
	d2rq:translateWith map:UriTranslator;
	d2rq:class swc:TrackEvent .	
	
map:track_super_event_of a d2rq:PropertyBridge;
	d2rq:belongsToClassMap map:Track_sessions;
	d2rq:property swc:isSuperEventOf;
	d2rq:uriPattern "${baseURI}conference/${confAcronym?lower_case}/${year}/@@SESSION.id@@" .