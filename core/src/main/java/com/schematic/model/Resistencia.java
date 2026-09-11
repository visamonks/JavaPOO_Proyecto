package com.schematic.model;

public class Resistencia extends Componente {

    private int valorOhmios;
    private int valorRequerido;
    private Terminal terminalEntrada;
    private Terminal terminalSalida;

    public Resistencia(String identificador, float posicionX, float posicionY, int valorOhmios, int valorRequerido) {
        super(identificador, posicionX, posicionY);
        this.valorOhmios = Math.max(1, valorOhmios);
        this.valorRequerido = valorRequerido;
        this.ancho = 60f;
        this.alto = 40f;

        this.terminalEntrada = new Terminal(identificador + "_IN", this, 0f, alto / 2f, true);
        this.terminalSalida = new Terminal(identificador + "_OUT", this, ancho, alto / 2f, false);

        this.terminales.add(terminalEntrada);
        this.terminales.add(terminalSalida);

        this.evaluarEstado();
    }

    @Override
    public boolean evaluarEstado() {
        boolean pasoCorriente = terminalEntrada != null && terminalEntrada.getValorLogico();
        
        if (valorOhmios == valorRequerido) {
            this.estadoActual = "EXITO";
            if (terminalSalida != null) {
                terminalSalida.setValorLogico(pasoCorriente);
            }
            return true;
        } else {
            this.estadoActual = "ERROR";
            if (terminalSalida != null) {
                terminalSalida.setValorLogico(false);
            }
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

    public String getTextoEtiqueta() {
        return valorOhmios + " Ω";
    }

    @Override
    public String toString() {
        return "Resistencia{" +
                "identificador='" + identificador + '\'' +
                ", valorOhmios=" + valorOhmios +
                ", valorRequerido=" + valorRequerido +
                ", estadoActual='" + estadoActual + '\'' +
                '}';
    }
}