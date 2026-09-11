package com.schematic.model;

public class CompuertaOR extends CompuertaLogica {

    public CompuertaOR(String identificador, float posicionX, float posicionY) {
        this(identificador, posicionX, posicionY, false, false);
    }

    public CompuertaOR(String identificador, float posicionX, float posicionY, boolean entradaA, boolean entradaB) {
        super(identificador, posicionX, posicionY, entradaA, entradaB);

    
        this.agregarPuntoConexion(0, 15);
        this.agregarPuntoConexion(0, 35);  
        this.agregarPuntoConexion(60, 25); 
    }

    @Override
    public boolean calcularSalida() {
        return entradaA || entradaB;
    }

    @Override
    public String toString() {
        return "CompuertaOR{" +
                "identificador='" + identificador + '\'' +
                ", entradaA=" + entradaA +
                ", entradaB=" + entradaB +
                ", salida=" + salida +
                ", estadoActual='" + estadoActual + '\'' +
                '}';
    }
}