package com.schematic.view;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.schematic.model.componentes.Capacitor;
import com.schematic.model.componentes.Componente;
import com.schematic.model.componentes.LED;
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

    private static final float MIN_X = 10f, MAX_X = 1610f, MIN_Y = 112f, MAX_Y = 980f;

    private static final int[] DX = {1, 0, -1, 0};
    private static final int[] DY = {0, 1, 0, -1};
    private static final int CUALQUIERA = -1;

    private final List<Rectangle> obstaculos = new ArrayList<>();
    private final List<float[]> tramosUsados = new ArrayList<>();

    public void prepararMesa(List<Componente> componentes) {
        obstaculos.clear();
        tramosUsados.clear();
        for (Componente c : componentes) {
            obstaculos.add(new Rectangle(c.getPosicionX() - MARGEN, c.getPosicionY() - MARGEN,
                    c.getAncho() + 2 * MARGEN, c.getAlto() + 2 * MARGEN));
        }
    }

    public List<Vector2> calcularRuta(Terminal origen, Terminal destino, Vector2 codo) {
        Vector2 pinA = origen.getPosicionAbsoluta();
        Vector2 pinB = destino.getPosicionAbsoluta();
        int dirA = direccionDeSalida(origen);
        int dirB = direccionDeSalida(destino);
        Vector2 salidaA = puntoDeSalida(origen, dirA);
        Vector2 salidaB = puntoDeSalida(destino, dirB);

        int llegadaB = (dirB + 2) % 4;

        List<Vector2> medio;
        if (codo != null && !estaDentroDeObstaculo(codo.x, codo.y)) {
            List<Vector2> parte1 = buscarCamino(salidaA, dirA, codo, CUALQUIERA);
            List<Vector2> parte2 = buscarCamino(codo, CUALQUIERA, salidaB, llegadaB);
            if (parte1 != null && parte2 != null) {
                medio = parte1;
                medio.addAll(parte2.subList(1, parte2.size()));
            } else {
                medio = buscarCamino(salidaA, dirA, salidaB, llegadaB);
            }
        } else {
            medio = buscarCamino(salidaA, dirA, salidaB, llegadaB);
        }

        List<Vector2> ruta = new ArrayList<>();
        ruta.add(pinA);
        if (medio != null) {
            ruta.addAll(medio);
        } else {
            ruta.addAll(rutaSimple(salidaA, salidaB));
        }
        ruta.add(pinB);
        return quitarPuntosSobrantes(ruta);
    }

    public List<Vector2> calcularRutaHaciaPunto(Terminal origen, Vector2 punto) {
        Vector2 pinA = origen.getPosicionAbsoluta();
        int dirA = direccionDeSalida(origen);
        Vector2 salidaA = puntoDeSalida(origen, dirA);

        List<Vector2> ruta = new ArrayList<>();
        ruta.add(pinA);
        List<Vector2> medio = estaDentroDeObstaculo(punto.x, punto.y) ? null : buscarCamino(salidaA, dirA, punto, CUALQUIERA);
        if (medio != null) {
            ruta.addAll(medio);
        } else {
            ruta.addAll(rutaSimple(salidaA, punto));
            ruta.add(new Vector2(punto));
        }
        return quitarPuntosSobrantes(ruta);
    }

    private int direccionDeSalida(Terminal t) {
        Componente c = t.getComponentePadre();
        if (c == null) return 0;
        if (c instanceof LED || c instanceof Capacitor) return 3;
        return (t.getOffsetX() < c.getAncho() / 2f) ? 2 : 0;
    }

    private Vector2 puntoDeSalida(Terminal t, int dir) {
        Componente c = t.getComponentePadre();
        Vector2 pin = t.getPosicionAbsoluta();
        if (c == null) return pin;
        if (dir == 3) return new Vector2(pin.x, c.getPosicionY() - MARGEN - SALIDA);
        if (dir == 2) return new Vector2(c.getPosicionX() - MARGEN - SALIDA, pin.y);
        return new Vector2(c.getPosicionX() + c.getAncho() + MARGEN + SALIDA, pin.y);
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

    private List<Vector2> buscarCamino(Vector2 inicio, int dirInicial, Vector2 fin, int dirLlegada) {
        float[] xs = armarLineas(inicio.x, fin.x, true);
        float[] ys = armarLineas(inicio.y, fin.y, false);
        int iInicio = indiceDe(xs, inicio.x), jInicio = indiceDe(ys, inicio.y);
        int iFin = indiceDe(xs, fin.x), jFin = indiceDe(ys, fin.y);

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
        for (int d = 0; d < 4; d++) {
            if (dirInicial == CUALQUIERA || d == dirInicial) {
                mejorCosto[iInicio][jInicio][d] = 0f;
                cola.add(new Estado(iInicio, jInicio, d, 0f));
            }
        }

        Estado mejorFinal = null;
        float costoFinal = Float.MAX_VALUE;

        while (!cola.isEmpty()) {
            Estado e = cola.poll();
            if (e.costo > mejorCosto[e.i][e.j][e.dir]) continue;
            if (e.costo >= costoFinal) break;

            if (e.i == iFin && e.j == jFin) {
                float extra = (dirLlegada != CUALQUIERA && e.dir != dirLlegada) ? COSTO_VUELTA : 0f;
                if (e.costo + extra < costoFinal) {
                    costoFinal = e.costo + extra;
                    mejorFinal = e;
                }
                continue;
            }

            for (int d = 0; d < 4; d++) {
                if (d == (e.dir + 2) % 4) continue;
                int ni = e.i + DX[d], nj = e.j + DY[d];
                if (ni < 0 || nj < 0 || ni >= xs.length || nj >= ys.length) continue;

                float x1 = xs[e.i], y1 = ys[e.j], x2 = xs[ni], y2 = ys[nj];
                boolean esFin = (ni == iFin && nj == jFin);
                if (!esFin && estaDentroDeObstaculo(x2, y2)) continue;
                if (estaDentroDeObstaculo((x1 + x2) / 2f, (y1 + y2) / 2f)) continue;

                float largo = Math.abs(x2 - x1) + Math.abs(y2 - y1);
                float costo = e.costo + largo;
                if (d != e.dir) costo += COSTO_VUELTA;
                if (vaEncimaDeOtroCable(x1, y1, x2, y2)) costo += largo * COSTO_ENCIMAR;

                if (costo < mejorCosto[ni][nj][d]) {
                    mejorCosto[ni][nj][d] = costo;
                    anterior[ni][nj][d] = e.i * 100000 + e.j * 10 + e.dir;
                    cola.add(new Estado(ni, nj, d, costo));
                }
            }
        }

        if (mejorFinal == null) return null;

        List<Vector2> camino = new ArrayList<>();
        int i = mejorFinal.i, j = mejorFinal.j, dir = mejorFinal.dir;
        while (true) {
            camino.add(0, new Vector2(xs[i], ys[j]));
            int previo = anterior[i][j][dir];
            if (previo < 0) break;
            i = previo / 100000;
            j = (previo % 100000) / 10;
            dir = previo % 10;
        }
        return camino;
    }

    private float[] armarLineas(float a, float b, boolean esX) {
        float min = esX ? MIN_X : MIN_Y;
        float max = esX ? MAX_X : MAX_Y;
        TreeSet<Float> lineas = new TreeSet<>();
        for (float v = min; v <= max; v += SEPARACION_LINEAS) lineas.add(v);
        lineas.add(max);
        lineas.add(a);
        lineas.add(b);
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

    private boolean vaEncimaDeOtroCable(float x1, float y1, float x2, float y2) {
        for (float[] t : tramosUsados) {
            boolean horizontal = Math.abs(y1 - y2) < 0.5f && Math.abs(t[1] - t[3]) < 0.5f && Math.abs(y1 - t[1]) < 6f;
            boolean vertical = Math.abs(x1 - x2) < 0.5f && Math.abs(t[0] - t[2]) < 0.5f && Math.abs(x1 - t[0]) < 6f;
            if (horizontal && seEnciman(x1, x2, t[0], t[2])) return true;
            if (vertical && seEnciman(y1, y2, t[1], t[3])) return true;
        }
        return false;
    }

    private boolean seEnciman(float a1, float a2, float b1, float b2) {
        return Math.min(Math.max(a1, a2), Math.max(b1, b2)) - Math.max(Math.min(a1, a2), Math.min(b1, b2)) > 1f;
    }

    public void marcarComoUsada(List<Vector2> ruta) {
        for (int k = 0; k < ruta.size() - 1; k++) {
            Vector2 a = ruta.get(k), b = ruta.get(k + 1);
            tramosUsados.add(new float[]{a.x, a.y, b.x, b.y});
        }
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
