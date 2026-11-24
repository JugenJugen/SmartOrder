package com.mycompany.smartorder.excepciones;

/**
 * Error al acceder a la base de datos. Envuelve las SQLException
 * para que las capas superiores no dependan de JDBC.
 *
 * @author Elian Rojas
 */
public class DAOException extends SmartOrderException {

    public DAOException(String message) {
        super(message);
    }

    public DAOException(String message, Throwable cause) {
        super(message, cause);
    }
}
