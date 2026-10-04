package com.schematic.model;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonWriter;
import com.schematic.constants.Constantes;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Clase encargada de manejar la lectura y escritura en disco del archivo local
 * de progreso en formato JSON usando la API de com.badlogic.gdx.utils.Json.
 */
public class GestorPersistenciaJSON {

    private Json json;

    public GestorPersistenciaJSON() {
        this.json = new Json();
        this.json.setOutputType(JsonWriter.OutputType.json);
        this.json.setUsePrototypes(false);
    }

    /**
     * Guarda el objeto ProgresoJugador en disco en la ruta especificada en formato JSON.
     *
     * @param progreso Objeto de datos del progreso del jugador.
     * @param ruta     Ruta del archivo (por ejemplo, Constantes.RUTA_PROGRESO).
     */
    public void guardarProgreso(ProgresoJugador progreso, String ruta) {
        if (progreso == null) {
            logError("No se puede guardar un progreso nulo.");
            return;
        }

        try {
            String jsonTexto = json.prettyPrint(progreso);

            if (Gdx.files != null) {
                FileHandle fileHandle = Gdx.files.local(ruta);
                fileHandle.writeString(jsonTexto, false, "UTF-8");
                logInfo("Progreso guardado exitosamente en: " + fileHandle.file().getAbsolutePath());
            } else {
                // Fallback para entornos donde Gdx.files aún no esté inicializado
                File file = new File(ruta);
                try (FileWriter writer = new FileWriter(file, false)) {
                    writer.write(jsonTexto);
                }
                logInfo("Progreso guardado (modo IO estándar) en: " + file.getAbsolutePath());
            }
        } catch (Exception e) {
            logError("Error al guardar el progreso en " + ruta + ": " + e.getMessage());
        }
    }

    /**
     * Carga el objeto ProgresoJugador desde el archivo JSON especificado.
     * Si el archivo no existe o ocurre un error de lectura, se retorna un nuevo progreso por defecto.
     *
     * @param ruta Ruta del archivo (por ejemplo, Constantes.RUTA_PROGRESO).
     * @return Instancia de ProgresoJugador cargada desde disco o por defecto (nivel 1).
     */
    public ProgresoJugador cargarProgreso(String ruta) {
        try {
            if (Gdx.files != null) {
                FileHandle fileHandle = Gdx.files.local(ruta);
                if (fileHandle.exists()) {
                    String contenido = fileHandle.readString("UTF-8");
                    if (contenido != null && !contenido.trim().isEmpty()) {
                        ProgresoJugador progreso = json.fromJson(ProgresoJugador.class, contenido);
                        if (progreso != null) {
                            logInfo("Progreso cargado exitosamente. Nivel máximo: " + progreso.getNivelMaximoDesbloqueado());
                            return progreso;
                        }
                    }
                }
            } else {
                File file = new File(ruta);
                if (file.exists()) {
                    ProgresoJugador progreso = json.fromJson(ProgresoJugador.class, new FileReader(file));
                    if (progreso != null) {
                        logInfo("Progreso cargado (modo IO estándar). Nivel máximo: " + progreso.getNivelMaximoDesbloqueado());
                        return progreso;
                    }
                }
            }
        } catch (Exception e) {
            logError("No se pudo cargar el archivo " + ruta + ", se creará uno nuevo: " + e.getMessage());
        }

        // Si no existe o hubo error, inicializamos progreso nuevo y lo guardamos
        logInfo("Creando progreso inicial por defecto.");
        ProgresoJugador progresoNuevo = new ProgresoJugador(1);
        guardarProgreso(progresoNuevo, ruta);
        return progresoNuevo;
    }

    /**
     * Método de conveniencia estático para guardar usando la ruta predeterminada de Constantes.
     */
    public static void guardar(ProgresoJugador progreso) {
        new GestorPersistenciaJSON().guardarProgreso(progreso, Constantes.RUTA_PROGRESO);
    }

    /**
     * Método de conveniencia estático para cargar usando la ruta predeterminada de Constantes.
     */
    public static ProgresoJugador cargar() {
        return new GestorPersistenciaJSON().cargarProgreso(Constantes.RUTA_PROGRESO);
    }

    private void logInfo(String mensaje) {
        if (Gdx.app != null) {
            Gdx.app.log("GestorPersistenciaJSON", mensaje);
        } else {
            System.out.println("[GestorPersistenciaJSON] " + mensaje);
        }
    }

    private void logError(String mensaje) {
        if (Gdx.app != null) {
            Gdx.app.error("GestorPersistenciaJSON", mensaje);
        } else {
            System.err.println("[GestorPersistenciaJSON] " + mensaje);
        }
    }

    @Override
    public String toString() {
        return "GestorPersistenciaJSON{" +
                "rutaDefault='" + Constantes.RUTA_PROGRESO + '\'' +
                '}';
    }
}
