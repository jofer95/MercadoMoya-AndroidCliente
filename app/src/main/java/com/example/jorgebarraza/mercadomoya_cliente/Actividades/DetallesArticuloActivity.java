package com.example.jorgebarraza.mercadomoya_cliente.Actividades;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.jorgebarraza.mercadomoya_cliente.DB.Servicios;
import com.example.jorgebarraza.mercadomoya_cliente.Modelos.Articulo;
import com.example.jorgebarraza.mercadomoya_cliente.Modelos.ArticuloPedido;
import com.example.jorgebarraza.mercadomoya_cliente.Modelos.Pedido;
import com.example.jorgebarraza.mercadomoya_cliente.R;
import com.example.jorgebarraza.mercadomoya_cliente.Utils.Utilerias;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DetallesArticuloActivity extends AppCompatActivity {

    private TextView tvNombre, tvDesc, tvPrecio, tvCategoria;
    private ImageView imgImagen;
    private Spinner spnCantidad;
    private Button btnAñadirAlCarrito, btnEliminar;
    private Context context;
    private ProgressDialog progressDialog;
    private String articuloID = "";
    private Articulo articulo;
    private ArticuloPedido articuloPedido;
    private String pedidoID;
    private String pedidoTempID;
    private Pedido pedidoRespuesta;
    private RequestQueue request;
    private String detallesCarrito;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalles_articulo);
        context = DetallesArticuloActivity.this;
        tvNombre = findViewById(R.id.tvNombre);
        tvDesc = findViewById(R.id.tvDescripcion);
        tvPrecio = findViewById(R.id.tvPrecio);
        tvCategoria = findViewById(R.id.tvCategoria);
        btnAñadirAlCarrito = findViewById(R.id.btnAñadirAlCarrito);
        imgImagen = findViewById(R.id.imgImagenProducto);
        spnCantidad = findViewById(R.id.spnCantidad);
        btnEliminar = findViewById(R.id.btnEliminarCarrito);
        //Utilerias.obtemerImei(context);

        List<String> cantidades = new ArrayList<>();
        cantidades.add("1");
        cantidades.add("2");
        cantidades.add("3");
        cantidades.add("4");
        cantidades.add("5");
        cantidades.add("6");
        cantidades.add("7");
        cantidades.add("8");
        cantidades.add("9");
        cantidades.add("10");
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(context,
                android.R.layout.simple_spinner_item, cantidades);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnCantidad.setAdapter(dataAdapter);

        setTitle("Detalles del articulo");
        Intent intent = getIntent();
        detallesCarrito = intent.getStringExtra("detalles");
        if (detallesCarrito != null) {
            spnCantidad.setEnabled(false);
            btnAñadirAlCarrito.setVisibility(View.GONE);
            btnEliminar.setVisibility(View.VISIBLE);
        }
        articuloID = intent.getStringExtra("articuloID");
        if (articuloID != null) {
            obtenerArticuloPorID();
        }
        pedidoID = Utilerias.getPreference(context, "pedidoID");


        btnAñadirAlCarrito.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String pedidoTemp = String.valueOf(System.currentTimeMillis());
                if (Utilerias.getPreference(context, "pedidoID").equals("")) {
                    Pedido pedido = new Pedido();
                    pedido.setTempID(pedidoTemp);
                    pedido.setUsuario(Utilerias.getPreference(context, "usuario"));
                    int cantidad = Integer.valueOf(spnCantidad.getSelectedItem().toString());
                    articuloPedido.setCantidad(cantidad);
                    pedido.getArticulos().add(articuloPedido);
                    pedido.setTotal(articulo.getPrecio() * cantidad);
                    añadirAlCarrito(pedido);
                } else {
                    //YA EXISTE UN PEDIDO
                    obtenerPedidoPorID();
                }
            }
        });
        btnEliminar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                obtenerPedidoPorID();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void asignarFoto(String foto_url) {
        request = Volley.newRequestQueue(context);
        ImageRequest imageRequest = new ImageRequest(foto_url, new Response.Listener<Bitmap>() {
            @Override
            public void onResponse(Bitmap response) {
                imgImagen.setImageBitmap(response);
            }
        }, 0, 0, ImageView.ScaleType.CENTER, null, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                //Toast.makeText(contexto, "Error al obtener fotografia", Toast.LENGTH_SHORT).show();
            }
        });
        request.add(imageRequest);
    }

    private void obtenerArticuloPorID() {
        try {
            progressDialog = new ProgressDialog(context);
            progressDialog.setMessage("Obteniendo datos...");
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.show();
            RequestQueue queue = Volley.newRequestQueue(this);
            String url = Servicios.obtenerArticuloPorID();
            JSONObject jsonBody = new JSONObject();
            jsonBody.put("ArticuloID", articuloID);

            JsonObjectRequest jsonOblect = new JsonObjectRequest(Request.Method.POST, url, jsonBody, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    if (response.length() > 0) {
                        ArrayList<Articulo> listArticulos = new ArrayList<Articulo>();
                        Gson gson = new GsonBuilder().create();
                        Articulo arti = gson.fromJson(String.valueOf(response), Articulo.class);
                        articulo = arti;
                        articuloPedido = new ArticuloPedido();
                        articuloPedido.setArticuloID(arti.getArticuloID());
                        articuloPedido.setPrecio(arti.getPrecio());
                        articuloPedido.setNombre(arti.getNombre());
                        articuloPedido.setImagenURL(arti.getImagenURL());
                        tvNombre.setText(arti.getNombre());
                        tvDesc.setText(arti.getDescripcion());
                        tvPrecio.setText(arti.getPrecio().toString());
                        tvCategoria.setText(arti.getCategoria());
                        asignarFoto(arti.getImagenURL());
                        if (progressDialog != null) {
                            progressDialog.dismiss();
                            progressDialog = null;
                        }
                    } else {
                        Toast.makeText(context, "Datos no disponibles", Toast.LENGTH_LONG).show();
                        if (progressDialog != null) {
                            progressDialog.dismiss();
                            progressDialog = null;
                        }
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Utilerias.mostrarToast(context, "Error al procesar la peticion");
                    if (progressDialog != null) {
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
            if (progressDialog != null) {
                progressDialog.dismiss();
                progressDialog = null;
            }
        }
    }

    private void añadirAlCarrito(final Pedido pedido) {
        try {
            progressDialog = new ProgressDialog(context);
            progressDialog.setMessage("Guardando datos...");
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.show();
            RequestQueue queue = Volley.newRequestQueue(this);
            String url = Servicios.crearActualizarPedido();
            if (pedidoID != null) {
                if(!pedidoID.equals("")){
                    pedido.setPedidoID(pedidoID);
                }
            }
            String jsonArticulo = new Gson().toJson(pedido);
            JSONObject jsonBody = new JSONObject(jsonArticulo);

            JsonObjectRequest jsonOblect = new JsonObjectRequest(Request.Method.POST, url, jsonBody, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    try {
                        if (response.length() > 0) {
                            Gson gson = new GsonBuilder().create();
                            Pedido pedido = gson.fromJson(String.valueOf(response), Pedido.class);
                            //ObjectMapper mapper = new ObjectMapper();
                            //Pedido student = mapper.readValue(response.toString(), Pedido.class);
                            pedidoRespuesta = pedido;
                            Utilerias.savePreference(context, "pedidoID", pedido.getPedidoID());
                            if (progressDialog != null) {
                                progressDialog.dismiss();
                                progressDialog = null;
                            }
                            finish();
                        } else {
                            Toast.makeText(context, "Datos no disponibles", Toast.LENGTH_LONG).show();
                            if (progressDialog != null) {
                                progressDialog.dismiss();
                                progressDialog = null;
                            }
                        }
                    } catch (Exception ex) {
                        ex.getStackTrace();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Utilerias.mostrarToast(context, "Error al procesar la peticion");
                    if (progressDialog != null) {
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
            if (progressDialog != null) {
                progressDialog.dismiss();
                progressDialog = null;
            }
        }
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
                        pedidoRespuesta = obj;
                        if (progressDialog != null) {
                            progressDialog.dismiss();
                            progressDialog = null;
                        }
                        int cantidad = Integer.valueOf(spnCantidad.getSelectedItem().toString());
                        articuloPedido.setCantidad(cantidad);
                        pedidoRespuesta.getArticulos().add(articuloPedido);
                        pedidoRespuesta.setUsuario(Utilerias.getPreference(context, "usuario"));
                        pedidoRespuesta.setTotal(pedidoRespuesta.getTotal() + (articuloPedido.getPrecio() * cantidad));
                        if (detallesCarrito == null) {
                            añadirAlCarrito(pedidoRespuesta);
                        }else{
                            Double totalNuevo = 0.0;
                            List<ArticuloPedido> pedidosNuevos = new ArrayList<>();
                            for(ArticuloPedido articuloPedido : pedidoRespuesta.getArticulos()){
                                if(!articuloPedido.getArticuloID().equals(articuloID)){
                                    pedidosNuevos.add(articuloPedido);
                                    totalNuevo += (articuloPedido.getPrecio()*articuloPedido.getCantidad());
                                }
                            }
                            pedidoRespuesta.setTotal(totalNuevo);
                            pedidoRespuesta.setArticulos(pedidosNuevos);
                            añadirAlCarrito(pedidoRespuesta);
                        }
                    } else {
                        Toast.makeText(context, "Datos no disponibles", Toast.LENGTH_LONG).show();
                        if (progressDialog != null) {
                            progressDialog.dismiss();
                            progressDialog = null;
                        }
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Utilerias.mostrarToast(context, "Error al procesar la peticion");
                    if (progressDialog != null) {
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
            if (progressDialog != null) {
                progressDialog.dismiss();
                progressDialog = null;
            }
        }
    }
}
