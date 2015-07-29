package com.flpitu88.fileswitcher.client;

import javax.swing.JProgressBar;

import com.flpitu88.fileSwitcher.utilitarios.Logueo;

//public class BarraThreadGuardar extends BarraThread implements Runnable {
	public class BarraThreadGuardar implements Runnable {
	
	private JProgressBar barraProgreso;
	private Logueo logger;
	private int value = 250;//retardo en milisegundos
	
	
	// Constructor de clase
	public BarraThreadGuardar(JProgressBar barra, Logueo logger){
		this.setBarraProgreso(barra);
		this.logger = logger;
	}

	public void run() {
//		logger.logArchivo("BARRA GUARDAR: Inicio");
		while (!jobGuardar.control){
			barraProgreso.setStringPainted(true);
			barraProgreso.setValue(jobGuardar.cantCompleto);
			barraProgreso.repaint(); 
			try {
				Thread.sleep( this.value );
				} catch (InterruptedException e){
					logger.logAmbos(e.getMessage()); 
					} 
			if( jobGuardar.control ){
                barraProgreso.setValue(100);
                logger.logAmbos("Finalizado el guardado de archivos"); 
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
