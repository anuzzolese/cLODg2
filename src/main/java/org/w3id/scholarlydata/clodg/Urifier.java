package org.w3id.scholarlydata.clodg;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import de.fuberlin.wiwiss.d2rq.values.Translator;

public class Urifier implements Translator {
	
	public static Map<String, String> map = new HashMap<String, String>();

	public static String toURI(String label) {
		// remove accents
		label = StringUtils.stripAccents(label);
		// lowercase:
		label = label.toLowerCase();
		// remove various characters:
		// '
		label = label.replaceAll("[\\']", "");
		// replace various characters with whitespace:
		// - + ( ) . , & " / ??? !
		label = label.replaceAll("[;.,&\\\"???!]", "");
		// squeeze whitespace to dashes:
		label = label.replaceAll("[ \\/]", "-");
		
		label = label.replaceAll("[\\(\\)]", "");

		label = label.replaceAll("\\-$", "");
        
        label = label.replaceAll("(\\-)+", "-");

		try {
			
			/*
			String[] labelParts = label.split("\\/");
			StringBuilder sb = new StringBuilder();
			for(String labelPart : labelParts){
				if(sb.length() > 0) sb.append("/");
				sb.append(URLEncoder.encode(labelPart, "UTF-8"));
			}
			return sb.toString();
			*/
			return URLEncoder.encode(label, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			return label;
		}
	}

	@Override
	public String toDBValue(String value) {
		return map.get(value);
	}

	@Override
	public String toRDFValue(String value) {
		
		String namespace = "http://data.semanticweb.org/";
		
		value = value.replace(namespace, "");
		
		int index = value.indexOf("/") + 1;
		namespace += value.substring(0, index);
		String localname = value.substring(index);
		
		String uri = namespace + toURI(localname);
		map.put(uri, namespace + localname);
		
		return uri;
	}

	
}
