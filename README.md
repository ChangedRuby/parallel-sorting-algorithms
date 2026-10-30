UNIVERSIDADE DE FORTALEZA<br>
CENTRO DE CIÊNCIAS TECNOLÓGICAS<br>
CURSO: CIÊNCIA DA COMPUTAÇÃO

# Pesquisa de algoritmos de ordenação seriais e paralelos

Autor 1: Paulo Shizuo Vasconcelos Tatibana

Autor 2: Gustavo Chaves Macedo

Palavras-chave: Algoritmos de ordenação. Programação paralela. Java. Speedup. Análise de desempenho.

## Resumo

O paralelismo só compensou com vetores grandes e dados desordenados. Este trabalho compara Bubble Sort, Insertion Sort, Merge Sort e Quick Sort em versões serial e paralela, implementadas em Java, em 2.400 medições com vetores de 1.000 a 100.000 elementos, quatro tipos de dados e até 16 threads. Com 100.000 números aleatórios, o Merge Sort paralelo foi 2,40 vezes mais rápido que o serial, e o Quick Sort, 2,85 vezes. Com vetores já ordenados, a versão paralela perdeu para a serial em três dos quatro algoritmos. O Insertion Sort chegou a 62,70 vezes, mas a maior parte disso vem da divisão do vetor em pedaços: as threads contribuíram com cerca de 3,9 vezes. No Merge Sort e no Quick Sort, o ganho parou de crescer a partir de 8 threads. O algoritmo pesou mais que o número de threads: o Quick Sort serial foi mais de 1.000 vezes mais rápido que o Bubble Sort com 16 threads.

## Introdução

Ordenação é uma operação comum em computação, e os processadores atuais têm vários núcleos, então executar em paralelo parece um caminho direto para reduzir o tempo. Porém, criar e coordenar threads tem custo, e a parte do trabalho que continua serial limita a redução possível (AMDAHL, 1967). Este trabalho mede esse efeito em Java, comparando versões serial e paralela de algoritmos de ordenação.

Dos seis algoritmos propostos, foram escolhidos quatro: Bubble Sort e Insertion Sort, de custo quadrático, e Merge Sort e Quick Sort, de custo médio O(n log n) (CORMEN et al., 2012; HOARE, 1962). Assim é possível comparar dois grupos de complexidade diferente e duas estratégias de paralelização.

O Merge Sort e o Quick Sort dividem o problema de forma recursiva e foram paralelizados com o framework ForkJoin, que distribui as subtarefas entre as threads (LEA, 2000; ORACLE, 2026). O Bubble Sort foi paralelizado pelo método Odd-Even Transposition (HABERMANN, 1972), que alterna fases de comparação em posições pares e ímpares. Como os pares comparados em uma fase não se sobrepõem, as threads atuam ao mesmo tempo sem conflito. O Insertion Sort usa um pool fixo de threads (GOETZ et al., 2006): cada thread ordena um pedaço do vetor, e os pedaços são unidos em seguida com a operação de junção do Merge Sort.

A abordagem foi experimental: um único programa executa todas as combinações de algoritmo, tamanho, tipo de dado e número de threads e registra os tempos em CSV, usados depois nos gráficos.

## Metodologia

Pesquisa Descritiva.

Os algoritmos foram implementados em Java, nas classes Serial e Paralelo. A classe Main é o framework de teste: gera os vetores, executa todas as combinações, confere se cada vetor ficou ordenado e grava os tempos em resultados.csv. A classe Graficos lê esse arquivo e mostra gráficos interativos em Java Swing, que se atualizam enquanto o teste roda.

Os testes combinaram cinco tamanhos de vetor (1.000, 5.000, 10.000, 50.000 e 100.000), quatro tipos de dados (aleatório, ordenado, invertido e com muitos valores repetidos, de 0 a 9) e seis modos de execução (serial e paralelo com 2, 4, 8, 12 e 16 threads). Cada combinação foi repetida cinco vezes, em um total de 2.400 medições. Os dados aleatórios usam semente fixa, então todos os algoritmos ordenam os mesmos vetores.

Tudo rodou em um único computador, com processador Intel Core i5-1235U (10 núcleos, sendo 2 de desempenho e 8 de eficiência, e 12 threads lógicas), 15,7 GB de memória, Windows 11 e OpenJDK 21. O número de núcleos foi aproximado pelo número de threads: o sistema operacional não restringiu os núcleos disponíveis, e a configuração com 16 threads excede as 12 threads lógicas do processador.

