package com.schematic.model.niveles;

public class TutorialNivel {

    public enum Esquema {
        SERIE,
        SERIE_CON_SWITCH,
        DIVISOR_AND,
        DIVISOR_OR,
        RAMAS
    }

    private final String titulo;
    private final String concepto;
    private final String vidaReal;
    private final Esquema esquema;
    private final String[] pasos;
    private final String mision;

    private boolean[] tablaVerdad;
    private String consejo;
    private String[][] ramas;
    private String icono;

    public TutorialNivel(String titulo, String concepto, String vidaReal,
                         Esquema esquema, String[] pasos, String mision) {
        this.titulo = titulo;
        this.concepto = concepto;
        this.vidaReal = vidaReal;
        this.esquema = esquema;
        this.pasos = (pasos != null) ? pasos : new String[0];
        this.mision = mision;
        this.tablaVerdad = null;
        this.consejo = "";
    }

    public TutorialNivel conTablaVerdad(boolean r00, boolean r10, boolean r01, boolean r11) {
        this.tablaVerdad = new boolean[]{r00, r10, r01, r11};
        return this;
    }

    public TutorialNivel conRamas(String[]... ramas) {
        this.ramas = ramas;
        return this;
    }

    public TutorialNivel conIcono(String icono) {
        this.icono = icono;
        return this;
    }

    public String[][] getRamas() { return ramas; }
    public String getIcono() { return icono; }

    public TutorialNivel conConsejo(String consejo) {
        this.consejo = (consejo != null) ? consejo : "";
        return this;
    }

    public boolean tieneTablaVerdad() {
        return tablaVerdad != null;
    }

    public String getTitulo() { return titulo; }
    public String getConcepto() { return concepto; }
    public String getVidaReal() { return vidaReal; }
    public Esquema getEsquema() { return esquema; }
    public String[] getPasos() { return pasos; }
    public String getMision() { return mision; }
    public boolean[] getTablaVerdad() { return tablaVerdad; }
    public String getConsejo() { return consejo; }
}
