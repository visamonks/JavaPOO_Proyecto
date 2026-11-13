package com.schematic.model.componentes;

public class Potenciometro extends Componente {

    public static final float PASO = 0.1f;

    private float valor;
    private final Terminal terminalEntrada;
    private final Terminal terminalSalida;

    public Potenciometro(String identificador, float posicionX, float posicionY) {
        super(identificador, posicionX, posicionY, 280.0f, 280.0f);
        this.valor = 1f;
        this.terminalEntrada = new Terminal(identificador + "_ENTRADA", this, 118.0f, 70.0f, true);
        this.terminalSalida = new Terminal(identificador + "_SALIDA", this, 162.0f, 70.0f, false);
        this.terminales.add(terminalEntrada);
        this.terminales.add(terminalSalida);
        this.agregarPuntoConexion(118, 70);
        this.agregarPuntoConexion(162, 70);
    }

    public void subir() {
        setValor(valor + PASO);
    }

    public void bajar() {
        setValor(valor - PASO);
    }

    @Override
    public boolean evaluarEstado() {
        return true;
    }

    public float getValor() { return valor; }

    public void setValor(float valor) {
        this.valor = Math.round(Math.max(0f, Math.min(1f, valor)) * 10f) / 10f;
    }

    public int getPorcentaje() { return Math.round(valor * 100f); }
    public Terminal getTerminalEntrada() { return terminalEntrada; }
    public Terminal getTerminalSalida() { return terminalSalida; }
}
