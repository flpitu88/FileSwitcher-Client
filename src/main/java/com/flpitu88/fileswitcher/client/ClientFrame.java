/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.flpitu88.fileswitcher.client;

import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

import com.flpitu88.fileSwitcher.mensajes.MensajeChequeoSocket;
import com.flpitu88.fileSwitcher.mensajes.MensajePresentacion;
import com.flpitu88.fileSwitcher.utilitarios.Archivo;
import com.flpitu88.fileSwitcher.utilitarios.Logueo;

/**
 *
 * @author Flavio L. Pietrolati
 */
public class ClientFrame extends javax.swing.JFrame{
    
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	// Atributos
    private static Socket socket;
    private Logueo logger;
    private static String usuario;
    private boolean conectado = false;
    private javax.swing.JButton bNuevoUser;
    private javax.swing.JButton bGuardar;
    private javax.swing.JButton bRecuperar;
    private javax.swing.JLabel etiqueta1;
    private javax.swing.JProgressBar barraProgreso;
    private javax.swing.JSeparator jSeparador;
    private javax.swing.JScrollPane jBarraDespl;
    private javax.swing.JTextArea estado;
//    public static RegistroUsuario registro = null;
    public static boolean enUso = false;
    
    // Constructor de clase
    public ClientFrame() {
    	this.setResizable(false);

            String pathDirLog = Cliente.obtenerConfiguracion().getProperty("logClien");
            String pathLog = pathDirLog + "LogCliente.log";
            Archivo dirLog = new Archivo(pathDirLog);
            // Si no existe la estructura de directorios la creo
            if (!dirLog.existe()){
            	dirLog.crearDirectorios();
            }

            this.logger = new Logueo(pathLog,"LogCliente.log");
            
            logger.logAmbos(" ---------------------- Inicio hilo de cliente ---------------------");
            this.setConectado(false);
            ClientFrame.socket = null;
            String userAux = Cliente.obtenerUsuario().getProperty("user");
            String titulo = Cliente.obtenerConfiguracion().getProperty("title");

            if (checkUser(userAux)){
            	logger.logAmbos("Chequeo de usuario correcto");
            	usuario = userAux;
            	this.setTitle(titulo);
            	this.setVisible(true);
            } else {
            	logger.logAmbos("Comprobacion de cliente invalida");
            }
            initComponents();
   	
    }
    
    
    // Se inicia la GUI de cliente
    private void initComponents() {
    	
    	bNuevoUser = new javax.swing.JButton();
        bGuardar = new javax.swing.JButton();
        bRecuperar = new javax.swing.JButton();
        barraProgreso = new javax.swing.JProgressBar(0,100);
        jSeparador = new javax.swing.JSeparator();
        etiqueta1 = new javax.swing.JLabel();
        jBarraDespl = new javax.swing.JScrollPane();
        estado = new javax.swing.JTextArea();
        
        Image icon = Toolkit.getDefaultToolkit().getImage(getClass().getResource("icono.jpg"));
    	setIconImage(icon);
    	
    	
    	// Agrego que el boton X cierre ventana y borre temporal de bloqueo
    	this.addWindowListener(new WindowAdapter(){
    		public void windowClosing( WindowEvent evt ) {
    			new Control().cerrarApp(); 
    			System.exit( 0 );
    		}
    	});
    	
        setName("ventana"); // NOI18N

        bNuevoUser.setText("Soy Nuevo");

        bGuardar.setBackground(new java.awt.Color(255, 255, 255));
        bGuardar.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        bGuardar.setText("GUARDAR");
        bGuardar.setName("bGuardar"); // NOI18N
        bGuardar.setEnabled(false);

        bRecuperar.setBackground(new java.awt.Color(255, 255, 255));
        bRecuperar.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        bRecuperar.setText("RECUPERAR");
        bRecuperar.setName("bRecuperar"); // NOI18N
        bRecuperar.setEnabled(true);

        barraProgreso.setName("barraProgreso"); // NOI18N

        etiqueta1.setText("Usuario");
        etiqueta1.setName("etiqueta1"); // NOI18N
        
        estado.setColumns(20);
        estado.setRows(5);
        jBarraDespl.setViewportView(estado);

        estado.setText("(estado)");
        estado.setName("estado"); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jBarraDespl, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(bGuardar, javax.swing.GroupLayout.PREFERRED_SIZE, 184, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(bRecuperar, javax.swing.GroupLayout.PREFERRED_SIZE, 178, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addComponent(barraProgreso, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jSeparador, javax.swing.GroupLayout.Alignment.TRAILING))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(10, 10, 10)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(bGuardar, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(bRecuperar, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(11, 11, 11)
                .addComponent(jBarraDespl, javax.swing.GroupLayout.PREFERRED_SIZE, 83, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(11, 11, 11)
                .addComponent(jSeparador, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(barraProgreso, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
        
   	
        // Agrego los eventos ante la presion del boton Guardar
        bGuardar.addActionListener(new ActionListener(){
        	public void actionPerformed(ActionEvent e){
        		// Lanzo hilo que controla los valores de la barra de progreso
        		new Thread( new BarraThreadGuardar(getBarraProgreso(),logger)).start();
        		// Lanzo hilo que lleva a cabo la tarea de guardar archivos
        		new Thread( new jobGuardar(logger,getFrame(),usuario)).start();
        	}
        });
        // Agrego los eventos ante la presion del boton Recuperar
        bRecuperar.addActionListener(new ActionListener(){
        	public void actionPerformed(ActionEvent e){
        		// Lanzo hilo que controla los valores de la barra de progreso
        		new Thread( new BarraThreadRecuperar(getBarraProgreso(),logger)).start();
        		// Lanzo hilo que lleva a cabo la tarea de guardar archivos
        		new Thread( new jobRecuperar(logger,getFrame(),usuario)).start();
        	}
        });
        
        new Thread(new ChequeoUsuario(logger,getFrame())).start();
        this.envioPresentacionUsuarioConConexion(logger);
//        new Thread(new ChequeoUsuario(logger,getFrame())).start();
    }
    
    /*
	 * Metodo que reemplaza las barras invertidas por las barras
	 * normales utilizadas por el sistema en los paths, para poder
	 * realizar las consultas a la base de la misma manera en la que se
	 * guardan los datos
	 */
	public String cambiarBarraInvertida(String cadena){
        char[] tmp = cadena.toCharArray();
        for (int i=0;i<cadena.length();i++){
            if (cadena.charAt(i) == '\\'){
                tmp[i] = '/';
            }
        }
        String resul = new String(tmp);
        return resul;
    }
        
    public String getUsuarioActivo(){
		return usuario;
	}
	
	public static void setUsuario(String user){
		ClientFrame.usuario = user;
	}
	
	public void escribirEstado(String state){
		this.estado.setText(state);
		this.setVisible(true);
	}
	
	
	public boolean estaSocketActivo(){
		boolean resul = false;
		try {
			Socket sck = conectarAServidor();
			if (sck != null) { 
				MensajeChequeoSocket mjeCheck = new MensajeChequeoSocket();
				ObjectOutputStream oosEnvio = new ObjectOutputStream(sck.getOutputStream());
				oosEnvio.writeObject(mjeCheck);
				resul = true; 
//				sck.close();
			}			
		} catch (IOException e) {
			this.logger.logAmbos("Servidor Inactivo");
			this.desactivarBotones();
			return false;
		}
		return resul;
	}
	
	public static Socket getSocket(){
		return socket;
	}
	
	public void actualizarEstado(){
		if (this.isConectado()){
			if (ClientFrame.usuario.equals("----")){
				this.estado.setText("Estado del cliente: usuario sin definir \nEstado del servidor: conectado al servidor");
			} else {
				this.estado.setText("Estado del cliente: usuario " + usuario + " activo \nEstado del servidor: conectado al servidor");
			}
		} else {
			if (ClientFrame.usuario.equals("----")){
				this.estado.setText("Estado del cliente: usuario sin definir \nEstado del servidor: sin conexion");
			} else {
				this.estado.setText("Estado del cliente: usuario " + usuario + " activo \nEstado del servidor: sin conexion");
			}
		}
	}
	
	public void actualizarEstadoSinConexion(){
//		boolean cerrado = this.estaSocketActivo();
//		if (cerrado){
		if (this.isConectado()){
			if (ClientFrame.usuario.equals("----")){
				this.estado.setText("Estado del cliente: usuario sin definir \nEstado del servidor: activo");
			} else {
				this.estado.setText("Estado del cliente: usuario " + usuario + " activo \nEstado del servidor: activo");
			}
		} else {
			this.desactivarBotones();
			if (ClientFrame.usuario.equals("----")){
				this.estado.setText("Estado del cliente: usuario sin definir \nEstado del servidor: inactivo");
			} else {
				this.estado.setText("Estado del cliente: usuario " + usuario + " activo \nEstado del servidor: inactivo");
			}
		}
	}
	
	public void escribirEstadoSinAct(String state){
		this.estado.setText(state);
	}

	public int obtenerRetardo(){
		return Integer.parseInt(Cliente.obtenerConfiguracion().getProperty("sleepUser"));
	}
        


    public Socket conectarAServidor(){
        Socket sck = null;
    	try {	
    		synchronized (this){
    			logger.logArchivo("Intento conexion de socket");
        		//Conecto el socket
        		this.escribirEstadoSinAct("Intento conectarme");
        		int puerto = Integer.parseInt(Cliente.obtenerConfiguracion().getProperty("serverPort"));
        		sck = new Socket(Cliente.obtenerConfiguracion().getProperty("serverIp"), puerto); 
        		logger.logArchivo("Conexion establecida");
        		this.setConectado(true);
        		this.actualizarEstado();
//        		this.setVisible(true);
    		}
    	} catch (UnknownHostException ex) {
    		this.estado.setText("Error: Imposible conectarse al servidor UHE");
    	} catch (IOException ex) {
    		this.estado.setText("Error: Imposible conectarse al servidor IOE");
                this.estado.setText("Socket Cerrado");
    	}
        return sck;
    }
 
    
    
	public boolean isConectado() {
		return conectado;
	}

	public void setConectado(boolean conectado) {
		this.conectado = conectado;
	}
	
	public javax.swing.JTextArea getEstado(){
		return this.estado;
	}
	
	public javax.swing.JProgressBar getBarraProgreso(){
		return this.barraProgreso;
	}
	
	public ClientFrame getFrame(){
		return this;
	}
	
	public static int calcularPorcentajeCompleto(long total, long acum){
    	int resul = 0;
    	long udPorc = total / 100;
    	long porcLong = acum / udPorc;
    	resul = (int) porcLong;
    	return resul;
    }
	
	public void desactivarBotones(){
		this.bGuardar.setEnabled(false);
		this.bRecuperar.setEnabled(false);
	}
	
	public void activarBotones(){
		this.bGuardar.setEnabled(true);
		this.bRecuperar.setEnabled(true);
	}
	
	public boolean checkUser(String usuario){
		boolean resul = false;
		int nroSeg = getCheckUser(usuario);
		int checkUser = Integer.parseInt(Cliente.obtenerUsuario().getProperty("check"));
		if (nroSeg == checkUser){
			resul = true;
		}
		return resul;
	}
	
	public int getCheckUser(String cadena){
		int total = 0;
		for (int x=0; x<cadena.length();x++){
			System.out.println(cadena.charAt(x) + " = " + cadena.codePointAt(x));
			total += cadena.codePointAt(x);
		}
		return total;
	}
        
        public void envioPresentacionUsuario(Logueo logger){
        		Socket sck = conectarAServidor();
        		try {
                    // Se envia un mensaje de presentacion
                ObjectOutputStream oosEnvio = new ObjectOutputStream(sck.getOutputStream());
                ObjectInputStream oisRecep = new ObjectInputStream(sck.getInputStream());
                   
			MensajePresentacion mjeConec = new MensajePresentacion();
			mjeConec.nombreUsuario = usuario;
			oosEnvio.writeObject(mjeConec);
			oosEnvio.flush(); // VER SI LO SACO
			
			this.estado.setText("Solicitada la conexion. Esperando chequeo de usuario");
			this.setVisible(true);
			
			// Recibo mje de confirmacion o rechazo
			Object mensajeAux = oisRecep.readObject();
			
			if (mensajeAux instanceof MensajePresentacion){
				MensajePresentacion mensajeResp = (MensajePresentacion) mensajeAux;
				this.estado.setText("Conexion establecida");
				this.setVisible(true);
				if (mensajeResp.nombreUsuario.equals(usuario)){
					bRecuperar.setEnabled(true);
					this.estado.setText("Usuario conocido");
					this.setConectado(true);
					this.setVisible(true);
					logger.logAmbos("Usuario " + usuario + " conectado.");
				} else {
					this.estado.setText("Usuario no registrado. Se procede a registro");
					logger.logAmbos("Usuario no registrado. Se procede a registro");
				}
			} else {
				this.estado.setText("Conexion rechazada");
				this.setVisible(true);
			}    
		} catch (IOException e) {
			this.logger.logAmbos("Falla la conexion del usuario IO");
			e.printStackTrace();
		} catch (ClassNotFoundException e){
			this.logger.logAmbos("Falla la conexion del usuario CNF");
			e.printStackTrace();
		}
	}
        
        public void envioPresentacionUsuarioConConexion(Logueo logger){
        	enUso = true;
    		Socket sck = conectarAServidor();
    		try {
                // Se envia un mensaje de presentacion
            ObjectOutputStream oosEnvio = new ObjectOutputStream(sck.getOutputStream());
            ObjectInputStream oisRecep = new ObjectInputStream(sck.getInputStream());
               
		MensajePresentacion mjeConec = new MensajePresentacion();
		mjeConec.nombreUsuario = usuario;
		oosEnvio.writeObject(mjeConec);
		oosEnvio.flush(); // VER SI LO SACO
		
		this.estado.setText("Solicitada la conexion. Esperando chequeo de usuario");
		this.setVisible(true);
		
		// Recibo mje de confirmacion o rechazo
		Object mensajeAux = oisRecep.readObject();
		
		if (mensajeAux instanceof MensajePresentacion){
			MensajePresentacion mensajeResp = (MensajePresentacion) mensajeAux;
			this.estado.setText("Conexion establecida");
			this.setVisible(true);
			if (mensajeResp.guardIni == 1){ 
				this.activarBotones();
			} 
			if (mensajeResp.nombreUsuario.equals(usuario)){
				bRecuperar.setEnabled(true);
				this.estado.setText("Usuario conocido");
				this.setConectado(true);
				this.setVisible(true);
				logger.logAmbos("Usuario " + usuario + " conectado.");
			} else {
				this.estado.setText("Usuario no registrado. Se procede a registro");
				logger.logAmbos("Usuario no registrado. Se procede a registro");
			}
		} else {
			this.estado.setText("Conexion rechazada");
			this.setVisible(true);
		}    
		sck.close();
		enUso = false;
	} catch (IOException e) {
		this.logger.logAmbos("Falla la conexion del usuario IO");
		e.printStackTrace();
	} catch (ClassNotFoundException e){
		this.logger.logAmbos("Falla la conexion del usuario CNF");
		e.printStackTrace();
	}
}
        
        public boolean getEstadoBotGuardar(){
    		return this.bGuardar.isEnabled();
    	}

        public void activarBotonRecuperar(){
        	this.bRecuperar.setEnabled(true);
        }
}
