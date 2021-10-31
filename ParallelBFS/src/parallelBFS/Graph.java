package parallelBFS;

import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Random;

public class Graph {

    private int size;                          // Numero de nodo dentro del grafo
    private int[][] vertices;                  // Matriz de Adjacencia 
    private Queue<Integer> globalQueue;        // Cola Global compartida para Bfs 
    private boolean[] visitados;               // Lista de nodos visitados 
    private List<Queue<Integer>> localQueues;  // Colas Privadas para cada hilo
    private boolean isDone;                    // Indicador 
    private int contador;                      // Contador 

    /*
     * Método que devuelve la referencia de las colas para cada hilo
     *
     * @return referencia de la lista de colas privadas
     */
    public List<Queue<Integer>> getLocalQueues()
    {
        return localQueues;
    }

    /*
     * Setter de colas locales
     *
     * @param localQueues colas privadas para cada hilo
     */
    public void setLocalQueues(List<Queue<Integer>> localQueues)
    {
        this.localQueues = localQueues;
    }

    /*
     * Devuelve el tamaño del grafo
     *
     * @return tamaño del grafo
     */
    public int getSize()  // Getter del tamaño del grafo
    {
        return size;
    }

    /*
     * El método sincroniza la visita de un nodo con el resto de hilos
     *
     * @return true o false ( si se havisitado ono el nodo )
     */
    public synchronized boolean getVisited(int index)  // Lista de visitados es una región compartida
    {
        return visitados[index];
    }

    /*
     * El método sincroniza la asignación de la visita de un nodo con el resto
     * de hilos
     *
     * @param index el indice del nodo
     * @param value true si se ha vistado , false sino ha sido visitado
     */
    public void setVisited(int index, boolean value)
    {
        visitados[index] = value;
    }

    public synchronized void incrementCounter()
    {
        contador++;
    }

    public boolean isDone()
    {
        return isDone;
    }

    /*
     * Método constructor del grafo
     *
     * @param size tamaño del grafo
     * @param visitados lista de nodos visitados
     * @param numerodeHilos total de hilos en ejecución sobre el programa
     */
    public Graph(int size, boolean[] visitados, int numerodeHilos){

        this.size = size;                                            // Obtenemos la referencia del tamaño del grafo
        localQueues = new ArrayList<Queue<Integer>>(numerodeHilos);  // Asignación cola privada por hilo

        for (int i = 0; i < numerodeHilos; i++) // instaciación de cada cola
        {
            localQueues.add(new PriorityQueue<Integer>());
        }

        vertices = new int[size][size];  // instanciamos matriz de Adyacencia	
        this.visitados = visitados;
        isDone = false;
        globalQueue = new PriorityQueue<Integer>();
        globalQueue.add(size - 1);  // Cola compartida para los hilos
        contador = 0;

        for (int i = 0; i < this.size; i++)  // Generando aleatoriamente valores de
        {                                    // de la matriz de adyacebncia 
            for (int j = 0; j < this.size; j++)
            {
                Random boolNumber = new Random();
                boolean edge = boolNumber.nextBoolean();
                if (i == j)
                {
                    vertices[i][j] = 1;
                }
                else
                {
                    vertices[i][j] = edge ? 1 : 0;
                }
            }
        }
    }

    /*
     * Vaciar o agregar todos los elementos de la cola privada de un hilo a la
     * cola Global
     *
     * @param tmp referencia de la cola privada
     */
    public synchronized void addQueue(Queue<Integer> tmp)
    {
        while (!tmp.isEmpty())
        {
            globalQueue.add(tmp.poll());
        }
    }

    /*
     * Consulta a partir de la Matriz de Adyacencia si un nodo es adyacente de
     * otro
     *
     * @param nodo elemento al que se le necesita determinar nodos adyacentes
     * @param neighbour nodo al que se pretende saber si es adyacente a nodo
     * @return true si un neighbour es adyacente a nodo, false en caso de que no
     * lo sea
     */
    public boolean isNeighbour(int node, int neighbour)
    {
        return vertices[node][neighbour] == 1 ? true : false;
    }

    public synchronized void bfs()
    {
        while (!isDone && globalQueue.isEmpty())
        {
            try
            {
                wait();
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
            }
        }

        int index = (int) (Thread.currentThread().getId());  // se obtiene el identificador del hilo 
        if (!globalQueue.isEmpty())                          // para aignar nodos a la cola privad
        {
            boolean popped = false;
            int node = globalQueue.poll();
            popped = true;
            while (visitados[node])
            {
                if (globalQueue.isEmpty())
                {
                    isDone = true;
                    popped = false;
                    break;
                }
                else
                {
                    node = globalQueue.poll();
                    popped = true;
                }
            }
            if (popped)
            {
                visitados[node] = true;
                contador++;
                boolean flag = false;
                for (int i = 0; i < size; i++)
                {
                    if (node == i)
                    {
                        continue;
                    }
                    if (isNeighbour(node, i) && !visitados[i] && !flag)
                    {
                        localQueues.get(index).add(i);  // Ingresa cada nodo Adyacente al nodos
                        flag = true;			// a la cola privada de un hilo
                    }
                    if (isNeighbour(node, i) && !visitados[i] && flag)  // En caso de que el nodo Adyacente
                    {                                                   // se haya ingresado a la cola privada
                        globalQueue.add(i);                             // se imgresa el nodo a la cola local
                    }
                }
            }
        }
        if (globalQueue.isEmpty())
        {
            isDone = true;
        }

        if (isDone && contador < size)
        {
            isDone = false;
            for (int i = 0; i < size; i++) // Se ingresan todos los nodos que no se 
            {                              // han visitado a la cola global
                if (!visitados[i])
                {
                    globalQueue.add(i);
                }
            }
        }
        notifyAll();
    }
}
