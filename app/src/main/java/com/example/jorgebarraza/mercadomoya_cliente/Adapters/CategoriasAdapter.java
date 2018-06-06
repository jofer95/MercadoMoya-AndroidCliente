package com.example.jorgebarraza.mercadomoya_cliente.Adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.Volley;
import com.example.jorgebarraza.mercadomoya_cliente.Modelos.Categoria;
import com.example.jorgebarraza.mercadomoya_cliente.R;

import java.util.ArrayList;

public class CategoriasAdapter extends RecyclerView.Adapter<CategoriasAdapter.ViewHolderCategorias> {
    private ArrayList<Categoria> listaCategorias;
    private RequestQueue request;

    public CategoriasAdapter(ArrayList<Categoria> listaDatos) {
        this.listaCategorias = listaDatos;
        setHasStableIds(true);
    }

    @Override
    public ViewHolderCategorias onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_categoria, null);
        return new ViewHolderCategorias(view);
    }

    @Override
    public void onBindViewHolder(ViewHolderCategorias holder, int position) {
        holder.asignarArticulos(listaCategorias.get(position));
    }

    @Override
    public int getItemCount() {
        return listaCategorias.size();
    }

    public class ViewHolderCategorias extends RecyclerView.ViewHolder {
        TextView nombre;
        ImageView foto;
        View estatus;
        Context contexto;

        public ViewHolderCategorias(View itemView) {
            super(itemView);
            nombre = (TextView) itemView.findViewById(R.id.tvNombreCategoria);
            foto = (ImageView) itemView.findViewById(R.id.idFoto);
            estatus = (View) itemView.findViewById(R.id.idEstatus);
            contexto = itemView.getContext();
            request = Volley.newRequestQueue(contexto);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int itemPosition = getLayoutPosition();
                    /*Intent intent = new Intent(contexto, AltaDeCategoria.class);
                    intent.putExtra("categoriaID", listaCategorias.get(itemPosition).getCategoriaID());
                    contexto.startActivity(intent);*/
                }
            });
        }

        public void asignarArticulos(Categoria categoria) {
            nombre.setText(categoria.getNombre());
            asignarFoto(categoria.getImagenURL());
        }

        private void asignarFoto(String foto_url) {
            ImageRequest imageRequest = new ImageRequest(foto_url, new Response.Listener<Bitmap>() {
                @Override
                public void onResponse(Bitmap response) {
                    foto.setImageBitmap(response);
                }
            }, 0, 0, ImageView.ScaleType.CENTER, null, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    //Toast.makeText(contexto, "Error al obtener fotografia", Toast.LENGTH_SHORT).show();
                }
            });
            request.add(imageRequest);
        }
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }
}

