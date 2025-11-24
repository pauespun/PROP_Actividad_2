package edu.epsevg.prop.lab.c4;

public class MiJugador implements Jugador, IAuto {

    private int profundidadMaxima;


    private static final int PUNTUACION_CUATRO     = 100000;
    private static final int PUNTUACION_TRES       = 1000;
    private static final int PUNTUACION_DOS        = 100;

    private static final int PENAL_CUATRO_ENEMIGO  = -100000;
    private static final int PENAL_TRES_ENEMIGO    = -1200;
    private static final int PENAL_DOS_ENEMIGO     = -150;
    
    private long nodosExplorados = 0;

    public MiJugador(int profundidad) {
        this.profundidadMaxima = profundidad;
    }

    @Override
    public int moviment(Tauler tablero, int colorJugador) {
        nodosExplorados = 0;
        int movimientoElegido = minimaxRaiz(tablero, colorJugador);
        System.out.println("Nodos explorados: " + nodosExplorados);
        return movimientoElegido;
    }

    @Override
    public String nom() {
        return "MiJugador";
    }


    private int minimaxRaiz(Tauler tablero, int colorJugador) {

        int mejorColumna = -1;
        int mejorValor = Integer.MIN_VALUE;

        int alfa = Integer.MIN_VALUE;
        int beta = Integer.MAX_VALUE;

        for (int columna = 0; columna < tablero.getMida(); columna++) {

            if (tablero.movpossible(columna)) {

                Tauler copiaTablero = new Tauler(tablero);
                copiaTablero.afegeix(columna, colorJugador);

                if (copiaTablero.solucio(columna, colorJugador)) {
                    return columna;
                }

                int valor = minimax(
                        copiaTablero,
                        profundidadMaxima - 1,
                        false,
                        -colorJugador,
                        alfa,
                        beta
                );

                if (valor > mejorValor) {
                    mejorValor = valor;
                    mejorColumna = columna;
                }

                alfa = Math.max(alfa, valor);
            }
        }

        return mejorColumna;
    }

    private int minimax(Tauler tablero, int profundidadRestante, boolean max, int colorActual, int alfa, int beta) {
        nodosExplorados++;
        if (profundidadRestante == 0 || !tablero.espotmoure()) {
            return evaluarTablero(tablero, colorActual);
        }

        if (max) {

            int mejorValor = Integer.MIN_VALUE;

            for (int columna = 0; columna < tablero.getMida(); columna++) {

                if (tablero.movpossible(columna)) {

                    Tauler copiaTablero = new Tauler(tablero);
                    copiaTablero.afegeix(columna, colorActual);

                    if (copiaTablero.solucio(columna, colorActual)) {
                        return PUNTUACION_CUATRO;
                    }

                    int valor = minimax(
                            copiaTablero,
                            profundidadRestante - 1,
                            false,
                            -colorActual,
                            alfa,
                            beta
                    );

                    mejorValor = Math.max(mejorValor, valor);
                    alfa = Math.max(alfa, mejorValor);

                    if (beta <= alfa) break;
                }
            }

            return mejorValor;

        } else {

            int peorValor = Integer.MAX_VALUE;

            for (int columna = 0; columna < tablero.getMida(); columna++) {

                if (tablero.movpossible(columna)) {

                    Tauler copiaTablero = new Tauler(tablero);
                    copiaTablero.afegeix(columna, colorActual);

                    if (copiaTablero.solucio(columna, colorActual)) {
                        return PENAL_CUATRO_ENEMIGO;
                    }

                    int valor = minimax(
                            copiaTablero,
                            profundidadRestante - 1,
                            true,
                            -colorActual,
                            alfa,
                            beta
                    );

                    peorValor = Math.min(peorValor, valor);
                    beta = Math.min(beta, peorValor);

                    if (beta <= alfa) break;
                }
            }

            return peorValor;
        }
    }

    private int evaluarTablero(Tauler tablero, int colorJugador) {

        int puntuacion = 0;

        int mida = tablero.getMida();
        int columnaCentral = mida / 2;

        for (int fila = 0; fila < mida; fila++) {
            int valor = tablero.getColor(fila, columnaCentral);
            if (valor == colorJugador) puntuacion += 5;
            else if (valor == -colorJugador) puntuacion -= 5;
        }

        for (int fila = 0; fila < mida; fila++) {
            for (int columna = 0; columna <= mida - 4; columna++) {

                int[] ventana = {
                        tablero.getColor(fila, columna),
                        tablero.getColor(fila, columna + 1),
                        tablero.getColor(fila, columna + 2),
                        tablero.getColor(fila, columna + 3)
                };

                puntuacion += puntuarVentana(ventana, colorJugador);
            }
        }

        for (int columna = 0; columna < mida; columna++) {
            for (int fila = 0; fila <= mida - 4; fila++) {

                int[] ventana = {
                        tablero.getColor(fila, columna),
                        tablero.getColor(fila + 1, columna),
                        tablero.getColor(fila + 2, columna),
                        tablero.getColor(fila + 3, columna)
                };

                puntuacion += puntuarVentana(ventana, colorJugador);
            }
        }

        for (int fila = 0; fila <= mida - 4; fila++) {
            for (int columna = 0; columna <= mida - 4; columna++) {

                int[] ventana = {
                        tablero.getColor(fila, columna),
                        tablero.getColor(fila + 1, columna + 1),
                        tablero.getColor(fila + 2, columna + 2),
                        tablero.getColor(fila + 3, columna + 3)
                };

                puntuacion += puntuarVentana(ventana, colorJugador);
            }
        }

        for (int fila = 3; fila < mida; fila++) {
            for (int columna = 0; columna <= mida - 4; columna++) {

                int[] ventana = {
                        tablero.getColor(fila, columna),
                        tablero.getColor(fila - 1, columna + 1),
                        tablero.getColor(fila - 2, columna + 2),
                        tablero.getColor(fila - 3, columna + 3)
                };

                puntuacion += puntuarVentana(ventana, colorJugador);
            }
        }

        return puntuacion;
    }

    private int puntuarVentana(int[] ventana, int colorJugador) {

        int propias = 0;
        int enemigas = 0;
        int vacias = 0;

        for (int casilla : ventana) {
            if (casilla == colorJugador) propias++;
            else if (casilla == -colorJugador) enemigas++;
            else vacias++;
        }

        if (propias == 4) return PUNTUACION_CUATRO;
        if (propias == 3 && vacias == 1) return PUNTUACION_TRES;
        if (propias == 2 && vacias == 2) return PUNTUACION_DOS;

        if (enemigas == 4) return PENAL_CUATRO_ENEMIGO;
        if (enemigas == 3 && vacias == 1) return PENAL_TRES_ENEMIGO;
        if (enemigas == 2 && vacias == 2) return PENAL_DOS_ENEMIGO;

        return 0;
    }
}
