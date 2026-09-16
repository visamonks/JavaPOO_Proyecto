package com.schematic.model;

public class Resistencia extends Componente {

    private int valorOhmios;
    private int valorRequerido;
    private Terminal terminalEntrada;
    private Terminal terminalSalida;
    private boolean quemada;

    public Resistencia(String identificador, float posicionX, float posicionY, int valorOhmios, int valorRequerido) {
        super(identificador, posicionX, posicionY, 360.0f, 230.0f);
        this.valorOhmios = Math.max(1, valorOhmios);
        this.valorRequerido = valorRequerido;
        this.quemada = false;

        this.terminalEntrada = new Terminal(identificador + "_ENTRADA", this, 48f, 115f, true);
        this.terminalSalida = new Terminal(identificador + "_SALIDA", this, 312f, 115f, false);

        this.terminales.add(terminalEntrada);
        this.terminales.add(terminalSalida);

        this.agregarPuntoConexion(48, 115);
        this.agregarPuntoConexion(312, 115);

        this.evaluarEstado();
    }

    public Resistencia(String identificador, float posicionX, float posicionY) {
        this(identificador, posicionX, posicionY, 220, 220);
    }

    @Override
    public boolean evaluarEstado() {
        if (quemada) {
            this.estadoActual = "ERROR";
            return false;
        }

        boolean senalIn = terminalEntrada != null && terminalEntrada.getValorLogico();
        boolean senalOut = terminalSalida != null && terminalSalida.getValorLogico();

        if (senalIn || senalOut) {
            if (terminalEntrada != null) terminalEntrada.setValorLogico(true);
            if (terminalSalida != null) terminalSalida.setValorLogico(true);
            this.estadoActual = "EXITO";
            return true;
        } else {
            this.estadoActual = "NEUTRO";
            return false;
        }
    }

    public int getValorOhmios() {
        return valorOhmios;
    }

    public void setValorOhmios(int valorOhmios) {
        this.valorOhmios = Math.max(1, valorOhmios);
        this.evaluarEstado();
    }

    public int getValorRequerido() {
        return valorRequerido;
    }

    public void setValorRequerido(int valorRequerido) {
        this.valorRequerido = valorRequerido;
        this.evaluarEstado();
    }

    public boolean estaQuemada() {
        return quemada;
    }

    public boolean isQuemada() {
        return quemada;
    }

    public void setQuemada(boolean quemada) {
        this.quemada = quemada;
        if (quemada) {
            this.estadoActual = "ERROR";
        }
    }

    public Terminal getTerminalEntrada() {
        return terminalEntrada;
    }

    public Terminal getTerminalSalida() {
        return terminalSalida;
    }

    public String getTextoEtiqueta() {
        return valorOhmios + " Ω";
    }

    @Override
    public String toString() {
        return "Resistencia{" +
                "identificador='" + identificador + '\'' +
                ", valorOhmios=" + valorOhmios +
                ", valorRequerido=" + valorRequerido +
                ", quemada=" + quemada +
                ", estadoActual='" + estadoActual + '\'' +
                '}';
    }
}
