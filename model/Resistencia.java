package model;

public class Resistencia extends Componente {

    private int valorOhmios;
    private int valorRequerido;

    public Resistencia(String identificador, float posicionX, float posicionY, int valorOhmios, int valorRequerido) {
        super(identificador, posicionX, posicionY);
        this.valorOhmios = valorOhmios;
        this.valorRequerido = valorRequerido;
        this.evaluarEstado();
    }

    public boolean evaluarEstado() {
        if (valorOhmios == valorRequerido) {
            this.estadoActual = "EXITO";
            return true;
        } else {
            this.estadoActual = "ERROR";
            return false;
        }
    }

    public int getValorOhmios() {
        return valorOhmios;
    }

    public void setValorOhmios(int valorOhmios) {
        this.valorOhmios = valorOhmios;
        this.evaluarEstado();
    }

    public int getValorRequerido() {
        return valorRequerido;
    }

    public void setValorRequerido(int valorRequerido) {
        this.valorRequerido = valorRequerido;
        this.evaluarEstado();
    }

    public String toString() {
        return "Resistencia{" +
                "identificador='" + identificador + '\'' +
                ", valorOhmios=" + valorOhmios +
                ", valorRequerido=" + valorRequerido +
                ", estadoActual='" + estadoActual + '\'' +
                '}';
    }
}
