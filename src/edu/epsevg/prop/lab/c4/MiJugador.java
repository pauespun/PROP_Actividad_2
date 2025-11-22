package edu.epsevg.prop.lab.c4;

public class MiJugador implements Jugador, IAuto {

    private int profunditatMax;

    // Pesos heurísticos
    private static final int CUATRO     = 100000;
    private static final int TRES       = 1000;
    private static final int DOS        = 100;
    private static final int CUATRO_ENE = -100000;
    private static final int TRES_ENE   = -1200;
    private static final int DOS_ENE    = -150;

    public MiJugador(int prof) {
        this.profunditatMax = prof;
    }

    @Override
    public int moviment(Tauler t, int color) {
        return minimaxRoot(t, color);
    }

    @Override
    public String nom() {
        return "MiJugador";
    }

    // =============================================================
    //  MINIMAX ROOT + ALPHA-BETA
    // =============================================================
    private int minimaxRoot(Tauler t, int color) {

        int mejorCol = -1;
        int mejorValor = Integer.MIN_VALUE;

        int alpha = Integer.MIN_VALUE;
        int beta = Integer.MAX_VALUE;

        for (int col = 0; col < t.getMida(); col++) {

            if (t.movpossible(col)) {

                Tauler copia = new Tauler(t);
                copia.afegeix(col, color);

                // victoria inmediata
                if (copia.solucio(col, color)) {
                    return col;
                }

                int valor = minimax(copia, profunditatMax - 1, false, -color, alpha, beta);

                if (valor > mejorValor) {
                    mejorValor = valor;
                    mejorCol = col;
                }

                alpha = Math.max(alpha, valor);
            }
        }

        return mejorCol;
    }

    // =============================================================
    //  MINIMAX + PODA ALPHA-BETA
    // =============================================================
    private int minimax(Tauler t, int profundidad, boolean maximizando, int color,
                        int alpha, int beta) {

        // caso base
        if (profundidad == 0 || !t.espotmoure()) {
            return evaluarTablero(t, color);
        }

        if (maximizando) {
            int mejor = Integer.MIN_VALUE;

            for (int col = 0; col < t.getMida(); col++) {

                if (t.movpossible(col)) {
                    Tauler copia = new Tauler(t);
                    copia.afegeix(col, color);

                    if (copia.solucio(col, color)) {
                        return CUATRO;  // victoria fuerte
                    }

                    int valor = minimax(copia, profundidad - 1, false, -color, alpha, beta);

                    mejor = Math.max(mejor, valor);
                    alpha = Math.max(alpha, mejor);

                    if (beta <= alpha) break;
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
                        return CUATRO_ENE;  // derrota fuerte
                    }

                    int valor = minimax(copia, profundidad - 1, true, -color, alpha, beta);

                    peor = Math.min(peor, valor);
                    beta = Math.min(beta, peor);

                    if (beta <= alpha) break;
                }
            }

            return peor;
        }
    }

    // =============================================================
    //  HEURÍSTICA COMPLETA
    // =============================================================
    private int evaluarTablero(Tauler t, int color) {
        int score = 0;

        // == 1) Priorizar columna central ==
        int colCentral = t.getMida() / 2;
        for (int fila = 0; fila < t.getMida(); fila++) {
            int c = t.getColor(fila, colCentral);
            if (c == color) score += 5;
            else if (c == -color) score -= 5;
        }

        // == 2) Evaluar ventanas horizontales ==
        for (int fila = 0; fila < t.getMida(); fila++) {
            for (int col = 0; col <= t.getMida() - 4; col++) {
                int[] ventana = {
                        t.getColor(fila, col),
                        t.getColor(fila, col + 1),
                        t.getColor(fila, col + 2),
                        t.getColor(fila, col + 3)
                };
                score += puntuarVentana(ventana, color);
            }
        }

        // == 3) Evaluar ventanas verticales ==
        for (int col = 0; col < t.getMida(); col++) {
            for (int fila = 0; fila <= t.getMida() - 4; fila++) {
                int[] ventana = {
                        t.getColor(fila, col),
                        t.getColor(fila + 1, col),
                        t.getColor(fila + 2, col),
                        t.getColor(fila + 3, col)
                };
                score += puntuarVentana(ventana, color);
            }
        }

        // == 4) Diagonal ↘ ==
        for (int fila = 0; fila <= t.getMida() - 4; fila++) {
            for (int col = 0; col <= t.getMida() - 4; col++) {
                int[] ventana = {
                        t.getColor(fila, col),
                        t.getColor(fila + 1, col + 1),
                        t.getColor(fila + 2, col + 2),
                        t.getColor(fila + 3, col + 3)
                };
                score += puntuarVentana(ventana, color);
            }
        }

        // == 5) Diagonal ↗ ==
        for (int fila = 3; fila < t.getMida(); fila++) {
            for (int col = 0; col <= t.getMida() - 4; col++) {
                int[] ventana = {
                        t.getColor(fila, col),
                        t.getColor(fila - 1, col + 1),
                        t.getColor(fila - 2, col + 2),
                        t.getColor(fila - 3, col + 3)
                };
                score += puntuarVentana(ventana, color);
            }
        }

        return score;
    }

    // =============================================================
    //  PUNTUACIÓN DE UNA VENTANA DE 4
    // =============================================================
    private int puntuarVentana(int[] ventana, int color) {

        int mine = 0, opp = 0, empty = 0;

        for (int c : ventana) {
            if (c == color) mine++;
            else if (c == -color) opp++;
            else empty++;
        }

        if (mine == 4) return CUATRO;
        if (mine == 3 && empty == 1) return TRES;
        if (mine == 2 && empty == 2) return DOS;

        if (opp == 4) return CUATRO_ENE;
        if (opp == 3 && empty == 1) return TRES_ENE;
        if (opp == 2 && empty == 2) return DOS_ENE;

        return 0;
    }
}
