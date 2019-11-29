package com.nlazzos.cuantoponemos.Entities;

import android.database.Cursor;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Nico on 12/1/2018.
 */
public class Persona implements Comparable<Persona>, Cloneable{
    private int id;
    private String nombre;
    private int puso;
    private int balance;
    private Map<String, Integer> leDebe = new HashMap<String, Integer>();

    public Persona(int id, String nombre, int puso, int balance, Map<String, Integer> leDebe) {
        this.id = id;
        this.nombre = nombre;
        this.puso = puso;
        this.balance = balance;
        this.leDebe = leDebe;
    }

    public Persona() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public int getPuso() {
        return puso;
    }

    public void setPuso(int puso) {
        this.puso = puso;
    }

    public int getBalance() {
        return balance;
    }

    public void setBalance(int balance) {
        this.balance = balance;
    }

    public Map<String, Integer> getLeDebe() {
        return leDebe;
    }

    public void setLeDebe(String nombre, int debe) {
        this.leDebe.put(nombre, debe);
    }

    public void resetMap(){
        leDebe = new HashMap<String, Integer>();
    }

    public Persona obtenerDatos(Cursor cursorPersonas, int puso){
        Persona persona = new Persona();
        while(cursorPersonas.moveToNext()){
            persona.setId(cursorPersonas.getInt(0));
            persona.setNombre(cursorPersonas.getString(1));
            persona.setPuso(puso);
        }
        return persona;
    }

    public Persona clone() {
        Persona clone = null;
        try {
            clone = (Persona) super.clone();
        }
        catch(CloneNotSupportedException e) {
            System.out.print("No se puede clonar!");
        }
        return clone;
    }

    @Override
    public int compareTo(Persona o) {
        if(this.balance > o.getBalance()){
            return -1;
        }
        if(this.balance < o.getBalance()){
            return 1;
        }
        return 0;
    }
}
