package com.example.jorgebarraza.mercadomoya_cliente.Actividades;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.jorgebarraza.mercadomoya_cliente.Adapters.ArticulosAdapter;
import com.example.jorgebarraza.mercadomoya_cliente.Adapters.CategoriasAdapter;
import com.example.jorgebarraza.mercadomoya_cliente.Adapters.PedidosAdapter;
import com.example.jorgebarraza.mercadomoya_cliente.DB.Servicios;
import com.example.jorgebarraza.mercadomoya_cliente.Modelos.Articulo;
import com.example.jorgebarraza.mercadomoya_cliente.Modelos.Categoria;
import com.example.jorgebarraza.mercadomoya_cliente.Modelos.Pedido;
import com.example.jorgebarraza.mercadomoya_cliente.R;
import com.example.jorgebarraza.mercadomoya_cliente.Utils.Utilerias;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MenuPrincipal extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private Context context;
    private ProgressDialog progressDialog;
    private RecyclerView recyclerView;
    private ArticulosAdapter articulosAdapter;
    private PedidosAdapter pedidosAdapter;
    private CategoriasAdapter categoriasAdapter;
    private String usuario;
    private Pedido pedidoRespuesta;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_principal);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        recyclerView = findViewById(R.id.lstMain);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        context = MenuPrincipal.this;
        setSupportActionBar(toolbar);
        usuario = Utilerias.getPreference(context,"usuario");

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setImageResource(R.drawable.shopping);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, MiCarritoActivity.class);
                startActivity(intent);
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_principal, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_articulos) {
            Utilerias.savePreference(context, "opcion", "articulos");
            setTitle("Articulos");
            consultarArticulos();
        } else if (id == R.id.nav_categorias) {
            Utilerias.savePreference(context, "opcion", "categorias");
            setTitle("Categorias");
            consultarCategorias();
        } else if (id == R.id.nav_pedidos) {
            Utilerias.savePreference(context, "opcion", "pedidos");
            setTitle("Mis pedidos");
            consultarPedidosPorUsuario();

        /*} else if (id == R.id.nav_carrito) {
            Utilerias.savePreference(context, "opcion", "carrito");
            setTitle("Mi carrito");
            //consultarArticulos();

        } else if (id == R.id.nav_share) {*/


        } else if (id == R.id.nav_logout) {
            Utilerias.savePreference(context,"usuario","");
            Utilerias.savePreference(context,"usuarioID","");
            Utilerias.savePreference(context,"nombre","");
            Utilerias.savePreference(context,"pedidoID","");
            Intent intent = new Intent(context,MainActivity.class);
            startActivity(intent);
            finish();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void consultarArticulos() {
        progressDialog = new ProgressDialog(context);
        progressDialog.setMessage("Obteniendo articulos...");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.show();
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = Servicios.obtenerTodosLosArticulos();
        StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONArray jsonArray = new JSONArray(response);
                            if (jsonArray.length() > 0) {
                                ArrayList<Articulo> listArticulos = new ArrayList<Articulo>();
                                Gson gson = new GsonBuilder().create();
                                for (int i = 0; i < jsonArray.length(); i++) {
                                    Articulo alumno = gson.fromJson(String.valueOf(jsonArray.getJSONObject(i)), Articulo.class);
                                    listArticulos.add(alumno);
                                    //baseDeDatos.insertOrReplaceAlumnos(alumno);
                                }
                                articulosAdapter = new ArticulosAdapter(listArticulos);
                                recyclerView.setAdapter(articulosAdapter);
                                if(progressDialog != null){
                                    progressDialog.dismiss();
                                    progressDialog = null;
                                }
                            } else {
                                Toast.makeText(context, "Articulos no disponibles", Toast.LENGTH_LONG).show();
                                ArrayList<Articulo> listaVacia = new ArrayList<>();
                                articulosAdapter = new ArticulosAdapter(listaVacia);
                                recyclerView.setAdapter(articulosAdapter);
                                if(progressDialog != null){
                                    progressDialog.dismiss();
                                    progressDialog = null;
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(context, "Ocurrio un error al procesar los articulos", Toast.LENGTH_LONG).show();
                            if(progressDialog != null){
                                progressDialog.dismiss();
                                progressDialog = null;
                            }
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if(progressDialog != null){
                            progressDialog.dismiss();
                            progressDialog = null;
                        }
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                //params.put("name", "Alif");
                //params.put("domain", "http://itsalif.info");

                return params;
            }
        };
        queue.add(postRequest);
    }

    private void consultarCategorias() {
        progressDialog = new ProgressDialog(context);
        progressDialog.setMessage("Obteniendo categorias...");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.show();
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = Servicios.obtenerTodasLasCategorias();
        StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONArray jsonArray = new JSONArray(response);
                            if (jsonArray.length() > 0) {
                                ArrayList<Categoria> list = new ArrayList<Categoria>();
                                Gson gson = new GsonBuilder().create();
                                for (int i = 0; i < jsonArray.length(); i++) {
                                    Categoria obj = gson.fromJson(String.valueOf(jsonArray.getJSONObject(i)), Categoria.class);
                                    list.add(obj);
                                    //baseDeDatos.insertOrReplaceAlumnos(alumno);
                                }
                                categoriasAdapter = new CategoriasAdapter(list);
                                recyclerView.setAdapter(categoriasAdapter);
                                progressDialog.dismiss();
                                progressDialog = null;
                            } else {
                                Toast.makeText(context, "Categorias no disponibles", Toast.LENGTH_LONG).show();
                                ArrayList<Categoria> listaVacia = new ArrayList<>();
                                categoriasAdapter = new CategoriasAdapter(listaVacia);
                                recyclerView.setAdapter(categoriasAdapter);
                                progressDialog.dismiss();
                                progressDialog = null;
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(context, "Ocurrio un error al procesar los datos", Toast.LENGTH_LONG).show();
                            progressDialog.dismiss();
                            progressDialog = null;
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        progressDialog.dismiss();
                        progressDialog = null;
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                //params.put("name", "Alif");
                //params.put("domain", "http://itsalif.info");

                return params;
            }
        };
        queue.add(postRequest);
    }

    private void consultarPedidosPorUsuario() {
        progressDialog = new ProgressDialog(context);
        progressDialog.setMessage("Obteniendo pedidos...");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.show();
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = Servicios.obtenerTodosLosPedidos();
        StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONArray jsonArray = new JSONArray(response);
                            if (jsonArray.length() > 0) {
                                ArrayList<Pedido> list = new ArrayList<Pedido>();
                                Gson gson = new GsonBuilder().create();
                                for (int i = 0; i < jsonArray.length(); i++) {
                                    Pedido obj = gson.fromJson(String.valueOf(jsonArray.getJSONObject(i)), Pedido.class);
                                    list.add(obj);
                                    //baseDeDatos.insertOrReplaceAlumnos(alumno);
                                }
                                ArrayList<Pedido> pedidosFiltrados = new ArrayList<Pedido>();
                                for(Pedido ped : list){
                                    if(ped.getUsuario().equals(usuario)){
                                        pedidosFiltrados.add(ped);
                                    }
                                }
                                pedidosAdapter = new PedidosAdapter(pedidosFiltrados);
                                recyclerView.setAdapter(pedidosAdapter);
                                progressDialog.dismiss();
                                progressDialog = null;
                            } else {
                                Toast.makeText(context, "Pedidos no disponibles", Toast.LENGTH_LONG).show();
                                ArrayList<Pedido> listaVacia = new ArrayList<>();
                                pedidosAdapter = new PedidosAdapter(listaVacia);
                                recyclerView.setAdapter(pedidosAdapter);
                                progressDialog.dismiss();
                                progressDialog = null;
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(context, "Ocurrio un error al procesar los datos", Toast.LENGTH_LONG).show();
                            progressDialog.dismiss();
                            progressDialog = null;
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        progressDialog.dismiss();
                        progressDialog = null;
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                //params.put("name", "Alif");
                //params.put("domain", "http://itsalif.info");

                return params;
            }
        };
        queue.add(postRequest);
    }
}
