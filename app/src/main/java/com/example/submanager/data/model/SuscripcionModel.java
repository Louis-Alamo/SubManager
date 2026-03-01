package com.example.submanager.data.model;


public class SuscripcionModel {
    private String nombre;
    private String categoria;
    private double precio;
    private String fechaCobro;
    private String estado;
    private int iconoId;


    public SuscripcionModel(String nombre, String categoria, double precio, String fechaCobro, String estado, int iconoId) {
        this.nombre = nombre;
        this.categoria = categoria;
        this.precio = precio;
        this.fechaCobro = fechaCobro;
        this.estado = estado;
        this.iconoId = iconoId;
    }

    public String getNombre() { return nombre; }
    public String getCategoria() { return categoria; }
    public double getPrecio() { return precio; }
    public String getFechaCobro() { return fechaCobro; }
    public String getEstado() { return estado; }
    public int getIconoId() { return iconoId; }

}