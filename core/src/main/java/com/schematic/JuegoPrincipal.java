package com.schematic;

import com.badlogic.gdx.Game;
import com.schematic.view.VistaMenu;

public class JuegoPrincipal extends Game {

    @Override
    public void create() {
        setScreen(new VistaMenu(this));
    }

    @Override
    public void dispose() {
        super.dispose();
    }
}
