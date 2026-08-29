package controller;

import com.badlogic.gdx.InputAdapter;
import model.Minijuego;

public class ControladorNivel extends InputAdapter {

    private Minijuego minijuego;

    public ControladorNivel(Minijuego minijuego) {
        this.minijuego = minijuego;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        // Logica para detectar clic sobre un componente
        return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        // Logica de arrastre (Drag & Drop)
        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        // Logica para soltar y encajar la pieza
        return false;
    }

    public Minijuego getMinijuego() {
        return minijuego;
    }

    public void setMinijuego(Minijuego minijuego) {
        this.minijuego = minijuego;
    }
}

