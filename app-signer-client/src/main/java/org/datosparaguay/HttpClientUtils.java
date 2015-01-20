package org.datosparaguay;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

/**
 * Clase para bajar archivos a firmar y subir archivos firmados
 * 
 * @author Nahuel Hernández
 *
 */
public class HttpClientUtils {
	
	private static String ARG_UP_ARCHIVO = "archivo";
	private static String ARG_UP_APP_PARAM = "app-param";
	private static String ARG_UP_SUCCESS = "success";
	
	public static InputStream getFileFromUrl(String urltofetch) {
		HttpClient client = HttpClientBuilder.create().build();
		HttpGet httpget = new HttpGet(urltofetch);
		HttpResponse response = null;
		InputStream inputStream = null;
		
		try {
			response = client.execute(httpget);
			
			if (response != null) {
				HttpEntity entity = response.getEntity();
				if (entity != null) {
					//long len = entity.getContentLength();
					inputStream = entity.getContent();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return inputStream;
	}
	
	public static String postFileToUrl(String urltofetch, String tmpFile, String success, String appParam, boolean deleteTmpFile) {
		HttpClient client = HttpClientBuilder.create().build();
		HttpPost httppost = new HttpPost(urltofetch);

		MultipartEntityBuilder builder = MultipartEntityBuilder.create();
		builder.addTextBody(ARG_UP_SUCCESS, success, ContentType.TEXT_PLAIN);
		builder.addTextBody(ARG_UP_APP_PARAM, appParam, ContentType.TEXT_PLAIN);
		
		String responseBody = "{\"success\":false}";

		File fileToSend = new File(tmpFile);
		FileInputStream fileToSendStream = null;
		try {
			fileToSendStream = new FileInputStream(fileToSend);
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
			return responseBody;
		}
		
		builder.addBinaryBody(ARG_UP_ARCHIVO, fileToSendStream, ContentType.APPLICATION_OCTET_STREAM, fileToSend.getName());
		HttpEntity multipart = builder.build();

		httppost.setEntity(multipart);

		HttpResponse response;
		
		
		try {
			response = client.execute(httppost);
			
			if (response != null) {
				HttpEntity entity = response.getEntity();
				if (entity != null) {
					responseBody = EntityUtils.toString(response.getEntity());
				}
			}
			fileToSendStream.close();
			
			//borrar archivo temporal
			if (deleteTmpFile) {
				try {
					if (fileToSend.delete()) {
						System.out.println(fileToSend.getName() + " borrado");
					} else {
						System.out.println("No se pudo borrar el archivo temporal: " + fileToSend.getName());
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return responseBody;
	}

}
