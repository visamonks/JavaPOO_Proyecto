package view;

import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import model.Minijuego;

public class VistaNivel extends ScreenAdapter {

    private Minijuego minijuego;
    private SpriteBatch batch;

    public VistaNivel(Minijuego minijuego) {
        this.minijuego = minijuego;
        this.batch = new SpriteBatch();
    }


    public void render(float delta) {
        // Aqui se dibujaran el fondo, los componentes y la interfaz mas adelante
    }

    public void dispose() {
        if (batch != null) {
            batch.dispose();
        }
    }
}

