package com.schematic.model.niveles;

import com.schematic.model.componentes.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class Nivel {

    public static class CasillaInventario {
        private String tipo;
        private String titulo;
        private int cantidadDisponible;
        private int cantidadTotal;

        public CasillaInventario(String tipo, String titulo, int cantidadTotal) {
            this.tipo = tipo;
            this.titulo = titulo;
            this.cantidadTotal = cantidadTotal;
            this.cantidadDisponible = cantidadTotal;
        }

        public String getTipo() { return tipo; }
        public String getTitulo() { return titulo; }
        public int getCantidadDisponible() { return cantidadDisponible; }
        public int getCantidadTotal() { return cantidadTotal; }

        public boolean puedeUsar() {
            return cantidadDisponible > 0;
        }

        public void decrementar() {
            if (cantidadDisponible > 0) {
                cantidadDisponible--;
            }
        }

        public void incrementar() {
            if (cantidadDisponible < cantidadTotal) {
                cantidadDisponible++;
            }
        }

        public void reiniciar() {
            this.cantidadDisponible = cantidadTotal;
        }
    }

    public static final float TIEMPO_REQUERIDO_VICTORIA = 5.0f;

    protected int numeroNivel;
    protected String tituloReto;
    protected float tiempoLimite;
    protected float tiempoRestante;
    protected boolean completado;
    protected boolean ganado;
    protected float tiempoCircuitoActivo;
    protected String mensajeEstado;
    protected float deltaActual;

    protected List<Componente> componentes;
    protected List<Componente> componentesCinta;
    protected List<Cable> cables;
    protected List<CasillaInventario> inventario;

    public Nivel(int numeroNivel, String tituloReto, float tiempoLimite) {
        this.numeroNivel = numeroNivel;
        this.tituloReto = tituloReto;
        this.tiempoLimite = tiempoLimite > 0 ? tiempoLimite : 30.0f;
        this.tiempoRestante = this.tiempoLimite;
        this.completado = false;
        this.ganado = false;
        this.tiempoCircuitoActivo = 0.0f;
        this.mensajeEstado = "";
        this.componentes = new ArrayList<>();
        this.componentesCinta = new ArrayList<>();
        this.cables = new ArrayList<>();
        this.inventario = new ArrayList<>();
    }

    public abstract void inicializar();

    public void reiniciarNivelActual() {
        this.tiempoCircuitoActivo = 0.0f;
        this.completado = false;
        this.ganado = false;
        inicializar();
    }

    public abstract boolean evaluarCircuitoCompleto();

    public TutorialNivel obtenerTutorial() {
        String[] pasos = {"Coloca las piezas", "Conéctalas con cables", "Enciende el circuito"};
        return new TutorialNivel(
                tituloReto,
                "Arma el circuito con las piezas del almacén.",
                "Los circuitos están en todo lo que usa electricidad en tu casa.",
                TutorialNivel.Esquema.SERIE,
                pasos,
                "Mantén el circuito encendido 5 segundos seguidos."
        );
    }

    public void actualizar(float deltaTiempo) {
        if (tiempoRestante > 0 && !(completado && ganado)) {
            tiempoRestante -= deltaTiempo;
            if (tiempoRestante <= 0) {
                tiempoRestante = 0;
            }
        }

        deltaActual = deltaTiempo;
        boolean circuitoActivo = evaluarCircuitoCompleto();
        actualizarVictoria(circuitoActivo, deltaTiempo);
    }

    protected void actualizarVictoria(boolean circuitoActivo, float deltaTiempo) {
        if (circuitoActivo) {
            tiempoCircuitoActivo += deltaTiempo;
            if (tiempoCircuitoActivo >= getTiempoRequeridoVictoria()) {
                tiempoCircuitoActivo = getTiempoRequeridoVictoria();
                this.ganado = true;
                this.completado = true;
                deltaActual = 0f;
                evaluarCircuitoCompleto();
            } else {
                this.ganado = false;
                this.completado = false;
            }
        } else {
            tiempoCircuitoActivo = 0.0f;
            this.ganado = false;
            this.completado = false;
        }
    }

    public String obtenerPiezasPendientes() {
        StringBuilder pendientes = new StringBuilder();
        for (CasillaInventario casilla : inventario) {
            if (casilla.getCantidadDisponible() > 0) {
                if (pendientes.length() > 0) pendientes.append(", ");
                pendientes.append(casilla.getTitulo());
            }
        }
        return pendientes.toString();
    }

    protected String mensajeColocarPiezas(String mensajeSiYaEstanTodas) {
        String pendientes = obtenerPiezasPendientes();
        if (pendientes.isEmpty()) {
            return mensajeSiYaEstanTodas;
        }
        return "Arrastra a la mesa: " + pendientes;
    }

    public CasillaInventario buscarCasilla(String tipo) {
        for (CasillaInventario casilla : inventario) {
            if (casilla.getTipo().equals(tipo)) {
                return casilla;
            }
        }
        return null;
    }

    public Componente crearYColocarComponente(String tipo, float x, float y) {
        CasillaInventario casilla = buscarCasilla(tipo);
        if (casilla == null || !casilla.puedeUsar()) {
            return null;
        }

        Componente nuevo = fabricarComponentePorTipo(tipo, x, y);

        if (nuevo != null) {
            casilla.decrementar();
            agregarComponente(nuevo);
            actualizar(0f);
        }
        return nuevo;
    }

    protected Componente fabricarComponentePorTipo(String tipo, float x, float y) {
        if (tipo.startsWith("LED")) {
            LED led = new LED(siguienteId("LED_"), x, y, true);
            if (tipo.startsWith("LED_")) led.setColor(tipo.substring(4));
            return led;
        } else if ("RESISTENCIA_220".equals(tipo)) {
            return new Resistencia(siguienteId("R_"), x, y, 220, 220);
        } else if ("SWITCH".equals(tipo) || "SWITCH_DIVISOR".equals(tipo)) {
            if (esSwitchDivisor(tipo)) {
                return new Switch(siguienteId("SW_"), x, y, false, false);
            }
            return new Switch(siguienteId("SW_"), x, y, false);
        } else if ("CAPACITOR".equals(tipo)) {
            return new Capacitor(siguienteId("C_"), x, y);
        } else if ("POTENCIOMETRO".equals(tipo)) {
            return new Potenciometro(siguienteId("P_"), x, y);
        } else if ("555".equals(tipo)) {
            return new Temporizador555(siguienteId("U_"), x, y, false);
        } else if ("AND".equals(tipo)) {
            return new CompuertaAND("AND_1", x, y, false, false);
        } else if ("OR".equals(tipo)) {
            return new CompuertaOR("OR_1", x, y, false, false);
        }
        return null;
    }

    public boolean usaSwitchDivisor() {
        return false;
    }

    public boolean esSwitchDivisor(String tipo) {
        return "SWITCH_DIVISOR".equals(tipo) || ("SWITCH".equals(tipo) && usaSwitchDivisor());
    }

    private String siguienteId(String prefijo) {
        int numero = 1;
        while (existeComponente(prefijo + numero)) {
            numero++;
        }
        return prefijo + numero;
    }

    private boolean existeComponente(String identificador) {
        for (Componente c : componentes) {
            if (identificador.equals(c.getIdentificador())) return true;
        }
        return false;
    }

    public void devolverAlInventario(Componente comp) {
        if (comp == null || comp instanceof FuenteAlimentacion) return;

        if (comp instanceof LED) {
            CasillaInventario casilla = buscarCasilla("LED_" + ((LED) comp).getColor());
            if (casilla == null) casilla = buscarCasilla("LED");
            if (casilla != null) casilla.incrementar();
        } else if (comp instanceof Resistencia) {
            CasillaInventario casilla = buscarCasilla("RESISTENCIA_220");
            if (casilla != null) casilla.incrementar();
        } else if (comp instanceof Switch) {
            CasillaInventario casilla = null;
            if (((Switch) comp).isDoble()) casilla = buscarCasilla("SWITCH_DIVISOR");
            if (casilla == null) casilla = buscarCasilla("SWITCH");
            if (casilla != null) casilla.incrementar();
        } else if (comp instanceof Capacitor) {
            CasillaInventario casilla = buscarCasilla("CAPACITOR");
            if (casilla != null) casilla.incrementar();
        } else if (comp instanceof Temporizador555) {
            CasillaInventario casilla = buscarCasilla("555");
            if (casilla != null) casilla.incrementar();
        } else if (comp instanceof Potenciometro) {
            CasillaInventario casilla = buscarCasilla("POTENCIOMETRO");
            if (casilla != null) casilla.incrementar();
        } else if (comp instanceof CompuertaAND) {
            CasillaInventario casilla = buscarCasilla("AND");
            if (casilla != null) casilla.incrementar();
        } else if (comp instanceof CompuertaOR) {
            CasillaInventario casilla = buscarCasilla("OR");
            if (casilla != null) casilla.incrementar();
        }

        eliminarComponente(comp);
        actualizar(0f);
    }

    public Cable buscarCableConTerminal(Terminal terminal) {
        if (terminal == null) return null;
        for (Cable c : cables) {
            if (c.conectaTerminal(terminal)) {
                return c;
            }
        }
        return null;
    }

    public List<Cable> buscarCablesConTerminal(Terminal terminal) {
        List<Cable> resultado = new ArrayList<>();
        if (terminal == null) return resultado;
        for (Cable c : cables) {
            if (c.conectaTerminal(terminal)) {
                resultado.add(c);
            }
        }
        return resultado;
    }

    public void agregarComponente(Componente componente) {
        if (componente != null && !componentes.contains(componente)) {
            this.componentes.add(componente);
            this.componentesCinta.remove(componente);
        }
    }

    public void eliminarComponente(Componente componente) {
        if (componente == null) return;
        List<Cable> cablesAEliminar = new ArrayList<>();
        for (Cable cable : cables) {
            if ((cable.getTerminalOrigen() != null && cable.getTerminalOrigen().getComponentePadre() == componente) ||
                (cable.getTerminalDestino() != null && cable.getTerminalDestino().getComponentePadre() == componente)) {
                cablesAEliminar.add(cable);
            }
        }
        for (Cable cable : cablesAEliminar) {
            eliminarCable(cable);
        }
        this.componentes.remove(componente);
    }

    public void agregarCable(Cable cable) {
        if (cable != null && !cables.contains(cable)) {
            this.cables.add(cable);
            actualizar(0f);
        }
    }

    public void eliminarCable(Cable cable) {
        if (cable != null) {
            Terminal orig = cable.getTerminalOrigen();
            Terminal dest = cable.getTerminalDestino();
            cable.desconectar();
            this.cables.remove(cable);
            if (orig != null) {
                Cable otro = buscarCableConTerminal(orig);
                if (otro != null) orig.setCableConectado(otro);
            }
            if (dest != null) {
                Cable otro = buscarCableConTerminal(dest);
                if (otro != null) dest.setCableConectado(otro);
            }
            actualizar(0f);
        }
    }

    public static class Recorrido {
        public boolean pasoPorResistencia;
        public boolean pasoPorLed;
        public boolean llegoATierra;
        public boolean polaridadInvertida;
        public Componente ultimaPieza;
        public Resistencia resistencia;
        public LED led;
        public Potenciometro potenciometro;
        public final List<Cable> cables = new ArrayList<>();
    }

    protected FuenteAlimentacion buscarFuente() {
        for (Componente c : componentes) {
            if (c instanceof FuenteAlimentacion) return (FuenteAlimentacion) c;
        }
        return null;
    }

    protected Capacitor buscarCapacitor() {
        for (Componente c : componentes) {
            if (c instanceof Capacitor) return (Capacitor) c;
        }
        return null;
    }

    protected Temporizador555 buscar555() {
        for (Componente c : componentes) {
            if (c instanceof Temporizador555) return (Temporizador555) c;
        }
        return null;
    }

    protected CompuertaLogica buscarCompuerta() {
        for (Componente c : componentes) {
            if (c instanceof CompuertaLogica) return (CompuertaLogica) c;
        }
        return null;
    }

    protected List<Switch> buscarSwitches() {
        List<Switch> lista = new ArrayList<>();
        for (Componente c : componentes) {
            if (c instanceof Switch) lista.add((Switch) c);
        }
        return lista;
    }

    protected void apagarTodo() {
        for (Componente c : componentes) {
            for (Terminal t : c.getTerminales()) {
                t.setValorLogico(false);
            }
            c.setEstadoActual("NEUTRO");
            if (c instanceof LED) {
                ((LED) c).setQuemado(false);
                ((LED) c).setBrillo(1f);
            }
            if (c instanceof Resistencia) ((Resistencia) c).setQuemada(false);
        }
        for (Cable c : cables) {
            c.setTieneEnergia(false);
        }
    }

    protected Cable cableEntre(Terminal a, Terminal b) {
        for (Cable c : cables) {
            if (c.conectaTerminal(a) && c.conectaTerminal(b)) return c;
        }
        return null;
    }

    protected boolean estanConectados(Terminal a, Terminal b) {
        return cableEntre(a, b) != null;
    }

    protected boolean hayCortoEnFuente(FuenteAlimentacion fuente) {
        Cable corto = cableEntre(fuente.getTerminalPositivo(), fuente.getTerminalNegativo());
        if (corto == null) return false;
        fuente.setEstadoActual("ERROR");
        corto.setTieneEnergia(true);
        mensajeEstado = "¡CORTOCIRCUITO DIRECTO! Nunca unas el (+) directo al (-) de la fuente.";
        return true;
    }

    protected boolean revisarCapacitorAlReves(Capacitor capacitor, Terminal salida, FuenteAlimentacion fuente, boolean hayCorriente) {
        boolean alReves = estanConectados(salida, capacitor.getTerminalNegativo())
                || estanConectados(capacitor.getTerminalPositivo(), fuente.getTerminalNegativo());
        if (!alReves && !capacitor.isExplotado()) return false;
        if (alReves && hayCorriente) capacitor.explotar();
        if (capacitor.isExplotado()) {
            capacitor.evaluarEstado();
            for (Cable c : buscarCablesConTerminal(capacitor.getTerminalPositivo())) c.setTieneEnergia(true);
            for (Cable c : buscarCablesConTerminal(capacitor.getTerminalNegativo())) c.setTieneEnergia(true);
            mensajeEstado = "¡EL CAPACITOR EXPLOTÓ! Estaba al revés: su pata larga (+) va hacia la corriente.";
        } else {
            mensajeEstado = "Cuidado: ese capacitor está al revés.";
        }
        return true;
    }

    private Cable siguienteCableDelRecorrido(Terminal actual, Recorrido r) {
        for (Cable c : buscarCablesConTerminal(actual)) {
            if (r.cables.contains(c)) continue;
            Terminal otro = c.getOtroTerminal(actual);
            if (otro != null && otro.getComponentePadre() instanceof Capacitor) continue;
            return c;
        }
        return null;
    }

    protected Recorrido recorrerHastaTierra(Terminal inicio, FuenteAlimentacion fuente) {
        Recorrido r = new Recorrido();
        Set<Componente> visitadas = new HashSet<>();
        Terminal actual = inicio;

        while (actual != null) {
            Cable cable = siguienteCableDelRecorrido(actual, r);
            if (cable == null) break;
            r.cables.add(cable);

            Terminal destino = cable.getOtroTerminal(actual);
            Componente pieza = (destino != null) ? destino.getComponentePadre() : null;
            if (pieza == null) break;
            if (pieza == fuente) {
                r.llegoATierra = (destino == fuente.getTerminalNegativo());
                break;
            }
            if (visitadas.contains(pieza)) break;
            visitadas.add(pieza);
            r.ultimaPieza = pieza;

            if (pieza instanceof Resistencia) {
                Resistencia res = (Resistencia) pieza;
                r.pasoPorResistencia = true;
                r.resistencia = res;
                actual = (destino == res.getTerminalEntrada()) ? res.getTerminalSalida() : res.getTerminalEntrada();
            } else if (pieza instanceof Potenciometro) {
                Potenciometro pot = (Potenciometro) pieza;
                r.potenciometro = pot;
                actual = (destino == pot.getTerminalEntrada()) ? pot.getTerminalSalida() : pot.getTerminalEntrada();
            } else if (pieza instanceof LED) {
                LED led = (LED) pieza;
                if (destino == led.getTerminalCatodo()) {
                    r.polaridadInvertida = true;
                    break;
                }
                r.pasoPorLed = true;
                r.led = led;
                actual = led.getTerminalCatodo();
            } else {
                break;
            }
        }
        return r;
    }

    protected boolean revisarErroresDeSalida(Recorrido r, boolean hayCorriente, FuenteAlimentacion fuente) {
        if (r.llegoATierra && !r.pasoPorLed && !r.pasoPorResistencia && hayCorriente) {
            fuente.setEstadoActual("ERROR");
            for (Cable c : r.cables) c.setTieneEnergia(true);
            mensajeEstado = "¡CORTOCIRCUITO! La salida va directo al (-) sin pasar por el LED.";
            return false;
        }
        if (r.polaridadInvertida) {
            mensajeEstado = "Polaridad invertida: el LED está al revés y no deja pasar la corriente.";
            return false;
        }
        if (r.llegoATierra && r.pasoPorLed && !r.pasoPorResistencia) {
            if (hayCorriente) {
                r.led.setQuemado(true);
                r.led.setEstadoActual("ERROR");
                fuente.setEstadoActual("ERROR");
                for (Cable c : r.cables) c.setTieneEnergia(true);
                mensajeEstado = "¡EL LED SE HA QUEMADO! Faltó la resistencia de 220 Ohm.";
            } else {
                mensajeEstado = "Al LED le falta su protección.";
            }
            return false;
        }
        return true;
    }

    protected boolean revisarSalidaCompleta(Recorrido r) {
        if (!r.llegoATierra) {
            if (r.cables.isEmpty()) {
                mensajeEstado = "La corriente todavía no llega al LED.";
            } else if (r.ultimaPieza instanceof LED && !r.pasoPorResistencia) {
                mensajeEstado = "La corriente se queda atorada en el LED.";
            } else if (r.ultimaPieza instanceof Resistencia && !r.pasoPorLed) {
                mensajeEstado = "La corriente se queda atorada en la resistencia.";
            } else {
                mensajeEstado = "El circuito está abierto: la corriente no regresa a la fuente.";
            }
            return false;
        }
        if (!r.pasoPorLed) {
            mensajeEstado = "La corriente pasa, pero no hay ninguna luz en su camino.";
            return false;
        }
        return true;
    }

    protected void encenderTramoDeSalida(Recorrido r, FuenteAlimentacion fuente) {
        for (Cable c : r.cables) c.setTieneEnergia(true);
        fuente.setEstadoActual("EXITO");
        fuente.getTerminalPositivo().setValorLogico(true);
        fuente.getTerminalNegativo().setValorLogico(true);
        if (r.resistencia != null) {
            r.resistencia.setEstadoActual("EXITO");
            r.resistencia.getTerminalEntrada().setValorLogico(true);
            r.resistencia.getTerminalSalida().setValorLogico(true);
        }
        if (r.led != null) {
            r.led.setEstadoActual("EXITO");
            r.led.getTerminalAnodo().setValorLogico(true);
            r.led.getTerminalCatodo().setValorLogico(true);
        }
    }

    protected String mensajeCircuitoActivo() {
        if (tiempoCircuitoActivo >= getTiempoRequeridoVictoria()) {
            return "¡Nivel superado!";
        }
        return String.format("Circuito activo: %.1f / %.0f s", tiempoCircuitoActivo, getTiempoRequeridoVictoria());
    }

    public int getNumeroNivel() { return numeroNivel; }
    public void setNumeroNivel(int numeroNivel) { this.numeroNivel = numeroNivel; }

    public String getTituloReto() { return tituloReto; }
    public void setTituloReto(String tituloReto) { this.tituloReto = tituloReto; }

    public float getTiempoLimite() { return tiempoLimite; }
    public void setTiempoLimite(float tiempoLimite) { this.tiempoLimite = tiempoLimite; }

    public float getTiempoRestante() { return tiempoRestante; }
    public void setTiempoRestante(float tiempoRestante) { this.tiempoRestante = tiempoRestante; }

    public boolean estaCompletado() { return completado; }
    public boolean isCompletado() { return completado; }
    public void setCompletado(boolean completado) { this.completado = completado; }

    public boolean estaGanado() { return ganado; }
    public boolean isGanado() { return ganado; }
    public void setGanado(boolean ganado) { this.ganado = ganado; }

    public String getMensajeEstado() { return mensajeEstado; }
    public void setMensajeEstado(String mensajeEstado) { this.mensajeEstado = mensajeEstado; }

    public float getTiempoCircuitoActivo() { return tiempoCircuitoActivo; }
    public void setTiempoCircuitoActivo(float tiempoCircuitoActivo) { this.tiempoCircuitoActivo = tiempoCircuitoActivo; }
    public float getTiempoRequeridoVictoria() { return TIEMPO_REQUERIDO_VICTORIA; }

    public String getTextoProgreso() {
        return String.format("%.1f / %.0fs", tiempoCircuitoActivo, getTiempoRequeridoVictoria());
    }

    protected List<LED> buscarLeds() {
        List<LED> lista = new ArrayList<>();
        for (Componente c : componentes) {
            if (c instanceof LED) lista.add((LED) c);
        }
        return lista;
    }

    public List<Componente> getComponentes() { return componentes; }
    public List<Componente> getComponentesCinta() { return componentesCinta; }
    public List<Cable> getCables() { return cables; }
    public List<CasillaInventario> getInventario() { return inventario; }
}
