package edu.epsevg.prop.lab.c4;

import java.util.ArrayList;
import java.util.List;

public class MiJugador implements Jugador, IAuto 
{

    private int _profundidadMaxima;   
    private long _contadorNodosHoja; 

    private static final int PUNTUACION_VICTORIA = 1000000;
    private static final int PUNTUACION_BLOQUEO_AMENAZA = 50000;
    private static final int PUNTUACION_3_RAYA_JUGABLE = 500;
    private static final int PUNTUACION_2_RAYA = 10;
    private static final int[] PESO_COLUMNA_7 = {1, 2, 3, 4, 3, 2, 1};

    public MiJugador(int profundidad) 
    {
        this._profundidadMaxima = profundidad;
        this._contadorNodosHoja = 0; // Inicializar
    }

    public long getNodosHojaExplorados() 
    {
        return _contadorNodosHoja;
    }

    @Override
    public int moviment(Tauler t, int color) 
    {
        return minimaxRaiz(t, color);
    }

    @Override
    public String nom() 
    {
        return "MiJugador (Refactor)";
    }
// Función RECURSIVA de Minimax con poda Alfa-Beta.
 
    private int minimaxRaiz(Tauler t, int color) 
    {
        this._contadorNodosHoja = 0;
        
        int mejorColumna = -1;
        int mejorValor = Integer.MIN_VALUE;
        int alfa = Integer.MIN_VALUE;
        int beta = Integer.MAX_VALUE;

        for (int col : obtenerMovimientosOrdenados(t)) 
        {
            Tauler copia = new Tauler(t);
            copia.afegeix(col, color);

            int valor = minimaxRecursivo(copia, _profundidadMaxima - 1, false, -color, color, alfa, beta);

            if (valor > mejorValor) 
            {
                mejorValor = valor;
                mejorColumna = col;
            }
            
            alfa = Math.max(alfa, mejorValor);
        }
        
        return mejorColumna;
    }
     
    private int minimaxRecursivo(Tauler t, int profundidad, boolean esMax, int color, int colorRaiz, int alfa, int beta) 
    {

        if (profundidad == 0 || !t.espotmoure()) 
        {
            this._contadorNodosHoja++; 
            
            return evaluarTablero(t, colorRaiz);
        }

        if (esMax) 
        { 
            int mejor = Integer.MIN_VALUE;
            
            for (int col : obtenerMovimientosOrdenados(t)) 
            {
                Tauler copia = new Tauler(t);
                copia.afegeix(col, color);

                if (copia.solucio(col, color)) 
                {
                    return PUNTUACION_VICTORIA + profundidad; 
                }

                int valor = minimaxRecursivo(copia, profundidad - 1, false, -color, colorRaiz, alfa, beta);
                mejor = Math.max(mejor, valor);
                alfa = Math.max(alfa, mejor);

                if (beta <= alfa) 
                {
                    break; // Poda Beta
                }
            }
            return mejor;
        } 
        else 
        {
            int peor = Integer.MAX_VALUE;
            
            for (int col : obtenerMovimientosOrdenados(t)) 
            {
                Tauler copia = new Tauler(t);
                copia.afegeix(col, color);

                if (copia.solucio(col, color)) 
                {
                    return -PUNTUACION_VICTORIA - profundidad; 
                }

                int valor = minimaxRecursivo(copia, profundidad - 1, true, -color, colorRaiz, alfa, beta);
                peor = Math.min(peor, valor);
                beta = Math.min(beta, peor);

                if (beta <= alfa) 
                {
                    break; // Poda Alfa
                }
            }
            return peor;
        }
    }

    private List<Integer> obtenerMovimientosOrdenados(Tauler t) 
    {
        List<Integer> movimientos = new ArrayList<>();
        int mida = t.getMida();
        int centro = mida / 2;

        for (int i = 0; i <= centro; i++) 
        {
            if (i == 0) 
            {
                if (t.movpossible(centro)) 
                {
                    movimientos.add(centro);
                }
            } 
            else 
            {
                if (centro + i < mida && t.movpossible(centro + i)) 
                {
                    movimientos.add(centro + i);
                }
                if (centro - i >= 0 && t.movpossible(centro - i)) 
                {
                    movimientos.add(centro - i);
                }
            }
        }
        return movimientos;
    }



