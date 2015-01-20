package org.datosparaguay;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import org.datosparaguay.FirmaDigital.RESULTADOS;
import org.datosparaguay.FirmaDigital.RESULTADOS_TOKEN;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.itextpdf.text.pdf.security.DigestAlgorithms;
import com.itextpdf.text.pdf.security.MakeSignature.CryptoStandard;

/**
 * Aplicación de firmado gráfica
 * 
 * @author Nahuel Hernández
 *
 */
public class App implements Runnable {
	private static String OS = System.getProperty("os.name").toLowerCase();
	private static String LTMP = "/tmp/";
	private static String TMP_FILE_NAME = "signed_%d.pdf";
	
	private static String DDL = "C:\\Windows\\System32\\aetpkss1.dll";
	private static String LIB_SO = "/usr/lib/libaetpkss.so.3.0";
	
	private String destino;
	
	/* Parametros */
	private static String ARG_URL = "url";
	private static String ARG_PRIMERA_FIRMA = "primera-firma";
	private static String ARG_BLOQUEAR = "bloquear";
	private static String ARG_CANTIDAD_FIRMAS = "cantidad-firmas";
	private static String ARG_URL_SUBIDA = "url-out";
	private static String ARG_APP_PARAM = "app-param";
	private static String ARG_CAMPO_FIRMA = "campo-firma";
	
	private static String ARG_UP_SUCCESS = "success";
	
	private String mensaje = "";
	
	private Display display;
	private Shell shell;
	private Text textIn;
	private Text textPin;
	private Label lblOK;
	private Text textOK;
	private Label lblNotOK;
	private Text textNotOK;
	private ProgressBar bar;
	private Button btnFirmar;
	
	private List<JSONParameterObject> params = new ArrayList<JSONParameterObject>();
	private ResourceBundle messages;
	private int argLength;
	
	private static final String CLASS_TO_LOAD = "sun.security.pkcs11.SunPKCS11";
	private FirmaDigital firmaDigital;
	
	
	public Display getDisplay() {
		return display;
	}

	public App(ResourceBundle m, String[] arg) {
		messages = m;
		argLength = arg.length;
		params = new ArrayList<JSONParameterObject>();
		JSONParameterObject param = null;

		for (int i = 0; i < argLength; i++) {
			param = JSONParameterObject.valueOf(arg[i]);
			if (param != null) {
				params.add(param);
			}
		}
	}
	
	public void run() {
		display = new Display();
		shell = new Shell(display);
		

		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 14;
		gridLayout.makeColumnsEqualWidth = true;
		gridLayout.marginWidth = 24;
		gridLayout.marginHeight = 24;
		//gridLayout.marginLeft = 30;

		shell.setLayout(gridLayout);
		shell.setSize(440, 220);
		shell.setText(messages.getString("window.main.title"));

		GridData gridData0 = new GridData(SWT.LEFT, SWT.CENTER, false, false, 8, 1);
		Label lblNewLabel = new Label(shell, SWT.NONE);
		lblNewLabel.setText(messages.getString("archivos.cantidad"));
		lblNewLabel.setLayoutData(gridData0);
		
		// entrada
		GridData gridData1 = new GridData(SWT.FILL, SWT.CENTER, false, false, 6, 1);
		textIn = new Text(shell, SWT.BORDER);
		textIn.setToolTipText("");
		textIn.setEditable(false);
		textIn.setEnabled(false);
		textIn.setLayoutData(gridData1);
		textIn.setText(new Integer(params.size()).toString());

		
		// label pin
		GridData gridDat3 = new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1);
		
		Label lblClave = new Label(shell, SWT.NONE);
		lblClave.setText(messages.getString("label.pin"));
		lblClave.setLayoutData(gridDat3);
		
		// pin
		GridData gridDat4 = new GridData(SWT.FILL, SWT.CENTER, false, false, 6, 1);
		textPin = new Text(shell, SWT.PASSWORD | SWT.BORDER);
		textPin.setLayoutData(gridDat4);
		
