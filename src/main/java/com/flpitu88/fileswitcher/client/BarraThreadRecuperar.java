package com.flpitu88.fileswitcher.client;

import javax.swing.JProgressBar;
import com.flpitu88.fileSwitcher.utilitarios.Logueo;

//public class BarraThreadRecuperar extends BarraThread implements Runnable {

public class BarraThreadRecuperar implements Runnable {
	private JProgressBar barraProgreso;
	private Logueo logger;
	private int value = 250;//retardo en milisegundos
//	
	
	// Constructor de clase
	public BarraThreadRecuperar(JProgressBar barra, Logueo logger){
		this.setBarraProgreso(barra);
		this.logger = logger;
	}

	public void run() {
//		logger.logArchivo("BARRA RECUPERAR: Inicio");
		while (!jobRecuperar.control){
			barraProgreso.setStringPainted(true);
			barraProgreso.setValue(jobRecuperar.cantCompleto);
			barraProgreso.repaint(); 
			try {
				Thread.sleep( this.value );
				} catch (InterruptedException e){
					logger.logArchivo(e.getMessage()); 
					} 
			if( jobRecuperar.control ){
                barraProgreso.setValue(100);
                logger.logAmbos("Finalizada la recuperacion de archivos"); 
                break;//rompe ciclo     
            }
		}
		
	}

	public JProgressBar getBarraProgreso() {
		return barraProgreso;
	}

	public void setBarraProgreso(JProgressBar jProgressBar) {
		this.barraProgreso = jProgressBar;
	}
}
