package com.flpitu88.fileswitcher.client;

import java.io.FileOutputStream;
import java.io.IOException;
//import java.io.ObjectInputStream;
//import java.io.ObjectOutputStream;
//import java.net.Socket;
import java.net.SocketException;

import com.flpitu88.fileSwitcher.utilitarios.Archivo;
import com.flpitu88.fileSwitcher.utilitarios.Logueo;
import com.flpitu88.fileSwitcher.mensajes.MensajeArchivosRecuperar;
import com.flpitu88.fileSwitcher.mensajes.MensajeConfirTransferencia;
import com.flpitu88.fileSwitcher.mensajes.MensajePedidoRecuperar;
import com.flpitu88.fileSwitcher.colecciones.ListaPaths;
import com.flpitu88.fileSwitcher.colecciones.ReprArchivo;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

public class jobRecuperar implements Runnable {

	public static boolean control = false;
	public static int cantCompleto;
	public static int acumulado;
	private Logueo logger;
	private ClientFrame ventana;
	private String usuario;
	ObjectOutputStream oosEnvio;
    ObjectInputStream oisRecep;
	
	
	public jobRecuperar(Logueo logger, ClientFrame ventana, String usuario){
		this.setLogger(logger);
		this.setVentana(ventana);
		this.usuario = usuario;
	}
	
