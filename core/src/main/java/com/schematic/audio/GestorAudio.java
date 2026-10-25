package com.schematic.audio;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Disposable;
import com.schematic.JuegoPrincipal;

import java.util.EnumMap;
import java.util.Map;

public class GestorAudio implements Disposable {

    private final Map<TipoEfecto, EfectoSonoro> efectos;
    private float volumen;
    private boolean silenciado;
    private long ultimoTiempoHover;

    public GestorAudio() {
        this.efectos = new EnumMap<>(TipoEfecto.class);
        this.volumen = 0.8f;
        this.silenciado = false;
        this.ultimoTiempoHover = 0L;
        instanciarEfectosBase();
    }

    private void instanciarEfectosBase() {
        for (TipoEfecto tipo : TipoEfecto.values()) {
            efectos.put(tipo, new EfectoSonoro(tipo));
        }
    }

    public void inicializar() {
        cargarVolumenGuardado();
        for (EfectoSonoro efecto : efectos.values()) {
            efecto.cargar();
        }
    }

    private void cargarVolumenGuardado() {
        if (Gdx.app == null) {
            return;
        }
        try {
            Preferences prefs = Gdx.app.getPreferences("schematic_config");
            this.volumen = MathUtils.clamp(prefs.getFloat("volumen", 0.8f), 0.0f, 1.0f);
        } catch (Exception ignored) {
        }
    }

    public void reproducirEfecto(TipoEfecto tipo) {
        if (silenciado || tipo == null || volumen <= 0f) {
            return;
        }
        long ahora = System.currentTimeMillis();
        if (tipo == TipoEfecto.HOVER_UI) {
            if (ahora < ultimoTiempoHover + 45L) {
                return;
            }
            ultimoTiempoHover = ahora;
        } else {
            ultimoTiempoHover = ahora + 80L;
        }
        EfectoSonoro efecto = efectos.get(tipo);
        if (efecto != null) {
            efecto.reproducir(volumen);
        }
    }

    public EfectoSonoro obtenerEfecto(TipoEfecto tipo) {
        return efectos.get(tipo);
    }

    public float getVolumen() {
        return volumen;
    }

    public void setVolumen(float volumen) {
        this.volumen = MathUtils.clamp(volumen, 0.0f, 1.0f);
    }

    public boolean isSilenciado() {
        return silenciado;
    }

    public void setSilenciado(boolean silenciado) {
        this.silenciado = silenciado;
    }

    public static GestorAudio obtenerDesde(Game juego) {
        if (juego instanceof JuegoPrincipal) {
            return ((JuegoPrincipal) juego).getGestorAudio();
        }
        return null;
    }

    @Override
    public void dispose() {
        for (EfectoSonoro efecto : efectos.values()) {
            efecto.dispose();
        }
    }
}
