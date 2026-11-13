package com.schematic.view;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.schematic.model.componentes.Capacitor;
import com.schematic.model.componentes.Componente;
import com.schematic.model.componentes.LED;
import com.schematic.model.componentes.Potenciometro;
import com.schematic.model.componentes.Resistencia;
import com.schematic.model.componentes.Terminal;

import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.TreeSet;

public class EnrutadorCables {

    private static final float MARGEN = 18f;
    private static final float SALIDA = 12f;
    private static final float SEPARACION_LINEAS = 40f;
    private static final float COSTO_VUELTA = 60f;
    private static final float COSTO_ENCIMAR = 3f;
    private static final float COSTO_MUY_CERCA = 0.7f;
    private static final float DISTANCIA_CERCA = 28f;
    private static final float COSTO_CRUCE = 150f;
    private static final float COSTO_OTRA_SALIDA = 50f;

    private static final float MIN_X = 10f, MAX_X = 1610f, MIN_Y = 112f, MAX_Y = 980f;

    private static final int[] DX = {1, 0, -1, 0};
    private static final int[] DY = {0, 1, 0, -1};
    private static final int CUALQUIERA = -1;

    private final List<Rectangle> obstaculos = new ArrayList<>();
    private final List<Tramo> tramosUsados = new ArrayList<>();
    private Terminal terminalA;
    private Terminal terminalB;

    private static class Tramo {
        float x1, y1, x2, y2;
        Terminal a, b;
        List<Vector2> ruta;

        Tramo(List<Vector2> ruta, int k, Terminal a, Terminal b) {
            Vector2 p = ruta.get(k), q = ruta.get(k + 1);
            this.x1 = p.x; this.y1 = p.y; this.x2 = q.x; this.y2 = q.y;
            this.a = a; this.b = b;
            this.ruta = ruta;
        }

        boolean esHorizontal() { return Math.abs(y1 - y2) < 0.5f; }
        boolean esVertical() { return Math.abs(x1 - x2) < 0.5f; }
    }

    private static class Salida {
        Vector2 punto;
        int dir;
        float costo;

        Salida(Vector2 punto, int dir, float costo) {
            this.punto = punto; this.dir = dir; this.costo = costo;
        }
    }

    private static class Camino {
        List<Vector2> puntos = new ArrayList<>();
        float costo;
    }

    private static class Estado implements Comparable<Estado> {
        int i, j, dir;
        float costo;

        Estado(int i, int j, int dir, float costo) {
            this.i = i; this.j = j; this.dir = dir; this.costo = costo;
        }

        @Override
        public int compareTo(Estado otro) {
            return Float.compare(costo, otro.costo);
        }
    }

    public void prepararMesa(List<Componente> componentes) {
        obstaculos.clear();
        tramosUsados.clear();
        for (Componente c : componentes) {
            obstaculos.add(new Rectangle(c.getPosicionX() - MARGEN, c.getPosicionY() - MARGEN,
                    c.getAncho() + 2 * MARGEN, c.getAlto() + 2 * MARGEN));
        }
    }

    public void marcarComoUsada(List<Vector2> ruta, Terminal a, Terminal b) {
        for (int k = 0; k < ruta.size() - 1; k++) {
            tramosUsados.add(new Tramo(ruta, k, a, b));
        }
    }

    public void olvidarRuta(List<Vector2> ruta) {
        tramosUsados.removeIf(t -> t.ruta == ruta);
    }

    public List<Vector2> calcularRuta(Terminal origen, Terminal destino, Vector2 codo) {
        terminalA = origen;
        terminalB = destino;
        List<Salida> inicios = salidasPosibles(origen);
        List<Salida> finales = salidasPosibles(destino);

        Camino camino = null;
        if (codo != null && !estaDentroDeObstaculo(codo.x, codo.y)) {
            Camino parte1 = buscarCamino(inicios, soloPunto(codo));
            Camino parte2 = buscarCamino(soloPunto(codo), finales);
            if (parte1 != null && parte2 != null) {
                camino = parte1;
                camino.puntos.addAll(parte2.puntos.subList(1, parte2.puntos.size()));
            }
        }
        if (camino == null) camino = buscarCamino(inicios, finales);

        List<Vector2> ruta = new ArrayList<>();
        ruta.add(origen.getPosicionAbsoluta());
        if (camino != null) {
            ruta.addAll(camino.puntos);
        } else {
            ruta.addAll(rutaSimple(inicios.get(0).punto, finales.get(0).punto));
        }
        ruta.add(destino.getPosicionAbsoluta());
        return quitarPuntosSobrantes(ruta);
    }

