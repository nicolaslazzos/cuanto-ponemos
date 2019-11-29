package com.nlazzos.cuantoponemos.DAOs;

import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

/**
 * Created by Nico on 12/1/2018.
 */
public class DAOPersona {
    private static final String NOMBRE_TABLA = "Personas";
    private static final String COL_ID = "idPersona";
    private static final String COL_NOMBRE = "nombre";
    public static final String CREATE_TABLE = "CREATE TABLE " + NOMBRE_TABLA + " (" +
            COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
            COL_NOMBRE + " VARCHAR(10) NOT NULL " +
            "); ";
    public static final String INSERT1 = "INSERT INTO Personas(nombre) VALUES('Nico'); ";


    public DAOPersona() {
    }

    public void insertar(SQLiteDatabase db, String nombre){
        db.execSQL("INSERT INTO " + NOMBRE_TABLA + "(" + COL_NOMBRE + ") VALUES('" + nombre + "'); ");
    }

    public Cursor consultar(SQLiteDatabase db){
        return db.rawQuery("SELECT * FROM " + NOMBRE_TABLA + " ORDER BY " + COL_NOMBRE + " ASC; ", null);
    }

    public Cursor consultarUltimo(SQLiteDatabase db){
        return db.rawQuery("SELECT MAX(" + COL_ID + "), " + COL_NOMBRE + " FROM " + NOMBRE_TABLA + " GROUP BY " + COL_NOMBRE +
                " HAVING MAX(" + COL_ID + ") > (SELECT MAX(p." + COL_ID + ") FROM " + NOMBRE_TABLA + " p GROUP BY p." + COL_NOMBRE + "); ", null);
    }

    public Cursor consultarPorNombre(SQLiteDatabase db, String nombre){
        return db.rawQuery("SELECT * FROM " + NOMBRE_TABLA + " WHERE " + COL_NOMBRE + " LIKE '" + nombre + "'; ", null);
    }

    public boolean existe(SQLiteDatabase db, String nombre){
        return (db.rawQuery("SELECT " + COL_ID + " FROM " + NOMBRE_TABLA + " WHERE " +
                COL_NOMBRE + " = '" + nombre + "'; ", null).getCount() != 0);
    }

    public Cursor consultarPorID(SQLiteDatabase db, int id){
        String idStr = String.valueOf(id);
        return db.rawQuery("SELECT " + COL_NOMBRE + " FROM " + NOMBRE_TABLA + " WHERE " + COL_ID + " = " + idStr + "; ", null);
    }

    public void eliminar(SQLiteDatabase db, int id){
        String idStr = String.valueOf(id);
        db.execSQL("DELETE FROM " + NOMBRE_TABLA + " WHERE " + COL_ID + " = " + idStr + "; ");
    }

    public void modificar(SQLiteDatabase db, int id, String nombre){
        String idStr = String.valueOf(id);
        db.execSQL("UPDATE " + NOMBRE_TABLA + " SET " + COL_NOMBRE + " = '" + nombre + "' WHERE " + COL_ID + " = " + idStr + "; ");
    }
}
