# **App-Signer**, Firma Digital de Documentos

## ¿Qué es?
Una aplicación Java que utiliza la tecnología Java Web Start para ejecutarse localmente en máquinas cliente que le permite acceso a dispositivos criptográficos (Tokens USB) para firmar digitalmente archivos PDF. Utiliza SWT para renderizar la interfaz gráfica tanto en sistemas Windows como Linux. Utiliza la librería iText para el tratamiento de los archivos PDF y la aplicación de la firma.

## Organización del código
El código está organizado en proyectos Maven.

- *app-signer*, proyecto Maven principal que agrega como módulos los dos siguientes.
- *app-signer-client*, proyecto para generar los JARs que se ejecutarán localmente. Usan un plugin maven para firmar los JARs a ser servidos por la el *app-signer-web*.
- *app-signer-web*, genera un WAR para ser deployado en un servidor de aplicaciones. Contiene un servlet con para generar dinámicamente el descriptor JNLP de la aplicación Java Web Start a partir de los parámetros que recibe en la URL de su invocación. Contiene además los JARs que son especificados en el archivo JNLP para ejecutar la aplicación localmente.

## Requisitos para la ejecución del cliente
### JRE
Para el acceso al dispositivo criptográfico de firmado se utiliza el estándar PKCS#11, que en java es implementado en las librerías de SUNPKCS11, que no están disponibles en todas las versiones del JRE. La aplicación es compatible con las siguientes versiones del JRE Oracle:

- **En Windows**: 1.6, 1.7 de 32 bits, 1.8 de 32 bits y 64 bits.
- **En Linux**: 1.6, 1.7, 1.8 de 32 bits y 64 bits.

Hay que tener en cuenta que la versión 1.8 tiene mayores restricciones de seguridad en lo que respecta a la ejecución de JARs usando la tecnología Java Web Start. Esta versión restringe la ejecución de JARs firmados con un certificado autofirmado, por lo que se debe configurar la plugin maven utilizado para el firmado con un KeyStore que contenga un certificado adecuado.

### Token USB de firmado
La aplicación está configurada para su funcionamiento con los tokens de firmado proporcionados por [eFirma](https://www.efirma.com.py) (de la marca Gemalto). Se necesita que el software middleware esté instalado, así como los drivers para los tokens. El driver instala los siguientes archivos:

- **En Windows**: C:\Windows\System32\aetpkss1.dll
- **En Linux**: /usr/lib/libaetpkss.so.3.0

Los instaladores pueden encontrarse [aquí](https://www.efirma.com.py/kit-de-seguridad-efirma-i14)

## Especificaciones de uso
### URL de invocación
Un archivo JNLP es generado dinámicamente para pasar como argumento del método *Main()* los parámetros pasados por la URL de invocación.
El formato de la url es el siguiente

```
http://dominio:puerto/app.jnlp?param=JSON1&param=JSON2&param=JSON3
```

El parámetro *param* en la URL es un JSON con los parámetros necesarios para llevar a cabo el firmado del archivo. Existe un *param* por archivo a ser firmado.

### Parámetros
Cada parámetro *param* de la URL es un JSON con los siguientes atributos:

Atributo&#160;del&#160;JSON  | Valores      | Descripción                   
---------------- | --------------- | ---------------------------------- 
url              | String          | URL del archivo a firmar 
primera-firma    | Booleano        | Si se firma por primera vez el documento
bloquear         | Booleano        | No permitir más firmas
campo-firma      | String          | (opcional) Nombre del campo de firma en el PDF
url-out          | String          | URL para el upload del archivo firmado. La aplicación realiza un POST a esta URL, y manda el archivo con parámetros adicionales en un request HTTP multipart
app-param        | JSON            | JSON, se pasa como un parámetro más al en url-out

El atributo *campo-firma* puede ser opcional. Si se pasa este parámetro debe existir en el documento un campo de firma con este nombre. Si no se recibe este parámetro se crea un campo de firma en la esquina superior derecha. El campo de firma creado tiene un desplazamiento proporcional a la cantidad de campos de firmas existentes en el documento.
La firma aplicada es visual. Tiene el formato **Firmado por: [O]**, donde *[0]* es el atributo *Organization* del nombre distinguido (DN) del certificado contenido en el Token USB, con el que se realiza la firma. En los certificados emitidos por eFirma, este atributo contiene el nombre natural de la persona dueña del certificado.

La subida de archivos mediante el request HTTP multipart a la URL especificada por el atributo *url-out* posee los siguientes parámetros:

Parámetro        | Valores         | Descripción                        
---------------- | --------------- | ----------------------------------
success          | String          | "true" firmado correctamente / "false" no se pudo firmar el documento
archivo          | Binary Part     | Archivo PDF firmado
app-param        | String          | String que representa el atributo JSON tal cual llegó como atributo de entrada

## Compilación
### JARs
El archivo *pom.xml* del proyecto *app-signer-client* posee cuatro profiles, para realizar la compilación de distintos JARS según la plataforma destino, estos son:

* windows-x86
* windows-x86_64
* linux-x86
* linux-x86_64

se debe realizar el goal package con la especificación de cada uno de los profiles para generar los JARs

```
mvn package -Pprofile
```

Existen dos shell-scripts para generar y copiar los JARs al directorio *webapp/lib/** del proyecto web (para que formen parte del WAR generado). Estos son *generate_jars.sh* y *copy_jars.sh* y se encuentran el directorio root del proyecto *app-signer-client*.

#### Firmado de JARs
Para hacer uso de los JARs usando Java Web Start estos deben estar firmados digitalmente. En el archivo *pom.xml* se especifica la configuración de un plugin maven que realiza la firma de los JARs. Se puede configurar el uso de un certificado específico en un KeyStore para habilitar la aplicación a funcionar con la versión 1.8 del JRE Java, cuyo nivel de seguridad exige que los JARs usados en la aplicación Java Web Start no estén firmados por un certificado auto-firmado. La configuración del plugin presente genera un KeyStore con un certificado 'al vuelo' para firmar los JARs, por lo que no funcionará con una versión 1.8 del JRE Java. Para configurar el plugin con un certificado proporcionado por una CA de confianza, se puede ver [este enlace](http://mojo.codehaus.org/keytool/keytool-maven-plugin/usage.html).

### WAR
Una vez agregados los JARs al directorio *webapp/lib/* se puede generar el WAR para agregarlo a un servidor de aplicaciones como Tomcat o JBoss.

```
mvn package
```
El WAR contiene un Servlet para subida de archivos a modo de ejemplo, que usa anotaciones de Servlet 3.0, por lo que el servidor de aplicaciones debería soportar esta versión de Java Servlet.


    