package org.datosparaguay;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Security;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import org.bouncycastle.asn1.x500.RDN;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.asn1.x500.style.IETFUtils;
import org.bouncycastle.cert.jcajce.JcaX509CertificateHolder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import sun.security.pkcs11.SunPKCS11;
import sun.security.pkcs11.wrapper.CK_C_INITIALIZE_ARGS;
import sun.security.pkcs11.wrapper.CK_TOKEN_INFO;
import sun.security.pkcs11.wrapper.PKCS11;
import sun.security.pkcs11.wrapper.PKCS11Exception;

import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Font;
import com.itextpdf.text.Font.FontFamily;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.log.LoggerFactory;
import com.itextpdf.text.log.SysoLogger;
import com.itextpdf.text.pdf.AcroFields;
import com.itextpdf.text.pdf.PdfAnnotation;
import com.itextpdf.text.pdf.PdfFormField;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfSigLockDictionary;
import com.itextpdf.text.pdf.PdfSigLockDictionary.LockPermissions;
import com.itextpdf.text.pdf.PdfSignatureAppearance;
import com.itextpdf.text.pdf.PdfStamper;
import com.itextpdf.text.pdf.security.BouncyCastleDigest;
import com.itextpdf.text.pdf.security.CertificateInfo;
import com.itextpdf.text.pdf.security.CrlClient;
import com.itextpdf.text.pdf.security.ExternalDigest;
import com.itextpdf.text.pdf.security.ExternalSignature;
import com.itextpdf.text.pdf.security.MakeSignature;
import com.itextpdf.text.pdf.security.MakeSignature.CryptoStandard;
import com.itextpdf.text.pdf.security.OcspClient;
import com.itextpdf.text.pdf.security.OcspClientBouncyCastle;
import com.itextpdf.text.pdf.security.PdfPKCS7;
import com.itextpdf.text.pdf.security.PrivateKeySignature;

/**
 * Contiene la implementación de la firma Digital
 * 
 * @author Nahuel Hernández
 *
 */
public class FirmaDigital {

	private X509Certificate signCert = null;
	private String driverPath;
	private KeyStore ks;
	private PrivateKey pk;
	private SunPKCS11 providerPKCS11;
	private Certificate[] chain;
	private List<CrlClient> crlList;
	private OcspClient ocspClient;
	private String alias;
	private boolean initialized = false;
	
	// propiedades del field de firma
	private static float LLY = 752; // punto B1
	private static float LLX = 350; // punto B2
	private static float URY = 780; // punto D1
	private static float URX = 550; // punto D2
	private static String DEFAULT_FIELD_NAME = "firma";
	
	private static float SEPARATION = 2;
	

	/** Tipos de resultado. */
	public enum RESULTADOS_FIRMA {
		FIRMADO, NO_FIRMADO, ERROR_PIN;
	}
	
	public enum RESULTADOS {
		FIRMADO_TOTAL_SINGULAR, FIRMADO_TOTAL_PLURAL, FIRMADO_PARCIAL, NINGUNA_FIRMA;
	}
	
	public enum RESULTADOS_TOKEN {
		PIN_INVALIDO, PIN_INCORRECTO, PIN_LONGITUD_INCORRECTA, PROVIDER_PROBLEMAS, DISPOSITIVO_NO_ENCONTRADO, INICIALIZADO, ALGORITMO_ENCRIPTACION, KEYSTORE_PROBLEMAS;
	}
	
	
	public String getAlias() {
		return alias;
	}

	public void setAlias(String alias) {
		this.alias = alias;
	}

	public String getDriverPath() {
		return driverPath;
	}

	public void setDriverPath(String driverPath) {
		this.driverPath = driverPath;
	}
	
