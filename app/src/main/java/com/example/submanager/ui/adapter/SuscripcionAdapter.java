package com.example.submanager.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.submanager.R;
import com.example.submanager.data.model.SuscripcionModel;

import java.util.List;

public class SuscripcionAdapter extends RecyclerView.Adapter<SuscripcionAdapter.SuscripcionViewHolder> {

    private List<SuscripcionModel> listaSuscripciones;

    public SuscripcionAdapter(List<SuscripcionModel> listaSuscripciones) {
        this.listaSuscripciones = listaSuscripciones;
    }

    @NonNull
    @Override
    public SuscripcionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_suscripcion, parent, false);
        return new SuscripcionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SuscripcionViewHolder holder, int position) {
        SuscripcionModel sub = listaSuscripciones.get(position);

        holder.tvTitulo.setText(sub.getNombre());
        holder.tvCategoria.setText(sub.getCategoria());
        holder.tvPrecio.setText(String.format("-$%.2f", sub.getPrecio()));
        holder.tvEstado.setText(sub.getEstado());
        holder.tvFecha.setText(sub.getFechaCobro());
        holder.ivLogo.setImageResource(sub.getIconoId());
    }

    @Override
    public int getItemCount() {
        return listaSuscripciones.size();
    }

    public static class SuscripcionViewHolder extends RecyclerView.ViewHolder {
        ImageView ivLogo;
        TextView tvTitulo, tvCategoria, tvPrecio, tvEstado, tvFecha;

        public SuscripcionViewHolder(@NonNull View itemView) {
            super(itemView);
            ivLogo = itemView.findViewById(R.id.ivLogo);
            tvTitulo = itemView.findViewById(R.id.tvTitulo);
            tvCategoria = itemView.findViewById(R.id.tvCategoria);
            tvPrecio = itemView.findViewById(R.id.tvPrecio);
            tvEstado = itemView.findViewById(R.id.tvEstado);
            tvFecha = itemView.findViewById(R.id.tvFecha);
        }
    }
}