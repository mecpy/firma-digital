package org.datosparaguay.servlet;

import java.io.IOException;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;

public class JnlpServlet extends HttpServlet {
	public void service(ServletRequest req, ServletResponse res)  throws ServletException, IOException {  
		HttpServletRequest request = (HttpServletRequest) req;
		res.setContentType("application/x-java-jnlp-file");
		request.getRequestDispatcher("/app.jsp").include(request, res); 

		/*
		Map<String, String[]> requestParams = request.getParameterMap();

		StringBuilder sb = new StringBuilder();
		for (Map.Entry<String, String[]> entry : requestParams.entrySet()) {
			String key = entry.getKey();         // parameter name
			String[] value = entry.getValue();   // parameter values as array of String
			String valueString = "";
			
			if (value.length > 1) {
				for (int i = 0; i < value.length; i++) {
					valueString += value[i] + " ";
				}
			} else {
				valueString = value[0];
			}
			System.out.println("***** " + key + " - " + valueString);
			sb.append(key).append(" - ").append(valueString).append("; ");
		}
		*/
	}
}
