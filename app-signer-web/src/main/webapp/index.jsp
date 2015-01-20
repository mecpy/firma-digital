<html>
<head>
<title>Sign with Web Start</title>
</head>
<body>

<h3>App signer Ejemplos</h3>
Para firmar documentos acceda a: 
<a href=<%= "\"" + request.getScheme() + "://"+ request.getServerName() + ":" + request.getServerPort() + request.getContextPath()  + "/app.jnlp" + (request.getQueryString() == null ? "\"" : "?" + request.getQueryString() + "\"") %> >app.jnlp</a>
<br>
Ejemplo 1: Un solo archivo, primera firma - 
<a href=<%= "\"" + request.getScheme() + "://"+ request.getServerName() + ":" + request.getServerPort() + request.getContextPath()  + "/app.jnlp?param={%22url%22:%22" + request.getScheme() + "://"+ request.getServerName() + ":" + request.getServerPort() + request.getContextPath() + "/pdfs/resolucion.pdf%22,%22primera-firma%22:true,%22bloquear%22:false,%22url-out%22:%22" + request.getScheme() + "://"+ request.getServerName() + ":" + request.getServerPort() + request.getContextPath() + "/upload%22,%22app-param%22:{%22id%22:1}}\"" %> >app.jnlp</a>
<br>
Ejemplo 2: Un solo archivo, primera firma, bloquear otras firmas - 
<a href=<%= "\"" + request.getScheme() + "://"+ request.getServerName() + ":" + request.getServerPort() + request.getContextPath()  + "/app.jnlp?param={%22url%22:%22" + request.getScheme() + "://"+ request.getServerName() + ":" + request.getServerPort() + request.getContextPath() + "/pdfs/resolucion.pdf%22,%22primera-firma%22:true,%22bloquear%22:true,%22url-out%22:%22" + request.getScheme() + "://"+ request.getServerName() + ":" + request.getServerPort() + request.getContextPath() + "/upload%22,%22app-param%22:{%22id%22:1}}\"" %> >app.jnlp</a>
<br>
Ejemplo 3: Dos archivos - 
<a href=<%= "\"" + request.getScheme() + "://"+ request.getServerName() + ":" + request.getServerPort() + request.getContextPath()  + "/app.jnlp?param={%22url%22:%22" + request.getScheme() + "://"+ request.getServerName() + ":" + request.getServerPort() + request.getContextPath() + "/pdfs/resolucion.pdf%22,%22primera-firma%22:true,%22bloquear%22:false,%22url-out%22:%22" + request.getScheme() + "://"+ request.getServerName() + ":" + request.getServerPort() + request.getContextPath() + "/upload%22,%22app-param%22:{%22id%22:1}}&param={%22url%22:%22" + request.getScheme() + "://"+ request.getServerName() + ":" + request.getServerPort() + request.getContextPath() + "/pdfs/firmado_sc.pdf%22,%22primera-firma%22:false,%22bloquear%22:false,%22url-out%22:%22" + request.getScheme() + "://"+ request.getServerName() + ":" + request.getServerPort() + request.getContextPath() + "/upload%22,%22app-param%22:{%22id%22:1}}\"" %> >app.jnlp</a>
<br>
Ejemplo 4: Firmar documento ya firmado, firma adicional en campo de firma presente en el documento - 
<a href=<%= "\"" + request.getScheme() + "://"+ request.getServerName() + ":" + request.getServerPort() + request.getContextPath()  + "/app.jnlp?param={%22url%22:%22" + request.getScheme() + "://"+ request.getServerName() + ":" + request.getServerPort() + request.getContextPath() + "/pdfs/firmado_cc_wf.pdf%22,%22primera-firma%22:false,%22bloquear%22:false,%22url-out%22:%22" + request.getScheme() + "://"+ request.getServerName() + ":" + request.getServerPort() + request.getContextPath() + "/upload%22,%22campo-firma%22:%22firma_2%22,%22app-param%22:{%22id%22:1}}\"" %> >app.jnlp</a>
<br>
Ejemplo 5: Firmar documento ya firmado, firma adicional sin campo de firma especificado - 
<a href=<%= "\"" + request.getScheme() + "://"+ request.getServerName() + ":" + request.getServerPort() + request.getContextPath()  + "/app.jnlp?param={%22url%22:%22" + request.getScheme() + "://"+ request.getServerName() + ":" + request.getServerPort() + request.getContextPath() + "/pdfs/firmado_cc.pdf%22,%22primera-firma%22:false,%22bloquear%22:false,%22url-out%22:%22" + request.getScheme() + "://"+ request.getServerName() + ":" + request.getServerPort() + request.getContextPath() + "/upload%22,%22app-param%22:{%22id%22:1}}\"" %> >app.jnlp</a>
<br>
Ejemplo 6: Firmar documento ya firmado y bloquear - 
<a href=<%= "\"" + request.getScheme() + "://"+ request.getServerName() + ":" + request.getServerPort() + request.getContextPath()  + "/app.jnlp?param={%22url%22:%22" + request.getScheme() + "://"+ request.getServerName() + ":" + request.getServerPort() + request.getContextPath() + "/pdfs/firmado_cc.pdf%22,%22primera-firma%22:false,%22bloquear%22:true,%22url-out%22:%22" + request.getScheme() + "://"+ request.getServerName() + ":" + request.getServerPort() + request.getContextPath() + "/upload%22,%22app-param%22:{%22id%22:1}}\"" %> >app.jnlp</a>
<br>
<br>
</body>
</html>
