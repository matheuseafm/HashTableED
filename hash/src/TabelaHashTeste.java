import java.util.Random; // Para gerar dados aleatórios.
import java.io.FileWriter; // Para escrever arquivos.
import java.io.IOException; // Para lidar com exceções de I/O.
import java.io.PrintWriter; // Para facilitar a escrita em arquivos.

class Registro {
    private String codigo; // Código do registro.

    public Registro(String codigo) { // Construtor.
        this.codigo = codigo;
    }

    public String getCodigo() { // Método getter para o código.
        return codigo;
    }
}

class No {
    Registro registro; // Registro armazenado neste nó.
    No proximo; // Referência para o próximo nó.

    public No(Registro registro) {
        this.registro = registro;
        this.proximo = null; // Inicializa o próximo como null.
    }
}

class TabelaHash {
    private No[] tabela; // Array de nós, que formam a tabela hash.
    private int tamanho; // Tamanho da tabela hash.
    private int colisoes; // Contador de colisões.
    private int comparacoes; // Contador de comparações realizadas.
    private HashFunction hashFunction; // Função hash utilizada para determinar índices.

    public int getTamanho() {
        return this.tamanho; // Retorna o tamanho da tabela.
    }

    public TabelaHash(int capacidade) {
        this.tamanho = capacidade;
        this.tabela = new No[capacidade]; // Inicializa o array de nós.
        this.hashFunction = this::hash; // Define a função hash padrão (resto da divisão).
    }

    public void setHashFunction(HashFunction hashFunction) {
        this.hashFunction = hashFunction; // Permite definir uma função hash personalizada.
    }

    public void adicionar(Registro registro) {
        int indice = hashFunction.hash(registro.getCodigo(), tamanho); // Calcula o índice usando a função hash.
        No atual = tabela[indice]; // Inicia a busca pelo nó na tabela.

        // Verifica se o registro já existe na lista encadeada.
        while (atual != null) {
            comparacoes++; // Incrementa o contador de comparações.
            if (atual.registro.getCodigo().equals(registro.getCodigo())) {
                return; // Registro já existe, não adiciona.
            }
            atual = atual.proximo; // Move para o próximo nó.
        }

        // Adiciona novo nó ao início da lista encadeada.
        No novoNo = new No(registro);
        novoNo.proximo = tabela[indice];
        tabela[indice] = novoNo;

        // Se há mais de um nó, contabiliza colisão.
        if (tabela[indice] != null && tabela[indice].proximo != null) {
            colisoes++;
        }
    }

    public Registro buscar(String codigo) {
        int indice = hashFunction.hash(codigo, tamanho); // Calcula o índice para busca.
        No atual = tabela[indice]; // Inicia a busca pelo nó.

        // Percorre a lista encadeada até encontrar o registro.
        while (atual != null) {
            comparacoes++; // Incrementa o contador de comparações.
            if (atual.registro.getCodigo().equals(codigo)) {
                return atual.registro; // Registro encontrado.
            }
            atual = atual.proximo; // Move para o próximo nó.
        }
        return null; // Se não encontrado, retorna null.
    }

    // Função hash padrão (resto da divisão).
    private int hash(String codigo, int tamanho) {
        return Integer.parseInt(codigo) % tamanho; // Garante que o índice esteja dentro dos limites da tabela.
    }

    // Função hash usando multiplicação.
    public int hashMultiplicacao(String codigo, int tamanho) {
        double A = (Math.sqrt(5) - 1) / 2; // Constante de Knuth para melhorar a dispersão.
        int hash = (int) (tamanho * (Double.parseDouble(codigo) * A % 1)); // Cálculo do índice.
        return hash;
    }

    // Função hash usando dobramento.
    public int hashDobramento(String codigo, int tamanho) {
        int hash = 0; // Inicializa o hash.
        // Soma os dígitos do código e aplica o módulo para o tamanho da tabela.
        for (int i = 0; i < codigo.length(); i++) {
            int valor = Character.digit(codigo.charAt(i), 10); // Converte o caractere em um dígito.
            if (valor != -1) {
                hash = (hash + valor) % tamanho;
            }
        }
        return hash; // Retorna o índice calculado.
    }

    public int getColisoes() {
        return colisoes; // Retorna o número de colisões.
    }

    public int getComparacoes() {
        return comparacoes; // Retorna o total de comparações feitas.
    }

    public void resetComparacoes() {
        this.comparacoes = 0; // Reseta o contador de comparações.
    }
}

@FunctionalInterface
interface HashFunction {
    int hash(String codigo, int tamanho); // Método para calcular o índice hash.
}

public class TabelaHashTeste {
    // Método para gerar dados aleatórios.
    public static Registro[] gerarDados(int quantidade, long seed) {
        Random random = new Random(seed); // Cria um gerador de números aleatórios.
        Registro[] registros = new Registro[quantidade]; // Array para armazenar registros.

        for (int i = 0; i < quantidade; i++) {
            String codigo = String.format("%09d", random.nextInt(1000000000)); // Gera um código aleatório de 9 dígitos.
            registros[i] = new Registro(codigo); // Cria um novo registro com o código gerado.
        }

        return registros; // Retorna o array de registros.
    }

