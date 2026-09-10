package com.schematic;

import com.badlogic.gdx.Game;
import com.schematic.model.*;
import com.schematic.view.VistaNivel;

public class JuegoPrincipal extends Game {

    @Override
    public void create() {
        Minijuego minijuego = new Minijuego(45.0f);

        minijuego.agregarComponente(new Switch("SW_1", 240f, 480f, false));
        minijuego.agregarComponente(new CompuertaAND("AND_1", 600f, 480f, false, false));
        minijuego.agregarComponente(new Resistencia("R_1", 960f, 480f, 220, 220));
        minijuego.agregarComponente(new LED("LED_1", 1320f, 480f, true));

        setScreen(new VistaNivel(minijuego));
    }

    @Override
    public void dispose() {
        super.dispose();
    }
}

/*
Que hay ahorita:
Clase solicitada por el diagrama que sirve como punto central para cambiar entre pantallas.

Que falta:
Conectar el sistema para guardar y cargar partidas con el archivo JSON.

Recomendaciones:
Mantenla sincronizada con Main para que el juego se pueda arrancar desde cualquiera de los dos lados.
 */


