package com.schematic;

import com.badlogic.gdx.Game;
import com.schematic.constants.Constantes;
import com.schematic.model.GestorPersistenciaJSON;
import com.schematic.model.ProgresoJugador;
import com.schematic.view.VistaMenu;

public class JuegoPrincipal extends Game {

    private GestorPersistenciaJSON gestorPersistencia;
    private ProgresoJugador progreso;

    @Override
    public void create() {
        this.gestorPersistencia = new GestorPersistenciaJSON();
        this.progreso = gestorPersistencia.cargarProgreso(Constantes.RUTA_PROGRESO);
        setScreen(new VistaMenu(this));
    }

    public GestorPersistenciaJSON getGestorPersistencia() {
        return gestorPersistencia;
    }

    public ProgresoJugador getProgreso() {
        if (progreso == null) {
            if (gestorPersistencia == null) {
                gestorPersistencia = new GestorPersistenciaJSON();
            }
            progreso = gestorPersistencia.cargarProgreso(Constantes.RUTA_PROGRESO);
        }
        return progreso;
    }

    public void setProgreso(ProgresoJugador progreso) {
        this.progreso = progreso;
    }

    public void guardarProgreso() {
        if (gestorPersistencia != null && progreso != null) {
            gestorPersistencia.guardarProgreso(progreso, Constantes.RUTA_PROGRESO);
        }
    }

    @Override
    public void dispose() {
        guardarProgreso();
        super.dispose();
    }
}
