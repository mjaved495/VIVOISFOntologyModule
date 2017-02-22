package edu.cornell.vivo.configuration;

public class Configuration {
	
	public static String date = null;
	
	public static String INPUT_FOLDER = null;
	public static String OUTPUT_FOLDER = null;
	
	
	
	public static String ONTOLOGY_FILENAME = "vivo-isf-public-1.6-fromImport.owl";
	public static String DOMAIN_MODEL_DESCRIPTION_FILENAME = "DomainModelDescription.csv";
	public static String NT_FILES_FOLDERNAME = "ntFiles";
	
	public static String LODE_FILE = "lode.xml";
	public static String ONTOLOGY_VER_CSV_FILE = "ontologyversions.csv";
	
	public static String OUTPUT_MODELS_FILENAME = "models.json";
	public static String OUTPUT_MODULES_FILENAME = "modules.json";
	
	public static void setDate(String date) {
		Configuration.date = date;
	}
	public static void setINPUT_FOLDER(String iNPUT_FOLDER) {
		INPUT_FOLDER = iNPUT_FOLDER;
	}
	public static void setOUTPUT_FOLDER(String oUTPUT_FOLDER) {
		OUTPUT_FOLDER = oUTPUT_FOLDER;
	}
}
