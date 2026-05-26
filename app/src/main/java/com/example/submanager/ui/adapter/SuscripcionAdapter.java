package com.example.submanager.ui.adapter;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.submanager.R;
import com.example.submanager.data.model.SuscripcionModel;
import com.example.submanager.ui.activity.DetalleSuscripcionActivity;

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
        holder.tvPrecio.setText(String.format("-$%.2f", sub.getMonto()));
        holder.tvEstado.setText("Modificado: " + formatearFecha(sub.getActualizadoEn()));
        holder.tvFecha.setText("Próximo cobro: " + formatearFecha(sub.getFechaProximoCobro()));
        android.content.Context context = holder.itemView.getContext();
        String nombreIcono = sub.getNombreIcono();
        int resId = context.getResources().getIdentifier(nombreIcono, "drawable", context.getPackageName());
        if (resId != 0) {
            holder.ivLogo.setImageResource(resId);
        } else {
            holder.ivLogo.setImageResource(R.mipmap.ic_launcher);
        }


        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, DetalleSuscripcionActivity.class);
            intent.putExtra("suscripcion_id", sub.getId());
            intent.putExtra("iconRes", resId != 0 ? resId : R.mipmap.ic_launcher);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return listaSuscripciones.size();
    }

    public void actualizarLista(List<SuscripcionModel> nuevaLista) {
        this.listaSuscripciones = nuevaLista;
        notifyDataSetChanged();
    }

    private String formatearFecha(String fechaStr) {
        if (fechaStr == null || fechaStr.length() < 10) return fechaStr != null ? fechaStr : "";
        try {
            String datePart = fechaStr.substring(0, 10);
            java.text.SimpleDateFormat sdfIn = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault());
            java.text.SimpleDateFormat sdfOut = new java.text.SimpleDateFormat("dd MMM yyyy", new java.util.Locale("es", "ES"));
            java.util.Date date = sdfIn.parse(datePart);
            return sdfOut.format(date);
        } catch (Exception e) {
            return fechaStr;
        }
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