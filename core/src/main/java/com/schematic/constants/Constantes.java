package com.schematic.constants;

public class Constantes {
    public static final int ANCHO_VENTANA = 1920;
    public static final int ALTO_VENTANA = 1080;
    public static final float TIEMPO_NIVEL_DEFAULT = 8.0f;

    public static final String ESTADO_NEUTRO = "NEUTRO";
    public static final String ESTADO_EXITO = "EXITO";
    public static final String ESTADO_ERROR = "ERROR";
    public static final String RUTA_PROGRESO = "progreso.json";
}

/*
Que hay ahorita:
Tiene los valores fijos del juego como la pantalla grande en 1920x1080, el tiempo base del nivel y los textos de estado como neutro, exito o error.

Que falta:
Agregar configuraciones que vayamos necesitando despues, como la velocidad de la cinta de piezas o colores para los cables.

Recomendaciones:
No pongas logica aqui, solo deja numeros o textos fijos para no tener que repetirlos en otros archivos.
*/
