package com.schematic;

import com.badlogic.gdx.Game;
import com.schematic.model.*;
import com.schematic.view.VistaNivel;

public class Main extends Game {

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
Arranca el juego, crea una partida con 45 segundos, pone 4 piezas de prueba en la pantalla grande y abre la vista del nivel.

Que falta:
Hacer que primero abra el menu de inicio en lugar de pasar directo al nivel de prueba.

*/
 