O tempo foi medido com System.nanoTime e cobre a ordenação de uma cópia do vetor e, nas versões paralelas, também a criação do pool de threads. Antes das medições, cada algoritmo rodou 20 vezes com vetores de 5.000 elementos, e esses resultados foram descartados, porque a JVM otimiza o código durante a execução e distorceria as primeiras medições (GEORGES; BUYTAERT; EECKHOUT, 2007).

Na análise estatística, a mediana foi usada como medida principal, por ser menos sensível a amostras discrepantes. Também foram calculados a média, o desvio padrão amostral, o coeficiente de variação (CV) e o intervalo de confiança de 95% pela distribuição t de Student com 4 graus de liberdade (t = 2,776). O ganho foi medido pelo speedup (S = tempo serial / tempo paralelo) e pela eficiência (E = S / número de threads). Duas configurações foram consideradas diferentes quando seus intervalos de confiança não se sobrepõem.

## Resultados e Discussão

Os quatro algoritmos rodaram com vetores de 1.000 a 100.000 elementos e quatro tipos de dados, primeiro na versão serial e depois na paralela com 2, 4, 8, 12 e 16 threads, com cinco amostras por configuração. A Tabela 1 resume os testes. As Tabelas 2 a 6 trazem os tempos com dados aleatórios, a Tabela 7 traz as estatísticas, a Tabela 8 mostra o ganho por tipo de dado e a Tabela 9 indica a partir de que tamanho o paralelo foi mais rápido que o serial. As Figuras 1 a 10 vêm do programa Graficos, em Java Swing, a partir do arquivo resultados.csv.

Nas Tabelas 2 a 6, os tempos estão em milissegundos (ms) e são a mediana de cinco amostras com dados aleatórios. Cores: azul = serial; verde = paralelo mais rápido que o serial; vermelho = paralelo mais lento que o serial.

**Tabela 1: Configuração dos testes**

| Algoritmo | Paralelização | Vetores | Threads |
| --- | --- | --- | --- |
| BubbleSort | ThreadPool (Odd-Even) | 1.000, 5.000, 10.000, 50.000, 100.000 | Serial, 2, 4, 8, 12, 16 |
| InsertionSort | ThreadPool (pedaços + merge) | 1.000, 5.000, 10.000, 50.000, 100.000 | Serial, 2, 4, 8, 12, 16 |
| MergeSort | ForkJoin | 1.000, 5.000, 10.000, 50.000, 100.000 | Serial, 2, 4, 8, 12, 16 |
| QuickSort | ForkJoin | 1.000, 5.000, 10.000, 50.000, 100.000 | Serial, 2, 4, 8, 12, 16 |

**Tabela 2: Tempos com o vetor de 1.000 números (ms)**

| Algoritmo | Serial | 2 threads | 4 threads | 8 threads | 12 threads | 16 threads |
| --- | --- | --- | --- | --- | --- | --- |
| BubbleSort | 🟦 0,98 | 🟥 25,72 | 🟥 26,09 | 🟥 25,09 | 🟥 27,31 | 🟥 28,82 |
| InsertionSort | 🟦 0,19 | 🟥 0,34 | 🟥 0,43 | 🟥 0,80 | 🟥 1,09 | 🟥 1,62 |
| MergeSort | 🟦 0,06 | 🟥 0,48 | 🟥 0,54 | 🟥 0,89 | 🟥 1,55 | 🟥 1,83 |
| QuickSort | 🟦 0,07 | 🟥 0,39 | 🟥 0,48 | 🟥 0,86 | 🟥 1,22 | 🟥 1,55 |

**Tabela 3: Tempos com o vetor de 5.000 números (ms)**

| Algoritmo | Serial | 2 threads | 4 threads | 8 threads | 12 threads | 16 threads |
| --- | --- | --- | --- | --- | --- | --- |
| BubbleSort | 🟦 25,42 | 🟥 163,18 | 🟥 158,47 | 🟥 156,93 | 🟥 159,77 | 🟥 171,15 |
| InsertionSort | 🟦 4,52 | 🟩 1,50 | 🟩 1,20 | 🟩 0,93 | 🟩 1,19 | 🟩 1,84 |
| MergeSort | 🟦 0,56 | 🟥 0,87 | 🟥 0,76 | 🟥 0,94 | 🟥 1,35 | 🟥 2,13 |
| QuickSort | 🟦 0,36 | 🟥 0,59 | 🟥 0,62 | 🟥 1,01 | 🟥 1,20 | 🟥 1,55 |

