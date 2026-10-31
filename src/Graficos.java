import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import javax.swing.*;

// Gráfico do resultados.csv; relê o arquivo a cada 2 segundos, então dá para acompanhar o Main rodando
public class Graficos extends JPanel implements ActionListener {

    static final int ESQUERDA = 80, DIREITA = 150, TOPO = 50, BASE = 60;
    static final Color[] CORES = {
        new Color(0x2a78d6), new Color(0xeb6834), new Color(0x1baf7a),
        new Color(0xeda100), new Color(0xe87ba4), new Color(0x777777)
    };

    // tempos e quantidade de amostras: [tipo][algoritmo][tamanho][modo], modo 0 = serial, 1 em diante = THREADS
    double[][][][][] tempos;
    int[][][][] quantidade;

    JComboBox<String> visao = new JComboBox<>(new String[]{"Tempo x tamanho", "Speedup x threads"});
    JComboBox<String> algoritmo = new JComboBox<>(Main.ALGORITMOS);
    JComboBox<String> tipo = new JComboBox<>(Main.TIPOS);
    JLabel status = new JLabel();

    Graficos() {
        setBackground(Color.WHITE);
        setPreferredSize(new Dimension(960, 600));
        visao.addActionListener(this);
        algoritmo.addActionListener(this);
        tipo.addActionListener(this);
        carregar();
    }

    // Chamado pelo Timer e quando muda a seleção
    public void actionPerformed(ActionEvent e) {
        carregar();
        repaint();
    }

    // Lê o CSV e guarda os tempos de cada configuração
    void carregar() {
        tempos = new double[Main.TIPOS.length][Main.ALGORITMOS.length][Main.TAMANHOS.length][Main.THREADS.length + 1][Main.AMOSTRAS];
        quantidade = new int[Main.TIPOS.length][Main.ALGORITMOS.length][Main.TAMANHOS.length][Main.THREADS.length + 1];
        int linhas = 0;

        try {
            List<String> arquivo = Files.readAllLines(new File("resultados.csv").toPath());
            for (String linha : arquivo) {
                // algoritmo,modo,threads,tipo,tamanho,amostra,tempo_ms
                String[] c = linha.split(",");
                if (c.length != 7 || c[0].equals("algoritmo")) continue;

                int a = posicao(Main.ALGORITMOS, c[0]);
                int k = posicao(Main.TIPOS, c[3]);
                int v = posicao(Main.TAMANHOS, Integer.parseInt(c[4]));
                int m = 0;
                if (c[1].equals("paralelo")) m = posicao(Main.THREADS, Integer.parseInt(c[2])) + 1;
                if (a < 0 || k < 0 || v < 0 || m < 0) continue;
                if (quantidade[k][a][v][m] == Main.AMOSTRAS) continue;

                tempos[k][a][v][m][quantidade[k][a][v][m]] = Double.parseDouble(c[6]);
                quantidade[k][a][v][m]++;
                linhas++;
            }
            status.setText(linhas + " medições em resultados.csv");
        } catch (Exception e) {
            status.setText("Não foi possível ler resultados.csv");
        }
    }

    // Mediana das amostras do tipo e algoritmo selecionados, ou 0 se ainda não há dados
    double mediana(int v, int m) {
        int k = tipo.getSelectedIndex();
        int a = algoritmo.getSelectedIndex();
        int n = quantidade[k][a][v][m];
        if (n == 0) return 0;

        // ordena os tempos lidos e pega o do meio
        double[] copia = Arrays.copyOf(tempos[k][a][v][m], n);
        Arrays.sort(copia);
        if (n % 2 == 0) return (copia[n / 2 - 1] + copia[n / 2]) / 2;
        return copia[n / 2];
    }

    static int posicao(String[] lista, String valor) {
        for (int i = 0; i < lista.length; i++) {
            if (lista[i].equals(valor)) return i;
        }
        return -1;
    }

    static int posicao(int[] lista, int valor) {
        for (int i = 0; i < lista.length; i++) {
            if (lista[i] == valor) return i;
        }
        return -1;
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        Graphics2D g = (Graphics2D) graphics;
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (visao.getSelectedIndex() == 0) desenharTempo(g);
        else desenharSpeedup(g);
    }

    // Uma linha por modo (serial e cada número de threads), tempo em cada tamanho de vetor
    void desenharTempo(Graphics2D g) {
        int modos = Main.THREADS.length + 1;

        String[] nomes = new String[modos];
        double[][] valores = new double[modos][Main.TAMANHOS.length];
        for (int m = 0; m < modos; m++) {
            if (m == 0) nomes[m] = "Serial";
            else nomes[m] = Main.THREADS[m - 1] + " threads";
            for (int v = 0; v < Main.TAMANHOS.length; v++) {
                valores[m][v] = mediana(v, m);
            }
        }

        String[] rotulosX = new String[Main.TAMANHOS.length];
        for (int v = 0; v < Main.TAMANHOS.length; v++) rotulosX[v] = String.valueOf(Main.TAMANHOS[v]);

        desenhar(g, "Tamanho do vetor", "Tempo em ms, escala logarítmica", rotulosX, nomes, valores, true);
    }

