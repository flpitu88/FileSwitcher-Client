package com.flpitu88.fileswitcher.client;

import com.flpitu88.fileSwitcher.utilitarios.Logueo;

public class ChequeoSocket implements Runnable {

	private Logueo logger;
	private int retardo;
	private ClientFrame ventana;
	
	public ChequeoSocket(Logueo logger,ClientFrame vent){
		this.setLogger(logger);
		this.setVentana(vent);
		this.retardo = this.ventana.obtenerRetardo();
		logger.logArchivo("Retardo vale : " + this.retardo);
	}
	
	private void setVentana(ClientFrame vent) {
		this.ventana = vent;
		
	}

	private void setLogger(Logueo log) {
		this.logger = log;
		
	}

	@Override
	public void run() {
		while(true){
			synchronized(this){
				if (this.ventana.estaSocketActivo() == false){
					this.ventana.setConectado(false);
					this.ventana.escribirEstadoSinAct("Servidor Inactivo");
					logger.logAmbos("El servidor esta inactivo");
					this.ventana.desactivarBotones();
					this.ventana.setConectado(false);
				} else {
					logger.logAmbos("CHEQUEO: El servidor esta activo");
				}
			}
			try {
				Thread.sleep(retardo);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
	}
	
	

}
