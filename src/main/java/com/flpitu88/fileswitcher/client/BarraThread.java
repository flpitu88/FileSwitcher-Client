package com.flpitu88.fileswitcher.client;

import javax.swing.JProgressBar;

import com.flpitu88.fileSwitcher.utilitarios.Logueo;

abstract class BarraThread implements Runnable {
	
	protected JProgressBar barraProgreso;
	protected Logueo logger;
	protected int value = 50;//retardo en milisegundos
	
	public abstract void run();
	
	public JProgressBar getBarraProgreso() {
		return barraProgreso;
	}

	public void setBarraProgreso(JProgressBar jProgressBar) {
		this.barraProgreso = jProgressBar;
	}

}