    // Uma linha por tamanho de vetor, speedup (tempo serial / tempo paralelo) em cada número de threads
    void desenharSpeedup(Graphics2D g) {
        int series = Main.TAMANHOS.length + 1;

        String[] nomes = new String[series];
        double[][] valores = new double[series][Main.THREADS.length];
        for (int v = 0; v < Main.TAMANHOS.length; v++) {
            nomes[v] = Main.TAMANHOS[v] + " números";
            for (int t = 0; t < Main.THREADS.length; t++) {
                double serial = mediana(v, 0);
                double paralelo = mediana(v, t + 1);
                if (serial > 0 && paralelo > 0) valores[v][t] = serial / paralelo;
            }
        }

        // linha ideal: speedup igual ao número de threads
        nomes[series - 1] = "Ideal";
        for (int t = 0; t < Main.THREADS.length; t++) valores[series - 1][t] = Main.THREADS[t];

        String[] rotulosX = new String[Main.THREADS.length];
        for (int t = 0; t < Main.THREADS.length; t++) rotulosX[t] = String.valueOf(Main.THREADS[t]);

        desenhar(g, "Threads", "Speedup (tempo serial / tempo paralelo)", rotulosX, nomes, valores, false);
    }

    // Desenha eixos, grade, linhas e legenda; valores 0 são pontos sem dados e não aparecem
    void desenhar(Graphics2D g, String tituloX, String tituloY, String[] rotulosX, String[] nomes, double[][] valores, boolean log) {
        int largura = getWidth() - ESQUERDA - DIREITA;
        int altura = getHeight() - TOPO - BASE;
        int fundo = TOPO + altura;

        g.setColor(Color.BLACK);
        g.setFont(getFont().deriveFont(Font.BOLD, 15f));
        g.drawString(algoritmo.getSelectedItem() + ", vetor " + tipo.getSelectedItem(), ESQUERDA, 28);
        g.setFont(getFont());

        // menor e maior valor (em log10 no gráfico de tempo)
        double min = Double.MAX_VALUE;
        double max = -Double.MAX_VALUE;
        boolean temDados = false;
        for (int s = 0; s < valores.length; s++) {
            for (int i = 0; i < valores[s].length; i++) {
                if (valores[s][i] <= 0) continue;
                double y = valores[s][i];
                if (log) y = Math.log10(y);
                min = Math.min(min, y);
                max = Math.max(max, y);
                if (!nomes[s].equals("Ideal")) temDados = true;
            }
        }
        if (!temDados) {
            g.drawString("Sem dados ainda. Rode o Main.", ESQUERDA, TOPO + 30);
            return;
        }

        // limites do eixo Y
        double piso = 0;
        double passo = Math.max(1, Math.ceil(max / 5));
        double teto = Math.ceil(max / passo) * passo;
        if (log) {
            teto = Math.ceil(max);
            piso = Math.floor(min);
            if (teto == piso) teto = piso + 1;
            passo = 1;
        }

        // grade e rótulos do eixo Y
        for (double marca = piso; marca <= teto; marca += passo) {
            int y = fundo - (int) ((marca - piso) / (teto - piso) * altura);
            g.setColor(new Color(0xe4e3df));
            g.drawLine(ESQUERDA, y, ESQUERDA + largura, y);

            String rotulo = String.valueOf((int) marca);
            if (log) rotulo = String.valueOf(Math.pow(10, marca));
            g.setColor(Color.DARK_GRAY);
            g.drawString(rotulo, ESQUERDA - 8 - g.getFontMetrics().stringWidth(rotulo), y + 4);
        }

        // rótulos do eixo X, com os pontos igualmente espaçados
        int[] xs = new int[rotulosX.length];
        for (int i = 0; i < rotulosX.length; i++) {
            xs[i] = ESQUERDA + i * largura / (rotulosX.length - 1);
            g.drawString(rotulosX[i], xs[i] - g.getFontMetrics().stringWidth(rotulosX[i]) / 2, fundo + 20);
        }
        g.drawString(tituloX, ESQUERDA + largura / 2, fundo + 44);
        g.drawString(tituloY, ESQUERDA, TOPO - 8);

        // uma linha por série, com marcadores e legenda
        g.setStroke(new BasicStroke(2));
        for (int s = 0; s < valores.length; s++) {
            g.setColor(CORES[s]);

            int[] ys = new int[rotulosX.length];
            for (int i = 0; i < rotulosX.length; i++) {
                if (valores[s][i] <= 0) continue;
                double y = valores[s][i];
                if (log) y = Math.log10(y);
                ys[i] = fundo - (int) ((y - piso) / (teto - piso) * altura);

                g.fillOval(xs[i] - 4, ys[i] - 4, 8, 8);
                if (i > 0 && valores[s][i - 1] > 0) g.drawLine(xs[i - 1], ys[i - 1], xs[i], ys[i]);
            }

            int legendaY = TOPO + 8 + s * 22;
            g.drawLine(ESQUERDA + largura + 16, legendaY, ESQUERDA + largura + 40, legendaY);
            g.setColor(Color.BLACK);
            g.drawString(nomes[s], ESQUERDA + largura + 46, legendaY + 4);
        }
    }

    public static void main(String[] args) {
        Graficos grafico = new Graficos();

        JPanel controles = new JPanel(new FlowLayout(FlowLayout.LEFT));
        controles.add(grafico.visao);
        controles.add(new JLabel("Algoritmo:"));
        controles.add(grafico.algoritmo);
        controles.add(new JLabel("Vetor:"));
        controles.add(grafico.tipo);
        controles.add(grafico.status);

        JFrame janela = new JFrame("Desempenho dos algoritmos de ordenação");
        janela.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        janela.add(controles, BorderLayout.NORTH);
        janela.add(grafico, BorderLayout.CENTER);
        janela.pack();
        janela.setLocationRelativeTo(null);
        janela.setVisible(true);

        // relê o CSV a cada 2 segundos
        Timer timer = new Timer(2000, grafico);
        timer.start();
    }
}
