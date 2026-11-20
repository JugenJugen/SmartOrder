/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.smartorder.excepciones;

/**
 *
 * @author angel
 */
public class SmartOrderException extends Exception {

    public SmartOrderException(String message) {
        super(message);
    }

    public SmartOrderException(String message, Throwable cause) {
        super(message, cause);
    }
}