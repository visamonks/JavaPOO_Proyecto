package com.schematic.lwjgl3;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.schematic.Main;
import com.schematic.constants.Constantes;

public class Lwjgl3Launcher {
    public static void main(String[] args) {
        if (StartupHelper.startNewJvmIfRequired()) return;
        createApplication();
    }

    private static Lwjgl3Application createApplication() {
        return new Lwjgl3Application(new Main(), getDefaultConfiguration());
    }

    private static Lwjgl3ApplicationConfiguration getDefaultConfiguration() {
        Lwjgl3ApplicationConfiguration configuration = new Lwjgl3ApplicationConfiguration();
        configuration.setTitle("SchematicGame");
        configuration.useVsync(true);
        configuration.setForegroundFPS(Lwjgl3ApplicationConfiguration.getDisplayMode().refreshRate + 1);
        configuration.setWindowedMode(Constantes.ANCHO_VENTANA, Constantes.ALTO_VENTANA);
        configuration.setWindowIcon("libgdx128.png", "libgdx64.png", "libgdx32.png", "libgdx16.png");
        return configuration;
    }
}

/*
que llevamos: 
  Este es el punto de partida que arranca el juego en la computadora. Abre la
  ventana en resolución 1920x1080, le pone el título "SchematicGame" y llama
  a la clase principal Main para iniciar la partida.
 
que falta:
 Si en el futuro queremos que el juego inicie en pantalla completa real o que
  permita cambiar de resolución desde un menú de opciones va aca.
 
 */