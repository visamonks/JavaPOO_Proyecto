package com.schematic.model.componentes;

public class Capacitor extends Componente {

    public static final float TIEMPO_CARGA = 2.0f;
    public static final float TIEMPO_DESCARGA = 4.0f;

    private float carga;
    private boolean explotado;
    private final Terminal terminalPositivo;
    private final Terminal terminalNegativo;

    public Capacitor(String identificador, float posicionX, float posicionY) {
        super(identificador, posicionX, posicionY, 240.0f, 240.0f);
        this.carga = 0f;
        this.terminalPositivo = new Terminal(identificador + "_POSITIVO", this, 89.0f, 36.0f, true);
        this.terminalNegativo = new Terminal(identificador + "_NEGATIVO", this, 152.0f, 44.0f, false);
        this.terminales.add(terminalPositivo);
        this.terminales.add(terminalNegativo);
        this.agregarPuntoConexion(89, 36);
        this.agregarPuntoConexion(152, 44);
    }

    public void cargar(float deltaTiempo) {
        carga = Math.min(1f, carga + deltaTiempo / TIEMPO_CARGA);
    }

    public void descargar(float deltaTiempo) {
        carga = Math.max(0f, carga - deltaTiempo / TIEMPO_DESCARGA);
    }

    public boolean tieneCarga() {
        return !explotado && carga > 0.02f;
    }

    public void explotar() {
        explotado = true;
        carga = 0f;
        estadoActual = "ERROR";
    }

    @Override
    public boolean evaluarEstado() {
        if (explotado) {
            this.estadoActual = "ERROR";
            return false;
        }
        this.estadoActual = tieneCarga() ? "EXITO" : "NEUTRO";
        return tieneCarga();
    }

    public float getCarga() { return carga; }
    public boolean isExplotado() { return explotado; }
    public void setCarga(float carga) { this.carga = Math.max(0f, Math.min(1f, carga)); }
    public Terminal getTerminalPositivo() { return terminalPositivo; }
    public Terminal getTerminalNegativo() { return terminalNegativo; }
}
