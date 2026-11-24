package com.mycompany.smartorder.dao;

import com.mycompany.smartorder.excepciones.DAOException;
import java.util.List;

/**
 * Interfaz genérica para las operaciones CRUD.
 * Cada DAO la implementa para su propia entidad (polimorfismo).
 *
 * @param <T> tipo de la entidad
 *
 * @author Elian Rojas
 */
public interface CrudDAO<T> {

    int insertar(T entidad) throws DAOException;

    boolean actualizar(T entidad) throws DAOException;

    boolean eliminar(int id) throws DAOException;

    T buscarPorId(int id) throws DAOException;

    List<T> listar() throws DAOException;
}
