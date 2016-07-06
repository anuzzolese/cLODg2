package org.w3id.scholarlydata.clodg;
/**
 * Author Andrea Nuzzolese
 */

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.sql.SQLException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Option.Builder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3id.scholarlydata.clodg.hsqldb.CSVLoader;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.util.FileManager;

public class Clodg {

	private static final String CONFIGURATION_FILE = "c";
    private static final String CONFIGURATION_FILE_LONG = "config";
    
    private static final String OUTPUT_FILE = "o";
    private static final String OUTPUT_FILE_LONG = "output";
    
    private static final String INPUT_MODEL = "m";
    private static final String INPUT_MODEL_LONG = "model";
    
    private static final String INPUT_CSV = "i";
    private static final String INPUT_CSV_LONG = "input";
    
    private static final Logger log = LoggerFactory.getLogger(Clodg.class);
	
	public static void main(String[] args) {
		
		/*
         * Set-up the options for the command line parser.
         */
        Options options = new Options();
        
        Builder optionBuilder = Option.builder(CONFIGURATION_FILE);
        Option configurationFileOption = optionBuilder.argName("file")
                                 .hasArg()
                                 .required(true)
                                 .desc("MANDATORY - Input file containing the app configuration.")
                                 .longOpt(CONFIGURATION_FILE_LONG)
                                 .build();
        
        optionBuilder = Option.builder(INPUT_CSV);
        Option inputCsvOption = optionBuilder.argName("folder")
                                 .hasArg()
                                 .required(true)
                                 .desc("MANDATORY - Folder containing the CSV with input data.")
                                 .longOpt(INPUT_CSV_LONG)
                                 .build();
        
        optionBuilder = Option.builder(OUTPUT_FILE);
        Option outputFileOption = optionBuilder.argName("file")
                                 .hasArg()
                                 .required(false)
                                 .desc("OPTIONAL - Output file for the final RDF model. If no value is provided, then system out is used by default.")
                                 .longOpt(OUTPUT_FILE_LONG)
                                 .build();
        
        optionBuilder = Option.builder(INPUT_MODEL);
        Option inputModelOption = optionBuilder.argName("file")
                                 .hasArg()
                                 .required(false)
                                 .desc("OPTIONAL - The path to an input RDF model to merge with the output of cLODg.")
                                 .longOpt(INPUT_MODEL_LONG)
                                 .build();
        
        options.addOption(configurationFileOption);
        options.addOption(inputCsvOption);
        options.addOption(outputFileOption);
        options.addOption(inputModelOption);
        
        CommandLine commandLine = null;
        
        CommandLineParser cmdLineParser = new DefaultParser();
        try {
            commandLine = cmdLineParser.parse(options, args);
        } catch (ParseException e) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp( "process", options );
        }
        
        if(commandLine != null){
        	
        	CSVLoader csvLoader = new CSVLoader();
        	
        	try{
	            
        		String configuration = commandLine.getOptionValue(CONFIGURATION_FILE);
	            String inputCSVFolder = commandLine.getOptionValue(INPUT_CSV);
	            String outputFile = commandLine.getOptionValue(OUTPUT_FILE);
	            String inputModelPath = commandLine.getOptionValue(INPUT_MODEL);
	            
	            if(configuration != null && inputCSVFolder != null){
	            	
	            	File csvFolder = new File(inputCSVFolder);
	            	
	            	//boolean debug = false;
	            	if(csvFolder.exists()){
	            	
	            		File[] csvFiles = csvFolder.listFiles(new FileFilter() {
							
							@Override
							public boolean accept(File file) {
								return file.getName().endsWith("csv");
							}
						});
	            		
	            		try {
	            			//if(!debug)
	            			
	            			log.info("Loading CSV data...");
	            			csvLoader.loadCSV(csvFiles);
	            			log.info("Loading of CSV data finished.");
	            			
						} catch (Exception e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
	            		
		            	LDGenerator datasetLoader = LDGenerator.getInstance();
		            	Model model = datasetLoader.generate(configuration);
		            	
		            	if(inputModelPath != null && !inputModelPath.trim().isEmpty())
		            		model.add(FileManager.get().loadModel(inputModelPath));
		            	
		            	try {
		            		if(outputFile != null && !outputFile.trim().isEmpty())
		            			model.write(new FileOutputStream(new File(outputFile)), "TURTLE");
		            		else model.write(System.out, "TURTLE");
		    			} catch (FileNotFoundException e) {
		    				// TODO Auto-generated catch block
		    				e.printStackTrace();
		    			}
		    			
		    			model.close();
	            	}
	            	else{
	            		System.err.println("The folder " + inputCSVFolder + " does not exist.");
	            	}
	            }
        	} catch(Exception e){
        		e.printStackTrace();
        	} finally {
				try {
					csvLoader.dropDatabase();
					System.out.println("DB dropped.");
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
        }
		
	}
}