	public RESULTADOS_TOKEN initToken(String password) {
		boolean result = true;
		initialized = false;
		LoggerFactory.getInstance().setLogger(new SysoLogger());
		
		char[] pass = password.toCharArray();
		//String pkcs11cfg = properties.getProperty("PKCS11CFG");
		
		String DLL = getDriverPath();
		String config;
		//try {
		try {
			config = "name = EFIRMA\n"
			+ "library = " + DLL + "\n"
			+ "slot = " //+ "0" + "\n"
			+ getSlotsWithTokens(DLL)[0] + "\n";
			//+ "showInfo = true";
			System.out.println("------ config \n" + config);
		} catch (Exception e) {
			e.printStackTrace();
			return RESULTADOS_TOKEN.DISPOSITIVO_NO_ENCONTRADO;
		}
		
		ByteArrayInputStream bais = new ByteArrayInputStream(config.getBytes());
			
		providerPKCS11 = new SunPKCS11(bais);
			
		if (-1 == Security.addProvider(providerPKCS11)) {
			result = false;
			//throw new RuntimeException("could not add security provider");
		}
	
		BouncyCastleProvider providerBC = new BouncyCastleProvider();
		Security.addProvider(providerBC);
	
		try {
			ks = KeyStore.getInstance("PKCS11");
		} catch (KeyStoreException e) {
			e.printStackTrace();
			//providerPKCS11.logout();
			return RESULTADOS_TOKEN.PROVIDER_PROBLEMAS;
		}
		
		try {
			ks.load(null, pass);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			return RESULTADOS_TOKEN.ALGORITMO_ENCRIPTACION;
		} catch (CertificateException e) {
			e.printStackTrace();
			return RESULTADOS_TOKEN.KEYSTORE_PROBLEMAS;
		} catch (IOException e) {
			e.printStackTrace();
			
			Throwable tr = e;
			
			while (tr.getCause() != null) {
				tr = tr.getCause();
			}
			
			String errorCode = tr.getMessage();
			switch (errorCode) {
			case "CKR_PIN_LEN_RANGE":
				System.out.println("CKR_PIN_LEN_RANGE");
				return RESULTADOS_TOKEN.PIN_LONGITUD_INCORRECTA;
			case "CKR_PIN_INVALID":
				System.out.println("CKR_PIN_INVALID");
				return RESULTADOS_TOKEN.PIN_INVALIDO;
			case "CKR_PIN_INCORRECT":
				System.out.println("CKR_PIN_INCORRECT");
				return RESULTADOS_TOKEN.PIN_INCORRECTO;
			default:
				if (errorCode.contains("CKR_PIN")) {
					return RESULTADOS_TOKEN.PIN_INVALIDO;
				}
				break;
			}
			
			return RESULTADOS_TOKEN.DISPOSITIVO_NO_ENCONTRADO;
		}
			
		Enumeration<String> aliases;
		try {
			aliases = ks.aliases();
		
			alias = "";
				
			while (aliases.hasMoreElements()) {
				alias = (String) aliases.nextElement();
				System.out.println("------ alias " + alias);
				signCert = (X509Certificate) ks.getCertificate(alias);
	
				if (signCert.getKeyUsage()[0]) {
					System.out.println("------ break " + alias);
					break;
				}
			}
			
			chain = ks.getCertificateChain(alias);
		} catch (KeyStoreException e) {
			e.printStackTrace();
			return RESULTADOS_TOKEN.KEYSTORE_PROBLEMAS;
		}
		
		try {
			pk = (PrivateKey) ks.getKey(alias, pass);
		} catch (UnrecoverableKeyException e) {
			e.printStackTrace();
			return RESULTADOS_TOKEN.KEYSTORE_PROBLEMAS;
		} catch (KeyStoreException e) {
			e.printStackTrace();
			return RESULTADOS_TOKEN.KEYSTORE_PROBLEMAS;
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			return RESULTADOS_TOKEN.ALGORITMO_ENCRIPTACION;
		}
		

		ocspClient = new OcspClientBouncyCastle();
			
		/*
		TSAClient tsaClient = null;
		for (int i = 0; i < chain.length; i++) {
			X509Certificate cert = (X509Certificate) chain[i];
			String tsaUrl = CertificateUtil.getTSAURL(cert);
			if (tsaUrl != null) {
				tsaClient = new TSAClientBouncyCastle(tsaUrl);
				break;
			}
		}
		*/
			
		crlList = new ArrayList<CrlClient>();
		//crlList.add(new CrlClientOnline(chain));
		
		initialized = true;
		
		return RESULTADOS_TOKEN.INICIALIZADO;
	}
	
	public static long[] getSlotsWithTokens(String libraryPath)
			throws IOException {
		CK_C_INITIALIZE_ARGS initArgs = new CK_C_INITIALIZE_ARGS();
		String functionList = "C_GetFunctionList";
		initArgs.flags = 0;
		PKCS11 tmpPKCS11 = null;
		long[] slotList = null;
		try {
			try {
				tmpPKCS11 = PKCS11.getInstance(libraryPath, functionList,
						initArgs, false);
			} catch (IOException ex) {
				ex.printStackTrace();
				throw ex;
			}
		} catch (PKCS11Exception e) {
			try {
				initArgs = null;
				tmpPKCS11 = PKCS11.getInstance(libraryPath, functionList,
						initArgs, true);
			} catch (IOException ex) {
				ex.printStackTrace();
			} catch (PKCS11Exception ex) {
				ex.printStackTrace();
			}
		}
		try {
			slotList = tmpPKCS11.C_GetSlotList(true);
			for (long slot : slotList) {
				CK_TOKEN_INFO tokenInfo = tmpPKCS11.C_GetTokenInfo(slot);
				System.out.println("slot: " + slot + "\nmanufacturerID: "
						+ String.valueOf(tokenInfo.manufacturerID)
						+ "\nmodel: " + String.valueOf(tokenInfo.model));
			}
		} catch (PKCS11Exception ex) {
			ex.printStackTrace();
		} catch (Throwable t) {
			t.printStackTrace();
		}
		return slotList;
	}
	
