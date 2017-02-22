package edu.cornell.vivo.ontologymodulebuilder;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.xml.parsers.ParserConfigurationException;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotationAssertionAxiom;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.xml.sax.SAXException;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.opencsv.CSVReader;

import edu.cornell.vivo.configuration.Configuration;

public class OwlOntologyModelsBuilder {

	private static final String BIBO = 		"http://purl.org/ontology/bibo/";
	private static final String C4O =  		"http://purl.org/spar/c4o/";
	private static final String CITO = 		"http://purl.org/spar/cito/";
	private static final String DC = 		"http://purl.org/dc/";
	private static final String EVENT = 	"http://purl.org/NET/c4dm/event.owl#";
	private static final String FABIO = 	"http://purl.org/spar/fabio/";
	private static final String FOAF = 		"http://xmlns.com/foaf/";
	private static final String GEO = 		"http://aims.fao.org/aos/geopolitical.owl#";
	private static final String OBO = 		"http://purl.obolibrary.org/obo/";
	private static final String OCRE_RSCH = "http://purl.org/net/OCRe/research.owl#";
	private static final String OCRE_SD = 	"http://purl.org/net/OCRe/study_design.owl#";
	private static final String OCRE_SP = 	"http://purl.org/net/OCRe/study_protocol.owl#";
	private static final String OCRE_STAT = "http://purl.org/net/OCRe/statistics.owl#";
	private static final String RO = 		"http://www.obofoundry.org/ro/ro.owl#";
	private static final String SKOS = 		"http://www.w3.org/2004/02/skos/";
	private static final String SOFTWARE = 	"http://www.ebi.ac.uk/efo/swo/";
	private static final String VANN = 		"http://purl.org/vocab/vann/";
	private static final String VCARD = 	"http://www.w3.org/2006/vcard/ns#";
	private static final String VITRO = 	"http://vitro.mannlib.cornell.edu/ns/vitro/public#";
	private static final String VIVOCORE = 	"http://vivoweb.org/ontology/core#";

	private TreeMap<String, List<OWLClass>> cls_map = new TreeMap<String, List<OWLClass>>();
	private TreeMap<String, List<OWLObjectProperty>> objprop_map = new TreeMap<String, List<OWLObjectProperty>>();
	private TreeMap<String, List<OWLDataProperty>> datatypeprop_map = new TreeMap<String, List<OWLDataProperty>>();
	private TreeMap<String, List<OWLAnnotationProperty>>  annotprop_map = new TreeMap<String, List<OWLAnnotationProperty>>();
	private TreeMap<String, List<OWLNamedIndividual>> indprop_map = new TreeMap<String, List<OWLNamedIndividual>>();
	private List<Ontology> ontologyList = null;
	private OWLOntology owlOntology = null;
	private Map<String, List<String>> map = new HashMap<String, List<String>>();
	private Map<String, String> lodeMap = null;
	private String [] namespaces = {BIBO, C4O, CITO, DC, EVENT, FABIO, FOAF, GEO, OBO, OCRE_RSCH, 
			OCRE_SD, OCRE_SP, OCRE_STAT, RO, SKOS, SOFTWARE, VANN, VCARD, VITRO, VIVOCORE};

	private String LODE_FILE = null;
	private String ONTOLOGY_VERSION_FILE = null;
	private String ONTOLOGY_FILE = null;
	private String OUTPUT_FILE = null;

	public static void main(String args[]){
		OwlOntologyModelsBuilder obj = new OwlOntologyModelsBuilder();
		try {
			obj.runProcess();
		} catch (OWLOntologyCreationException | ParserConfigurationException | SAXException | IOException e) {
			e.printStackTrace();
		}
	}

	public void runProcess() throws ParserConfigurationException, SAXException, IOException, OWLOntologyCreationException{
		setLocalDirectories();
		lodeMap = LODEPageSourceReader.readLODEClasses(new File(LODE_FILE));
		processontologyversionfile(ONTOLOGY_VERSION_FILE);
		processFile(ONTOLOGY_FILE);
	}


