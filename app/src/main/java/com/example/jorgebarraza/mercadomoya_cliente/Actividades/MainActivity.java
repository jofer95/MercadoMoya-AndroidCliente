package com.example.jorgebarraza.mercadomoya_cliente.Actividades;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.jorgebarraza.mercadomoya_cliente.DB.Servicios;
import com.example.jorgebarraza.mercadomoya_cliente.Modelos.Usuario;
import com.example.jorgebarraza.mercadomoya_cliente.R;
import com.example.jorgebarraza.mercadomoya_cliente.Utils.Utilerias;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private EditText edtUsuario;
    private EditText edtContra;
    private Button btnIniciarSesion;
    private Button btnEntrarTienda;
    private CheckBox checkRecordar;
    private Context context;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = MainActivity.this;

        //Finds
        edtUsuario = findViewById(R.id.edtUsuario);
        btnEntrarTienda = findViewById(R.id.btnEntrarTienda);
        btnIniciarSesion = findViewById(R.id.btnIniciarSesion);
        checkRecordar = findViewById(R.id.chRecordar);

        btnIniciarSesion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String correo = edtUsuario.getText().toString();
                if(!correo.equals("")){
                    obtenerUsuarioPorCorreo();
                }else{
                    Utilerias.mostrarToast(context,"Ingrese un usuario o correo");
                }
            }
        });

        btnEntrarTienda.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context,MenuPrincipal.class);
                startActivity(intent);
                finish();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        String usuarioID = Utilerias.getPreference(context,"usuarioID");
        if(!usuarioID.equals("")){
            Intent intent = new Intent(context,MenuPrincipal.class);
            startActivity(intent);
        }
    }

    private void obtenerUsuarioPorCorreo() {
        try {
            progressDialog = new ProgressDialog(context);
            progressDialog.setMessage("Iniciando sesion...");
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.show();
            RequestQueue queue = Volley.newRequestQueue(this);
            String url = Servicios.obtenerUsuarioPorCorreo();
            JSONObject jsonBody = new JSONObject();
            jsonBody.put("Correo", edtUsuario.getText());

            JsonObjectRequest jsonOblect = new JsonObjectRequest(Request.Method.POST, url, jsonBody, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    if (response.length() > 0) {
                        ArrayList<Usuario> list = new ArrayList<Usuario>();
                        Gson gson = new GsonBuilder().create();
                        Usuario obj = gson.fromJson(String.valueOf(response), Usuario.class);
                        if(obj.getCorreo() == null){
                            Toast.makeText(context, "Datos incorrectos o usuario no existe", Toast.LENGTH_LONG).show();
                            if(progressDialog != null){
                                progressDialog.dismiss();
                                progressDialog = null;
                            }
                        }else{
                            if(obj.getRol() == 0){
                                Utilerias.mostrarToast(context,"Este usuario no es administrador!");
                                if(progressDialog != null){
                                    progressDialog.dismiss();
                                    progressDialog = null;
                                }
                            }else{
                                if(checkRecordar.isChecked()){
                                    Utilerias.savePreference(context,"usuario",obj.getCorreo());
                                    Utilerias.savePreference(context,"usuarioID",obj.getUsuarioID());
                                    Utilerias.savePreference(context,"nombre",obj.getNombre());
                                }
                                Intent intent = new Intent(context,MenuPrincipal.class);
                                startActivity(intent);
                                if(progressDialog != null){
                                    progressDialog.dismiss();
                                    progressDialog = null;
                                }
                                finish();
                            }
                        }
                    } else {
                        Toast.makeText(context, "Datos incorrectos o usuario no existe", Toast.LENGTH_LONG).show();
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
}