**Tabela 4: Tempos com o vetor de 10.000 números (ms)**

| Algoritmo | Serial | 2 threads | 4 threads | 8 threads | 12 threads | 16 threads |
| --- | --- | --- | --- | --- | --- | --- |
| BubbleSort | 🟦 130,04 | 🟥 370,57 | 🟥 350,06 | 🟥 266,13 | 🟥 298,70 | 🟥 330,21 |
| InsertionSort | 🟦 17,35 | 🟩 5,12 | 🟩 2,45 | 🟩 1,74 | 🟩 1,76 | 🟩 1,83 |
| MergeSort | 🟦 0,92 | 🟥 1,26 | 🟥 1,21 | 🟥 1,11 | 🟥 1,66 | 🟥 1,91 |
| QuickSort | 🟦 0,79 | 🟥 1,25 | 🟥 1,00 | 🟥 1,20 | 🟥 1,34 | 🟥 1,79 |

**Tabela 5: Tempos com o vetor de 50.000 números (ms)**

| Algoritmo | Serial | 2 threads | 4 threads | 8 threads | 12 threads | 16 threads |
| --- | --- | --- | --- | --- | --- | --- |
| BubbleSort | 🟦 4.055,45 | 🟩 3.163,03 | 🟩 3.466,87 | 🟩 3.673,51 | 🟩 3.672,97 | 🟩 3.260,56 |
| InsertionSort | 🟦 439,21 | 🟩 118,06 | 🟩 44,45 | 🟩 16,00 | 🟩 9,00 | 🟩 8,16 |
| MergeSort | 🟦 5,53 | 🟩 3,88 | 🟩 3,28 | 🟩 3,02 | 🟩 2,85 | 🟩 2,99 |
| QuickSort | 🟦 4,54 | 🟩 3,32 | 🟩 2,74 | 🟩 2,25 | 🟩 2,50 | 🟩 2,55 |

**Tabela 6: Tempos com o vetor de 100.000 números (ms)**

| Algoritmo | Serial | 2 threads | 4 threads | 8 threads | 12 threads | 16 threads |
| --- | --- | --- | --- | --- | --- | --- |
| BubbleSort | 🟦 16.497,61 | 🟩 10.156,69 | 🟩 10.361,42 | 🟩 13.009,70 | 🟩 10.075,03 | 🟩 9.531,84 |
| InsertionSort | 🟦 1.750,58 | 🟩 438,91 | 🟩 164,30 | 🟩 56,11 | 🟩 29,26 | 🟩 27,92 |
| MergeSort | 🟦 11,49 | 🟩 7,80 | 🟩 5,99 | 🟩 4,99 | 🟩 4,80 | 🟩 4,89 |
| QuickSort | 🟦 9,24 | 🟩 6,19 | 🟩 4,29 | 🟩 3,29 | 🟩 3,43 | 🟩 3,25 |

**Tabela 7:** **Estatísticas com 100.000 números aleatórios (ms)**

| Algoritmo | Modo | Média | Desvio padrão | CV (%) | IC 95% |
| --- | --- | --- | --- | --- | --- |
| BubbleSort | Serial | 16.438,03 | 156,53 | 1,0 | ± 194,32 |
| BubbleSort | 16 threads | 9.540,34 | 74,78 | 0,8 | ± 92,84 |
| InsertionSort | Serial | 1.832,18 | 121,02 | 6,6 | ± 150,24 |
| InsertionSort | 16 threads | 27,71 | 0,60 | 2,2 | ± 0,75 |
| MergeSort | Serial | 11,74 | 0,62 | 5,3 | ± 0,77 |
| MergeSort | 16 threads | 4,97 | 0,39 | 7,8 | ± 0,48 |
| QuickSort | Serial | 9,37 | 0,31 | 3,3 | ± 0,38 |
| QuickSort | 16 threads | 3,30 | 0,13 | 3,8 | ± 0,16 |

