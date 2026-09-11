package com.schematic.model;

public class CompuertaAND extends CompuertaLogica {

    public CompuertaAND(String identificador, float posicionX, float posicionY, boolean entradaA, boolean entradaB) {
       
        super(identificador, posicionX, posicionY, entradaA, entradaB);

        
        this.agregarPuntoConexion(0, 15);
    
        this.agregarPuntoConexion(0, 35);
    
        this.agregarPuntoConexion(60, 25);
    }

    public CompuertaAND(String identificador, float posicionX, float posicionY) {
        this(identificador, posicionX, posicionY, false, false);
    }

    @Override
    public boolean calcularSalida() {
        this.salida = entradaA && entradaB;
        return this.salida;
    }

    @Override
    public String toString() {
        return "CompuertaAND{" +
                "identificador='" + identificador + '\'' +
                ", entradaA=" + entradaA +
                ", entradaB=" + entradaB +
                ", salida=" + salida +
                ", estadoActual='" + estadoActual + '\'' +
                '}';
    }
}
