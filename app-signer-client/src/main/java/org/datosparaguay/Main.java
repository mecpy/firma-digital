package org.datosparaguay;

import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Clase principal que llama a la UI. Es la llamada por la aplcación Web Start
 * 
 * @author Nahuel Hernández
 *
 */
public class Main {

	public static void main(String[] arg) throws Exception {
		System.out.println("+++ main " );
		if (arg.length > 0) {
			System.out.println("+++ arg0" + arg[0]);
		}
		
		Locale currentLocale = new Locale("es", "PY");
		ResourceBundle messages = ResourceBundle.getBundle("messages", currentLocale);
		
		String json = "{\"url\":\"http://127.0.0.1:8088/app-signer-web/pdfs/resolucion.pdf\",\"primera-firma\":true,\"bloquear\":false,\"url-out\":\"http://127.0.0.1:8088/app-signer-web/upload\",\"app-param\":{\"id\":1}}";
		String json2 = "{\"url\":\"http://127.0.0.1:8088/app-signer-web/pdfs/firmado_cc_wf.pdf\",\"primera-firma\":false,\"bloquear\":true,\"url-out\":\"http://127.0.0.1:8088/app-signer-web/upload\",\"campo-firma\":\"firma_2\",\"app-param\":{\"id\":1}}";
		String json3 = "{\"url\":\"http://127.0.0.1:8088/app-signer-web/pdfs/firmado_cc.pdf\",\"primera-firma\":false,\"bloquear\":false,\"url-out\":\"http://127.0.0.1:8088/app-signer-web/upload\",\"app-param\":{\"id\":1}}";
		String json4 = "{\"url\":\"http://127.0.0.1:8088/app-signer-web/pdfs/firmado_sc.pdf\",\"primera-firma\":false,\"bloquear\":false,\"url-out\":\"http://127.0.0.1:8088/app-signer-web/upload\",\"app-param\":{\"id\":1}}";
		String json5 = "{\"url\":\"http://127.0.0.1:8088/app-signer-web/pdfs/firmado_cc.pdf\",\"primera-firma\":false,\"bloquear\":false,\"url-out\":\"http://127.0.0.1:8088/app-signer-web/upload\",\"app-param\":{\"id\":1}}";

		String [] args = {json, json2, json3, json4, json5};
		
		final App gui = new App(messages, arg);
		//final App gui = new App(messages, args);
		
		Thread t = new Thread(gui);
		t.start();

	}
}