import java.util.LinkedList;
import java.util.Random;

class Registro {
    private String codigo;

    public Registro(String codigo) {
        this.codigo = codigo;
    }

    public String getCodigo() {
        return codigo;
    }
}

class TabelaHash {
    private LinkedList<Registro>[] tabela;
    private int tamanho;
    private int colisoes;
    private int comparacoes;
    private HashFunction hashFunction;

    public TabelaHash(int capacidade) {
        this.tamanho = capacidade;
        this.tabela = new LinkedList[capacidade];
        for (int i = 0; i < capacidade; i++) {
            tabela[i] = new LinkedList<>();
        }
        this.hashFunction = this::hash; // Padrão: resto da divisão
    }

    public void setHashFunction(HashFunction hashFunction) {
        this.hashFunction = hashFunction;
    }

    public void adicionar(Registro registro) {
        int indice = hashFunction.hash(registro.getCodigo(), tamanho);
        for (Registro r : tabela[indice]) {
            comparacoes++;
            if (r.getCodigo().equals(registro.getCodigo())) {
                return; // Registro já existe
            }
        }
        tabela[indice].add(registro);
        if (tabela[indice].size() > 1) {
            colisoes++;
        }
    }

    public Registro buscar(String codigo) {
        int indice = hashFunction.hash(codigo, tamanho);
        for (Registro r : tabela[indice]) {
            comparacoes++;
            if (r.getCodigo().equals(codigo)) {
                return r;
            }
        }
        return null; // Não encontrado
    }

    private int hash(String codigo, int tamanho) {
        return Integer.parseInt(codigo) % tamanho; // Resto da divisão
    }

    public int hashMultiplicacao(String codigo, int tamanho) {
        double A = (Math.sqrt(5) - 1) / 2; // Constante de Knuth
        int hash = (int) (tamanho * (Double.parseDouble(codigo) * A % 1));
        return hash;
    }

    public int hashDobramento(String codigo, int tamanho) {
        int hash = 0;
        for (int i = 0; i < codigo.length(); i++) {
            hash += Character.getNumericValue(codigo.charAt(i));
        }
        return hash % tamanho;
    }

    public int getColisoes() {
        return colisoes;
    }

    public int getComparacoes() {
        return comparacoes;
    }

    public void resetComparacoes() {
        this.comparacoes = 0;
    }
}

@FunctionalInterface
interface HashFunction {
    int hash(String codigo, int tamanho);
}

public class TabelaHashTeste {
    public static Registro[] gerarDados(int quantidade, long seed) {
        Random random = new Random(seed);
        Registro[] registros = new Registro[quantidade];

        for (int i = 0; i < quantidade; i++) {
            String codigo = String.format("%09d", random.nextInt(1000000000)); // Nove dígitos
            registros[i] = new Registro(codigo);
        }

        return registros;
    }

    public static void medirDesempenho(TabelaHash tabela, Registro[] registros) {
        long inicio = System.nanoTime();
        for (Registro r : registros) {
            tabela.adicionar(r);
        }
        long tempoInsercao = System.nanoTime() - inicio;

        // Medir busca
        long totalComparacoes = 0;
        for (int i = 0; i < 5; i++) { // Realiza 5 buscas para avaliação
            tabela.resetComparacoes(); // Reseta contagem de comparações
            inicio = System.nanoTime();
            for (Registro r : registros) {
                tabela.buscar(r.getCodigo());
            }
            totalComparacoes += tabela.getComparacoes();
            long tempoBusca = System.nanoTime() - inicio;

            System.out.println("Tempo de busca (iteração " + (i + 1) + "): " + tempoBusca + " ns");
        }

        System.out.println("Tempo de inserção: " + tempoInsercao + " ns");
        System.out.println("Colisões: " + tabela.getColisoes());
        System.out.println("Total de comparações em buscas: " + totalComparacoes);
    }

    public static void main(String[] args) {
        int[] tamanhos = {10, 100, 1000}; // Tamanhos da tabela
        long seed = 12345; // Semente para gerar dados aleatórios
        int[] quantidades = {1000000, 5000000, 20000000}; // Tamanhos dos dados

        for (int tamanho : tamanhos) {
            System.out.println("Tabela Hash de tamanho: " + tamanho);
            for (int quantidade : quantidades) {
                Registro[] registros = gerarDados(quantidade, seed);

                System.out.println("Inserindo " + quantidade + " registros com hash resto...");
                TabelaHash tabelaResto = new TabelaHash(tamanho);
                medirDesempenho(tabelaResto, registros);
                System.out.println();

                System.out.println("Inserindo " + quantidade + " registros com hash multiplicação...");
                TabelaHash tabelaMultiplicacao = new TabelaHash(tamanho);
                tabelaMultiplicacao.setHashFunction(tabelaMultiplicacao::hashMultiplicacao);
                medirDesempenho(tabelaMultiplicacao, registros);
                System.out.println();

                System.out.println("Inserindo " + quantidade + " registros com hash dobramento...");
                TabelaHash tabelaDobramento = new TabelaHash(tamanho);
                tabelaDobramento.setHashFunction(tabelaDobramento::hashDobramento);
                medirDesempenho(tabelaDobramento, registros);
                System.out.println();
            }
        }
    }
}
