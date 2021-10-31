package parallelBFS;

import java.util.Calendar;

public class ParallelBFS_Main {

    public static void main(String[] args) {

        long inicio, termino;            // Medidores de tiempo de ejecución
        final int numerodeNodos = 5000;  // Tamaño del grafo 
        final int numerodeHilos = 4;	 // Total de hilos en ejecución

        boolean[] visitados = new boolean[numerodeNodos];    // Lista de visitados

        for (int i = 0; i < numerodeNodos; i++) // Inicializamos la lista de visitados 
        {
            visitados[i] = false;							 // a false
        }
        Graph grafo = new Graph(numerodeNodos, visitados, numerodeHilos);

        /*
         ******************************************************
         *                   BFS Paralelo                     *
	 ******************************************************
         */
        inicio = Calendar.getInstance().getTimeInMillis();   // Medimos el tiempo de inicio de ejecución 
        Thread[] processors = new Processor[numerodeHilos];  // Intanciamos una lista de cuatro hilos 

        for (int i = 0; i < numerodeHilos; i++) 
        {
            processors[i] = new Processor(grafo, i);         // Asigamos a cada hilo la referncia del grafo	
            processors[i].start();                           // y su identificador
        }

        for (int i = 0; i < numerodeHilos; i++)
        {
            try
            {
                processors[i].join();                        // Esperamos que cada hilo termine su trabajo
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
            }
        }

        termino = Calendar.getInstance().getTimeInMillis();               // Determinamos el tiempo de ejecución final
        System.out.println("Tiempo en Paralelo: " + (termino - inicio));  // Imprimimos el tiempo total de ejecución del Algoritmo

        boolean exito = true;
        for (int i = 0; i < numerodeNodos; i++)  // Se verifica que cada hilo haya culminado su 
        {                                        // trabajo satifactoriamente 
            if (!visitados[i])
            {
                exito = false;
                System.out.println("Failure");
                break;
            }
        }
        if (exito)
        {
            System.out.println("Exito en BFS paralelo!  ");
        }

        /*
         *********************************************************
         *                    BFS Secucencial                    *
	 *********************************************************
         */
        for (int i = 0; i < numerodeNodos; i++)  // Iniciamos nuevamente nuestra lista
        {                                        // de visitados a False
            visitados[i] = false;
        }
        inicio = Calendar.getInstance().getTimeInMillis();  // Determinamos el tiempo inicial 
        SerialBFS serialBFS = new SerialBFS(numerodeNodos, visitados, numerodeNodos - 1);  // Contructor de BFS
        Thread hiloBfs = new Thread(serialBFS);						   // con nodo 4999 como
        hiloBfs.start();                                                                   // nodo inicial 
                                                                                           // secuencial
        try
        {
            hiloBfs.join();  // esperamos la respuesta
        }                    // a cada hilo 
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }

        termino = Calendar.getInstance().getTimeInMillis();                  // Tiempo final de ejecución 
        System.out.println("Tiempo secuencial BFS: " + (termino - inicio));  // Tiempo total de ejecución

        exito = true;

        for (int i = 0; i < numerodeNodos; i++)
        {
            if (!visitados[i])
            {
                exito = false;
                System.out.println("Failure");
                break;
            }
        }
        if (exito)
        {
            System.out.println("Exito en BFS Secuencial  ");
        }
    }
}