    public List<Vector2> calcularRutaHaciaPunto(Terminal origen, Vector2 punto) {
        terminalA = origen;
        terminalB = null;
        List<Salida> inicios = salidasPosibles(origen);

        List<Vector2> ruta = new ArrayList<>();
        ruta.add(origen.getPosicionAbsoluta());
        Camino camino = estaDentroDeObstaculo(punto.x, punto.y) ? null : buscarCamino(inicios, soloPunto(punto));
        if (camino != null) {
            ruta.addAll(camino.puntos);
        } else {
            ruta.addAll(rutaSimple(inicios.get(0).punto, punto));
            ruta.add(new Vector2(punto));
        }
        return quitarPuntosSobrantes(ruta);
    }

    private List<Salida> soloPunto(Vector2 punto) {
        List<Salida> lista = new ArrayList<>();
        lista.add(new Salida(new Vector2(punto), CUALQUIERA, 0f));
        return lista;
    }

    private List<Salida> salidasPosibles(Terminal t) {
        List<Salida> lista = new ArrayList<>();
        Componente c = t.getComponentePadre();
        if (c == null) {
            lista.add(new Salida(t.getPosicionAbsoluta(), CUALQUIERA, 0f));
            return lista;
        }
        int haciaAfuera = (t.getOffsetX() < c.getAncho() / 2f) ? 2 : 0;
        int preferida;
        if (c instanceof LED || c instanceof Capacitor || c instanceof Potenciometro) {
            preferida = 3;
            agregarSalida(lista, t, 3, 0f);
            agregarSalida(lista, t, haciaAfuera, COSTO_OTRA_SALIDA);
        } else if (c instanceof Resistencia) {
            preferida = haciaAfuera;
            agregarSalida(lista, t, haciaAfuera, 0f);
            agregarSalida(lista, t, 1, COSTO_OTRA_SALIDA);
            agregarSalida(lista, t, 3, COSTO_OTRA_SALIDA);
        } else {
            preferida = haciaAfuera;
            agregarSalida(lista, t, haciaAfuera, 0f);
        }
        if (lista.isEmpty()) {
            lista.add(new Salida(puntoDeSalida(t, preferida), preferida, 0f));
        }
        return lista;
    }

    private void agregarSalida(List<Salida> lista, Terminal t, int dir, float costoExtra) {
        Vector2 punto = puntoDeSalida(t, dir);
        if (punto.x < MIN_X || punto.x > MAX_X || punto.y < MIN_Y || punto.y > MAX_Y) return;
        if (estaDentroDeObstaculo(punto.x, punto.y)) return;
        lista.add(new Salida(punto, dir, costoExtra + t.getPosicionAbsoluta().dst(punto)));
    }

    private Vector2 puntoDeSalida(Terminal t, int dir) {
        Componente c = t.getComponentePadre();
        Vector2 pin = t.getPosicionAbsoluta();
        if (dir == 0) return new Vector2(c.getPosicionX() + c.getAncho() + MARGEN + SALIDA, pin.y);
        if (dir == 1) return new Vector2(pin.x, c.getPosicionY() + c.getAlto() + MARGEN + SALIDA);
        if (dir == 2) return new Vector2(c.getPosicionX() - MARGEN - SALIDA, pin.y);
        return new Vector2(pin.x, c.getPosicionY() - MARGEN - SALIDA);
    }

