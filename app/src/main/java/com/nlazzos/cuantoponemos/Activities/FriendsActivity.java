package com.nlazzos.cuantoponemos.Activities;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.nlazzos.cuantoponemos.BDHelper.BDHelper;
import com.nlazzos.cuantoponemos.DAOs.DAOPersona;
import com.nlazzos.cuantoponemos.R;

import java.util.ArrayList;

public class FriendsActivity extends AppCompatActivity {

    //CONEXION CON LA BD
    private BDHelper helper;
    private SQLiteDatabase db;

    //WIDGETS
    private ListView lvAmigos;
    private EditText txtNombre;

    //DAOS / ENTIDADES
    private DAOPersona daoPersona = new DAOPersona();

    //VARIABLES
    private ArrayList<Integer> listaIDs;
    private ArrayList<String> listaNombres;
    private ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        helper = new BDHelper(this);
        db = helper.getWritableDatabase();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                nuevaPersona();
            }
        });

        lvAmigos = (ListView) findViewById(R.id.lvAmigos);
        registerForContextMenu(lvAmigos);
        cargarLista();
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        if(v.getId() == R.id.lvAmigos){
            this.getMenuInflater().inflate(R.menu.menu_opciones, menu);
        }
        super.onCreateContextMenu(menu, v, menuInfo);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        switch (item.getItemId()){
            case R.id.opcionEditar:
                editar(info.position);
                return true;
            case R.id.opcionEliminar:
                eliminar(info.position);
                return true;
            default:
                break;
        }
        return super.onContextItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id){
            case android.R.id.home:
                toActivity(MainActivity.class);
                return true;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)  {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            toActivity(MainActivity.class);
        }
        return super.onKeyDown(keyCode, event);
    }

    public boolean toActivity(Class activity){
        Intent nuevo = new Intent(this, activity);
        startActivity(nuevo);
        this.finish();
        return true;
    }

    public void cargarLista(){
        Cursor cursor = daoPersona.consultar(db);
        listaNombres = new ArrayList<>();
        listaIDs = new ArrayList<>();

        while (cursor.moveToNext()){
            listaNombres.add(cursor.getString(1));
            listaIDs.add(cursor.getInt(0));
        }

        adapter = new ArrayAdapter<String>(this,android.R.layout.simple_selectable_list_item,listaNombres);
        lvAmigos.setAdapter(adapter);
    }
    public void nuevaPersona(){
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = this.getLayoutInflater().inflate(R.layout.nuevo_amigo, null);

        txtNombre = (EditText) view.findViewById(R.id.txtNombre);

        builder.setView(view);
        builder.setTitle(R.string.agregarPersonaTitulo);
        builder.setPositiveButton(R.string.guardar, null);
        builder.setNegativeButton(R.string.cancelar, null);
        final AlertDialog dialog = builder.create();
        dialog.show();

        Button aceptar = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
        aceptar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!txtNombre.getText().toString().matches("")){
                    if(!daoPersona.existe(db, txtNombre.getText().toString())){
                        daoPersona.insertar(db, txtNombre.getText().toString());
                        dialog.dismiss();
                        cargarLista();
                    }else{
                        toast(getString(R.string.laPersonaExiste));
                    }
                }else{
                    toast(getString(R.string.debeIngresarNombre));
                }
            }
        });
    }

    public void editar(final int position){
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = this.getLayoutInflater().inflate(R.layout.nuevo_amigo, null);

        txtNombre = (EditText) view.findViewById(R.id.txtNombre);
        txtNombre.setText(listaNombres.get(position));

        builder.setView(view);
        builder.setTitle(R.string.editarNombre);
        builder.setPositiveButton(R.string.guardar, null);
        builder.setNegativeButton(R.string.cancelar, null);
        final AlertDialog dialog = builder.create();
        dialog.show();

        Button aceptar = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
        aceptar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!txtNombre.getText().toString().matches("")){
                    if(txtNombre.getText().toString().matches(listaNombres.get(position))){
                        dialog.dismiss();
                        cargarLista();
                    }else if(!daoPersona.existe(db, txtNombre.getText().toString())){
                        daoPersona.modificar(db, listaIDs.get(position), txtNombre.getText().toString());
                        dialog.dismiss();
                        cargarLista();
                    }else{
                        toast(getString(R.string.laPersonaExiste));
                    }
                }else{
                    toast(getString(R.string.debeIngresarNombre));
                }
            }
        });


    }

    public void eliminar(final int posicion){
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.confirmar));
        builder.setMessage(getString(R.string.deseaEliminar));
        builder.setPositiveButton(getString(R.string.si), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                daoPersona.eliminar(db, listaIDs.get(posicion));
                cargarLista();
            }
        });
        builder.setNegativeButton(getString(R.string.no), null);
        Dialog dialog = builder.create();
        dialog.show();
    }

    public void toast(String mensaje){
        Toast.makeText(this,mensaje, Toast.LENGTH_SHORT).show();
    }

}
