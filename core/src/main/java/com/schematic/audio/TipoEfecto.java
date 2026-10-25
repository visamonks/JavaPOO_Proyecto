package com.schematic.audio;

public enum TipoEfecto {
    HOVER_UI("sonidos/hover.wav", 0.45f),
    CLICK_UI("sonidos/click.wav", 0.75f),
    CONECTAR_CABLE("sonidos/conectar.wav", 0.80f),
    DESCONECTAR_CABLE("sonidos/desconectar.wav", 0.75f),
    CONMUTAR_SWITCH("sonidos/switch.wav", 0.80f),
    ELECTRON("sonidos/electron.wav", 0.75f),
    ALARMA_TIEMPO("sonidos/alarma.wav", 0.80f),
    CORTOCIRCUITO("sonidos/explosion.wav", 0.95f),
    EXITO_NIVEL("sonidos/victoria.wav", 0.90f);

    private final String ruta;
    private final float volumenBase;

    TipoEfecto(String ruta, float volumenBase) {
        this.ruta = ruta;
        this.volumenBase = volumenBase;
    }

    public String getRuta() {
        return ruta;
    }

    public float getVolumenBase() {
        return volumenBase;
    }
}
