package com.example.jorgebarraza.mercadomoya_cliente.Actividades;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.jorgebarraza.mercadomoya_cliente.Adapters.ArticulosPedidosAdapter;
import com.example.jorgebarraza.mercadomoya_cliente.Adapters.CategoriasAdapter;
import com.example.jorgebarraza.mercadomoya_cliente.DB.Servicios;
import com.example.jorgebarraza.mercadomoya_cliente.Modelos.ArticuloPedido;
import com.example.jorgebarraza.mercadomoya_cliente.Modelos.Pedido;
import com.example.jorgebarraza.mercadomoya_cliente.R;
import com.example.jorgebarraza.mercadomoya_cliente.Utils.Utilerias;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MiCarritoActivity extends AppCompatActivity {

    private TextView tvTotal;
    private Button btnComprar;
    private RecyclerView recyclerView;
    private Context context;
    private ProgressDialog progressDialog;
    private String pedidoID;
    private Pedido pedidoRespuesta;
    private ArticulosPedidosAdapter articulosPedidosAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mi_carrito);
        context = MiCarritoActivity.this;
        tvTotal = findViewById(R.id.tvTotal);
        btnComprar = findViewById(R.id.btnPagarCarrito);
        recyclerView = findViewById(R.id.listCarrito);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        pedidoID = Utilerias.getPreference(context,"pedidoID");


        setTitle("Mi carrito");
        btnComprar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!pedidoID.equals("")){
                    String usuarioID = Utilerias.getPreference(context,"usuarioID");
                    String usuario = Utilerias.getPreference(context,"usuario");
                    if(!usuarioID.equals("")){
                        pedidoRespuesta.setPagado(true);
                        pedidoRespuesta.setUsuario(usuario);
                        guardarPedido(pedidoRespuesta);
                    }else{
                        Utilerias.mostrarToast(context,"Favor de iniciar sesion para comprar");
                        Intent intent = new Intent(context,MainActivity.class);
                        startActivity(intent);
                    }
                }else{
                    Utilerias.mostrarToast(context,"Agregue articulos para comprar...");
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        obtenerPedidoPorID();
    }

    private void obtenerPedidoPorID() {
        try {
            progressDialog = new ProgressDialog(context);
            progressDialog.setMessage("Obteniendo datos...");
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.show();
            RequestQueue queue = Volley.newRequestQueue(this);
            String url = Servicios.obtenerPedidoPorID();
            JSONObject jsonBody = new JSONObject();
            jsonBody.put("pedidoID", pedidoID);

            JsonObjectRequest jsonOblect = new JsonObjectRequest(Request.Method.POST, url, jsonBody, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    if (response.length() > 0) {
                        Gson gson = new GsonBuilder().create();
                        Pedido obj = gson.fromJson(String.valueOf(response), Pedido.class);
                        tvTotal.setText("$"+obj.getTotal().toString());
                        if(obj.getArticulos() !=null){
                            if(obj.getArticulos().size() > 0){
                                articulosPedidosAdapter = new ArticulosPedidosAdapter(obj.getArticulos());
                                recyclerView.setAdapter(articulosPedidosAdapter);
                            }else{
                                List<ArticuloPedido> articuloPedidos = new ArrayList<>();
                                articulosPedidosAdapter = new ArticulosPedidosAdapter(articuloPedidos);
                                recyclerView.setAdapter(articulosPedidosAdapter);
                            }
                        }
                        pedidoRespuesta = obj;
                        if(progressDialog != null){
                            progressDialog.dismiss();
                            progressDialog = null;
                        }
                    } else {
                        Toast.makeText(context, "Datos no disponibles", Toast.LENGTH_LONG).show();
                        if(progressDialog != null){
                            progressDialog.dismiss();
                            progressDialog = null;
                        }
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Utilerias.mostrarToast(context, "Error al procesar la peticion");
                    if(progressDialog != null){
                        progressDialog.dismiss();
                        progressDialog = null;
                    }
                }
            }) {
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    HashMap<String, String> headers = new HashMap<String, String>();
                    // headers.put("Content-Type", "application/json; charset=utf-8");
                    return headers;
                }

                @Override
                public String getBodyContentType() {
                    return "application/json";
                }
            };
            queue.add(jsonOblect);
            //VolleyApplication.getInstance().addToRequestQueue(jsonOblect);
        } catch (Exception ex) {
            Utilerias.mostrarToast(context, "Error al procesar la peticion");
            if(progressDialog != null){
                progressDialog.dismiss();
                progressDialog = null;
            }
        }
    }

    private void guardarPedido(final Pedido pedido) {
        try {
            progressDialog = new ProgressDialog(context);
            progressDialog.setMessage("Realizando compra!!...");
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.show();
            RequestQueue queue = Volley.newRequestQueue(this);
            String url = Servicios.crearActualizarPedido();
            if (pedidoID != null) {
                pedido.setPedidoID(pedidoID);
            }
            String jsonArticulo = new Gson().toJson(pedido);
            JSONObject jsonBody = new JSONObject(jsonArticulo);

            JsonObjectRequest jsonOblect = new JsonObjectRequest(Request.Method.POST, url, jsonBody, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    try{
                        if (response.length() > 0) {
                            Gson gson = new GsonBuilder().create();
                            Pedido pedido = gson.fromJson(String.valueOf(response), Pedido.class);
                            //ObjectMapper mapper = new ObjectMapper();
                            //Pedido student = mapper.readValue(response.toString(), Pedido.class);
                            pedidoRespuesta = pedido;
                            Utilerias.savePreference(context,"pedidoID","");
                            Utilerias.mostrarToast(context,"Compra realizada con exito!!");
                            if(progressDialog != null){
                                progressDialog.dismiss();
                                progressDialog = null;
                            }
                            finish();
                        } else {
                            Toast.makeText(context, "Datos no disponibles", Toast.LENGTH_LONG).show();
                            if(progressDialog != null){
                                progressDialog.dismiss();
                                progressDialog = null;
                            }
                        }
                    }catch (Exception ex){
                        ex.getStackTrace();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Utilerias.mostrarToast(context, "Error al procesar la peticion");
                    if(progressDialog != null){
                        progressDialog.dismiss();
                        progressDialog = null;
                    }
                }
            }) {
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    HashMap<String, String> headers = new HashMap<String, String>();
                    // headers.put("Content-Type", "application/json; charset=utf-8");
                    return headers;
                }

                @Override
                public String getBodyContentType() {
                    return "application/json";
                }
            };
            queue.add(jsonOblect);
            //VolleyApplication.getInstance().addToRequestQueue(jsonOblect);
        } catch (Exception ex) {
            Utilerias.mostrarToast(context, "Error al procesar la peticion");
            if(progressDialog != null){
                progressDialog.dismiss();
                progressDialog = null;
            }
        }
    }
}
