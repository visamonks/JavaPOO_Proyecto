package com.schematic.model.niveles;

public class GestorNiveles {

    public static final int TOTAL_NIVELES = 8;

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
            case 6:
                return new Nivel6();
            case 7:
                return new Nivel7();
            case 8:
                return new Nivel8();
            default:
                return new Nivel1();
        }
    }

    public static int obtenerTotalNiveles() {
        return TOTAL_NIVELES;
    }
}
