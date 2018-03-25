# Examples

### SPARQL Query for LinkedLifeData

    PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>  
    PREFIX skos: <http://www.w3.org/2004/02/skos/core#>  
    PREFIX biopax2: <http://www.biopax.org/release/biopax-level2.owl#>  
    PREFIX uniprot: <http://purl.uniprot.org/core/>  
    
    SELECT distinct ?object ?attribute  
    WHERE {  
    ?interaction biopax2:PARTICIPANTS ?participant .  
    ?participant biopax2:PHYSICAL-ENTITY ?physicalEntity .  
    ?physicalEntity skos:exactMatch ?protein .  
    ?protein uniprot:classifiedWith <http://purl.uniprot.org/go/0006954>.  
    ?protein uniprot:recommendedName ?name.  
    ?name uniprot:fullName ?object .  
    ?target skos:exactMatch ?protein .  
    ?target rdf:type <http://linkedlifedata.com/resource/drugbank/Molecule> .  
    ?drug drug:targetLink [targetLink:target ?target] ;  
    drug:name ?attribute .  
    }  

### SPARQL Query for FactForge

    PREFIX ff: <http://factforge.net/>  
    PREFIX om: <http://www.ontotext.com/owlim/>  
    PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>  
    PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>  
    PREFIX dbp-ont: <http://dbpedia.org/ontology/>  
    PREFIX dbp-prop: <http://dbpedia.org/property/>  
    PREFIX dbpedia: <http://dbpedia.org/resource/>  
    PREFIX geo-ont: <http://www.geonames.org/ontology#>  
    
    SELECT DISTINCT ?object ?attribute  
    WHERE {  
    ?person ff:preferredLabel ?object ;  
    rdf:type dbp-ont:MusicalArtist ;  
    dbp-prop:instrument ?instrument .  
    ?instrument rdfs:label ?attribute .  
    } LIMIT 3333  