**Tabela 8: Melhor speedup com 100.000 números (número de threads entre parênteses)**

| Algoritmo | Aleatório | Ordenado | Invertido | Repetidos |
| --- | --- | --- | --- | --- |
| BubbleSort | 🟩 1,73 (16) | 🟥 0,07 (2) | 🟥 0,89 (12) | 🟩 1,93 (16) |
| InsertionSort | 🟩 62,70 (16) | 🟥 0,14 (2) | 🟩 108,17 (16) | 🟩 62,24 (16) |
| MergeSort | 🟩 2,40 (12) | 🟩 1,35 (8) | 🟩 1,85 (12) | 🟩 2,09 (8) |
| QuickSort | 🟩 2,85 (16) | 🟥 0,77 (4) | 🟩 1,09 (12) | 🟩 1,30 (8) |

**Tabela 9: Menor vetor a partir do qual o paralelo compensou**

| Algoritmo | Aleatório | Ordenado | Invertido | Repetidos |
| --- | --- | --- | --- | --- |
| BubbleSort | 50.000 | Não compensou | Não compensou | 50.000 |
| InsertionSort | 5.000 | Não compensou | 1.000 | 5.000 |
| MergeSort | 50.000 | 100.000 | 50.000 | 50.000 |
| QuickSort | 50.000 | Não compensou | 100.000 | 50.000 |

Nos vetores de até 10.000 números aleatórios, as versões paralelas do Bubble Sort, do Merge Sort e do Quick Sort foram mais lentas que as seriais (Tabelas 2 a 4). Nesses casos, o custo de criar o pool de threads e de coordenar as tarefas foi maior que o tempo da própria ordenação. Com 1.000 números, por exemplo, o Merge Sort serial levou 0,06 ms, enquanto a melhor versão paralela levou 0,48 ms. A partir de 50.000 números, as quatro versões paralelas foram mais rápidas que as seriais com dados aleatórios (Tabelas 5 e 6 e Figuras 1 a 4).

O tempo serial cresceu como a teoria prevê. De 10.000 para 100.000 números aleatórios, o expoente de crescimento medido (inclinação em escala log-log) foi de 2,10 no Bubble Sort e 2,00 no Insertion Sort, compatível com O(n²), e de 1,10 no Merge Sort e 1,07 no Quick Sort, compatível com O(n log n). Com 100.000 números, o Bubble Sort serial levou 16.497,61 ms e o Quick Sort serial, 9,24 ms, mais de 1.700 vezes menos. Nem com 16 threads o Bubble Sort (9.531,84 ms) chegou perto: o Quick Sort serial ainda foi mais de 1.000 vezes mais rápido.

**Figura 1: Tempo x tamanho do Bubble Sort com dados aleatórios**

![Figura 1: Tempo x tamanho do Bubble Sort com dados aleatórios](graficos/figura01-tempo-bubblesort-aleatorio.png)

**Figura 2: Tempo x tamanho do Insertion Sort com dados aleatórios**

![Figura 2: Tempo x tamanho do Insertion Sort com dados aleatórios](graficos/figura02-tempo-insertionsort-aleatorio.png)

**Figura 3: Tempo x tamanho do Merge Sort com dados aleatórios**

![Figura 3: Tempo x tamanho do Merge Sort com dados aleatórios](graficos/figura03-tempo-mergesort-aleatorio.png)

**Figura 4: Tempo x tamanho do Quick Sort com dados aleatórios**

![Figura 4: Tempo x tamanho do Quick Sort com dados aleatórios](graficos/figura04-tempo-quicksort-aleatorio.png)

O Insertion Sort foi o algoritmo que mais se beneficiou do paralelismo. Com 100.000 números aleatórios, o tempo caiu de 1.750,58 ms no serial para 27,92 ms com 16 threads, cerca de 63 vezes menos, com eficiência de 3,92. O ganho passa do número de threads porque o custo do algoritmo cresce com o quadrado do tamanho da entrada: ordenar pedaços e juntá-los dá muito menos trabalho que ordenar o vetor inteiro. Por isso, a divisão em pedaços explica mais do ganho do que as threads. Um teste complementar, feito fora do framework com a mesma lógica de divisão, separou os dois efeitos: com 16 pedaços em uma única thread, o tempo foi de 108,80 ms, contra 1.719,77 ms do serial puro, e com 16 threads caiu para 27,67 ms. A divisão reduziu o tempo em 15,8 vezes, e as threads, em mais 3,9 vezes.

