import java.awt.*;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.swing.*;

// Gráfico interativo do resultados.csv; relê o arquivo enquanto o Main ainda está gravando
public class Graficos extends JPanel {

    static final String ARQUIVO_CSV = "resultados.csv";
    static final int RECARGA_MS = 2000;
    static final int RAIO_DICA = 12;
    static final int ESQUERDA = 80, DIREITA = 150, TOPO = 50, BASE = 60;
    static final String[] VISOES = {"Tempo x tamanho", "Speedup x threads"};
    static final String IDEAL = "Ideal";

    // Ordem fixa: cada série mantém a cor quando a seleção muda
    static final Color[] CORES = {
        new Color(0x2a78d6), new Color(0xeb6834), new Color(0x1baf7a),
        new Color(0xeda100), new Color(0xe87ba4), new Color(0x008300)
    };
    static final Color FUNDO = new Color(0xfcfcfb);
    static final Color TEXTO = new Color(0x0b0b0b);
    static final Color TEXTO_SUAVE = new Color(0x52514e);
    static final Color GRADE = new Color(0xe4e3df);

    record Chave(String algoritmo, String tipo, int threads, int tamanho) {}
    record Ponto(int x, int y, String texto) {}

    final JComboBox<String> visao = new JComboBox<>(VISOES);
    final JComboBox<String> algoritmo = new JComboBox<>(Main.ALGORITMOS);
    final JComboBox<String> tipo = new JComboBox<>(Main.TIPOS);
    final JLabel status = new JLabel(ARQUIVO_CSV + " não encontrado");
    final List<Ponto> pontos = new ArrayList<>();
    Map<Chave, List<Double>> amostras = new HashMap<>();
    long ultimaModificacao;

    Graficos() {
        setBackground(FUNDO);
        setPreferredSize(new Dimension(960, 600));
        setToolTipText("");
        visao.addActionListener(e -> repaint());
        algoritmo.addActionListener(e -> repaint());
        tipo.addActionListener(e -> repaint());
        carregar();
    }

    void carregar() {
        File arquivo = new File(ARQUIVO_CSV);
        if (arquivo.lastModified() == ultimaModificacao) return;

        Map<Chave, List<Double>> lidas = new HashMap<>();
        int linhas = 0;
        try {
            for (String linha : Files.readAllLines(arquivo.toPath())) {
                String[] c = linha.split(",");
                if (c.length != 7 || c[0].equals("algoritmo")) continue;
                Chave chave = new Chave(c[0], c[3], Integer.parseInt(c[2]), Integer.parseInt(c[4]));
                lidas.computeIfAbsent(chave, k -> new ArrayList<>()).add(Double.parseDouble(c[6]));
                linhas++;
            }
        } catch (IOException | NumberFormatException e) {
            status.setText("Erro ao ler " + ARQUIVO_CSV + ": " + e.getMessage());
            return;   // ultimaModificacao não muda, então a próxima recarga tenta de novo
        }
        ultimaModificacao = arquivo.lastModified();
        amostras = lidas;
        status.setText(linhas + " medições em " + ARQUIVO_CSV);
        repaint();
    }

    double mediana(int threads, int tamanho) {
        Chave chave = new Chave((String) algoritmo.getSelectedItem(), (String) tipo.getSelectedItem(), threads, tamanho);
        List<Double> valores = amostras.get(chave);
        if (valores == null) return Double.NaN;
        return Main.mediana(valores.stream().mapToDouble(Double::doubleValue).toArray());
    }

    double[] temposPorTamanho(int threads) {
        return Arrays.stream(Main.TAMANHOS).mapToDouble(n -> mediana(threads, n)).toArray();
    }

    Map<String, double[]> seriesTempo() {
        Map<String, double[]> series = new LinkedHashMap<>();
        series.put("Serial", temposPorTamanho(1));
        for (int t : Main.THREADS) series.put(t + " threads", temposPorTamanho(t));
        return series;
    }

    Map<String, double[]> seriesSpeedup() {
        Map<String, double[]> series = new LinkedHashMap<>();
        for (int n : Main.TAMANHOS) {
            series.put(n + " números", Arrays.stream(Main.THREADS).mapToDouble(t -> mediana(1, n) / mediana(t, n)).toArray());
        }
        series.put(IDEAL, Arrays.stream(Main.THREADS).asDoubleStream().toArray());
        return series;
    }

