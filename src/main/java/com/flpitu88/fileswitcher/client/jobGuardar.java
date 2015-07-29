package com.flpitu88.fileswitcher.client;

import com.flpitu88.fileSwitcher.utilitarios.Archivo;
import com.flpitu88.fileSwitcher.colecciones.ListaPaths;
import com.flpitu88.fileSwitcher.colecciones.ReprArchivo;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.flpitu88.fileSwitcher.utilitarios.Logueo;
import com.flpitu88.fileSwitcher.mensajes.MensajeArchivosGuardar;
import com.flpitu88.fileSwitcher.mensajes.MensajeConfirTransferencia;
import com.flpitu88.fileSwitcher.mensajes.MensajePedidoGuardar;

public class jobGuardar implements Runnable {

    public static boolean control = false;
    public static int cantCompleto;
    public static int acumulado;
    private Logueo logger;
    private ClientFrame ventana;
    private String usuario;
    ObjectOutputStream oosEnvio;
    ObjectInputStream oisRecep;

    public jobGuardar(Logueo logger, ClientFrame ventana, String usuario) {
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
        guardarArchivos();
        ventana.activarBotones();
        try {
			sock.close();
			ClientFrame.enUso = false;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }

    public Logueo getLogger() {
        return logger;
    }

    public void setLogger(Logueo logger) {
        this.logger = logger;
    }

    /*
     * Metodo que se ejecuta ante la presion de guardar los 
     * archivos modificados en la sesion del usuario
     */
    public void guardarArchivos() {
        MensajePedidoGuardar mjeGuardar = new MensajePedidoGuardar(this.usuario);
        try {
            synchronized (this) {
                logger.logAmbos(" ##############        INICIO PROCESO DE GUARDADO DE ARCHIVOS       ############### ");
                int cantArchivosRec = 0;
                long cantKBytesRec = 0;
                this.oosEnvio.writeObject(mjeGuardar);
                logger.logAmbos("----- Solicitud de guardado de archivos ------");
                this.getVentana().getEstado().setText("Inicio guardado de archivos");
                this.getVentana().setVisible(true);
                Object mensajeAux;
                MensajeArchivosGuardar mjeArchGuardar;
                mensajeAux = this.oisRecep.readObject();
                if (mensajeAux instanceof MensajeArchivosGuardar) {
                    mjeArchGuardar = (MensajeArchivosGuardar) mensajeAux;
                    // obtengo el maximo tamanio de un archivo (o de todos, es el del limite)
                    int sizeMax = mjeArchGuardar.getSizeMax();
                    // Envio al servidor la lista con las fechas de modificacion para chequear en server
                    ListaPaths listaConFechas = armarListaConModificacion(mjeArchGuardar.getListado(), sizeMax);
                    MensajeArchivosGuardar mjeArchConFecha = new MensajeArchivosGuardar(listaConFechas);
                    this.oosEnvio.writeObject(mjeArchConFecha);
                    this.oosEnvio.flush(); // VER SI LO SACO
                    logger.logArchivo("Envio lista de archivos con fechas de modificacion");
                    // Espero respuesta con los archivos que efectivamente se deben enviar
                    mensajeAux = this.oisRecep.readObject();
                    if (mensajeAux instanceof MensajeArchivosGuardar) {
                        MensajeArchivosGuardar mjeArchEnviar;
                        mjeArchEnviar = (MensajeArchivosGuardar) mensajeAux;
                        logger.logArchivo("Lista filtrada por servidor - TIENE ");
                        ListaPaths lista = mjeArchEnviar.getListado();
                        logger.logArchivo("Lista con " + lista.getTamanio() + " elementos para guardar");
                        // Clono la lista evitando enviar los que exceden tamanio
                        ListaPaths listaOkTamanio = lista.clonarSinLimitadas();
                        logger.logArchivo(listaOkTamanio.getCantSinTransferir() + " archivos sin transferir por exceder limite (exceso " + lista.getTamTotalSinTransferir() + " bytes)");
                        // Muestro en el estado la cantidad de archivos que no se pueden enviar
                        this.getVentana().getEstado().setText(listaOkTamanio.getCantSinTransferir() + " archivos sin transferir por exceder limite (exceso " + lista.getTamTotalSinTransferir() + " bytes)");
                        this.getVentana().getEstado().setVisible(true);
                        long totalbytes = listaOkTamanio.getTamanioTotal();
                        cantArchivosRec = listaOkTamanio.getTamanio();
                        cantKBytesRec = listaOkTamanio.getTamanioTotal() / 1024;
//            				boolean corte = false;
                        if (listaOkTamanio.getTamanio() == 0) {
                            logger.logAmbos("Los archivos del servidor se encuentran actualizados. No se envian archivos");
                            this.getVentana().getEstado().setText("No hay archivos nuevos para guardar en el servidor");
                            this.getVentana().getEstado().setVisible(true);
                        } else {
                            cantCompleto = 0;
                            for (int i = 0; i < listaOkTamanio.getTamanio(); i++) {
//            						if (i == listaOkTamanio.getTamanio()-1) { corte = true; }
                                enviarArchivosDeLista(listaOkTamanio.getPath(i), totalbytes);
                            }
                        }
                    }
                }
                control = true;
                this.getVentana().getEstado().setText("Finalizado el guardado de archivos. \nEnviados " + cantArchivosRec + " archivos (" + cantKBytesRec + "Kb)");
                this.getVentana().getEstado().setVisible(true);
                logger.logAmbos("Finalizado el guardado de archivos. \nEnviados " + cantArchivosRec + " archivos (" + cantKBytesRec + "Kb)");
                logger.logAmbos(" ##############        FIN GUARDADO DE ARCHIVOS       ############### ");
            }

        } catch (UnknownHostException ex) {
            this.getVentana().getEstado().setText("Error: Imposible conectarse al servidor UHE");
            this.getVentana().getEstado().setVisible(true);
            logger.logAmbos("UnknownHostException Exception");
        } catch (IOException ex) {
            this.getVentana().getEstado().setText("Error: Imposible conectarse al servidor IOE");
            this.getVentana().getEstado().setVisible(true);
            logger.logAmbos("IOException Exception");
        } catch (ClassNotFoundException ex) {
            this.getVentana().getEstado().setText("Error: Imposible conectarse al servidor CNFE");
            this.getVentana().getEstado().setVisible(true);
            logger.logAmbos("ClassNotFoundException Exception");
        }
    }

    /*
     * Metodo que recorre la lista de archivos a enviar y los va
     * enviando al servidor para que los guarde
     */
    public void enviarArchivosDeLista(ReprArchivo repArch, long totalBytes) {
        try {

            //Nuevo metodo de envio de archivos a guardar
            this.oosEnvio.writeObject(repArch);
//    		ClientFrame.oosEnvio.flush();
            // Armo el path del archivo de origen
            String pathOrigen = repArch.getPathIniCli() + repArch.getPathFin();
            logger.logAmbos("Enviando archivo en cliente: " + this.cambiarBarraInvertida(pathOrigen));
            // Se abre el fichero.
            FileInputStream fis = new FileInputStream(pathOrigen);

            // Creamos un array de tipo byte con el tamaño del archivo 
            byte[] buffer = new byte[(int) repArch.getTamanio()];

            logger.logArchivo("El archivo tiene un tamanio de " + repArch.getTamanio());
//            logger.logArchivo("El buffer mide: " + buffer.length);

            // Leemos el archivo y lo introducimos en el array de bytes 
            fis.read(buffer);
//            MensajeDatos mjeDatos = new MensajeDatos((int)repArch.getTamanio());
//            fis.read(mjeDatos.getDatosArchivo()); 
//            acumulado = acumulado + mjeDatos.getDatosArchivo().length;
//            cantCompleto = ClientFrame.calcularPorcentajeCompleto(totalBytes,acumulado);

            // Realizamos el envio de los bytes que conforman el archivo
            for (int i = 0; i < buffer.length; i++) {
                this.oosEnvio.write(buffer[ i]);
                acumulado++;
                cantCompleto = ClientFrame.calcularPorcentajeCompleto(totalBytes, acumulado);
            }

//            // Pruebo para mandar de otra manera
//            ClientFrame.oosEnvio.writeObject(mjeDatos);
//            ClientFrame.oosEnvio.flush();
            fis.close();
            this.oosEnvio.flush();

            // Recibir la confirmacion de que termino
            Object mensajeAux;
            MensajeConfirTransferencia mjeConfir;
            mensajeAux = this.oisRecep.readObject();
            if (mensajeAux instanceof MensajeConfirTransferencia) {
                mjeConfir = (MensajeConfirTransferencia) mensajeAux;
                logger.logArchivo("Recibo confirmacion de archivo " + this.cambiarBarraInvertida(pathOrigen));
                logger.logArchivo("Confirmados " + mjeConfir.total + " bytes");
            } else {
                logger.logArchivo("No recibo confirmacion de archivo " + pathOrigen);
            }

        } catch (FileNotFoundException e) {
            System.err.println("FileNotFound Exception envArch");
        } catch (IOException e) {
            System.err.println("IO Exception envArch");
        } catch (ClassNotFoundException e) {
            System.err.println("ClassNotFound Exception envArch");
        }

    }

    /*
     * Metodo que toma los paths enviados por el servidor de los cuales
     * se deben guardar los archivos, arman la estructura de archivos
     * y abren una nueva lista de paths con fechas de modificacion.
     */
    public ListaPaths armarListaConModificacion(ListaPaths lista, int sizeMax) {
        ListaPaths listaRetorno = new ListaPaths();
        for (int x = 0; x < lista.getTamanio(); x++) {
            String path1 = lista.getPath(x).getPathIni();
            String path2 = lista.getPath(x).getPathFin();
            String pathArchivo = path1 + "/" + path2;
            logger.logArchivo("Agrego a lista el archivo: " + cambiarBarraInvertida(pathArchivo));
            Archivo fichero = new Archivo(pathArchivo);
            if (fichero.existe()) {
                indexarDirectorios(fichero, listaRetorno, path1, sizeMax);
            } else {
                logger.logAmbos("El directorio a guardar no existe");
            }

        }
        return listaRetorno;
    }

    /*
     * Metodo que recorre directorios indexando en una lista
     * si son archivos, junto con su fecha de modificacion y 
     * sus paths absolutos
     */
    public void indexarDirectorios(Archivo archivo, ListaPaths listado, String pathIni, int sizeMax) {
        logger.logArchivo("Comienzo indexado de archivos en cliente");
        if (archivo.esArchivo()) {
            String pathFin = archivo.getPathCompleto().substring(pathIni.length(), archivo.getPathCompleto().length());
            ReprArchivo nuevoArch = new ReprArchivo(pathIni, pathFin, archivo.ultimaModif());
            nuevoArch.setTamanio(archivo.getTamanio());
            if (nuevoArch.getTamanio() <= sizeMax) {
                logger.logAmbos("Agrego a la lista el archivo: " + cambiarBarraInvertida(nuevoArch.getPathIni() + nuevoArch.getPathFin()));
                listado.addPath(nuevoArch);
            } else {
                logger.logAmbos("No se agrega el archivo por exceder el tamanio maximo");
            }
        } else {
            String[] subArchivos = archivo.listarArchivosDelDir();
            for (int i = 0; i < subArchivos.length; i++) {
                Archivo subArchivo = new Archivo(archivo.getPathCompleto() + "/" + subArchivos[i]);
                logger.logArchivo("Indexo la carpeta : " + cambiarBarraInvertida(subArchivo.getPathCompleto()));
                indexarDirectorios(subArchivo, listado, pathIni, sizeMax);
            }
        }
    }

    public ClientFrame getVentana() {
        return ventana;
    }

    public void setVentana(ClientFrame ventana) {
        this.ventana = ventana;
    }

    /*
     * Metodo que reemplaza las barras invertidas por las barras
     * normales utilizadas por el sistema en los paths, para poder
     * realizar las consultas a la base de la misma manera en la que se
     * guardan los datos
     */
    public String cambiarBarraInvertida(String cadena) {
        char[] tmp = cadena.toCharArray();
        for (int i = 0; i < cadena.length(); i++) {
            if (cadena.charAt(i) == '\\') {
                tmp[i] = '/';
            }
        }
        String resul = new String(tmp);
        return resul;
    }

}
