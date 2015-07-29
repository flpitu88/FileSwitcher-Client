package com.flpitu88.fileswitcher.client;

import com.flpitu88.fileSwitcher.utilitarios.Logueo;

public class ChequeoUsuario implements Runnable {

	private Logueo logger;
	private int retardo;
	private ClientFrame ventana;

	public ChequeoUsuario(Logueo logger, ClientFrame vent) {
		this.setLogger(logger);
		this.setVentana(vent);
		this.retardo = this.ventana.obtenerRetardo();
		logger.logArchivo("Retardo vale : " + this.retardo);
	}

	@Override
	public void run() {

		while (true) {
			synchronized (this) {
				boolean estBGuard = this.ventana.getEstadoBotGuardar();
				this.ventana.desactivarBotones();
				String userLect = Cliente.obtenerUsuario().getProperty("user");
				if (this.ventana.getUsuarioActivo().equals(userLect)) {
					this.ventana.actualizarEstado();
				} else {
					if (this.ventana.checkUser(userLect)) {
						logger.logArchivo("Usuario chequeado");
						ClientFrame.setUsuario(userLect);
						logger.logAmbos("Cambio de usuario a: " + userLect);
						this.ventana
								.escribirEstadoSinAct("Se ha modificado el usuario activo");
						ventana.envioPresentacionUsuario(logger);
						this.ventana.actualizarEstado();
						this.ventana.activarBotones();
					} else {
						// logger.logAmbos("Comprobacion de cliente invalida. Se bloquean acciones");
						ClientFrame.setUsuario("----");
						this.ventana
								.escribirEstado("Chequeo de cliente erroneo");
//						this.ventana.desactivarBotones();
					}
				}
				// if (this.ventana.isEnProceso() == false){
				// synchronized (this){
				if (!this.ventana.estaSocketActivo()) {
					this.ventana.setConectado(false);
					this.ventana.desactivarBotones();
					 this.ventana.conectarAServidor();
				} else {
					if (estBGuard){
						this.ventana.activarBotones();	
					} else {
						this.ventana.activarBotonRecuperar();
					}
					
				}

			}
			// }
			try {
				Thread.sleep(retardo);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public ClientFrame getVentana() {
		return ventana;
	}

	public void setVentana(ClientFrame ventana) {
		this.ventana = ventana;
	}

	public Logueo getLogger() {
		return logger;
	}

	public void setLogger(Logueo logger) {
		this.logger = logger;
	}

}