	public void run() {
		ClientFrame.enUso = true;
		ventana.desactivarBotones();
                Socket sock = ventana.conectarAServidor();
        try {
            // Se envia un mensaje de presentacion
            this.oosEnvio = new ObjectOutputStream(sock.getOutputStream());
        } catch (IOException ex) {
            Logger.getLogger(jobGuardar.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            this.oisRecep = new ObjectInputStream(sock.getInputStream());
        } catch (IOException ex) {
            Logger.getLogger(jobGuardar.class.getName()).log(Level.SEVERE, null, ex);
        }
		recuperarArchivos();	
		ventana.activarBotones();
		ClientFrame.enUso = false;
		try {
			sock.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	/*
	   * Metodo que se ejecuta ante la presion de recuperar
	   * los archivos de la base.
	   */
	    public void recuperarArchivos(){
	    	try {
	    		synchronized (this){
	    			logger.logAmbos(" ##############        INICIO PROCESO DE RECUPERACION DE ARCHIVOS       ############### ");
	    			int cantArchivosRec = 0;
		    		long cantKBytesRec = 0;
		    		logger.logAmbos("Inicio Recuperacion de archivos");
		    		this.getVentana().getEstado().setText("Inicio Recuperacion de archivos");
		    		this.getVentana().setVisible(true);
		    		// Se envia un mensaje para iniciar la recuperacion de archivos
		    		MensajePedidoRecuperar mjeRecuperar = new MensajePedidoRecuperar(this.usuario);
		    		this.oosEnvio.writeObject(mjeRecuperar);
//		    		ClientFrame.oosEnvio.flush(); // VER SI LO SACO
		    		Object mensajeAux;
		    		MensajeArchivosRecuperar mjeArchGuardar;
		    		mensajeAux = this.oisRecep.readObject();
		    		if (mensajeAux instanceof MensajeArchivosRecuperar){
		    			mjeArchGuardar = (MensajeArchivosRecuperar) mensajeAux;
		    			logger.logArchivo("Recibo lista de archivos alojados en servidor");
		    			ListaPaths listaFiltrada = mjeArchGuardar.getListado();
		    			ListaPaths listaDepurada = depurarListaArchivosActualizados(listaFiltrada);
		    			// limito la cantidad de archivos de la lista que se deben transferir por exceder el tamaño
		    			int limiteTamanio = mjeArchGuardar.getMaxTamanio();
	    				listaDepurada.limitarTamanio(limiteTamanio);
	    				logger.logArchivo(listaDepurada.getCantSinTransferir() + " archivos no se transfieren por exceder maximo. (" + listaDepurada.getTamTotalSinTransferir() + " bytes)");
	    				// Clono la lista evitando enviar los que exceden tamanio
	    				ListaPaths listaOkTamanio = listaDepurada.clonarSinLimitadas();
	    				cantArchivosRec = listaOkTamanio.getTamanio();
	    				cantKBytesRec = listaOkTamanio.getTamanioTotal() / 1024;
	    				logger.logArchivo("Filtro los archivos que exceden la capacidad maxima");
		    			// Envio un nuevo mensaje al servidor con la lista filtrada
		    			MensajeArchivosRecuperar mjeListaFiltrada = new MensajeArchivosRecuperar(listaOkTamanio,(int)listaOkTamanio.getTamanioTotal()); // Este tamanio total meti cualquiera
	        			this.oosEnvio.writeObject(mjeListaFiltrada);
//	        			ClientFrame.oosEnvio.flush(); // VER SI LO SACO
	        			logger.logArchivo("Envio lista filtrada con " + listaOkTamanio.getTamanio() + " elementos");
	        			// Voy guardando los archivos recibidos
	        			if (listaOkTamanio.getTamanio() > 0){
	        				cantCompleto = 0;
//	        				recibirArchivosARecuperar(listaOkTamanio.getTamanioTotal());
	        				for (int i=0;i<listaOkTamanio.getTamanio();i++){
	        					recibirArchivo(listaOkTamanio.getTamanioTotal());
	        				}
	        			} else {
	        				logger.logAmbos("Los archivos se encuentran actualizados. No se requiere transferencia");
	        				this.getVentana().getEstado().setText("No hay archivos nuevos en servidor");
	        				this.getVentana().setVisible(true);
	        			}
		    		}
		    		control = true;
		    		logger.logAmbos("Finalizada recuperacion de archivos. \nRecibidos " + cantArchivosRec + " archivos (" + cantKBytesRec + "Kb)");
		    		this.getVentana().getEstado().setText("Finalizada recuperacion de archivos. \nRecibidos " + cantArchivosRec + " archivos (" + cantKBytesRec + "Kb)");	    		
		    		this.getVentana().setVisible(true);
		    		logger.logAmbos(" ##############        FIN RECUPERACION DE ARCHIVOS       ############### ");
	    		}
	    		
	    	} catch(SocketException e){
	    		logger.logArchivo("Socket Exception");
//	    		System.err.println("Socket Exception run");
	    	} catch(IOException e){
	    		logger.logArchivo("IO Exception");
//	    		e.printStackTrace();
	    	} catch(ClassNotFoundException e){
	    		logger.logArchivo("Class Not Found Exception");
//	    		System.err.println("ClassNotFound Exception run");
	    	}
	    }
	    
	    
	    /*
	     * Metodo que toma todos los paths recibidos por el cliente,
	     * y verifica en cada uno si es mas nuevo o mas viejo. De estar actualizado el del servidor,
	     * lo elimina de la lista, dejando solo los que se deben enviar desde el
	     * cliente.
	     */
	    @SuppressWarnings("unused")
		public ListaPaths depurarListaArchivosActualizados(ListaPaths lista){
	    	// Creo lista de retorno
	    	ListaPaths listaRetorno = new ListaPaths();
	    	// Recorro la lista para verificar si los paths del cliente existen
	    	for (int i=0;i<lista.getTamanio();i++){
	    		ReprArchivo datosFichero = lista.getPath(i);
	    		String pathCliente = datosFichero.getPathIniCli() + "/" + datosFichero.getPathFin();
	    		logger.logArchivo("Nuevo path en cliente: " + cambiarBarraInvertida(pathCliente));
	    		Archivo fichero = new Archivo(pathCliente);
	    		if (fichero != null){
	    			if (estaActualizado(fichero.ultimaModif(),datosFichero.getfUltMod())){
	    				logger.logArchivo(cambiarBarraInvertida(pathCliente) + " se encuentra actualizado. No se lista.");
	        		} else {
	        			logger.logArchivo(cambiarBarraInvertida(pathCliente) + " se encuentra desactualizado. Agregado a lista.");
	        			// Debo cambiar el nombre del archivo para poder recibir
	        			fichero.modificarNombreTemporal();
	        			listaRetorno.addPath(datosFichero);
	        		}
	    		} else {
	    			logger.logArchivo("No existe en cliente, lo dejo en la lista de retorno");
	    			listaRetorno.addPath(datosFichero);
	    			logger.logArchivo("Agrego a lista de retorno");
	    		}
	    	}
	    	return listaRetorno;
	    }

	    
	    // nuevo metodo para recibir archivo transferido
	    public void recibirArchivo(long totalBytes){
	    	try{
//	    			cantCompleto = 0;
	    		FileOutputStream fos = null;
	    		Object mensajeAux;
	    		ReprArchivo repArch;
	    	
	    		mensajeAux = this.oisRecep.readObject();
	    		if (mensajeAux instanceof ReprArchivo){
	    			repArch = (ReprArchivo) mensajeAux;
	    			int tamArchivo = (int)repArch.getTamanio();
	    			String pathDestino = repArch.getPathIniCli() + "/" + repArch.getPathFin();
	    			// Chequeo si existe el directorio contenedor, sino lo creo
	    			Archivo dirDestino = new Archivo((new Archivo(pathDestino)).getDirPadre());
	    			if (!dirDestino.existe()){
	    				dirDestino.crearDirectorios();
	    			}
	    			fos = new FileOutputStream(pathDestino);
         		   
	    			// Creamos el array de bytes para leer los datos del archivo
	    			byte[] buffer = new byte[tamArchivo];
     
	    			// Obtenemos el archivo mediante la lectura de bytes enviados
	    			for(int i=0; i<buffer.length ; i++){
	    				buffer[i] = (byte)this.oisRecep.read( );
	    				acumulado++;
	    				cantCompleto = ClientFrame.calcularPorcentajeCompleto(totalBytes, acumulado);
	    			}
	    			ClientFrame.calcularPorcentajeCompleto(totalBytes, acumulado);
     
                   // Escribimos el archivo 
	    			fos.write( buffer ); 
     
         		  
	    			// Enviar confirmacion de que termino este archivo
	    			MensajeConfirTransferencia mjeConfir = new MensajeConfirTransferencia();
	    			mjeConfir.total = tamArchivo;
	    			logger.logArchivo("Envio confirmacion de recepcion");
	    			this.oosEnvio.writeObject(mjeConfir);
	    			fos.close();
	    		}
	    	} catch(IOException e){
//	    		System.err.println("IO Exception recibiendoArchivos");
	    		logger.logAmbos("Falla la Recepcion de archivos para recuperar");
	    	} catch(ClassNotFoundException e){
//	    		System.err.println("ClassNotFoundException Exception recibir recibiendoArchivos");
	    		logger.logAmbos("Falla la Recepcion de archivos para recuperar");
	    	} 
	    }
	    
	   
	    
	    /*
	     * Metodo que chequea si el archivo es mas nuevo 
	     * que el que se encuentra en el servidor.
	     */
	    public boolean estaActualizado(long fCliente, long fServer){
	    	boolean resul = false;
	    	if (fCliente >= fServer){
	    		resul = true;
	    	}
	    	return resul;
	    }
	    
	    public void setLogger(Logueo logger){
	    	this.logger = logger;
	    }
	    
	    
	    public void setVentana(ClientFrame ventana){
	    	this.ventana = ventana;
	    }
	    
	    
	    public ClientFrame getVentana(){
	    	return this.ventana;
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
	
}