    static boolean desenhavel(double v) {
        return Double.isFinite(v) && v > 0;
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        Graphics2D g = (Graphics2D) graphics;
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        pontos.clear();

        boolean speedup = visao.getSelectedIndex() == 1;
        int[] categorias = speedup ? Main.THREADS : Main.TAMANHOS;
        Map<String, double[]> series = speedup ? seriesSpeedup() : seriesTempo();

        double min = Double.MAX_VALUE, max = 0;
        boolean temDados = false;
        for (Map.Entry<String, double[]> serie : series.entrySet()) {
            for (double v : serie.getValue()) {
                if (desenhavel(v)) {
                    min = Math.min(min, v);
                    max = Math.max(max, v);
                    temDados |= !serie.getKey().equals(IDEAL);
                }
            }
        }

        g.setColor(TEXTO);
        g.setFont(getFont().deriveFont(Font.BOLD, 15f));
        g.drawString(algoritmo.getSelectedItem() + ", dados: " + tipo.getSelectedItem(), ESQUERDA, 28);
        g.setFont(getFont());
        if (!temDados) {
            g.drawString("Sem dados para esta seleção. Rode: java Main", ESQUERDA, TOPO + 30);
            return;
        }

        // Os tempos cobrem várias ordens de grandeza, por isso o eixo é log; o speedup é linear a partir de 0
        double piso = speedup ? 0 : Math.floor(Math.log10(min));
        double teto = speedup ? Math.ceil(max) : Math.max(piso + 1, Math.ceil(Math.log10(max)));
        int largura = getWidth() - ESQUERDA - DIREITA;
        int altura = getHeight() - TOPO - BASE;
        int fundoGrafico = TOPO + altura;

        // grade e rótulos do eixo Y
        double passo = speedup ? Math.max(1, Math.ceil(teto / 5)) : 1;
        for (double marca = piso; marca <= teto; marca += passo) {
            int y = fundoGrafico - (int) ((marca - piso) / (teto - piso) * altura);
            g.setColor(GRADE);
            g.drawLine(ESQUERDA, y, ESQUERDA + largura, y);
            g.setColor(TEXTO_SUAVE);
            String rotulo = speedup ? String.format("%.0f", marca) : String.format("%." + (int) Math.max(0, -marca) + "f", Math.pow(10, marca));
            g.drawString(rotulo, ESQUERDA - 8 - g.getFontMetrics().stringWidth(rotulo), y + 4);
        }

        // rótulos do eixo X
        int[] xs = new int[categorias.length];
        for (int i = 0; i < categorias.length; i++) {
            // as threads não têm espaçamento uniforme: eixo proporcional mantém a linha ideal reta
            int primeira = categorias[0], ultima = categorias[categorias.length - 1];
            xs[i] = ESQUERDA + (speedup ? (categorias[i] - primeira) * largura / (ultima - primeira) : i * largura / (categorias.length - 1));
            String rotulo = String.valueOf(categorias[i]);
            g.drawString(rotulo, xs[i] - g.getFontMetrics().stringWidth(rotulo) / 2, fundoGrafico + 20);
        }

        // títulos dos eixos
        String tituloX = speedup ? "Threads" : "Tamanho do vetor";
        g.drawString(tituloX, ESQUERDA + (largura - g.getFontMetrics().stringWidth(tituloX)) / 2, fundoGrafico + 44);
        g.drawString(speedup ? "Speedup (tempo serial / tempo paralelo)" : "Tempo em ms, escala logarítmica", ESQUERDA, TOPO - 8);

        // linhas, marcadores e legenda
        int indice = 0;
        int legendaY = TOPO + 8;
        for (Map.Entry<String, double[]> serie : series.entrySet()) {
            boolean ideal = serie.getKey().equals(IDEAL);
            Color cor = ideal ? TEXTO_SUAVE : CORES[indice++];
            float[] tracejado = ideal ? new float[]{6, 5} : null;
            g.setColor(cor);
            g.setStroke(new BasicStroke(2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 1, tracejado, 0));

            int[] ys = new int[categorias.length];
            double[] valores = serie.getValue();
            for (int i = 0; i < categorias.length; i++) {
                if (!desenhavel(valores[i])) continue;
                double posicao = speedup ? valores[i] : Math.log10(valores[i]);
                ys[i] = fundoGrafico - (int) ((posicao - piso) / (teto - piso) * altura);
                if (i > 0 && desenhavel(valores[i - 1])) g.drawLine(xs[i - 1], ys[i - 1], xs[i], ys[i]);
            }
            if (!ideal) {
                for (int i = 0; i < categorias.length; i++) {
                    if (!desenhavel(valores[i])) continue;
                    g.setColor(FUNDO);
                    g.fillOval(xs[i] - 6, ys[i] - 6, 12, 12);
                    g.setColor(cor);
                    g.fillOval(xs[i] - 4, ys[i] - 4, 8, 8);
                    String valor = speedup ? String.format("%.2fx", valores[i]) : String.format("%.2f ms", valores[i]);
                    pontos.add(new Ponto(xs[i], ys[i], serie.getKey() + ", " + categorias[i] + ": " + valor));
                }
            }

            g.drawLine(ESQUERDA + largura + 16, legendaY, ESQUERDA + largura + 40, legendaY);
            g.setStroke(new BasicStroke(1));
            g.setColor(TEXTO);
            g.drawString(serie.getKey(), ESQUERDA + largura + 46, legendaY + 4);
            legendaY += 22;
        }
    }

    @Override
    public String getToolTipText(MouseEvent evento) {
        Ponto maisPerto = null;
        double menor = RAIO_DICA;
        for (Ponto ponto : pontos) {
            double distancia = evento.getPoint().distance(ponto.x(), ponto.y());
            if (distancia <= menor) {
                menor = distancia;
                maisPerto = ponto;
            }
        }
        return maisPerto == null ? null : maisPerto.texto();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Graficos grafico = new Graficos();

            JPanel controles = new JPanel(new FlowLayout(FlowLayout.LEFT));
            controles.add(grafico.visao);
            controles.add(new JLabel("Algoritmo:"));
            controles.add(grafico.algoritmo);
            controles.add(new JLabel("Dados:"));
            controles.add(grafico.tipo);
            controles.add(grafico.status);

            JFrame janela = new JFrame("Desempenho dos algoritmos de ordenação");
            janela.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            janela.add(controles, BorderLayout.NORTH);
            janela.add(grafico, BorderLayout.CENTER);
            janela.pack();
            janela.setLocationRelativeTo(null);
            janela.setVisible(true);

            new Timer(RECARGA_MS, e -> grafico.carregar()).start();
        });
    }
}
