package com.nlazzos.cuantoponemos.Activities;

import android.app.AlertDialog;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.nlazzos.cuantoponemos.BDHelper.BDHelper;
import com.nlazzos.cuantoponemos.DAOs.DAOPersona;
import com.nlazzos.cuantoponemos.Entities.Persona;
import com.nlazzos.cuantoponemos.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    //BASE DE DATOS
    private BDHelper helper;
    private SQLiteDatabase db;

    //WIDGETS
    private TextView lblNuevoNombre;
    private Spinner cmbNombres;
    private Button cmdNuevoNombre;
    private EditText txtNuevoNombre;
    private EditText txtPuso;
    private ListView lvListado;
    private TextView lblIntro;
    private TextView lblIntroBotonAgregar;
    private TextView lblIntroBotonResolver;

    //COMBO
    private ArrayList<Persona> listaPersonasCombo;
    private ArrayList<String> listaCombo;
    private ArrayAdapter<CharSequence> adapterCombo;
    private Cursor cursorPersonas;

    //DAOS / ENTIDADES
    private DAOPersona daoPersona = new DAOPersona();
    private Persona persona;
    private ArrayList<Persona> listaPersonas = new ArrayList<Persona>();
    private ArrayList<Persona> listaPositivos;
    private ArrayList<Persona> listaNegativos;

    //LISTVIEW
    private ArrayList<String> listaInicial;
    private ArrayAdapter<String> adapterListView;
    private ArrayList<Persona> listaFinal;

    //VARs
    private boolean methodFlag = true;
    private boolean firstClick = true;
    private boolean firstAdd = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //ABRO LA CONEXION A LA BD
        helper = new BDHelper(this);
        db = helper.getWritableDatabase();

        //FIND DE WIDGETS
        lblIntro = (TextView) findViewById(R.id.lblIntroduccion);
        lblIntroBotonAgregar = (TextView) findViewById(R.id.lblIntroBotonAgregar);
        lblIntroBotonResolver = (TextView) findViewById(R.id.lblIntroBotonResolver);
        lvListado = (ListView) findViewById(R.id.lvListado);
        lvListado.setVisibility(View.GONE);
        registerForContextMenu(lvListado);

        final FloatingActionButton fabAdd = (FloatingActionButton) findViewById(R.id.fabAdd);
        fabAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(firstAdd){
                    lblIntro.setVisibility(View.GONE);
                    lblIntroBotonAgregar.setVisibility(View.GONE);
                    lblIntroBotonResolver.setVisibility(View.GONE);
                    lvListado.setVisibility(View.VISIBLE);
                    firstAdd = false;
                }
                if(!firstClick){
                    listaPersonas.clear();
                    firstClick = true;
                }
                agregarPersona();
            }
        });

        FloatingActionButton fabResolve = (FloatingActionButton) findViewById(R.id.fabResolve);
        fabResolve.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(firstClick){
                    calcularBalance();
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        switch (id){
            case R.id.administrarAmigos:
                toActivity(FriendsActivity.class);
                return true;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        if(v.getId() == R.id.lvListado){
            this.getMenuInflater().inflate(R.menu.menu_opciones, menu);
        }
        super.onCreateContextMenu(menu, v, menuInfo);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        switch(item.getItemId()){
            case R.id.opcionEditar:
                editarPersona(info.position);
                return true;
            case R.id.opcionEliminar:
                if(!firstClick){
                    listaPersonas.remove(info.position);
                    cargarListaInicial();
                }
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)  {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            setResult(RESULT_OK);
            this.finish();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    public boolean toActivity(Class activity){
        Intent nuevo = new Intent(this, activity);
        startActivity(nuevo);
        this.finish();
        return true;
    }

    private void cargarListaCombo(){
        cursorPersonas = daoPersona.consultar(db);
        listaPersonasCombo = new ArrayList<Persona>();
        listaCombo = new ArrayList<String>();

        while(cursorPersonas.moveToNext()){
            persona = new Persona();
            persona.setId(cursorPersonas.getInt(0));
            persona.setNombre(cursorPersonas.getString(1));
            listaPersonasCombo.add(persona);
        }

        listaCombo.add(getString(R.string.seleccione));

        for(int i=0; i<listaPersonasCombo.size(); i++){
            listaCombo.add(listaPersonasCombo.get(i).getNombre());
        }
    }

    public void agregarPersona(){
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = this.getLayoutInflater().inflate(R.layout.agregar_persona, null);

        //FIND DE LOS WIDGETS
        cmbNombres = (Spinner) view.findViewById(R.id.cmbNombres);
        cmdNuevoNombre = (Button) view.findViewById(R.id.cmdNuevoNombre);
        lblNuevoNombre = (TextView) view.findViewById(R.id.lblNuevoNombre);
        lblNuevoNombre.setVisibility(View.GONE);
        txtNuevoNombre = (EditText) view.findViewById(R.id.txtNuevoNombre);
        txtNuevoNombre.setEnabled(false);
        txtNuevoNombre.setVisibility(View.GONE);
        txtPuso = (EditText) view.findViewById(R.id.txtPuso);

        //CARGO EL COMBO/SPINNER
        cargarListaCombo();
        adapterCombo = new ArrayAdapter(this,android.R.layout.simple_spinner_dropdown_item, listaCombo);
        cmbNombres.setAdapter(adapterCombo);

        builder.setView(view);
        builder.setTitle(R.string.agregarPersonaTitulo);
        builder.setPositiveButton(R.string.aceptar, null);
        builder.setNegativeButton(R.string.cancelar, null);

        final AlertDialog dialog = builder.create();
        dialog.show();

        cmbNombres.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position != 0){
                    txtNuevoNombre.setEnabled(false);
                    lblNuevoNombre.setVisibility(View.GONE);
                    txtNuevoNombre.setVisibility(View.GONE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                txtNuevoNombre.setEnabled(true);
            }
        });

        cmdNuevoNombre.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                lblNuevoNombre.setVisibility(View.VISIBLE);
                txtNuevoNombre.setVisibility(View.VISIBLE);
                txtNuevoNombre.setEnabled(true);
                txtNuevoNombre.requestFocus();
                cmbNombres.setSelection(0);
            }
        });

        Button aceptar = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
        aceptar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean yaPuso = false;
                if(!txtPuso.getText().toString().matches("")){
                    if(cmbNombres.getSelectedItemPosition() == 0){
                        if(!txtNuevoNombre.getText().toString().matches("")){
                            if(!daoPersona.existe(db,txtNuevoNombre.getText().toString())){
                                daoPersona.insertar(db,txtNuevoNombre.getText().toString());
                            }
                            persona = new Persona();
                            persona = persona.obtenerDatos(daoPersona.consultarPorNombre(db,txtNuevoNombre.getText().toString()),Integer.parseInt(txtPuso.getText().toString()));
                            for(int i=0; i<listaPersonas.size(); i++){
                                if(listaPersonas.get(i).getId() == persona.getId()){
                                    yaPuso = true;
                                    break;
                                }
                            }
                            if(yaPuso){
                                toast(persona.getNombre() + " " + getString(R.string.yaPuso));
                            }else{
                                listaPersonas.add(persona);
                                cargarListaInicial();
                                dialog.dismiss();
                            }

                        }else{
                            toast(getString(R.string.debeIngresarOSeleccionar));
                        }
                    }else{
                        for(int i=0; i<listaPersonas.size(); i++){
                            if(listaPersonas.get(i).getId() == listaPersonasCombo.get(cmbNombres.getSelectedItemPosition()-1).getId()){
                                yaPuso = true;
                                break;
                            }
                        }
                        if(yaPuso){
                            toast(listaPersonasCombo.get(cmbNombres.getSelectedItemPosition()-1).getNombre() + " " + getString(R.string.yaPuso));
                        }else{
                            persona = new Persona();
                            persona.setId(listaPersonasCombo.get(cmbNombres.getSelectedItemPosition()-1).getId());
                            persona.setNombre(listaPersonasCombo.get(cmbNombres.getSelectedItemPosition()-1).getNombre());
                            persona.setPuso(Integer.parseInt(txtPuso.getText().toString()));
                            listaPersonas.add(persona);
                            cargarListaInicial();
                            dialog.dismiss();
                        }
                    }
                }else{
                    toast(getString(R.string.debeIngresarCuantoPuso));
                }
            }
        });
    }

    public void editarPersona(final int posicion){
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = this.getLayoutInflater().inflate(R.layout.agregar_persona, null);

        //FIND DE LOS WIDGETS
        cmbNombres = (Spinner) view.findViewById(R.id.cmbNombres);
        cmdNuevoNombre = (Button) view.findViewById(R.id.cmdNuevoNombre);
        lblNuevoNombre = (TextView) view.findViewById(R.id.lblNuevoNombre);
        lblNuevoNombre.setVisibility(View.GONE);
        txtNuevoNombre = (EditText) view.findViewById(R.id.txtNuevoNombre);
        txtNuevoNombre.setEnabled(false);
        txtNuevoNombre.setVisibility(View.GONE);
        txtPuso = (EditText) view.findViewById(R.id.txtPuso);

        //CARGO EL COMBO/SPINNER
        cargarListaCombo();
        adapterCombo = new ArrayAdapter(this,android.R.layout.simple_spinner_dropdown_item, listaCombo);
        cmbNombres.setAdapter(adapterCombo);

        //SETEO LOS DATOS
        for(int i=0; i<listaPersonasCombo.size(); i++){
            if(listaPersonas.get(posicion).getId() == listaPersonasCombo.get(i).getId()){
                cmbNombres.setSelection(i+1);
                break;
            }
        }
        txtPuso.setText(String.valueOf(listaPersonas.get(posicion).getPuso()));

        builder.setView(view);
        builder.setTitle(R.string.agregarPersonaTitulo);
        builder.setPositiveButton(R.string.aceptar, null);
        builder.setNegativeButton(R.string.cancelar, null);

        final AlertDialog dialog = builder.create();
        dialog.show();

        cmbNombres.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position != 0){
                    txtNuevoNombre.setEnabled(false);
                    lblNuevoNombre.setVisibility(View.GONE);
                    txtNuevoNombre.setVisibility(View.GONE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                txtNuevoNombre.setEnabled(true);
            }
        });

        cmdNuevoNombre.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                lblNuevoNombre.setVisibility(View.VISIBLE);
                txtNuevoNombre.setVisibility(View.VISIBLE);
                txtNuevoNombre.setEnabled(true);
                txtNuevoNombre.requestFocus();
                cmbNombres.setSelection(0);
            }
        });

        Button aceptar = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
        aceptar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean yaPuso = false;
                if(!txtPuso.getText().toString().matches("")){
                    if(cmbNombres.getSelectedItemPosition() == 0){
                        if(!txtNuevoNombre.getText().toString().matches("")){
                            if(!daoPersona.existe(db,txtNuevoNombre.getText().toString())){
                                daoPersona.insertar(db,txtNuevoNombre.getText().toString());
                            }
                            persona = new Persona();
                            persona = persona.obtenerDatos(daoPersona.consultarPorNombre(db,txtNuevoNombre.getText().toString()),Integer.parseInt(txtPuso.getText().toString()));
                            for(int i=0; i<listaPersonas.size(); i++){
                                if(listaPersonas.get(i).getId() == persona.getId()){
                                    yaPuso = true;
                                    break;
                                }
                            }
                            if(yaPuso){
                                toast(persona.getNombre() + " " + getString(R.string.yaPuso));
                            }else{
                                listaPersonas.get(posicion).setId(persona.getId());
                                listaPersonas.get(posicion).setNombre(persona.getNombre());
                                listaPersonas.get(posicion).setPuso(persona.getPuso());
                                cargarListaInicial();
                                dialog.dismiss();
                            }
                        }else{
                            toast(getString(R.string.debeIngresarOSeleccionar));
                        }
                    }else{
                        for(int i=0; i<listaPersonas.size(); i++){
                            if(listaPersonas.get(i).getId() == listaPersonasCombo.get(cmbNombres.getSelectedItemPosition()-1).getId()){
                                yaPuso = true;
                                break;
                            }
                        }
                        if(yaPuso){
                            toast(listaPersonas.get(posicion).getNombre() + " " + getString(R.string.yaPuso));
                        }else{
                            listaPersonas.get(posicion).setId(listaPersonasCombo.get(cmbNombres.getSelectedItemPosition()-1).getId());
                            listaPersonas.get(posicion).setNombre(listaPersonasCombo.get(cmbNombres.getSelectedItemPosition()-1).getNombre());
                            listaPersonas.get(posicion).setPuso(Integer.parseInt(txtPuso.getText().toString()));
                            cargarListaInicial();
                            dialog.dismiss();
                        }
                    }
                }else{
                    toast(getString(R.string.debeIngresarCuantoPuso));
                }
            }
        });
    }

    public void cargarListaInicial(){
        listaInicial = new ArrayList<String>();

        for(int i=0; i<listaPersonas.size(); i++){
            listaInicial.add(listaPersonas.get(i).getNombre() + " " + getString(R.string.puso$) + String.valueOf(listaPersonas.get(i).getPuso()));
        }

        adapterListView = new ArrayAdapter<String>(this,android.R.layout.simple_selectable_list_item, listaInicial);
        lvListado.setAdapter(adapterListView);
    }

    public void calcularBalance(){
        int cadaUno;
        int total = 0;

        if(listaPersonas.size() == 0){
            toast(getString(R.string.debeIngresarDatos));
            return;
        }else if(listaPersonas.size() == 1){
            toast(getString(R.string.debeIngresarMasDatos));
            return;
        }else{
            for(int i=0; i<listaPersonas.size(); i++){
                total += listaPersonas.get(i).getPuso();
            }
            cadaUno = total / listaPersonas.size();

            if(total == 0){
                toast(getString(R.string.nadiePuso));
                return;
            }

            firstClick = false;
        }

        for(int i=0; i<listaPersonas.size(); i++) {
            listaPersonas.get(i).setBalance(listaPersonas.get(i).getPuso() - cadaUno);
        }

        resolver();
    }

    public void ordenarListas(){
        listaPositivos = new ArrayList<Persona>();
        listaNegativos = new ArrayList<Persona>();

        for(int i=0; i<listaPersonas.size(); i++){
            listaPersonas.get(i).resetMap();
            persona = listaPersonas.get(i).clone();
            if(listaPersonas.get(i).getBalance() > 0){
                listaPositivos.add(persona);
            }
            if(listaPersonas.get(i).getBalance() < 0){
                listaNegativos.add(persona);
            }
        }

        Collections.sort(listaPositivos);
        Collections.reverse(listaNegativos);
    }

    public void resolver(){
        listaFinal = new ArrayList<Persona>();
        ordenarListas();

        int j = 0;
        int i = 0;
        while(listaPositivos.get(i).getBalance() != 0){
            if(listaNegativos.get(j).getBalance() != 0 && j< listaNegativos.size()) {
                if (listaPositivos.get(i).getBalance() >= (listaNegativos.get(j).getBalance() * -1)) {
                    listaNegativos.get(j).setLeDebe(listaPositivos.get(i).getNombre(), (listaNegativos.get(j).getBalance() * -1));
                    listaPositivos.get(i).setBalance(listaPositivos.get(i).getBalance() + listaNegativos.get(j).getBalance());
                    listaNegativos.get(j).setBalance(0);

                    listaFinal.add(listaNegativos.get(j));
                    listaNegativos.remove(j);
                }else if (listaPositivos.get(i).getBalance() < (listaNegativos.get(j).getBalance() * -1)) {
                    listaNegativos.get(j).setLeDebe(listaPositivos.get(i).getNombre(), listaPositivos.get(i).getBalance());
                    listaNegativos.get(j).setBalance(listaNegativos.get(j).getBalance() + listaPositivos.get(i).getBalance());
                    listaPositivos.get(i).setBalance(0);

                    listaPositivos.remove(i);
                }
            }
            if(listaPositivos.size() == 0 || listaNegativos.size() == 0){
                break;
            }

            Collections.sort(listaPositivos);
            Collections.reverse(listaNegativos);
        }

        cargarListaResuelta();
    }

    public void cargarListaResuelta(){
        listaInicial = new ArrayList<String>();

        for(int i = 0; i< listaFinal.size(); i++){
            for (Map.Entry<String, Integer> leDebe : listaFinal.get(i).getLeDebe().entrySet())
                listaInicial.add(listaFinal.get(i).getNombre() + " " + getString(R.string.leTieneQueDar) +
                        leDebe.getValue() + " " + getString(R.string.aQuien) + " " + leDebe.getKey());
        }

        adapterListView = new ArrayAdapter<String>(this,android.R.layout.simple_selectable_list_item, listaInicial);
        lvListado.setAdapter(adapterListView);
    }

    public void cargarListaPrueba(){
        ordenarListas();
        listaInicial = new ArrayList<String>();

        listaInicial.add("LISTA ORIGINAL");
        for(int i=0; i<listaPersonas.size(); i++){
            listaInicial.add(listaPersonas.get(i).getNombre() + " puso $" + listaPersonas.get(i).getPuso() + " balance $"
                    + listaPersonas.get(i).getBalance());
        }

        Collections.sort(listaPersonas);

        listaInicial.add("LISTA ORDENADA");
        for(int i=0; i<listaPersonas.size(); i++){
            listaInicial.add(listaPersonas.get(i).getNombre() + " puso $" + listaPersonas.get(i).getPuso() + " balance $"
                    + listaPersonas.get(i).getBalance());
        }

        listaInicial.add("LISTA POSITIVOS");
        for(int i = 0; i< listaPositivos.size(); i++){
            listaInicial.add(listaPositivos.get(i).getNombre() + " puso $" + listaPositivos.get(i).getPuso() + " balance $"
                    + listaPositivos.get(i).getBalance());
        }
        listaInicial.add("LISTA NEGATIVOS");
        for(int i = 0; i< listaNegativos.size(); i++){
            listaInicial.add(listaNegativos.get(i).getNombre() + " puso $" + listaNegativos.get(i).getPuso() + " balance $"
                    + listaNegativos.get(i).getBalance());
        }

        adapterListView = new ArrayAdapter<String>(this,android.R.layout.simple_selectable_list_item, listaInicial);
        lvListado.setAdapter(adapterListView);
    }

    public void toast(String mensaje){
        Toast.makeText(this,mensaje, Toast.LENGTH_SHORT).show();
    }
}
