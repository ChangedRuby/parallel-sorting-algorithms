import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class Paralelo {

    // ForkJoin
    public static void mergeSort(int[] a, int threads) {
        ForkJoinPool pool = new ForkJoinPool(threads);
        pool.invoke(new MergeTask(a, 0, a.length));
        pool.shutdown();
    }

    // ForkJoin
    public static void quickSort(int[] a, int threads) {
        ForkJoinPool pool = new ForkJoinPool(threads);
        pool.invoke(new QuickTask(a, 0, a.length - 1));
        pool.shutdown();
    }

    // Tarefa do merge
    static class MergeTask extends RecursiveAction {
        int[] a;
        int inicio, fim;

        MergeTask(int[] a, int inicio, int fim) {
            this.a = a;
            this.inicio = inicio;
            this.fim = fim;
        }

        @Override
        protected void compute() {
            if (fim - inicio < 2) return;

            // ordena as duas metades em paralelo
            int meio = (inicio + fim) / 2;
            invokeAll(new MergeTask(a, inicio, meio), new MergeTask(a, meio, fim));

            // junta as metades
            Serial.merge(a, inicio, meio, fim);
        }
    }

    // Tarefa do quick
    static class QuickTask extends RecursiveAction {
        int[] a;
        int esq, dir;

        QuickTask(int[] a, int esq, int dir) {
            this.a = a;
            this.esq = esq;
            this.dir = dir;
        }

        @Override
        protected void compute() {
            if (esq >= dir) return;

            // encontra o pivô
            int pivo = a[(esq + dir) / 2];

            // menores para a esquerda, maiores para a direita
            int i = esq;
            int j = dir;
            while (i <= j) {
                while (a[i] < pivo) i++;
                while (a[j] > pivo) j--;
                if (i <= j) {
                    Serial.troca(a, i, j);
                    i++;
                    j--;
                }
            }

            // ordena os dois lados em paralelo
            invokeAll(new QuickTask(a, esq, j), new QuickTask(a, i, dir));
        }
    }

    // Bubble Sort (Odd-Even) - ThreadPool
    public static void bubbleSort(int[] a, int threads) throws Exception {
        ExecutorService pool = Executors.newFixedThreadPool(threads);
        int tamanho = a.length / threads;
        int fase = 0;
        int fasesSemTroca = 0;

        while (fasesSemTroca < 2) {
            // divide o vetor entre as threads
            List<FaseTask> tarefas = new ArrayList<>();
            for (int t = 0; t < threads; t++) {
                int inicio = t * tamanho;
                int fim = inicio + tamanho;
                if (t == threads - 1) fim = a.length;
                tarefas.add(new FaseTask(a, inicio, fim, fase % 2));
            }

            // executa a fase e espera todas as threads
            List<Future<Boolean>> resultados = pool.invokeAll(tarefas);

            // verifica se alguma thread trocou
            boolean trocou = false;
            for (Future<Boolean> r : resultados) {
                if (r.get()) trocou = true;
            }

            if (trocou) fasesSemTroca = 0;
            else fasesSemTroca++;
            fase++;
        }
        pool.shutdown();
    }

    // Tarefa de uma fase do Bubble Sort - ThreadPool
    static class FaseTask implements Callable<Boolean> {
        int[] a;
        int inicio, fim, paridade;

        FaseTask(int[] a, int inicio, int fim, int paridade) {
            this.a = a;
            this.inicio = inicio;
            this.fim = fim;
            this.paridade = paridade;
        }

        @Override
        public Boolean call() {
            boolean trocou = false;
            for (int i = inicio; i < fim; i++) {
                // compara só os pares da fase (par ou ímpar)
                if (i % 2 == paridade && i + 1 < a.length && a[i] > a[i + 1]) {
                    Serial.troca(a, i, i + 1);
                    trocou = true;
                }
            }
            return trocou;
        }
    }

    // Insertion Sort - ThreadPool
    public static void insertionSort(int[] a, int threads) throws Exception {
        ExecutorService pool = Executors.newFixedThreadPool(threads);
        int tamanho = a.length / threads;

        // cada thread ordena um pedaço
        for (int t = 0; t < threads; t++) {
            int inicio = t * tamanho;
            int fim = inicio + tamanho;
            if (t == threads - 1) fim = a.length;
            pool.submit(new PedacoTask(a, inicio, fim));
        }

        // espera todas as threads
        pool.shutdown();
        pool.awaitTermination(1, TimeUnit.HOURS);

        // junta os pedaços
        for (int t = 1; t < threads; t++) {
            int inicio = t * tamanho;
            int fim = inicio + tamanho;
            if (t == threads - 1) fim = a.length;
            Serial.merge(a, 0, inicio, fim);
        }
    }

    // Tarefa de um pedaço do Insertion Sort - ThreadPool
    static class PedacoTask implements Runnable {
        int[] a;
        int inicio, fim;

        PedacoTask(int[] a, int inicio, int fim) {
            this.a = a;
            this.inicio = inicio;
            this.fim = fim;
        }

        @Override
        public void run() {
            Serial.insertionSort(a, inicio, fim);
        }
    }
}