    // Método para exportar resultados para CSV.
    public static void exportarResultadosParaCSV(String arquivo, int tamanho, String funcaoHash, int quantidade, long tempoInsercao, long tempoBusca, int colisoes, long totalComparacoes) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(arquivo, true))) {
            writer.printf("%d,%s,%d,%d,%d,%d,%d%n", tamanho, funcaoHash, quantidade, tempoInsercao, tempoBusca, colisoes, totalComparacoes);
        } catch (IOException e) {
            e.printStackTrace(); // Exibe erro se houver problema ao escrever.
        }
    }

    // Método para medir desempenho da tabela hash.
    public static void medirDesempenho(TabelaHash tabela, Registro[] registros, String funcaoHash, int quantidade, String arquivo) {
        long inicio = System.nanoTime(); // Marca o tempo de início da inserção.
        for (Registro r : registros) {
            tabela.adicionar(r); // Adiciona cada registro à tabela.
        }
        long tempoInsercao = System.nanoTime() - inicio; // Tempo total de inserção.

        long totalComparacoes = 0; // Inicializa total de comparações.
        long tempoBusca = 0; // Inicializa tempo total de busca.

        // Realiza buscas múltiplas para calcular média.
        for (int i = 0; i < 5; i++) {
            tabela.resetComparacoes(); // Reseta contagem de comparações.
            inicio = System.nanoTime(); // Marca o tempo de início da busca.
            for (Registro r : registros) {
                tabela.buscar(r.getCodigo()); // Busca cada registro na tabela.
            }
            totalComparacoes += tabela.getComparacoes(); // Soma total de comparações.
            tempoBusca += (System.nanoTime() - inicio); // Acumula tempo total de busca.
        }

        tempoBusca /= 5; // Calcula tempo médio de busca.

        // Exibe resultados.
        System.out.println("Tempo de inserção: " + tempoInsercao + " ns");
        System.out.println("Colisões: " + tabela.getColisoes());
        System.out.println("Total de comparações em buscas: " + totalComparacoes);
        System.out.println("Tempo médio de busca: " + tempoBusca + " ns");

        // Exporta resultados para CSV.
        exportarResultadosParaCSV(arquivo, tabela.getTamanho(), funcaoHash, quantidade, tempoInsercao, tempoBusca, tabela.getColisoes(), totalComparacoes);
    }

    public static void main(String[] args) {
        int[] tamanhos = {1000, 2000, 5000}; // Diferentes tamanhos de tabela hash.
        long seed = 12345; // Semente para gerar dados aleatórios.
        int[] quantidades = {10000, 50000, 20000}; // Quantidades de registros a serem gerados.
        String arquivoCSV = "resultados.csv"; // Nome do arquivo CSV para armazenar resultados.

        // Criar arquivo CSV e adicionar cabeçalho.
        try (PrintWriter writer = new PrintWriter(new FileWriter(arquivoCSV))) {
            writer.println("Tamanho,FuncaoHash,Quantidade,TempoInsercao,TempoBusca,Colisoes,Comparacoes"); // Cabeçalho do CSV.
        } catch (IOException e) {
            e.printStackTrace(); // Exibe erro se houver problema ao criar o arquivo.
        }

        // Itera sobre tamanhos da tabela hash.
        for (int tamanho : tamanhos) {
            System.out.println("Tabela Hash de tamanho: " + tamanho);
            // Itera sobre quantidades de registros.
            for (int quantidade : quantidades) {
                Registro[] registros = gerarDados(quantidade, seed); // Gera dados aleatórios.

                System.out.println("Inserindo " + quantidade + " registros com hash resto...");
                TabelaHash tabelaResto = new TabelaHash(tamanho);
                medirDesempenho(tabelaResto, registros, "Resto", quantidade, arquivoCSV);
                System.out.println();

                System.out.println("Inserindo " + quantidade + " registros com hash multiplicação...");
                TabelaHash tabelaMultiplicacao = new TabelaHash(tamanho);
                tabelaMultiplicacao.setHashFunction(tabelaMultiplicacao::hashMultiplicacao);
                medirDesempenho(tabelaMultiplicacao, registros, "Multiplicacao", quantidade, arquivoCSV);
                System.out.println();

                System.out.println("Inserindo " + quantidade + " registros com hash dobramento...");
                TabelaHash tabelaDobramento = new TabelaHash(tamanho);
                tabelaDobramento.setHashFunction(tabelaDobramento::hashDobramento);
                medirDesempenho(tabelaDobramento, registros, "Dobramento", quantidade, arquivoCSV);
                System.out.println();
            }
        }
    }
}
