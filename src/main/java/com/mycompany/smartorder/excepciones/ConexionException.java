package com.mycompany.smartorder.excepciones;

/**
 *
 * @author joshar
 */
public class ConexionException extends Exception {
    
    public ConexionException(String mensaje) {
        super(mensaje);
    } 
    
    public ConexionException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
