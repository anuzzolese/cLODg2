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

# Conference
map:Conference a d2rq:ClassMap;
	d2rq:dataStorage map:database;
	d2rq:constantValue <${baseURI}conference/${confAcronym?lower_case}/${year}>;
	d2rq:class swc:ConferenceEvent .
	
map:conferences_Acronym a d2rq:PropertyBridge;
	d2rq:belongsToClassMap map:Conference;
	d2rq:property swc:hasAcronym;
	d2rq:constantValue "${confAcronym}${year}" .
	
map:conferences_Name a d2rq:PropertyBridge;
	d2rq:belongsToClassMap map:Conference;
	d2rq:property rdfs:label;
	d2rq:constantValue "${confAcronym?upper_case} ${year}" .
	
map:conferences_StartDate a d2rq:PropertyBridge;
	d2rq:belongsToClassMap map:Conference;
	d2rq:property icaltzd:dtstart;
	d2rq:constantValue "${startDate}" .
	
map:conferences_EndDate a d2rq:PropertyBridge;
	d2rq:belongsToClassMap map:Conference;
	d2rq:property icaltzd:dtend;
	d2rq:constantValue "${endDate}" .
	
map:conferences_Location a d2rq:PropertyBridge;
	d2rq:belongsToClassMap map:Conference;
	d2rq:property icaltzd:location;
	d2rq:constantValue "${location}" .
	
# Organizations
map:AuthorOrganization a d2rq:ClassMap;
	d2rq:dataStorage map:database;
	#d2rq:uriSqlExpression "CONCAT('${baseURI}organization/', LOWER(REPLACE(SUBMISSION.organization, ' ', '-')))";
	d2rq:uriPattern "${baseURI}organization/@@AUTHOR.organization@@";
	d2rq:translateWith map:UriTranslator;
	d2rq:join "AUTHOR.submission # = SUBMISSION.#";
	d2rq:condition "SUBMISSION.decision = 'accept'";
	d2rq:class foaf:Organization .
	
map:organization_foaf_name a d2rq:PropertyBridge;
	d2rq:belongsToClassMap map:AuthorOrganization;
	d2rq:property foaf:name;
	d2rq:property rdfs:label;
    d2rq:column "AUTHOR.organization" .
    
map:organization_member a d2rq:PropertyBridge;
	d2rq:belongsToClassMap map:AuthorOrganization;
	d2rq:property foaf:member;
	#d2rq:uriSqlExpression "LOWER(CONCAT('${baseURI}person/', REPLACE(AUTHOR.`first name`, ' ', '-'), '-', REPLACE(AUTHOR.`last name`, ' ', '-')))" .
	d2rq:uriPattern "${baseURI}person/@@AUTHOR.first name@@-@@AUTHOR.last name@@";
	d2rq:translateWith map:UriTranslator.
	
map:organization_auth_based_near  a d2rq:PropertyBridge;
	d2rq:belongsToClassMap map:AuthorOrganization;
	d2rq:property foaf:based_near;
	d2rq:column "AUTHOR.country" .
	
# Authors
map:Author a d2rq:ClassMap;
	d2rq:dataStorage map:database;
	#d2rq:uriSqlExpression "LOWER(CONCAT('${baseURI}person/', REPLACE(AUTHOR.`first name`, ' ', '-'), '-', REPLACE(AUTHOR.`last name`, ' ', '-')))";
	d2rq:uriPattern "${baseURI}person/@@AUTHOR.first name@@-@@AUTHOR.last name@@";
	d2rq:translateWith map:UriTranslator;
	d2rq:join "AUTHOR.submission # = SUBMISSION.#";
	d2rq:join "SUBMISSION.track # = TRACK.#";
	d2rq:condition "SUBMISSION.decision = 'accept'";
	d2rq:class foaf:Person .
	
map:author_given_name a d2rq:PropertyBridge;
	d2rq:belongsToClassMap map:Author;
	d2rq:property foaf:givenName;
	d2rq:column "AUTHOR.first name" .
	
map:author_family_name a d2rq:PropertyBridge;
	d2rq:belongsToClassMap map:Author;
	d2rq:property foaf:familyName;
	d2rq:column "AUTHOR.last name" .
	
map:author_name a d2rq:PropertyBridge;
	d2rq:belongsToClassMap map:Author;
	d2rq:property foaf:name;
	d2rq:property rdfs:label;
	d2rq:pattern "@@AUTHOR.first name@@ @@AUTHOR.last name@@" .

map:author_affiliation a d2rq:PropertyBridge;
	d2rq:belongsToClassMap map:Author;
	d2rq:property swrc:affiliation;
	#d2rq:uriSqlExpression "CONCAT('${baseURI}organization/', LOWER(REPLACE(AUTHOR.organization, ' ', '-')))" .
	d2rq:uriPattern "${baseURI}organization/@@AUTHOR.organization@@";
	d2rq:translateWith map:UriTranslator .

map:author_made_paper a d2rq:PropertyBridge;
	d2rq:belongsToClassMap map:Author;
	d2rq:uriPattern "${baseURI}conference/${confAcronym?lower_case}/${year}/paper/@@TRACK.name@@/@@AUTHOR.submission #@@";
	d2rq:property foaf:made .
	