O Bubble Sort paralelo, implementado com o método Odd-Even Transposition, só superou o serial a partir de 50.000 números e chegou a ser 1,73 vez mais rápido com 100.000 números aleatórios, com eficiência de 0,11. O ganho é limitado porque o algoritmo executa milhares de fases, e cada fase precisa esperar que todas as threads terminem antes de a próxima começar. Com 8 threads, as amostras variaram de 10.008,19 ms a 17.829,86 ms (coeficiente de variação de 22,9%), enquanto com 16 threads o coeficiente ficou em 0,8%. A causa não foi identificada, mas a variação não se repetiu: dez novas amostras com 8 threads, feitas fora do framework, ficaram entre 8.834,07 ms e 9.633,52 ms. Por isso, o valor de 8 threads na Tabela 6 deve ser lido com cautela.

O Merge Sort e o Quick Sort, implementados com ForkJoin, foram mais rápidos que as versões seriais a partir de 50.000 números aleatórios. Com 100.000 números, os ganhos foram de 2,40 vezes (12 threads) e 2,85 vezes (16 threads), com eficiência de 0,20 e 0,18, e os intervalos de confiança de 95% das versões serial e paralela não se sobrepõem. Dois fatores limitam o ganho. A última junção do Merge Sort e a primeira partição do Quick Sort percorrem o vetor inteiro em uma única thread, o limite descrito pela Lei de Amdahl (AMDAHL, 1967). Além disso, a implementação não define tamanho mínimo de tarefa: a divisão continua até subvetores de um elemento e gera muitas tarefas pequenas.

**Figura 5: Speedup do Bubble Sort com dados aleatórios**

![Figura 5: Speedup do Bubble Sort com dados aleatórios](graficos/figura05-speedup-bubblesort-aleatorio.png)

**Figura 6: Speedup do Insertion Sort com dados aleatórios**

![Figura 6: Speedup do Insertion Sort com dados aleatórios](graficos/figura06-speedup-insertionsort-aleatorio.png)

**Figura 7: Speedup do Merge Sort com dados aleatórios**

![Figura 7: Speedup do Merge Sort com dados aleatórios](graficos/figura07-speedup-mergesort-aleatorio.png)

**Figura 8: Speedup do Quick Sort com dados aleatórios**

![Figura 8: Speedup do Quick Sort com dados aleatórios](graficos/figura08-speedup-quicksort-aleatorio.png)

A partir de 8 threads, aumentar o número de threads quase não reduziu o tempo do Merge Sort e do Quick Sort. Com 100.000 números aleatórios, o Merge Sort passou de 4,99 ms com 8 threads para 4,89 ms com 16, e o Quick Sort, de 3,29 ms para 3,25 ms (Figuras 7 e 8). O processador tem 12 threads lógicas, então 16 threads excedem o que ele executa ao mesmo tempo. Seus núcleos também não são iguais entre si, o que pode reduzir o ganho por thread. Já o Insertion Sort continuou melhorando (de 56,11 ms para 27,92 ms), porque mais threads significam pedaços menores.

A natureza dos dados mudou o resultado (Tabelas 8 e 9). Com vetores já ordenados, a versão paralela foi mais lenta em três dos quatro algoritmos: o Bubble Sort e o Insertion Sort seriais terminam com uma única passada pelo vetor (0,02 ms e 0,08 ms), e a versão paralela ainda precisa criar o pool de threads. Com vetores invertidos, o Bubble Sort paralelo não superou o serial (7.344,81 ms contra 6.528,52 ms, Figura 9). No Quick Sort, os dados ordenados e invertidos reduziram o tempo serial para 1,25 ms e 2,73 ms, contra 9,24 ms nos aleatórios, porque o pivô central divide esses vetores ao meio. Sobra menos trabalho para dividir entre as threads, e o ganho desaparece (Figura 10). Nos vetores invertidos com 100.000 números, os intervalos de confiança do Merge Sort e do Quick Sort se sobrepõem, então não se pode afirmar que a versão paralela foi mais rápida nesses dois casos.

**Figura 9: Speedup do Bubble Sort com dados invertidos**

