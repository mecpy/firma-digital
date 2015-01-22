package org.datosparaguay.servlet;

import java.io.IOException;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;

/**
 * Servlet para generación de archivo JNLP
 * 
 * @author Nahuel Hernández
 *
 */
public class JnlpServlet extends HttpServlet {
	public void service(ServletRequest req, ServletResponse res)  throws ServletException, IOException {  
		HttpServletRequest request = (HttpServletRequest) req;
		res.setContentType("application/x-java-jnlp-file");
		request.getRequestDispatcher("/app.jsp").include(request, res); 
	}
}
