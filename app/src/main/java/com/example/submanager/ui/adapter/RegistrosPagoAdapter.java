package com.example.submanager.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.submanager.R;
import com.example.submanager.data.model.RegistrosPagoModel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class RegistrosPagoAdapter extends RecyclerView.Adapter<RegistrosPagoAdapter.PagoViewHolder> {

    private List<RegistrosPagoModel> pagos = new ArrayList<>();

    public void setPagos(List<RegistrosPagoModel> pagos) {
        this.pagos = pagos;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public PagoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_registro_pago, parent, false);
        return new PagoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PagoViewHolder holder, int position) {
        RegistrosPagoModel pago = pagos.get(position);
        holder.bind(pago);
    }

    @Override
    public int getItemCount() {
        return pagos.size();
    }

    static class PagoViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvFechaPago;
        private final TextView tvMontoPago;
        private final TextView tvMetodoPagoItem;

        public PagoViewHolder(@NonNull View itemView) {
            super(itemView);
            tvFechaPago = itemView.findViewById(R.id.tvFechaPago);
            tvMontoPago = itemView.findViewById(R.id.tvMontoPago);
            tvMetodoPagoItem = itemView.findViewById(R.id.tvMetodoPagoItem);
        }

        public void bind(RegistrosPagoModel pago) {
            tvFechaPago.setText(formatearFecha(pago.getFechaPago()));
            tvMontoPago.setText(String.format(Locale.getDefault(), "$%.2f", pago.getMonto()));
            tvMetodoPagoItem.setText("Pago registrado");
        }

        private String formatearFecha(String fechaStr) {
            if (fechaStr == null || fechaStr.isEmpty()) return "N/A";
            try {
                SimpleDateFormat sdfIn = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                SimpleDateFormat sdfOut = new SimpleDateFormat("dd MMM yyyy", new Locale("es", "ES"));
                Date date = sdfIn.parse(fechaStr);
                return sdfOut.format(date);
            } catch (Exception e) {
                return fechaStr;
            }
        }
    }
}