map:author_homepage a d2rq:PropertyBridge;
	d2rq:belongsToClassMap map:Author;
	d2rq:uriColumn "AUTHOR.Web site";
	d2rq:condition "AUTHOR.`Web site` <> ''";
	d2rq:property foaf:homepage .
	
map:author_email a d2rq:PropertyBridge;
	d2rq:belongsToClassMap map:Author;
	d2rq:uriPattern "mailto:@@AUTHOR.email@@";
	d2rq:condition "AUTHOR.email <> ''";
	d2rq:property foaf:mbox .
	
map:author_email_sha1 a d2rq:PropertyBridge;
	d2rq:belongsToClassMap map:Author;
	d2rq:sqlExpression "SHA1(CONCAT('mailto:', AUTHOR.email))";
	d2rq:condition "AUTHOR.email <> ''";
	d2rq:property foaf:mbox_sha1sum .

# Papers
map:Paper a d2rq:ClassMap;
	d2rq:dataStorage map:database;
	d2rq:uriPattern "${baseURI}conference/${confAcronym?lower_case}/${year}/paper/@@TRACK.name@@/@@SUBMISSION.#@@";
	d2rq:condition "SUBMISSION.decision = 'accept'";	
	d2rq:join "SUBMISSION.# = AUTHOR.submission #";
	d2rq:join "SUBMISSION.track # = TRACK.#";
	d2rq:class swrc:InProceedings .
	
map:paper_title a d2rq:PropertyBridge;
	d2rq:belongsToClassMap map:Paper;
	d2rq:column "SUBMISSION.title";
	d2rq:property dc:title ;
	d2rq:property rdfs:label .
	
map:paper_year a d2rq:PropertyBridge;
	d2rq:belongsToClassMap map:Paper;
	d2rq:constantValue "${year}";
	d2rq:property swrc:year .
	
map:paper_month a d2rq:PropertyBridge;
	d2rq:belongsToClassMap map:Paper;
	d2rq:constantValue "May";
	d2rq:property swrc:month .
	
map:paper_year a d2rq:PropertyBridge;
	d2rq:belongsToClassMap map:Paper;
	d2rq:constantValue "${year}";
	d2rq:property swrc:year .
	
map:paper_is_part_of a d2rq:PropertyBridge;
	d2rq:belongsToClassMap map:Paper;
	d2rq:constantValue <${baseURI}conference/${confAcronym?lower_case}/${year}/proceedings>;
	d2rq:property swc:isPartOf .
	
map:paper_creator a d2rq:PropertyBridge;
	d2rq:belongsToClassMap map:Paper;
	#d2rq:uriSqlExpression "LOWER(CONCAT('${baseURI}person/', REPLACE(AUTHOR.`first name`, ' ', '-'), '-', REPLACE(AUTHOR.`last name`, ' ', '-')))";
	d2rq:uriPattern "${baseURI}person/@@AUTHOR.first name@@-@@AUTHOR.last name@@";
	d2rq:translateWith map:UriTranslator ;
	d2rq:property dc:creator ;
	d2rq:property foaf:make .
	
map:paper_abstract a d2rq:PropertyBridge;
	d2rq:belongsToClassMap map:Paper;
	d2rq:column "SUBMISSION.abstract";
	d2rq:condition "SUBMISSION.abstract <> ''";
	d2rq:property swrc:abstract .
	
map:paper_is_part_of a d2rq:PropertyBridge;
	d2rq:belongsToClassMap map:Paper;
	d2rq:constantValue <${baseURI}conference/${confAcronym?lower_case}/${year}/proceedings>;
	d2rq:property swrc:isPartOf .
	
map:paper_keywords a d2rq:PropertyBridge;
	d2rq:belongsToClassMap map:Paper;
	d2rq:pattern "@@SUBMISSION.keywords@@";
	d2rq:property dc:subject .
	
# Proceedings
map:Proceedings a d2rq:ClassMap;
	d2rq:dataStorage map:database;
	d2rq:constantValue <${baseURI}conference/${confAcronym?lower_case}/${year}/proceedings>;
	d2rq:join "SUBMISSION.track # = TRACK.#";
	d2rq:class swrc:Proceedings .
	
map:proceedings_has_part a d2rq:PropertyBridge;
	d2rq:belongsToClassMap map:Proceedings;
	d2rq:uriPattern "${baseURI}conference/${confAcronym?lower_case}/${year}/paper/@@TRACK.name@@/@@SUBMISSION.#@@";
	d2rq:condition "SUBMISSION.decision = 'accept'";	
	d2rq:property swrc:hasPart .
	
map:proceedings_label a d2rq:PropertyBridge;
	d2rq:belongsToClassMap map:Proceedings;
	d2rq:constantValue "Proceedings of ${confAcronym?upper_case} ${year}";	
	d2rq:property rdfs:label .
	
# Tracks

map:Track a d2rq:ClassMap;
	d2rq:dataStorage map:database;
	d2rq:uriPattern "${baseURI}conference/${confAcronym?lower_case}/${year}/@@TRACK.name@@";
	d2rq:class swc:TrackEvent .
	
map:track_label a d2rq:PropertyBridge;
	d2rq:belongsToClassMap map:Track;
	d2rq:property rdfs:label;
	d2rq:pattern "@@TRACK.long name@@" .