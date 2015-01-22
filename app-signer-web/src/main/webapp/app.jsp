<%@ page import="java.util.Map" language="java" contentType="application/x-java-jnlp-file; UTF-8" pageEncoding="UTF-8"%>
<?xml version="1.0" encoding="UTF-8"?>
<jnlp spec="1.0+" codebase=<%= "\"" + request.getScheme() + "://"+ request.getServerName() + ":" + request.getServerPort() + request.getContextPath() + "/\"" + " href=\"app.jnlp" + (request.getQueryString() == null ? "\"" : "?" + request.getQueryString() + "\"") %>  >
  <information>
   <title>App Signer</title>
   <vendor>Datos Paraguay</vendor>
  </information>
  <security>
   <all-permissions/>
  </security>
  <resources>
    <j2se href="http://java.sun.com/products/autodl/j2se" initial-heap-size="32m" max-heap-size="512m" version="1.6+"/>
    <property name="jnlp.versionEnabled" value="false"/>
    <property name="SWT_GTK3" value="0"/>
    
    <jar href="lib/itextpdf-5.5.3.jar"/>
    <jar href="lib/bcpkix-jdk15on-1.51.jar"/>
    <jar href="lib/bcprov-jdk15on-1.51.jar"/>
    
    <jar href="lib/jackson-annotations-2.4.0.jar"/>
    <jar href="lib/jackson-core-2.4.4.jar"/>
    <jar href="lib/jackson-databind-2.4.4.jar"/>
    
    <jar href="lib/httpclient-4.3.6.jar"/>
    <jar href="lib/httpcore-4.3.3.jar"/>
    <jar href="lib/httpmime-4.3.6.jar"/>
    <jar href="lib/commons-logging-1.1.3.jar"/>
    <jar href="lib/commons-codec-1.6.jar"/>
    
    <jar href="lib/org.eclipse.ui.workbench-3.7.1.v20120104-1859.jar"/>
    <jar href="lib/org.eclipse.jface-3.7.0.v20110928-1505.jar"/>
    <jar href="lib/org.eclipse.equinox.common-3.6.0.v20110523.jar"/>
    <jar href="lib/org.eclipse.core.commands-3.6.0.I20110111-0800.jar"/>
    <jar href="lib/org.eclipse.osgi-3.7.2.v20120110-1415.jar"/>
  </resources>
  
  <resources arch="x86" os="Windows">
    <jar href="lib/org.eclipse.swt.win32.win32.x86-4.3.2.jar"/>
    <jar href="lib/app-signer-client-windows-x86.jar" main="true"/>
  </resources>
  <resources arch="x86_64" os="Windows">
    <jar href="lib/org.eclipse.swt.win32.win32.x86_64-4.3.2.jar"/>
    <jar href="lib/app-signer-client-windows-x86_64.jar" main="true"/>
  </resources>
  <resources arch="amd64" os="Windows">
    <jar href="lib/org.eclipse.swt.win32.win32.x86_64-4.3.2.jar"/>
    <jar href="lib/app-signer-client-windows-x86_64.jar" main="true"/>
  </resources>
  <resources arch="x86_64" os="Linux">
    <jar href="lib/org.eclipse.swt.gtk.linux.x86_64-4.3.2.jar"/>
    <jar href="lib/app-signer-client-linux-x86_64.jar" main="true"/>
  </resources>
  <resources arch="amd64" os="Linux">
    <jar href="lib/org.eclipse.swt.gtk.linux.x86_64-4.3.2.jar"/>
    <jar href="lib/app-signer-client-linux-x86_64.jar" main="true"/>
  </resources>
  <resources arch="x86" os="Linux">
    <jar href="lib/org.eclipse.swt.gtk.linux.x86-4.3.2.jar"/>
    <jar href="lib/app-signer-client-linux-x86.jar" main="true"/>
  </resources>

  
  <application-desc main-class="org.datosparaguay.Main">
  <%
 	Map<String, String[]> requestParams = request.getParameterMap();

 	for (Map.Entry<String, String[]> entry : requestParams.entrySet()) {
 		String key = entry.getKey(); // parameter name
 		String[] value = entry.getValue(); // parameter values as array of String
 		String valueString = "";
		
 		if (key.compareTo("param") == 0) {
 			out.write("    ");
 			for (int i = 0; i < value.length; i++) {
 				out.write("<argument>" + value[i] + "</argument>\n    ");
 			} 
 		}
 		
 		if (value.length > 1) {
 			for (int i = 0; i < value.length; i++) {
 				valueString += value[i] + " ";
 			}
 		} else {
 			valueString = value[0];
 		}
 		
 		//System.out.println("***** " + key + " - " + valueString);
 	}
 %>
  </application-desc>
  <update check="background"/>
</jnlp>