	private void setLocalDirectories() {
		ONTOLOGY_FILE = Configuration.INPUT_FOLDER +"/"+ Configuration.date +"/"+Configuration.ONTOLOGY_FILENAME;
		ONTOLOGY_VERSION_FILE = Configuration.INPUT_FOLDER +"/"+ Configuration.date +"/"+Configuration.ONTOLOGY_VER_CSV_FILE;
		LODE_FILE = Configuration.INPUT_FOLDER +"/"+ Configuration.date +"/"+Configuration.LODE_FILE;
		OUTPUT_FILE = Configuration.OUTPUT_FOLDER +"/"+ Configuration.date +"/"+Configuration.OUTPUT_MODULES_FILENAME;
	}

	private void processontologyversionfile(String filePath) {
		CSVReader reader;
		try {
			reader = new CSVReader(new FileReader(new File(filePath)),',','\"');
			String [] nextLine;	
			reader.readNext();  // header
			while ((nextLine = reader.readNext()) != null) {
				List<String> list = new ArrayList<String>();
				list.add(nextLine[0].trim());
				list.add(nextLine[1].trim());
				list.add(nextLine[2].trim());
				list.add(nextLine[3].trim());
				map.put(nextLine[0].trim(), list);
			}
			reader.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println(map.size());
	}

	private void processFile(String filePath) throws OWLOntologyCreationException {
		ontologyList = new ArrayList<Ontology>();
		OWLOntologyManager m = OWLManager.createOWLOntologyManager();
		IRI inputDocumentIRI = IRI.create(new File(filePath));
		owlOntology = m.loadOntologyFromOntologyDocument(inputDocumentIRI);

		System.out.println("Total Axioms: "+owlOntology.getAxiomCount());

		for(String ns: namespaces){
			createClassMap(ns, owlOntology);
			createObjectPropertyMap(ns, owlOntology);
			createDatatypePropertyMap(ns, owlOntology);
			creatAnnotationPropertyMap(ns, owlOntology);
			createIndividualMap(ns, owlOntology);
		}

		saveDataInJSONFile();

	}

	private void saveDataInJSONFile() {
		String code ="";
		for(String ns: namespaces){
			code ="";
			switch(ns){
			case BIBO:	 	code = "bibo"; 		break;
			case C4O: 		code = "c4o";		break;
			case CITO: 		code = "cito";		break;
			case DC: 		code = "dc";		break;
			case EVENT: 	code = "event";		break;
			case FABIO: 	code = "fabio";		break;
			case FOAF: 		code = "foaf"; 		break;
			case GEO: 		code = "geo"; 		break;
			case OBO: 		code = "obo"; 		break;
			case OCRE_RSCH: code = "ocre_rsch"; break;
			case OCRE_SD: 	code = "ocre_sd"; 	break;
			case OCRE_SP: 	code = "ocre_sp"; 	break;
			case OCRE_STAT: code = "ocre_stat"; break;
			case RO: 		code = "ro"; 		break;
			case SKOS: 		code = "skos";		break;
			case SOFTWARE: 	code = "software";	break;
			case VANN: 		code = "vann";		break;
			case VCARD: 	code = "vcard";		break;
			case VITRO: 	code = "vitro";		break;
			case VIVOCORE:	code = "vivocore";	break;
			}
			if(code.isEmpty()) continue;

			List<OWLClass> clsList = cls_map.get(ns);
			List<OWLObjectProperty> objList = objprop_map.get(ns);
			List<OWLDataProperty> dataList= datatypeprop_map.get(ns);
			List<OWLAnnotationProperty> annotList = annotprop_map.get(ns);
			List<OWLNamedIndividual> indvList= indprop_map.get(ns);
			saveData(ns, code, clsList, objList, dataList, annotList, indvList);
		}

		try {
			createJSONFile(ontologyList, OUTPUT_FILE);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void createJSONFile(Collection<Ontology> collection, String filePath) throws JsonGenerationException, JsonMappingException, IOException{
		ObjectMapper mapper = new ObjectMapper();
		String jsonInString = null;
		mapper.writeValue(new File(filePath), collection);
		jsonInString = mapper.writeValueAsString(collection);
		//System.out.println(jsonInString);
	}

	private void saveData(String namespace, String code, List<OWLClass> cList, List<OWLObjectProperty> oList, List<OWLDataProperty> dList, List<OWLAnnotationProperty> aList, List<OWLNamedIndividual> iList) {
		Ontology ont = new Ontology();

		List<String> metadata = map.get(code);
		if(metadata != null){
			ont.setCode(metadata.get(0));
			ont.setOntologyName(metadata.get(1));
			ont.setNamespace(metadata.get(2));
			ont.setOntologyVersion(metadata.get(3));
		}

		for(OWLClass cls : cList){
			String label = getRDFLabel(cls.getIRI());
			if(label == null || label.length() == 0){
				label = getIRIFragment(cls.getIRI());
			}
			String uri = cls.getIRI().toString();
			String lodeLink = getLODELink(cls.getIRI());
			Entity e = new Entity(label, uri, lodeLink);
			ont.addClass(e);
		}
		if(ont.getClasses()!= null && ont.getClasses().size() > 0){
			Collections.sort(ont.getClasses(), new Comparator<Entity>() {
				public int compare(Entity o1, Entity o2) {
					return o1.getLabel().compareTo(o2.getLabel());
				}
			});
		}

		for(OWLObjectProperty objProp : oList){
			String label = getRDFLabel(objProp.getIRI());
			if(label == null || label.length() == 0){
				label = getIRIFragment(objProp.getIRI());
			}
			String uri = objProp.getIRI().toString();
			String lodeLink = getLODELink(objProp.getIRI());
			Entity e = new Entity(label, uri, lodeLink);
			ont.addObjProperty(e);
		}
		if(ont.getObjProperties()!= null && ont.getObjProperties().size() > 0){
			Collections.sort(ont.getObjProperties(), new Comparator<Entity>() {
				public int compare(Entity o1, Entity o2) {
					return o1.getLabel().compareTo(o2.getLabel());
				}
			});
		}

		for(OWLDataProperty dataProp : dList){
			String label = getRDFLabel(dataProp.getIRI());
			if(label == null || label.length() == 0){
				label = getIRIFragment(dataProp.getIRI());
			}
			String uri = dataProp.getIRI().toString();
			String lodeLink = getLODELink(dataProp.getIRI());
			Entity e = new Entity(label, uri, lodeLink);
			ont.addDataProperty(e);
		}
		if(ont.getDataProperties()!= null && ont.getDataProperties().size() > 0){
			Collections.sort(ont.getDataProperties(), new Comparator<Entity>() {
				public int compare(Entity o1, Entity o2) {
					return o1.getLabel().compareTo(o2.getLabel());
				}
			});
		}

		for(OWLAnnotationProperty aProp : aList){
			String label = getRDFLabel(aProp.getIRI());
			if(label == null || label.length() == 0){
				label = getIRIFragment(aProp.getIRI());
			}
			String uri = aProp.getIRI().toString();
			String lodeLink = getLODELink(aProp.getIRI());
			Entity e = new Entity(label, uri, lodeLink);
			ont.addAnnotationProperty(e);
		}
		if(ont.getAnnotProperties()!= null && ont.getAnnotProperties().size() > 0){
			Collections.sort(ont.getAnnotProperties(), new Comparator<Entity>() {
				public int compare(Entity o1, Entity o2) {
					return o1.getLabel().compareTo(o2.getLabel());
				}
			});
		}

		for(OWLNamedIndividual ind : iList){
			String label = getRDFLabel(ind.getIRI());
			if(label == null || label.length() == 0){
				label = getIRIFragment(ind.getIRI());
			}
			String uri = ind.getIRI().toString();
			String lodeLink = getLODELink(ind.getIRI());
			Entity e = new Entity(label, uri, lodeLink);
			ont.addIndividual(e);
		}
		if(ont.getIndividuals()!= null && ont.getIndividuals().size() > 0){
			Collections.sort(ont.getIndividuals(), new Comparator<Entity>() {
				public int compare(Entity o1, Entity o2) {
					return o1.getLabel().compareTo(o2.getLabel());
				}
			});
		}

		ontologyList.add(ont);
	}

	private String getLODELink(IRI iri) {
		String href = lodeMap.get(iri.toString());
		if (href == null){
			System.err.println("No LODE LINK: "+iri.toString());
		}
		return href!=null?href:"";
	}

	private String getRDFLabel(IRI iri){
		String label = "";
		for(OWLAnnotationAssertionAxiom a : owlOntology.getAnnotationAssertionAxioms(iri)) {
			if(a.getProperty().isLabel()) {
				if(a.getValue() instanceof OWLLiteral) {
					OWLLiteral val = (OWLLiteral) a.getValue();
					label = val.getLiteral();
					//System.out.println(val.getLiteral().toUpperCase());
				}
			}
		}
		return label.toUpperCase();
	}

	private String getIRIFragment(IRI iri){
		String fragment = "";
		String iriStr =iri.toString();
		if(iriStr.contains("#")){
			fragment = (iriStr.substring(iriStr.lastIndexOf('#')+1));
		}else{
			fragment = (iriStr.substring(iriStr.lastIndexOf('/')+1));
		}  

		//TODO Making spacing between camel case capitalized fragments

		return fragment.toUpperCase();
	}

	private void createClassMap(String namespace, OWLOntology ontology) {
		Set<OWLClass> iris = new HashSet<OWLClass>();
		for (OWLClass cl : ontology.getClassesInSignature()) {
			assert cl != null;
			if(cl.getIRI().toString().startsWith(namespace)){
				iris.add(cl);
			}
		}
		List<OWLClass> list = new ArrayList<OWLClass>(iris);
		cls_map.put(namespace, list);
		//		System.out.println(namespace);
		//		for(OWLClass tt : list){
		//			System.out.print(tt.getIRI().toString()+", ");
		//		}
		//		System.out.println("\n\n");
	}


	private void createObjectPropertyMap(String namespace, OWLOntology ontology) {
		Set<OWLObjectProperty> iris = new HashSet<OWLObjectProperty>();
		for (OWLObjectProperty objprop : ontology.getObjectPropertiesInSignature()) {
			assert objprop != null;
			if(objprop.getIRI().toString().startsWith(namespace)){
				iris.add(objprop);
			}
		}
		List<OWLObjectProperty> list = new ArrayList<OWLObjectProperty>(iris);
		objprop_map.put(namespace, list);

		//		System.out.println(namespace);
		//		for(OWLObjectProperty tt : list){
		//			System.out.print(tt.getIRI().toString());
		//		}
		//		System.out.println("\n\n");
	}


	private void creatAnnotationPropertyMap(String namespace, OWLOntology ontology) {
		Set<OWLAnnotationProperty> iris = new HashSet<OWLAnnotationProperty>();
		for (OWLAnnotationProperty annotprop : ontology.getAnnotationPropertiesInSignature()) {
			assert annotprop != null;
			if(annotprop.getIRI().toString().startsWith(namespace)){
				iris.add(annotprop);
			}
		}
		List<OWLAnnotationProperty> list = new ArrayList<OWLAnnotationProperty>(iris);
		annotprop_map.put(namespace, list);
	}


	private void createDatatypePropertyMap(String namespace, OWLOntology ontology) {
		Set<OWLDataProperty> iris = new HashSet<OWLDataProperty>();
		for (OWLDataProperty dataprop : ontology.getDataPropertiesInSignature()) {
			assert dataprop != null;
			if(dataprop.getIRI().toString().startsWith(namespace)){
				iris.add(dataprop);
			}
		}
		List<OWLDataProperty> list = new ArrayList<OWLDataProperty>(iris);
		datatypeprop_map.put(namespace, list);

		System.out.println(namespace);
		//		for(OWLDataProperty tt : list){
		//			//System.out.print(tt.getIRI().toString()+", ");
		//		}
		//		System.out.println("\n\n");
	}

	private void createIndividualMap(String namespace, OWLOntology ontology) {
		Set<OWLNamedIndividual> iris = new HashSet<OWLNamedIndividual>();
		for (OWLNamedIndividual indv : ontology.getIndividualsInSignature()) {
			assert indv != null;
			if(indv.getIRI().toString().startsWith(namespace)){
				iris.add(indv);
			}
		}
		List<OWLNamedIndividual> list = new ArrayList<OWLNamedIndividual>(iris);
		indprop_map.put(namespace, list);

		//		System.out.println(namespace);
		//		for(OWLNamedIndividual tt : list){
		//			System.out.print(tt.getIRI().toString()+", ");
		//		}
		//		System.out.println("\n\n");
	}



}