![Figura 9: Speedup do Bubble Sort com dados invertidos](graficos/figura09-speedup-bubblesort-invertido.png)

**Figura 10: Speedup do Quick Sort com dados ordenados**

![Figura 10: Speedup do Quick Sort com dados ordenados](graficos/figura10-speedup-quicksort-ordenado.png)

As medições longas foram estáveis, e as curtas tiveram mais ruído: o coeficiente de variação mediano foi de 3,9% nos tempos a partir de 100 ms e de 14,8% nos tempos abaixo de 1 ms (10,0% nas 480 configurações). Esse ruído justifica o uso da mediana. A primeira amostra de cada configuração não ficou mais lenta que as demais (razão mediana de 1,00), sinal de que o aquecimento da JVM funcionou. Em 15 de 384 comparações entre tamanhos consecutivos, o vetor maior foi mais de 10% mais rápido que o menor, sempre com tempos abaixo de 5 ms, faixa em que o custo das threads pesa mais que o tamanho do vetor.

## Conclusão

O paralelismo reduziu o tempo de ordenação apenas com vetores grandes e dados desordenados. As versões paralelas do Bubble Sort, do Merge Sort e do Quick Sort passaram a compensar a partir de 50.000 números aleatórios; abaixo disso, o custo de criar e coordenar threads superou o ganho. Com dados já ordenados, a versão serial foi a melhor escolha em três dos quatro algoritmos.

Trocar de algoritmo rendeu mais do que aumentar as threads. Com 100.000 números, o Quick Sort foi o mais rápido com dados aleatórios, invertidos e repetidos, tanto no modo serial quanto no paralelo, e o Bubble Sort ficou mais de 1.000 vezes atrás mesmo com 16 threads. O Merge Sort teve ganho parecido com o do Quick Sort nos dados aleatórios e foi o único dos quatro em que a versão paralela superou a serial com dados ordenados. O ganho do Insertion Sort paralelo vem principalmente da divisão em pedaços, e por isso essa versão deve ser lida como um algoritmo híbrido.

No equipamento usado, 8 a 12 threads bastaram para o Merge Sort e o Quick Sort. A eficiência ficou perto de 0,20: a parte serial do trabalho e o custo de coordenação limitam o ganho.

O trabalho tem limitações: todos os testes foram feitos em uma única máquina, o número de núcleos foi aproximado pelo número de threads, sem restringir os núcleos pelo sistema operacional, e cinco amostras dão intervalos de confiança largos para tempos muito curtos. Como continuação, sugere-se definir um tamanho mínimo de tarefa no Merge Sort e no Quick Sort, restringir os núcleos com afinidade de CPU, testar vetores maiores e repetir o experimento em outros equipamentos.

## Referências

AMDAHL, Gene M. Validity of the single processor approach to achieving large scale computing capabilities. In: AFIPS SPRING JOINT COMPUTER CONFERENCE, 1967, Atlantic City. **Proceedings [...]**. New York: ACM, 1967. p. 483-485.

CORMEN, Thomas H. et al. **Algoritmos**: teoria e prática. 3. ed. Rio de Janeiro: Elsevier, 2012.

GEORGES, Andy; BUYTAERT, Dries; EECKHOUT, Lieven. Statistically rigorous Java performance evaluation. **ACM SIGPLAN Notices**, New York, v. 42, n. 10, p. 57-76, 2007.

GOETZ, Brian et al. **Java concurrency in practice**. Upper Saddle River: Addison-Wesley, 2006.

HABERMANN, A. N. **Parallel neighbor-sort** (or the glory of the induction principle). Pittsburgh: Carnegie Mellon University, 1972. Relatório técnico.

HOARE, C. A. R. Quicksort. **The Computer Journal**, Oxford, v. 5, n. 1, p. 10-16, 1962.

LEA, Doug. A Java fork/join framework. In: ACM CONFERENCE ON JAVA GRANDE, 2000, San Francisco. **Proceedings [...]**. New York: ACM, 2000. p. 36-43.

ORACLE. **ForkJoinPool (Java SE 21 & JDK 21)**. Disponível em: https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/concurrent/ForkJoinPool.html. Acesso em: 30 out. 2026.

## Anexos

Códigos das implementações, resultados e gráficos: https://github.com/ChangedRuby/parallel-sorting-algorithms
