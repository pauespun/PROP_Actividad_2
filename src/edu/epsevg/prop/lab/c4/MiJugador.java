package edu.epsevg.prop.lab.c4;

public class MiJugador implements Jugador, IAuto {

    private int profunditatMax;

    public MiJugador(int prof) {
        this.profunditatMax = prof;
    }

    @Override
    public int moviment(Tauler t, int color) {
        // Llamamos al minimax root
        return minimaxRoot(t, color);
    }

    @Override
    public String nom() {
        return "MiJugador";
    }

    // ----------------------------------------------------------
    //  MINIMAX ROOT → decide la columna final
    // ----------------------------------------------------------
    private int minimaxRoot(Tauler t, int color) {

        int mejorCol = -1;
        int mejorValor = Integer.MIN_VALUE;

        for (int col = 0; col < t.getMida(); col++) {

            if (t.movpossible(col)) {

                Tauler copia = new Tauler(t);
                copia.afegeix(col, color);

                // Llamamos al minimax recursivo
                int valor = minimax(copia, profunditatMax - 1, false, -color);

                if (valor > mejorValor) {
                    mejorValor = valor;
                    mejorCol = col;
                }
            }
        }

        return mejorCol;
    }

    // ----------------------------------------------------------
    //  MINIMAX SIN PODA
    // ----------------------------------------------------------
    private int minimax(Tauler t, int profundidad, boolean maximizando, int color) {

        // CASO BASE: no quedan movimientos o profundidad 0
        if (profundidad == 0 || !t.espotmoure()) {
            return 0;   // SIN HEURÍSTICA AÚN
        }

        if (maximizando) {
            int mejor = Integer.MIN_VALUE;

            for (int col = 0; col < t.getMida(); col++) {

                if (t.movpossible(col)) {

                    Tauler copia = new Tauler(t);
                    copia.afegeix(col, color);

                    // Victoria inmediata
                    if (copia.solucio(col, color)) {
                        return Integer.MAX_VALUE / 2;
                    }

                    int valor = minimax(copia, profundidad - 1, false, -color);
                    mejor = Math.max(mejor, valor);
                }
            }

            return mejor;

        } else {
            int peor = Integer.MAX_VALUE;

            for (int col = 0; col < t.getMida(); col++) {

                if (t.movpossible(col)) {

                    Tauler copia = new Tauler(t);
                    copia.afegeix(col, color);

                    if (copia.solucio(col, color)) {
                        return Integer.MIN_VALUE / 2;
                    }

                    int valor = minimax(copia, profundidad - 1, true, -color);
                    peor = Math.min(peor, valor);
                }
            }

            return peor;
        }
    }
}
