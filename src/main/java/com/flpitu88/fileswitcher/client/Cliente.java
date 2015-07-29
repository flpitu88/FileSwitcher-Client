/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.flpitu88.fileswitcher.client;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;
import javax.swing.JFrame;
import javax.swing.WindowConstants;

/**
 *
 * @author Flavio L. Pietrolati
 */
public class Cliente {
    
    public static void main(String[] args){
    	if( new Control().comprobar() )
        {

    		Properties config = obtenerConfiguracion();
        	if (config != null){
        		System.out.println("Obtenido archivo de configuracion");
        	} else {
        		System.out.println("No se encuentra archivo de configuracion");
        	}
        	JFrame ventana = new ClientFrame();
//        	ventana.setTitle("FILE SWITCHER");
        	// Se le dice a la ventana que termine el programa cuando se la cierre
        	ventana.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        	ventana.setVisible(true);    		
    		     
        }        
        else
        {
            System.exit(0);
        }

    } 
    
    
    /*
     * Metodo para obtener la configuracion desde el archivo
     * correspondiente.
     */
    public static Properties obtenerConfiguracion(){
		Properties config = new Properties();
		try{
			// Cargo el archivo en la ruta especificada
			config.load(new FileInputStream("clientConfig.properties"));
		} catch (FileNotFoundException e) {
			System.out.println("Error, no existe archivo de configuracion");	
		} catch (IOException e) {
			System.out.println("Error, No se puede leer el archivo de configuracion")	;
		  }
		return config;
	}
    
    
    /*
     * Metodo para obtener la configuracion del cliente
     * que tiene la sesion iniciada en el sistema
     */
    public static Properties obtenerUsuario(){
    	Properties user = new Properties();
    	try{
			// Cargo el archivo en la ruta especificada
			user.load(new FileInputStream("data.u"));
		} catch (FileNotFoundException e) {
			System.out.println("Error, El archivo de usuario no existe");	
		} catch (IOException e) {
			System.out.println("Error, No se puede leer el archivo de usuario")	;
		  }
		return user;
    }
    
}
