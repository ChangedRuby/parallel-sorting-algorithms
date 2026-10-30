import java.io.PrintWriter;
import java.util.Random;

public class Main {

    static final String[] ALGORITMOS = {"BubbleSort", "InsertionSort", "MergeSort", "QuickSort"};
    static final int[] THREADS = {2, 4, 8, 16};   // só o paralelo; o serial sempre roda
    static final int[] TAMANHOS = {1000, 5000, 10000, 50000, 100000};

    // cores
    static final String RESET = "\u001B[0m";
    static final String NEGRITO = "\u001B[1m";
    static final String VERMELHO = "\u001B[31m";
    static final String VERDE = "\u001B[32m";
    static final String AMARELO = "\u001B[33m";
    static final String AZUL = "\u001B[34m";
    static final String CIANO = "\u001B[36m";

    static int[][] vetores;
    static double[][] temposSerial;       // temposSerial[algoritmo][vetor]
    static double[][][] temposParalelo;   // temposParalelo[algoritmo][vetor][threads]
    static PrintWriter csv;

    public static void main(String[] args) throws Exception {
        // cria os 5 vetores usados por todos os algoritmos
        vetores = new int[TAMANHOS.length][];
        for (int i = 0; i < TAMANHOS.length; i++) {
            vetores[i] = gerar(TAMANHOS[i]);
        }

        csv = new PrintWriter("resultados.csv");
        csv.println("algoritmo,modo,threads,tamanho,tempo_ms");

        temposSerial = new double[ALGORITMOS.length][TAMANHOS.length];
        temposParalelo = new double[ALGORITMOS.length][TAMANHOS.length][THREADS.length];
        for (int a = 0; a < ALGORITMOS.length; a++) {
            testarAlgoritmo(a);
        }

        csv.close();
        System.out.println(VERDE + "Resultados gravados em resultados.csv" + RESET);

        imprimirTabelas();
    }

    // Testa um algoritmo com todos os vetores: primeiro o serial, depois o paralelo com cada número de threads
    static void testarAlgoritmo(int a) throws Exception {
        for (int v = 0; v < vetores.length; v++) {
            temposSerial[a][v] = medir(ALGORITMOS[a], vetores[v], "serial", 1);
            for (int t = 0; t < THREADS.length; t++) {
                temposParalelo[a][v][t] = medir(ALGORITMOS[a], vetores[v], "paralelo", THREADS[t]);
            }
        }
    }

    // Mede o tempo da execução, grava no CSV e devolve o tempo
    static double medir(String algoritmo, int[] dados, String modo, int threads) throws Exception {
        int[] copia = dados.clone();

        // mede o tempo
        long inicio = System.nanoTime();
        if (modo.equals("serial")) ordenarSerial(algoritmo, copia);
        else ordenarParalelo(algoritmo, copia, threads);
        long fim = System.nanoTime();
        double ms = (fim - inicio) / 1000000.0;

        // confere o resultado
        if (!estaOrdenado(copia)) {
            throw new RuntimeException(algoritmo + " " + modo + " não ordenou corretamente");
        }

        // grava no CSV
        String linha = algoritmo + "," + modo + "," + threads + "," + dados.length + "," + ms;
        csv.println(linha);
        csv.flush();
        if (modo.equals("serial")) System.out.println(AZUL + linha + RESET);
        else System.out.println(AMARELO + linha + RESET);
        return ms;
    }

    // Imprime uma tabela de tempos (ms) para cada vetor
    static void imprimirTabelas() {
        for (int v = 0; v < vetores.length; v++) {
            System.out.println();
            System.out.println(CIANO + NEGRITO + "Vetor de " + TAMANHOS[v] + " números (tempo em ms)" + RESET);

            // cabeçalho
            System.out.print(NEGRITO);
            System.out.printf("%-15s", "Algoritmo");
            System.out.printf("%12s", "Serial");
            for (int t = 0; t < THREADS.length; t++) {
                System.out.printf("%12s", THREADS[t] + " threads");
            }
            System.out.println(RESET);

            // uma linha por algoritmo
            for (int a = 0; a < ALGORITMOS.length; a++) {
                System.out.printf("%-15s", ALGORITMOS[a]);

                double serial = temposSerial[a][v];
                System.out.print(AZUL);
                System.out.printf("%12.2f", serial);
                System.out.print(RESET);

                for (int t = 0; t < THREADS.length; t++) {
                    // verde: mais rápido que o serial, vermelho: mais lento
                    if (temposParalelo[a][v][t] < serial) System.out.print(VERDE);
                    else System.out.print(VERMELHO);

                    System.out.printf("%12.2f", temposParalelo[a][v][t]);
                    System.out.print(RESET);
                }
                System.out.println();
            }
        }
    }

    // Chama a versão serial do algoritmo
    static void ordenarSerial(String algoritmo, int[] a) {
        if (algoritmo.equals("BubbleSort")) Serial.bubbleSort(a);
        if (algoritmo.equals("InsertionSort")) Serial.insertionSort(a, 0, a.length);
        if (algoritmo.equals("MergeSort")) Serial.mergeSort(a, 0, a.length);
        if (algoritmo.equals("QuickSort")) Serial.quickSort(a, 0, a.length - 1);
    }

    // Chama a versão paralela do algoritmo
    static void ordenarParalelo(String algoritmo, int[] a, int threads) throws Exception {
        if (algoritmo.equals("BubbleSort")) Paralelo.bubbleSort(a, threads);
        if (algoritmo.equals("InsertionSort")) Paralelo.insertionSort(a, threads);
        if (algoritmo.equals("MergeSort")) Paralelo.mergeSort(a, threads);
        if (algoritmo.equals("QuickSort")) Paralelo.quickSort(a, threads);
    }

    // Gera um vetor com n números aleatórios (semente fixa: sempre os mesmos números)
    static int[] gerar(int n) {
        Random random = new Random(42);
        int[] a = new int[n];
        for (int i = 0; i < n; i++) {
            a[i] = random.nextInt(n);
        }
        return a;
    }

    // Verifica se o vetor está ordenado
    static boolean estaOrdenado(int[] a) {
        for (int i = 0; i < a.length - 1; i++) {
            if (a[i] > a[i + 1]) return false;
        }
        return true;
    }
}