    private Camino buscarCamino(List<Salida> inicios, List<Salida> finales) {
        float[] xs = armarLineas(inicios, finales, true);
        float[] ys = armarLineas(inicios, finales, false);

        float[][][] mejorCosto = new float[xs.length][ys.length][4];
        int[][][] anterior = new int[xs.length][ys.length][4];
        for (int i = 0; i < xs.length; i++) {
            for (int j = 0; j < ys.length; j++) {
                for (int d = 0; d < 4; d++) {
                    mejorCosto[i][j][d] = Float.MAX_VALUE;
                    anterior[i][j][d] = -1;
                }
            }
        }

        PriorityQueue<Estado> cola = new PriorityQueue<>();
        for (Salida s : inicios) {
            int i = indiceDe(xs, s.punto.x), j = indiceDe(ys, s.punto.y);
            for (int d = 0; d < 4; d++) {
                if (s.dir != CUALQUIERA && d != s.dir) continue;
                if (s.costo < mejorCosto[i][j][d]) {
                    mejorCosto[i][j][d] = s.costo;
                    cola.add(new Estado(i, j, d, s.costo));
                }
            }
        }

        int[] finI = new int[finales.size()];
        int[] finJ = new int[finales.size()];
        for (int k = 0; k < finales.size(); k++) {
            finI[k] = indiceDe(xs, finales.get(k).punto.x);
            finJ[k] = indiceDe(ys, finales.get(k).punto.y);
        }

        Estado mejorFinal = null;
        float costoFinal = Float.MAX_VALUE;

        while (!cola.isEmpty()) {
            Estado e = cola.poll();
            if (e.costo > mejorCosto[e.i][e.j][e.dir]) continue;
            if (e.costo >= costoFinal) break;

            for (int k = 0; k < finales.size(); k++) {
                if (finI[k] != e.i || finJ[k] != e.j) continue;
                Salida f = finales.get(k);
                float total = e.costo + f.costo;
                if (f.dir != CUALQUIERA && e.dir != (f.dir + 2) % 4) total += COSTO_VUELTA;
                if (total < costoFinal) {
                    costoFinal = total;
                    mejorFinal = e;
                }
            }

            for (int d = 0; d < 4; d++) {
                if (d == (e.dir + 2) % 4) continue;
                int ni = e.i + DX[d], nj = e.j + DY[d];
                if (ni < 0 || nj < 0 || ni >= xs.length || nj >= ys.length) continue;

                float x1 = xs[e.i], y1 = ys[e.j], x2 = xs[ni], y2 = ys[nj];
                if (!esFinal(ni, nj, finI, finJ) && estaDentroDeObstaculo(x2, y2)) continue;
                if (estaDentroDeObstaculo((x1 + x2) / 2f, (y1 + y2) / 2f)) continue;

                float largo = Math.abs(x2 - x1) + Math.abs(y2 - y1);
                float costo = e.costo + largo;
                if (d != e.dir) costo += COSTO_VUELTA;
                costo += costoPorOtrosCables(x1, y1, x2, y2, largo);

                if (costo < mejorCosto[ni][nj][d]) {
                    mejorCosto[ni][nj][d] = costo;
                    anterior[ni][nj][d] = e.i * 100000 + e.j * 10 + e.dir;
                    cola.add(new Estado(ni, nj, d, costo));
                }
            }
        }

        if (mejorFinal == null) return null;

        Camino camino = new Camino();
        camino.costo = costoFinal;
        int i = mejorFinal.i, j = mejorFinal.j, dir = mejorFinal.dir;
        while (true) {
            camino.puntos.add(0, new Vector2(xs[i], ys[j]));
            int previo = anterior[i][j][dir];
            if (previo < 0) break;
            i = previo / 100000;
            j = (previo % 100000) / 10;
            dir = previo % 10;
        }
        return camino;
    }

    private boolean esFinal(int i, int j, int[] finI, int[] finJ) {
        for (int k = 0; k < finI.length; k++) {
            if (finI[k] == i && finJ[k] == j) return true;
        }
        return false;
    }