    // HEURÍSTICA 
    private int evaluarTablero(Tauler t, int colorRaiz) 
    {
        int puntuacion = 0;
        int colorOponente = -colorRaiz;
        final int MIDA = t.getMida();
        
        boolean usarPesos = (MIDA == 7);
        
        for (int f = 0; f < MIDA; f++) 
        {
            for (int c = 0; c < MIDA; c++) 
            {
                if (usarPesos) 
                {
                    if (t.getColor(f, c) == colorRaiz) 
                    {
                        puntuacion += PESO_COLUMNA_7[c];
                    } 
                    else if (t.getColor(f, c) == colorOponente) 
                    {
                        puntuacion -= PESO_COLUMNA_7[c];
                    }
                }
            }
        }

        int[] ventana = new int[4];
        
        for (int f = 0; f < MIDA; f++) 
        {
            for (int c = 0; c <= MIDA - 4; c++) 
            {
                for(int i=0; i<4; i++) ventana[i] = t.getColor(f, c+i);
                puntuacion += evaluarVentana(t, ventana, colorRaiz, f, c, 0, 1);
            }
        }

        for (int c = 0; c < MIDA; c++) 
        {
            for (int f = 0; f <= MIDA - 4; f++) 
            {
                for(int i=0; i<4; i++) ventana[i] = t.getColor(f+i, c);
                puntuacion += evaluarVentana(t, ventana, colorRaiz, f, c, 1, 0);
            }
        }
        
        for (int f = 0; f <= MIDA - 4; f++) 
        {
            for (int c = 0; c <= MIDA - 4; c++) 
            {
                for(int i=0; i<4; i++) ventana[i] = t.getColor(f+i, c+i);
                puntuacion += evaluarVentana(t, ventana, colorRaiz, f, c, 1, 1);
            }
        }
        
        for (int f = 3; f < MIDA; f++) 
        {
            for (int c = 0; c <= MIDA - 4; c++) 
            {
                for(int i=0; i<4; i++) ventana[i] = t.getColor(f-i, c+i);
                puntuacion += evaluarVentana(t, ventana, colorRaiz, f, c, -1, 1);
            }
        }     
        return puntuacion;
    }

    private int evaluarVentana(Tauler t, int[] v, int colorRaiz, int f, int c, int df, int dc) 
    {
        int puntuacion = 0;
        int colorOponente = -colorRaiz;
        
        int piezasRaiz = 0;
        int piezasOponente = 0;
        int piezasVacias = 0;

        for (int i = 0; i < 4; i++) 
        {
            if (v[i] == colorRaiz) piezasRaiz++;
            else if (v[i] == colorOponente) piezasOponente++;
            else piezasVacias++;
        }
        
        if (piezasRaiz == 3 && piezasVacias == 1) 
        {
            if (esAmenazaJugable(t, f, c, df, dc)) 
            {
                puntuacion += PUNTUACION_3_RAYA_JUGABLE;
            }
        } 
        else if (piezasRaiz == 2 && piezasVacias == 2) 
        {
            puntuacion += PUNTUACION_2_RAYA;
        }

        if (piezasOponente == 3 && piezasVacias == 1) 
        {
            if (esAmenazaJugable(t, f, c, df, dc)) 
            {
                puntuacion -= PUNTUACION_BLOQUEO_AMENAZA;
            }
        } 
        else if (piezasOponente == 2 && piezasVacias == 2) 
        {
            puntuacion -= PUNTUACION_2_RAYA;
        }
        
        return puntuacion;
    }

    private boolean esCasillaJugable(Tauler t, int f, int c) 
    {
        if (t.getColor(f, c) != 0) 
        {
            return false;
        }
        return (f == 0) || (t.getColor(f - 1, c) != 0);
    }

    private boolean esAmenazaJugable(Tauler t, int f, int c, int df, int dc) 
    {

        if (df == 1 && dc == 0) 
        {
            return true;
        }

        for (int i = 0; i < 4; i++) 
        {
            int filaActual = f + (i * df);
            int columnaActual = c + (i * dc);
            
            if (t.getColor(filaActual, columnaActual) == 0) 
            {
                return esCasillaJugable(t, filaActual, columnaActual);
            }
        }
        return false; 
    }
}