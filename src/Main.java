import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Random;

public class Main {

    static final String[] ALGORITMOS = {"BubbleSort", "InsertionSort", "MergeSort", "QuickSort"};
    static final int[] THREADS = {2, 4, 8, 12, 16};   // só o paralelo; o serial sempre roda
    static final int[] TAMANHOS = {1000, 5000, 10000, 50000, 100000};
    static final String[] TIPOS = {"aleatorio", "ordenado", "invertido", "repetidos"};
    static final int AMOSTRAS = 5;
    static final int TAMANHO_AQUECIMENTO = 5000;
    static final int RODADAS_AQUECIMENTO = 20;

    // cores
    static final String RESET = "\u001B[0m";
    static final String NEGRITO = "\u001B[1m";
    static final String VERMELHO = "\u001B[31m";
    static final String VERDE = "\u001B[32m";
    static final String AMARELO = "\u001B[33m";
    static final String AZUL = "\u001B[34m";
    static final String CIANO = "\u001B[36m";

    static int[][][] vetores;             // vetores[tipo][tamanho]
    static double[][][] temposSerial;     // temposSerial[tipo][algoritmo][vetor], mediana das amostras
    static double[][][][] temposParalelo; // temposParalelo[tipo][algoritmo][vetor][threads], mediana das amostras
    static PrintWriter csv;

    public static void main(String[] args) throws Exception {
        // cria os vetores usados por todos os algoritmos
        vetores = new int[TIPOS.length][TAMANHOS.length][];
        for (int k = 0; k < TIPOS.length; k++) {
            for (int i = 0; i < TAMANHOS.length; i++) {
                vetores[k][i] = gerar(TIPOS[k], TAMANHOS[i]);
            }
        }

        aquecer();

        csv = new PrintWriter("resultados.csv");
        csv.println("algoritmo,modo,threads,tipo,tamanho,amostra,tempo_ms");

        temposSerial = new double[TIPOS.length][ALGORITMOS.length][TAMANHOS.length];
        temposParalelo = new double[TIPOS.length][ALGORITMOS.length][TAMANHOS.length][THREADS.length];
        for (int k = 0; k < TIPOS.length; k++) {
            for (int a = 0; a < ALGORITMOS.length; a++) {
                testarAlgoritmo(k, a);
            }
        }

        csv.close();
        System.out.println(VERDE + "Resultados gravados em resultados.csv" + RESET);

        imprimirTabelas();
    }

    // Rodadas descartadas: sem elas, as primeiras medições cronometram o JIT, não o algoritmo
    static void aquecer() throws Exception {
        System.out.println("Aquecendo a JVM...");
        for (int rodada = 0; rodada < RODADAS_AQUECIMENTO; rodada++) {
            for (String tipo : TIPOS) {
                int[] dados = gerar(tipo, TAMANHO_AQUECIMENTO);
                for (String algoritmo : ALGORITMOS) {
                    ordenarSerial(algoritmo, dados.clone());
                    ordenarParalelo(algoritmo, dados.clone(), 4);
                }
            }
        }
    }

    // Testa um algoritmo com todos os vetores: primeiro o serial, depois o paralelo com cada número de threads
    static void testarAlgoritmo(int k, int a) throws Exception {
        for (int v = 0; v < TAMANHOS.length; v++) {
            temposSerial[k][a][v] = tempoMediano(k, a, v, "serial", 1);
            for (int t = 0; t < THREADS.length; t++) {
                temposParalelo[k][a][v][t] = tempoMediano(k, a, v, "paralelo", THREADS[t]);
            }
        }
    }

    // Roda todas as amostras de uma configuração e devolve a mediana
    static double tempoMediano(int k, int a, int v, String modo, int threads) throws Exception {
        double[] tempos = new double[AMOSTRAS];
        for (int s = 0; s < AMOSTRAS; s++) {
            tempos[s] = medir(ALGORITMOS[a], vetores[k][v], modo, threads, TIPOS[k], s + 1);
        }
        return mediana(tempos);
    }

    static double mediana(double[] valores) {
        double[] ordenados = valores.clone();
        Arrays.sort(ordenados);
        int n = ordenados.length;
        return (ordenados[(n - 1) / 2] + ordenados[n / 2]) / 2;
    }

    // Mede o tempo da execução, grava no CSV e devolve o tempo
    static double medir(String algoritmo, int[] dados, String modo, int threads, String tipo, int amostra) throws Exception {
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
        String linha = algoritmo + "," + modo + "," + threads + "," + tipo + "," + dados.length + "," + amostra + "," + ms;
        csv.println(linha);
        csv.flush();
        if (modo.equals("serial")) System.out.println(AZUL + linha + RESET);
        else System.out.println(AMARELO + linha + RESET);
        return ms;
    }

    // Imprime uma tabela de tempos (ms, mediana das amostras) para cada tipo de dado e vetor
    static void imprimirTabelas() {
        for (int k = 0; k < TIPOS.length; k++) {
            for (int v = 0; v < TAMANHOS.length; v++) {
                System.out.println();
                System.out.println(CIANO + NEGRITO + TIPOS[k] + ": vetor de " + TAMANHOS[v] + " números (tempo em ms)" + RESET);

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

                    double serial = temposSerial[k][a][v];
                    System.out.print(AZUL);
                    System.out.printf("%12.2f", serial);
                    System.out.print(RESET);

                    for (int t = 0; t < THREADS.length; t++) {
                        // verde: mais rápido que o serial, vermelho: mais lento
                        if (temposParalelo[k][a][v][t] < serial) System.out.print(VERDE);
                        else System.out.print(VERMELHO);

                        System.out.printf("%12.2f", temposParalelo[k][a][v][t]);
                        System.out.print(RESET);
                    }
                    System.out.println();
                }
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

    // Gera um vetor com n números do tipo pedido (semente fixa: sempre os mesmos números)
    static int[] gerar(String tipo, int n) {
        Random random = new Random(42);
        int[] a = new int[n];
        for (int i = 0; i < n; i++) {
            if (tipo.equals("aleatorio")) a[i] = random.nextInt(n);
            if (tipo.equals("ordenado")) a[i] = i;
            if (tipo.equals("invertido")) a[i] = n - i;
            if (tipo.equals("repetidos")) a[i] = random.nextInt(10);
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
