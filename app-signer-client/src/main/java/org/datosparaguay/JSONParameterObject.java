package org.datosparaguay;


import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Clase utilitaria para tratar cadenas JSON
 * 
 * @author Nahuel Hernández
 *
 */
public class JSONParameterObject {
	private Map<String, Object> object;
	
	public JSONParameterObject() {
		super();
		this.object = new HashMap<String, Object>();
	}

	public static JSONParameterObject valueOf(String json) {
		JSONParameterObject root = null;
		ObjectMapper mapper = new ObjectMapper();

		try {
			root = new JSONParameterObject();
			Map<String, Object> parsed = (Map<String, Object>) mapper.readValue(json,
					new TypeReference<Map<String, Object>>() {
					});
			
			root.setObject(parsed);
		} catch (Exception e) {
			root = null;
			e.printStackTrace();
		}

		return root;
	}

	public String toString() {
		ObjectMapper mapper = new ObjectMapper();
		StringWriter json = new StringWriter();

		try {
			mapper.writeValue(json, this.object);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return json.toString();
	}
	
	public Map<String, Object> getObject() {
		return object;
	}

	public void setObject(Map<String, Object> object) {
		this.object = object;
	}
		
}
