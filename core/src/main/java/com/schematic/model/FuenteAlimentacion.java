package com.schematic.model;

public class FuenteAlimentacion extends Componente {

    private float voltaje;
    private Terminal terminalPositivo;
    private Terminal terminalNegativo;
    private boolean encendida;

    public FuenteAlimentacion(String identificador, float posicionX, float posicionY) {
        super(identificador, posicionX, posicionY, 220f, 500f);
        this.voltaje = 5.0f;
        this.encendida = true;

        this.terminalPositivo = new Terminal(identificador + "_POSITIVO", this, ancho - 20f, alto * 0.70f, false);
        this.terminalPositivo.setValorLogico(true);

        this.terminalNegativo = new Terminal(identificador + "_NEGATIVO", this, ancho - 20f, alto * 0.30f, true);

        this.terminales.add(terminalPositivo);
        this.terminales.add(terminalNegativo);

        this.agregarPuntoConexion((int) (ancho - 20f), (int) (alto * 0.70f));
        this.agregarPuntoConexion((int) (ancho - 20f), (int) (alto * 0.30f));

        this.evaluarEstado();
    }

    @Override
    public boolean evaluarEstado() {
        if (terminalPositivo != null) {
            terminalPositivo.setValorLogico(encendida);
        }
        boolean retornoActivo = terminalNegativo != null && terminalNegativo.getValorLogico();
        if (retornoActivo) {
            this.estadoActual = "EXITO";
            return true;
        } else {
            this.estadoActual = "NEUTRO";
            return false;
        }
    }

    public float getVoltaje() {
        return voltaje;
    }

    public void setVoltaje(float voltaje) {
        this.voltaje = voltaje;
    }

    public boolean estaEncendida() {
        return encendida;
    }

    public boolean isEncendida() {
        return encendida;
    }

    public void setEncendida(boolean encendida) {
        this.encendida = encendida;
        if (terminalPositivo != null) {
            terminalPositivo.setValorLogico(encendida);
        }
    }

    public Terminal getTerminalPositivo() {
        return terminalPositivo;
    }

    public Terminal getTerminalNegativo() {
        return terminalNegativo;
    }

    @Override
    public String toString() {
        return "FuenteAlimentacion{" +
                "id='" + identificador + '\'' +
                ", voltaje=" + voltaje +
                ", encendida=" + encendida +
                ", estado='" + estadoActual + '\'' +
                '}';
    }
}
