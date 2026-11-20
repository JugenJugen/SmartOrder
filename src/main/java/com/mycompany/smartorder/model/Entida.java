/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package com.mycompany.smartorder.model;

/**
 *
 * @author angel
 */

import com.mycompany.smartorder.excepciones.ValidacionException;
import java.io.Serializable;

public abstract class Entida implements Serializable {
    private static final long serialVersionUID = 1L;
    protected int id;

    public abstract void validar() throws ValidacionException;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
