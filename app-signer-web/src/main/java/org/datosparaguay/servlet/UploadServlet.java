/**
 * 
 */
package org.datosparaguay.servlet;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

/**
 * Servlet para subida de archivos firmados
 * 
 * @author Nahuel Hernández
 *
 */
@SuppressWarnings("serial")
@WebServlet(name = "UploadServlet", urlPatterns = { "/upload" })
@MultipartConfig(fileSizeThreshold = 1024 * 1024 * 2, // 2MB
				maxFileSize = 1024 * 1024 * 10, // 10MB
				maxRequestSize = 1024 * 1024 * 50)// 50MB
public class UploadServlet extends HttpServlet {
	
	private final static Logger LOGGER = Logger.getLogger(UploadServlet.class
			.getCanonicalName());

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("text/html;charset=UTF-8");

		// Create path components to save the file
		final String appParam = request.getParameter("app-param");
		final String path = System.getProperty("java.io.tmpdir");
		final Boolean success = Boolean.valueOf(request.getParameter("success"));
		final PrintWriter writer = response.getWriter();
		
		if (success.booleanValue()) {
			final Part filePart = request.getPart("archivo");
			//final String fileName = getFileName(filePart);
			
			OutputStream out = null;
			InputStream filecontent = null;
			

			try {
				String fileName = new SimpleDateFormat("yyyyMMddhhmmss'_signed.pdf'").format(new Date());
				out = new FileOutputStream(new File(path + File.separator + fileName));
				
				filecontent = filePart.getInputStream();

				int read = 0;
				final byte[] bytes = new byte[1024];

				while ((read = filecontent.read(bytes)) != -1) {
					out.write(bytes, 0, read);
				}
				
				writer.println("{\"success\":true}");
				LOGGER.log(Level.INFO, "Archivo {0} subido a {1} con parametros recibidos {2}", new Object[] { fileName, path, appParam });
				
			} catch (Exception e) {
				writer.println("{\"success\":false}");
				e.printStackTrace();
				LOGGER.log(Level.SEVERE, "Problemas subiendo el archivo: {0}",
						new Object[] { e.getMessage() });
			} finally {
				if (out != null) {
					out.close();
				}
				if (filecontent != null) {
					filecontent.close();
				}
				if (writer != null) {
					writer.close();
				}
			}
		}
		
	}

	private String getFileName(final Part part) {
		final String partHeader = part.getHeader("content-disposition");
		LOGGER.log(Level.INFO, "Part Header = {0}", partHeader);
		for (String content : part.getHeader("content-disposition").split(";")) {
			if (content.trim().startsWith("filename")) {
				return content.substring(content.indexOf('=') + 1).trim()
						.replace("\"", "");
			}
		}
		return null;
	}
}
