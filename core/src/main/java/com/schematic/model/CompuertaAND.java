package model;

public class CompuertaAND extends Componente {

    private boolean entradaA;
    private boolean entradaB;

    public CompuertaAND(String identificador, float posicionX, float posicionY, boolean entradaA, boolean entradaB) {
        super(identificador, posicionX, posicionY);
        this.entradaA = entradaA;
        this.entradaB = entradaB;
        this.evaluarEstado();
    }


    public boolean evaluarEstado() {
        boolean resultado = entradaA && entradaB;
        if (resultado) {
            this.estadoActual = "EXITO";
        } else {
            this.estadoActual = "NEUTRO";
        }
        return resultado;
    }

    public boolean isEntradaA() {
        return entradaA;
    }

    public void setEntradaA(boolean entradaA) {
        this.entradaA = entradaA;
        this.evaluarEstado();
    }

    public boolean isEntradaB() {
        return entradaB;
    }

    public void setEntradaB(boolean entradaB) {
        this.entradaB = entradaB;
        this.evaluarEstado();
    }
    
    public String toString() {
        return "CompuertaAND{" +
                "identificador='" + identificador + '\'' +
                ", entradaA=" + entradaA +
                ", entradaB=" + entradaB +
                ", estadoActual='" + estadoActual + '\'' +
                '}';
    }
}