    private float costoPorOtrosCables(float x1, float y1, float x2, float y2, float largo) {
        boolean horizontal = Math.abs(y1 - y2) < 0.5f;
        float extra = 0f;
        for (Tramo t : tramosUsados) {
            if (esDelMismoNodo(t)) continue;
            float distancia = Float.MAX_VALUE;
            if (horizontal && t.esHorizontal() && seEnciman(x1, x2, t.x1, t.x2)) distancia = Math.abs(y1 - t.y1);
            if (!horizontal && t.esVertical() && seEnciman(y1, y2, t.y1, t.y2)) distancia = Math.abs(x1 - t.x1);
            if (distancia < 6f) {
                extra += largo * COSTO_ENCIMAR;
            } else if (distancia < DISTANCIA_CERCA) {
                extra += largo * COSTO_MUY_CERCA;
            } else if (horizontal && t.esVertical() && tocaRango(t.x1, x1, x2) && tocaRango(y1, t.y1, t.y2)) {
                extra += COSTO_CRUCE;
            } else if (!horizontal && t.esHorizontal() && tocaRango(t.y1, y1, y2) && tocaRango(x1, t.x1, t.x2)) {
                extra += COSTO_CRUCE;
            }
        }
        return extra;
    }

    private boolean esDelMismoNodo(Tramo t) {
        return (terminalA != null && (t.a == terminalA || t.b == terminalA))
                || (terminalB != null && (t.a == terminalB || t.b == terminalB));
    }

    private boolean tocaRango(float valor, float a, float b) {
        return valor >= Math.min(a, b) - 0.5f && valor <= Math.max(a, b) + 0.5f;
    }

    private boolean seEnciman(float a1, float a2, float b1, float b2) {
        return Math.min(Math.max(a1, a2), Math.max(b1, b2)) - Math.max(Math.min(a1, a2), Math.min(b1, b2)) > 1f;
    }

    private float[] armarLineas(List<Salida> inicios, List<Salida> finales, boolean esX) {
        float min = esX ? MIN_X : MIN_Y;
        float max = esX ? MAX_X : MAX_Y;
        TreeSet<Float> lineas = new TreeSet<>();
        for (float v = min; v <= max; v += SEPARACION_LINEAS) lineas.add(v);
        lineas.add(max);
        for (Salida s : inicios) lineas.add(esX ? s.punto.x : s.punto.y);
        for (Salida s : finales) lineas.add(esX ? s.punto.x : s.punto.y);
        for (Rectangle r : obstaculos) {
            float borde1 = esX ? r.x : r.y;
            float borde2 = esX ? r.x + r.width : r.y + r.height;
            if (borde1 >= min && borde1 <= max) lineas.add(borde1);
            if (borde2 >= min && borde2 <= max) lineas.add(borde2);
        }
        float[] resultado = new float[lineas.size()];
        int k = 0;
        for (float v : lineas) resultado[k++] = v;
        return resultado;
    }

    private int indiceDe(float[] lineas, float valor) {
        for (int k = 0; k < lineas.length; k++) {
            if (Math.abs(lineas[k] - valor) < 0.01f) return k;
        }
        return 0;
    }

    private boolean estaDentroDeObstaculo(float x, float y) {
        for (Rectangle r : obstaculos) {
            if (x > r.x + 0.5f && x < r.x + r.width - 0.5f && y > r.y + 0.5f && y < r.y + r.height - 0.5f) {
                return true;
            }
        }
        return false;
    }

    private List<Vector2> rutaSimple(Vector2 a, Vector2 b) {
        List<Vector2> ruta = new ArrayList<>();
        float medioX = (a.x + b.x) / 2f;
        ruta.add(new Vector2(a));
        ruta.add(new Vector2(medioX, a.y));
        ruta.add(new Vector2(medioX, b.y));
        ruta.add(new Vector2(b));
        return ruta;
    }

    private List<Vector2> quitarPuntosSobrantes(List<Vector2> puntos) {
        List<Vector2> limpia = new ArrayList<>();
        for (Vector2 p : puntos) {
            if (!limpia.isEmpty() && limpia.get(limpia.size() - 1).dst(p) < 0.5f) continue;
            if (limpia.size() >= 2) {
                Vector2 a = limpia.get(limpia.size() - 2), b = limpia.get(limpia.size() - 1);
                boolean mismaHorizontal = Math.abs(a.y - b.y) < 0.5f && Math.abs(b.y - p.y) < 0.5f;
                boolean mismaVertical = Math.abs(a.x - b.x) < 0.5f && Math.abs(b.x - p.x) < 0.5f;
                if (mismaHorizontal || mismaVertical) {
                    limpia.set(limpia.size() - 1, p);
                    continue;
                }
            }
            limpia.add(p);
        }
        return limpia;
    }
}
