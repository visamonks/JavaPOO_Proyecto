package com.schematic.model.componentes;

public class Temporizador555 extends Componente {

    public static final float SEMIPERIODO_ASTABLE = 0.5f;
    public static final float DURACION_PULSO = 3.0f;

    private boolean astable;
    private boolean salidaAlta;
    private boolean pulsoActivo;
    private boolean disparoAnterior;
    private float tiempo;
    private int pulsos;

    private final Terminal terminalVcc;
    private final Terminal terminalDisparo;
    private final Terminal terminalTierra;
    private final Terminal terminalSalida;
    private final Terminal terminalCapacitor;

    public Temporizador555(String identificador, float posicionX, float posicionY, boolean astable) {
        super(identificador, posicionX, posicionY, 360.0f, 360.0f);
        this.astable = astable;
        this.terminalVcc = new Terminal(identificador + "_VCC", this, 82.0f, 219.0f, true);
        this.terminalDisparo = new Terminal(identificador + "_TRIG", this, 82.0f, 184.0f, true);
        this.terminalTierra = new Terminal(identificador + "_GND", this, 82.0f, 149.0f, true);
        this.terminalSalida = new Terminal(identificador + "_OUT", this, 278.0f, 219.0f, false);
        this.terminalCapacitor = new Terminal(identificador + "_C", this, 278.0f, 149.0f, false);
        this.terminales.add(terminalVcc);
        this.terminales.add(terminalDisparo);
        this.terminales.add(terminalTierra);
        this.terminales.add(terminalSalida);
        this.terminales.add(terminalCapacitor);
        reiniciar();
    }

    public void cambiarModo() {
        astable = !astable;
        reiniciar();
        pulsos = 0;
    }

    public void reiniciar() {
        salidaAlta = false;
        pulsoActivo = false;
        tiempo = 0f;
    }

    public void avanzar(float deltaTiempo, boolean listo, boolean disparo) {
        if (!listo) {
            reiniciar();
            disparoAnterior = disparo;
            return;
        }
        if (astable) {
            tiempo += deltaTiempo;
            if (tiempo >= SEMIPERIODO_ASTABLE) {
                tiempo = 0f;
                salidaAlta = !salidaAlta;
                if (salidaAlta) pulsos++;
            }
        } else {
            if (disparo && !disparoAnterior && !pulsoActivo) {
                pulsoActivo = true;
                salidaAlta = true;
                tiempo = 0f;
            }
            if (pulsoActivo) {
                tiempo += deltaTiempo;
                if (tiempo >= DURACION_PULSO) {
                    pulsoActivo = false;
                    salidaAlta = false;
                    pulsos++;
                }
            }
        }
        disparoAnterior = disparo;
    }

    public float getProgresoCiclo() {
        float total = astable ? SEMIPERIODO_ASTABLE : DURACION_PULSO;
        return Math.min(1f, tiempo / total);
    }

    @Override
    public boolean evaluarEstado() {
        this.estadoActual = salidaAlta ? "EXITO" : "NEUTRO";
        terminalSalida.setValorLogico(salidaAlta);
        return salidaAlta;
    }

    public boolean isAstable() { return astable; }
    public boolean isSalidaAlta() { return salidaAlta; }
    public boolean isPulsoActivo() { return pulsoActivo; }
    public int getPulsos() { return pulsos; }
    public float getTiempo() { return tiempo; }
    public Terminal getTerminalVcc() { return terminalVcc; }
    public Terminal getTerminalDisparo() { return terminalDisparo; }
    public Terminal getTerminalTierra() { return terminalTierra; }
    public Terminal getTerminalSalida() { return terminalSalida; }
    public Terminal getTerminalCapacitor() { return terminalCapacitor; }
}
