package org.w3id.scholarlydata.clodg;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NamespaceChanger {

	public String change(String context, String nsIn, String nsOut){
		return context.replaceAll(nsIn, nsOut);
	}
	
	public static void main(String[] args) {
		
		NamespaceChanger namespaceChanger = new NamespaceChanger();
		File folder = new File("/Library/WebServer/Documents/scholarlydata.org/website/dumps/conferences/alignments");
		for (File file : folder.listFiles(new FileFilter() {
			
			@Override
			public boolean accept(File pathname) {
				return pathname.getName().endsWith(".rdf");
			}
		})){
			StringBuilder content = new StringBuilder();
			
			BufferedReader reader = null;
			try {
				reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));
				
				String line = null;
				while((line = reader.readLine()) != null){
					if(content.length() > 0)
						content.append('\n');
					content.append(line);
				}
			} catch (UnsupportedEncodingException | FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			if(reader != null){
				try {
					reader.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
			String rdf = namespaceChanger.change(content.toString(), "http://www.scholarlydata.org/", "https://w3id.org/scholarlydata/");
			
			
			try {
				Writer writer = new OutputStreamWriter(new FileOutputStream(file), "UTF-8");
				writer.write(rdf);
				writer.close();
				
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
	}
	
}