		textPin.addListener(SWT.Traverse, new Listener() {
			@Override
			public void handleEvent(Event event) {
				if (event.detail == SWT.TRAVERSE_RETURN) {
					btnFirmar.notifyListeners(SWT.Selection, new Event());
				}
			}
		});

		GridData gridDat5 = new GridData(SWT.FILL, SWT.CENTER, false, false, 6, 1);
		btnFirmar = new Button(shell, SWT.NONE);
		btnFirmar.setLayoutData(gridDat5);
		btnFirmar.setText(messages.getString("boton.firmar"));
		
		
		btnFirmar.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (textPin.getText() == null
						|| textPin.getText().trim().length() == 0) {
						MessageDialog dialog = new MessageDialog(
								shell,
								messages.getString("window.error"),
								null,
								messages.getString("error.pin.vacio"),
								MessageDialog.ERROR, new String[] { messages.getString("boton.aceptar") }, 0);
						dialog.open();
						
				} else {
					//String PASSWORD = "1234";
					final String PASSWORD = textPin.getText();
					
					Runnable runnable = new Runnable() {
				
						public void run() {
							
							lockFieldAndButton();
							
							RESULTADOS_TOKEN resultadoInicializacion = firmaDigital.initToken(PASSWORD);
							
							switch (resultadoInicializacion) {
							case INICIALIZADO:
								//hacer visible la barra de progreso
								setVisibleProgressBar();
								//textIn.setText(firmaDigital.getAlias());
								break;
								
							case ALGORITMO_ENCRIPTACION:
								unlockFieldAndButton();
								openDialog("window.warning", "error.encriptacion.algoritmo", MessageDialog.WARNING, "boton.aceptar");
								return;
								
							case DISPOSITIVO_NO_ENCONTRADO:
								unlockFieldAndButton();
								openDialog("window.warning", "error.token.ausente", MessageDialog.WARNING, "boton.aceptar");
								return;
								
							case KEYSTORE_PROBLEMAS:
								unlockFieldAndButton();
								openDialog("window.warning", "error.keystore", MessageDialog.WARNING, "boton.aceptar");
								return;
							/*	
							case PIN_LONGITUD_INCORRECTA:
								unlockFieldAndButton();
								openDialog("window.warning", "error.pin.longitud", MessageDialog.WARNING, "boton.aceptar");
								return;
							*/
							case PIN_LONGITUD_INCORRECTA:
							case PIN_INCORRECTO:
								unlockFieldAndButton();
								openDialog("window.warning", "error.pin.incorrecto", MessageDialog.WARNING, "boton.aceptar");
								return;
								
							case PIN_INVALIDO:
								unlockFieldAndButton();
								openDialog("window.warning", "error.pin.invalido", MessageDialog.WARNING, "boton.aceptar");
								return;
								
							case PROVIDER_PROBLEMAS:
								unlockFieldAndButton();
								openDialog("window.warning", "error.provider", MessageDialog.WARNING, "boton.aceptar");
								return;
							
							default:
								unlockFieldAndButton();
								openDialog("window.warning", "error.inicializar.general", MessageDialog.WARNING, "boton.aceptar");
								return;
							}
							
							RESULTADOS firmado = firmarArchivos();
							
							switch (firmado) {
							case FIRMADO_TOTAL_SINGULAR:
								openDialog("window.success", "firma.exitosa", MessageDialog.INFORMATION, "boton.aceptar");
								break;
								
							case FIRMADO_TOTAL_PLURAL:
								openDialog("window.success", "firmas.exitosas", MessageDialog.INFORMATION, "boton.aceptar");
								break;
							
							case FIRMADO_PARCIAL:
								openDialog("window.result", "firmas.parcial", MessageDialog.WARNING, "boton.aceptar");
								break;
							
							case NINGUNA_FIRMA:
								openDialog("window.error", "firmas.ninguna", MessageDialog.WARNING, "boton.aceptar");
								break;
								
							default:
								break;
							}
							
						}
					
					};
					new Thread(runnable).start();
				}
			}
		});
		
		// salida
		GridData gridData6 = new GridData(SWT.FILL, SWT.CENTER, false, false, 14, 1);
		bar = new ProgressBar(shell, SWT.SMOOTH);
		bar.setSelection(0);
		bar.setEnabled(false);
		bar.setLayoutData(gridData6);
		
		//hacer invisible la barra de progreso
		bar.setVisible(false);
		
		GridData gridData7 = new GridData(SWT.LEFT, SWT.CENTER, false, false, 8, 1);
		lblOK = new Label(shell, SWT.NONE);
		lblOK.setText(messages.getString("archivos.ok"));
		lblOK.setLayoutData(gridData7);
		lblOK.setVisible(false);
		
		// entrada
		GridData gridData8 = new GridData(SWT.FILL, SWT.CENTER, false, false, 6, 1);
		textOK = new Text(shell, SWT.BORDER);
		textOK.setEditable(false);
		textOK.setEnabled(false);
		textOK.setLayoutData(gridData8);
		textOK.setText("0");
		textOK.setVisible(false);
		
		GridData gridData9 = new GridData(SWT.LEFT, SWT.CENTER, false, false, 8, 1);
		lblNotOK = new Label(shell, SWT.NONE);
		lblNotOK.setText(messages.getString("archivos.not.ok"));
		lblNotOK.setLayoutData(gridData9);
		lblNotOK.setVisible(false);
		
		// entrada
		GridData gridData10 = new GridData(SWT.FILL, SWT.CENTER, false, false, 6, 1);
		textNotOK = new Text(shell, SWT.BORDER);
		textNotOK.setEditable(false);
		textNotOK.setEnabled(false);
		textNotOK.setLayoutData(gridData10);
		textNotOK.setText("0");
		textNotOK.setVisible(false);
		
		//establecer icono
		try {
			final Image small = new Image(shell.getDisplay(),
					getClass().getResource("/images/small.png").openStream());
			final Image large = new Image(shell.getDisplay(),
					getClass().getResource("/images/large.png").openStream());
			final Image[] images = new Image[] { small, large };
			shell.setImages(images);
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("No se carg\u00F3 el icono ");
		}
		
		shell.open();
		
		boolean sunClassNotFound = false;
		try {
			Class loadedClass = Class.forName(CLASS_TO_LOAD);
			System.out.println("Se carg\u00F3 la clase " + loadedClass);
		}
		catch (ClassNotFoundException ex) {
			sunClassNotFound = true;
			System.out.println("No se pudo cargar carg\u00F3 la clase " + CLASS_TO_LOAD + "usar jre de 32 bits para java 1.6 y 1.7 en Windows" );
			ex.printStackTrace();
		}
		
		if (sunClassNotFound) {
			openDialog("window.error", "error.no.se.puede.firmar", messages.getString("error.clase.sun.mensaje"), MessageDialog.ERROR, "boton.aceptar");
			lockFieldAndButton();
		} else {
			firmaDigital = new FirmaDigital();
			if (!defineOS()) {
				openDialog("window.error", "error.sistema.no.soportado", MessageDialog.ERROR, "boton.aceptar");
				lockFieldAndButton();
			}
		}
		
		if (argLength == 0) {
			openDialog("window.error", "error.parametros.ninguno", MessageDialog.ERROR, "boton.aceptar");
			lockFieldAndButton();
		} else if (params.size() == 0) {
			openDialog("window.error", "error.parametros", MessageDialog.ERROR, "boton.aceptar");
			lockFieldAndButton();
		} else if (params.size() < argLength) {
			openDialog("window.warning", "error.parametros.algunos", MessageDialog.WARNING, "boton.aceptar");
		}
		
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}
		display.dispose();
	}

	public synchronized void lockFieldAndButton() {
		if (display == null || display.isDisposed())
			return;
		
		display.asyncExec(new Runnable() {
			public void run() {
				if (!shell.isDisposed()) {
					btnFirmar.setEnabled(false);
					textPin.setEnabled(false);
				}
			}
		});
	}
	
	public synchronized void unlockFieldAndButton() {
		if (display == null || display.isDisposed())
			return;
		
		display.asyncExec(new Runnable() {
			public void run() {
				if (!shell.isDisposed()) {
					btnFirmar.setEnabled(true);
					textPin.setEnabled(true);
				}
			}
		});
	}
	
	public synchronized void setVisibleProgressBar() {
		if (display == null || display.isDisposed())
			return;
		
		display.asyncExec(new Runnable() {
			public void run() {
				if (!shell.isDisposed()) {
					bar.setVisible(true);
					bar.setEnabled(true);
				}
			}
		});
	}
	
	public synchronized void updateProgressBar(final int selection) {
		if (display == null || display.isDisposed())
			return;
		
		display.asyncExec(new Runnable() {
			public void run() {
				if (!shell.isDisposed()) {
					bar.setSelection(selection);
				}
			}
		});
	}
	
	public synchronized void openDialog(final String titleKey, final String bodyKey, final int messageDialogValue, final String buttonKey) {
		if (display == null || display.isDisposed())
			return;
		
		display.asyncExec(new Runnable() {
			public void run() {
				if (!shell.isDisposed()) {
					MessageDialog dialog;
					dialog = new MessageDialog(shell, 
							messages.getString(titleKey),
							null, 
							messages.getString(bodyKey),
							messageDialogValue,
							new String[] { messages.getString(buttonKey) }, 0);
					dialog.open();
				}
			}
		});
	}
	
	public synchronized void openDialog(final String titleKey, final String bodyKey, final String message, final int messageDialogValue, final String buttonKey) {
		if (display == null || display.isDisposed())
			return;
		
		display.asyncExec(new Runnable() {
			public void run() {
				if (!shell.isDisposed()) {
					MessageDialog dialog;
					dialog = new MessageDialog(shell, 
							messages.getString(titleKey),
							null, 
							messages.getString(bodyKey) + message,
							messageDialogValue,
							new String[] { messages.getString(buttonKey) }, 0);
					dialog.open();
				}
			}
		});
	}
	
	public synchronized void showResultFields(final String cantFirmados, final String cantNoFirmados) {
		if (display == null || display.isDisposed())
			return;
		
		display.asyncExec(new Runnable() {
			public void run() {
				if (!shell.isDisposed()) {
					bar.setSelection(100);
					textOK.setText(cantFirmados);
					lblOK.setVisible(true);
					textOK.setVisible(true);
					textNotOK.setText(cantNoFirmados);
					lblNotOK.setVisible(true);
					textNotOK.setVisible(true);
					//shell.setSize(440, 210);
				}
			}
		});
	}
	
	public boolean defineOS() {
		 
		System.out.println(OS);
		boolean supported = true;
		
		if (isWindows()) {
			System.out.println("Usando Windows");
			firmaDigital.setDriverPath(DDL);
			
			destino = System.getProperty("java.io.tmpdir"); //WTMP;
		} else if (isUnix()) {
			System.out.println("Usando Unix o Linux");
			firmaDigital.setDriverPath(LIB_SO);
			
			destino = LTMP;
		} else if (isMac()) {
			System.out.println("Usando Mac");
			//TODO drivers Mac
			 supported = false;
		} else {
			System.out.println("Sistema operativo no soportado");
			 supported = false;
		}
		
		return supported;
	}
	
	public static boolean isWindows() {
		return (OS.indexOf("win") >= 0);
	}
 
	public static boolean isMac() {
		return (OS.indexOf("mac") >= 0);
	}
 
	public static boolean isUnix() {
		return (OS.indexOf("nix") >= 0 || OS.indexOf("nux") >= 0 || OS.indexOf("aix") > 0 );
	}

	
	private RESULTADOS firmarArchivos() {
		Integer cantFirmados = 0;

		try {
			String urlPdf = "";
			String urlDestino = "";
			
			Integer numberTmpFile = 0;
			String tmpFileDestino;
			JSONParameterObject jsonResponse;
			
			int part = 100 / params.size();
			for (JSONParameterObject param : params) {
				tmpFileDestino = destino + String.format(TMP_FILE_NAME, numberTmpFile);
				
				Boolean primeraFirma = (Boolean) param.getObject().get(ARG_PRIMERA_FIRMA);
				//System.out.println("primera firma " + primeraFirma);
				Boolean bloquear = (Boolean) param.getObject().get(ARG_BLOQUEAR);
				Integer cantidadFirmas = (Integer) param.getObject().get(ARG_CANTIDAD_FIRMAS);
				//System.out.println("cantidad firmas " + cantidadFirmas);
				urlPdf = (String) param.getObject().get(ARG_URL);
				urlDestino = (String) param.getObject().get(ARG_URL_SUBIDA);
				String fieldName = (String) param.getObject().get(ARG_CAMPO_FIRMA);
				
				JSONParameterObject appParam = new JSONParameterObject();
				
				boolean excepcion = false;
				try {
					appParam.setObject((Map<String,Object>) param.getObject().get(ARG_APP_PARAM));
					
					firmaDigital.signWithToken(urlPdf, tmpFileDestino,
							DigestAlgorithms.SHA256, CryptoStandard.CMS,
							primeraFirma.booleanValue(), bloquear.booleanValue(), fieldName, cantidadFirmas);
					
					mensaje = firmaDigital.verify(tmpFileDestino);
				} catch (ClassCastException e) {
					e.printStackTrace();
					mensaje = messages.getString("error.parametros");
					excepcion = true;
				} catch (Exception e) {
					e.printStackTrace();
					mensaje = e.getMessage();
					excepcion = true;
				}
				
				if (excepcion) {
					openDialog("window.error", "firmas.problema.no.identificado", mensaje, MessageDialog.WARNING, "boton.aceptar");
 				} else if ("OK".compareTo(mensaje) == 0) {
					System.out.println("Archivo firmado con exito: ");
					//se sube el archivo
					String success = "true";
					boolean deleteTmpFile = false;
					String response = HttpClientUtils.postFileToUrl(urlDestino, tmpFileDestino, success, appParam.toString(), deleteTmpFile);
					System.out.println("Subida response: " + response);
					jsonResponse = JSONParameterObject.valueOf(response);
					
					Boolean upSuccess = (Boolean) jsonResponse.getObject().get(ARG_UP_SUCCESS);
					if (upSuccess.booleanValue()) {
						cantFirmados++;
					} else {
						openDialog("window.warning", "firmas.problema.upload", MessageDialog.WARNING, "boton.aceptar");
					}
				} else {
					System.out.println("Problemas: " + mensaje);
					openDialog("window.warning", "firmas.integridad", mensaje, MessageDialog.WARNING, "boton.aceptar");
				}
				
				numberTmpFile++;
				updateProgressBar(part);
			}
			
			showResultFields(cantFirmados.toString(), new Integer(params.size() - cantFirmados).toString());
		} catch (Exception e) {
			e.printStackTrace();
			//throw new RuntimeException(e);
		}
		
		if (cantFirmados == 0) {
			return RESULTADOS.NINGUNA_FIRMA;
		} else if ((cantFirmados == params.size())) {
			if (params.size() == 1) {
				return RESULTADOS.FIRMADO_TOTAL_SINGULAR;
			} else {
				return RESULTADOS.FIRMADO_TOTAL_PLURAL;
			}
		} else {
			return RESULTADOS.FIRMADO_PARCIAL;
		}
	}
}