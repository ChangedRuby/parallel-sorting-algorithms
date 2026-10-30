public class Serial {

    public static void bubbleSort(int[] a) {
        int n = a.length;
        for (int i = 0; i < n - 1; i++) {
            boolean trocou = false;
            for (int j = 0; j < n - 1 - i; j++) {
                if (a[j] > a[j + 1]) {
                    troca(a, j, j + 1);
                    trocou = true;
                }
            }
            // já está ordenado
            if (!trocou) break;
        }
    }

    public static void insertionSort(int[] a, int inicio, int fim) {
        for (int i = inicio + 1; i < fim; i++) {
            int atual = a[i];
            int j = i - 1;
            // abre espaço para o atual
            while (j >= inicio && a[j] > atual) {
                a[j + 1] = a[j];
                j--;
            }
            a[j + 1] = atual;
        }
    }

    public static void mergeSort(int[] a, int inicio, int fim) {
        if (fim - inicio < 2) return;
        int meio = (inicio + fim) / 2;
        mergeSort(a, inicio, meio);
        mergeSort(a, meio, fim);
        merge(a, inicio, meio, fim);
    }

    // Junta as duas metades ordenadas
    public static void merge(int[] a, int inicio, int meio, int fim) {
        int[] aux = new int[fim - inicio];
        int i = inicio;
        int j = meio;
        int k = 0;

        // pega o menor dos dois lados
        while (i < meio && j < fim) {
            if (a[i] <= a[j]) {
                aux[k] = a[i];
                i++;
            } else {
                aux[k] = a[j];
                j++;
            }
            k++;
        }

        // copia o que sobrou
        while (i < meio) {
            aux[k] = a[i];
            i++;
            k++;
        }
        while (j < fim) {
            aux[k] = a[j];
            j++;
            k++;
        }

        // devolve para o vetor
        for (k = 0; k < aux.length; k++) {
            a[inicio + k] = aux[k];
        }
    }

    public static void quickSort(int[] a, int esq, int dir) {
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
                troca(a, i, j);
                i++;
                j--;
            }
        }

        // ordena os dois lados
        quickSort(a, esq, j);
        quickSort(a, i, dir);
    }

    // Troca dois elementos de lugar
    public static void troca(int[] a, int i, int j) {
        int tmp = a[i];
        a[i] = a[j];
        a[j] = tmp;
    }
}
