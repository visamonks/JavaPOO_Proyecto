package com.schematic.model;

public class GestorNiveles {

    public static final int TOTAL_NIVELES = 5;

    public static Nivel crearNivel(int numero) {
        switch (numero) {
            case 1:
                return new Nivel1();
            case 2:
                return new Nivel2();
            case 3:
                return new Nivel3();
            case 4:
                return new Nivel4();
            case 5:
                return new Nivel5();
            default:
                return new Nivel1();
        }
    }

    public static int obtenerTotalNiveles() {
        return TOTAL_NIVELES;
    }
}