	public String addSignFields(InputStream src, String dest, String fieldName, Integer cantidadFirmas) 
			throws IOException, DocumentException {
		PdfReader reader = new PdfReader(src);
		PdfStamper stamper = new PdfStamper(reader, new FileOutputStream(dest));
		String newFieldName = null;
	
		int cantidad = cantidadFirmas == null ? 0 : cantidadFirmas.intValue();
		for (int firmas = cantidad; firmas > 0; firmas--) {
			// create a signature form field
			PdfFormField field = PdfFormField.createSignature(stamper.getWriter());
			newFieldName = fieldName + "_" + firmas;
			field.setFieldName(newFieldName);
			// set the widget properties
			float lly = 742 - ((firmas - 1) * 40);
			float ury = 780 - ((firmas - 1) * 40);
			field.setWidget(new Rectangle(380, lly, 550, ury), PdfAnnotation.HIGHLIGHT_OUTLINE);
			field.setFlags(PdfAnnotation.FLAGS_PRINT);
			// add the annotation
			stamper.addAnnotation(field, 1);
		}
	
		// close the stamper
		stamper.close();
		return newFieldName;
	}
	public void signWithToken(String src, String dest,
			String digestAlgorithm,
			CryptoStandard subfilter, 
			boolean firstSigner, boolean lock, String fieldName, Integer cantidadFirmas)
			throws GeneralSecurityException, IOException, DocumentException {
		if (!initialized) {
			return;
		}
		InputStream srcStream = HttpClientUtils.getFileFromUrl(src);
		
		File srcFile = null;
		if (firstSigner && cantidadFirmas != null && fieldName == null) {
			String fileWithFieldSignature = dest + "_temp";
			
			fieldName = addSignFields(srcStream, fileWithFieldSignature, DEFAULT_FIELD_NAME, cantidadFirmas);
			
			srcFile = new File(fileWithFieldSignature);
			srcStream = new FileInputStream(srcFile);
		}
		
		boolean useTempFile = false;
		signVisible(srcStream, dest, chain, pk, digestAlgorithm,
				providerPKCS11.getName(), subfilter, ocspClient, crlList,
				fieldName, firstSigner, lock,
				useTempFile);
		
		//borrar archivo temporal
		if (firstSigner && cantidadFirmas != null && fieldName == null) {
			try {
				if (srcFile.delete()) {
					System.out.println(srcFile.getName() + " borrado");
				} else {
					System.out.println("No se pudo borrar el archivo temporal: " + srcFile.getName());
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
	}
	
	public void sign(InputStream src, String dest, Certificate[] chain,
			PrivateKey pk, String digestAlgorithm, String provider,
			CryptoStandard subfilter, boolean firstSigner, boolean lock, OcspClient ocsp, 
			List<CrlClient> crlList, boolean useTmpFile)
			throws GeneralSecurityException, IOException, DocumentException {

		// Creating the reader and the stamper
		PdfReader reader = new PdfReader(src);
		FileOutputStream os = new FileOutputStream(dest);
		PdfStamper stamper;
		
		if (useTmpFile) {
			String tmp = "/tmp/stamper_tmp.pdf";
			stamper = PdfStamper.createSignature(reader, os, '\0', new File(tmp));
		} else {
			stamper = PdfStamper.createSignature(reader, os, '\0');
		}
		
		// Creating the appearance
		PdfSignatureAppearance appearance = stamper.getSignatureAppearance();
		
		if (lock) {
			appearance
					.setCertificationLevel(PdfSignatureAppearance.CERTIFIED_NO_CHANGES_ALLOWED);
		} else if (firstSigner) {
			appearance
			.setCertificationLevel(PdfSignatureAppearance.CERTIFIED_FORM_FILLING);
		} else {
			appearance
			.setCertificationLevel(PdfSignatureAppearance.NOT_CERTIFIED);
		}
		// Creating the signature
		ExternalDigest digest = new BouncyCastleDigest();
		ExternalSignature signature = new PrivateKeySignature(pk,
				digestAlgorithm, provider);
		MakeSignature.signDetached(appearance, digest, signature, chain, crlList,
				ocsp, null, 0, subfilter);
	}
	
	public void signVisible(InputStream src, String dest, Certificate[] chain,
			PrivateKey pk, String digestAlgorithm, String provider,
			CryptoStandard subfilter, OcspClient ocsp,
			List<CrlClient> crlList, String fieldName, boolean firstSigner, boolean lock, boolean useTmpFile)
			throws GeneralSecurityException, IOException, DocumentException {
		// Creating the reader and the stamper
		PdfReader reader = new PdfReader(src);
		FileOutputStream os = new FileOutputStream(dest);
		
		PdfStamper stamper;
		if (useTmpFile && firstSigner) {
			String tmp = dest + "__stamper_tmp";
			stamper = PdfStamper.createSignature(reader, os, '\0', new File(tmp));
		} else if (firstSigner) {
			stamper = PdfStamper.createSignature(reader, os, '\0');
		} else if (useTmpFile) {
			String tmp = dest + "__stamper_tmp";
			stamper = PdfStamper.createSignature(reader, os, '\0', new File(tmp), true);
		} else {
			stamper = PdfStamper.createSignature(reader, os, '\0', null, true);
		}
		
		// Creating the appearance
		PdfSignatureAppearance appearance = stamper.getSignatureAppearance();
		PdfSigLockDictionary lockDic = null;
		if (lock && firstSigner) {
			appearance
			.setCertificationLevel(PdfSignatureAppearance.CERTIFIED_NO_CHANGES_ALLOWED);
		} else if (lock) {
			appearance
			.setCertificationLevel(PdfSignatureAppearance.NOT_CERTIFIED);
			lockDic = new PdfSigLockDictionary(LockPermissions.NO_CHANGES_ALLOWED);
			stamper.getWriter().addToBody(lockDic).getIndirectReference();
		} else if (firstSigner) {
			appearance
			.setCertificationLevel(PdfSignatureAppearance.CERTIFIED_FORM_FILLING);
		} else {
			appearance
			.setCertificationLevel(PdfSignatureAppearance.NOT_CERTIFIED);
		}
		
		//appearance.setReason(reason);
		//appearance.setLocation(location);
		
		//numero de campos de firmas existentes
		if (fieldName == null) {
			AcroFields af = reader.getAcroFields();
			ArrayList<String> names = af.getSignatureNames();
			int firmas = names.size();
			System.out.println("Cantidad de firmas: " + firmas);
			String newFieldName = DEFAULT_FIELD_NAME + "_" + (firmas + 1);
			
			float lly = LLY - (firmas * (URY - LLY - SEPARATION));
			float ury = URY - (firmas * (URY - LLY - SEPARATION));
			appearance.setVisibleSignature(new Rectangle(LLX, lly, URX, ury), 1, newFieldName);
		
			//PdfFormField field = PdfFormField.createSignature(stamper.getWriter());
			//fieldName = "firma_" + (firmas + 1);
			//field.setFieldName(fieldName);

			//field.setWidget(new Rectangle(LLX, lly, URX, ury), PdfAnnotation.HIGHLIGHT_OUTLINE);
			//field.setFlags(PdfAnnotation.FLAGS_PRINT);
			// add the annotation
			//stamper.addAnnotation(field, 1);
		
		} else {
			appearance.setVisibleSignature(fieldName);
		}

		String firmante = "Firmante";
		//for (Certificate cert : chain) {
			//System.out.println("Certificate is: " + cert);
		if (chain.length > 0) {
			Certificate cert = chain[0];
			if (cert instanceof X509Certificate) {
				X509Certificate x = (X509Certificate) cert;
				X500Name x500name = new JcaX509CertificateHolder(x).getSubject();
				RDN o = x500name.getRDNs(BCStyle.O)[0];
				firmante = IETFUtils.valueToString(o.getFirst().getValue());
			}
		}
		//}
		
		appearance.setLayer2Text("Firmado por: " + firmante);
		float fontSize = 7f;
		appearance.setLayer2Font(new Font(FontFamily.TIMES_ROMAN, fontSize));
		
		// Creating the signature
		ExternalDigest digest = new BouncyCastleDigest();
		ExternalSignature signature = new PrivateKeySignature(pk,
				digestAlgorithm, provider);

		MakeSignature.signDetached(appearance, digest, signature, chain, crlList,
				ocsp, null, 0, subfilter);
		
		reader.close();
		stamper.close();
	}
	
	public String verify(String documentoFirmado) throws IOException, GeneralSecurityException{
		PdfReader reader = new PdfReader(documentoFirmado);
		AcroFields af = reader.getAcroFields();
		ArrayList<String> names = af.getSignatureNames();
		String mensaje = "";
		boolean ok = true;
		for (String name : names) {
			PdfPKCS7 pk = af.verifySignature(name);
			if (!pk.verify()) {
				ok = false;
				mensaje = mensaje
						+ "\n"
						+ CertificateInfo.getSubjectFields(pk
								.getSigningCertificate());
			}
			System.out.println(name + " - Verificaci\u00F3n de integridad OK? " + pk.verify());
		}
		
		if (ok) {
			mensaje = "OK";
		}
		
		reader.close();
		return mensaje;
	}
	
}
