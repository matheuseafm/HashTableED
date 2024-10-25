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

    public TabelaHash(int capacidade) {
        this.tamanho = capacidade;
        this.tabela = new LinkedList[capacidade];
        for (int i = 0; i < capacidade; i++) {
            tabela[i] = new LinkedList<>();
        }
    }

    public void adicionar(Registro registro) {
        int indice = hash(registro.getCodigo());
        for (Registro r : tabela[indice]) {
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
        int indice = hash(codigo);
        for (Registro r : tabela[indice]) {
            if (r.getCodigo().equals(codigo)) {
                return r;
            }
        }
        return null; // Não encontrado
    }

    private int hash(String codigo) {
        // Resto da divisão
        return Integer.parseInt(codigo) % tamanho;
    }

    public int getColisoes() {
        return colisoes;
    }
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
        inicio = System.nanoTime();
        for (Registro r : registros) {
            tabela.buscar(r.getCodigo());
        }
        long tempoBusca = System.nanoTime() - inicio;

        System.out.println("Tempo de inserção: " + tempoInsercao + " ns");
        System.out.println("Tempo de busca: " + tempoBusca + " ns");
        System.out.println("Colisões: " + tabela.getColisoes());
    }

    public static void main(String[] args) {
        int[] tamanhos = {10, 100, 1000, 10000, 100000};
        long seed = 12345; // Semente para gerar dados aleatórios

        for (int tamanho : tamanhos) {
            System.out.println("Tabela Hash de tamanho: " + tamanho);
            for (int quantidade : new int[]{1000000, 5000000, 20000000}) {
                Registro[] registros = gerarDados(quantidade, seed);
                TabelaHash tabela = new TabelaHash(tamanho);

                System.out.println("Inserindo " + quantidade + " registros...");
                medirDesempenho(tabela, registros);
                System.out.println();
            }
        }
    }
}
