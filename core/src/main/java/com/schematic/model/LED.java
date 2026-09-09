package model;

public class LED extends Componente {

    private boolean polaridadCorrecta;

    public LED(String identificador, float posicionX, float posicionY, boolean polaridadCorrecta) {
        super(identificador, posicionX, posicionY);
        this.polaridadCorrecta = polaridadCorrecta;
        this.evaluarEstado();
    }

    @Override
    public boolean evaluarEstado() {
        if (polaridadCorrecta) {
            this.estadoActual = "EXITO";
            return true;
        } else {
            this.estadoActual = "ERROR";
            return false;
        }
    }

    public boolean isPolaridadCorrecta() {
        return polaridadCorrecta;
    }

    public void setPolaridadCorrecta(boolean polaridadCorrecta) {
        this.polaridadCorrecta = polaridadCorrecta;
        this.evaluarEstado();
    }

    @Override
    public String toString() {
        return "LED{" +
                "identificador='" + identificador + '\'' +
                ", polaridadCorrecta=" + polaridadCorrecta +
                ", estadoActual='" + estadoActual + '\'' +
                '}';
    }
}
