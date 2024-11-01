import java.util.LinkedList; // Importa a classe LinkedList para uso na tabela hash.
import java.util.Random; // Importa a classe Random para gerar dados aleatórios.
import java.io.FileWriter; // Importa a classe FileWriter para escrever arquivos.
import java.io.IOException; // Importa a classe IOException para lidar com exceções de I/O.
import java.io.PrintWriter; // Importa a classe PrintWriter para facilitar a escrita de textos em arquivos.

class Registro {
    private String codigo; // Atributo que armazena o código do registro.

    public Registro(String codigo) { // Construtor da classe Registro.
        this.codigo = codigo; // Inicializa o atributo codigo.
    }

    public String getCodigo() { // Método getter para obter o código.
        return codigo; // Retorna o código do registro.
    }
}

class TabelaHash {
    private LinkedList<Registro>[] tabela; // Array de listas encadeadas para armazenar registros.
    private int tamanho; // Tamanho da tabela hash.
    private int colisoes; // Contador de colisões.
    private int comparacoes; // Contador de comparações.
    private HashFunction hashFunction; // Função hash utilizada.

    public int getTamanho(){ // Método para obter o tamanho da tabela.
        return this.tamanho; // Retorna o tamanho da tabela.
    }

    public TabelaHash(int capacidade) { // Construtor da classe TabelaHash.
        this.tamanho = capacidade; // Inicializa o tamanho da tabela.
        this.tabela = new LinkedList[capacidade]; // Cria um array de listas encadeadas.
        for (int i = 0; i < capacidade; i++) {
            tabela[i] = new LinkedList<>(); // Inicializa cada lista encadeada.
        }
        this.hashFunction = this::hash; // Define a função hash padrão (resto da divisão).
    }

    public void setHashFunction(HashFunction hashFunction) { // Método para definir uma função hash personalizada.
        this.hashFunction = hashFunction; // Atribui a nova função hash.
    }

    public void adicionar(Registro registro) { // Método para adicionar um registro à tabela.
        int indice = hashFunction.hash(registro.getCodigo(), tamanho); // Calcula o índice usando a função hash.
        for (Registro r : tabela[indice]) { // Itera sobre os registros na lista correspondente.
            comparacoes++; // Incrementa o contador de comparações.
            if (r.getCodigo().equals(registro.getCodigo())) { // Verifica se o registro já existe.
                return; // Se já existe, sai do método.
            }
        }
        tabela[indice].add(registro); // Adiciona o registro à lista.
        if (tabela[indice].size() > 1) { // Se a lista contém mais de um registro.
            colisoes++; // Incrementa o contador de colisões.
        }
    }

    public Registro buscar(String codigo) { // Método para buscar um registro pelo código.
        int indice = hashFunction.hash(codigo, tamanho); // Calcula o índice.
        for (Registro r : tabela[indice]) { // Itera sobre os registros na lista correspondente.
            comparacoes++; // Incrementa o contador de comparações.
            if (r.getCodigo().equals(codigo)) { // Verifica se o registro foi encontrado.
                return r; // Se encontrado, retorna o registro.
            }
        }
        return null; // Se não encontrado, retorna null.
    }

    private int hash(String codigo, int tamanho) { // Função hash padrão (resto da divisão).
        return Integer.parseInt(codigo) % tamanho; // Retorna o índice como o resto da divisão do código.
    }

    public int hashMultiplicacao(String codigo, int tamanho) { // Função hash usando o método de multiplicação.
        double A = (Math.sqrt(5) - 1) / 2; // Constante de Knuth.
        int hash = (int) (tamanho * (Double.parseDouble(codigo) * A % 1)); // Cálculo do índice.
        return hash; // Retorna o índice calculado.
    }

    public int hashDobramento(String codigo, int tamanho) { // Função hash usando dobramento.
        int hash = 0; // Inicializa o hash em zero.
        for (int i = 0; i < codigo.length(); i++) { // Itera sobre cada caractere do código.
            int valor = Character.digit(codigo.charAt(i), 10); // Converte o caractere em um dígito.
            if (valor != -1) { // Se o dígito é válido.
                hash = (hash + valor) % tamanho; // Atualiza o hash.
            }
        }
        return hash; // Retorna o índice calculado.
    }

    public int getColisoes() { // Método para obter o número de colisões.
        return colisoes; // Retorna o número de colisões.
    }

    public int getComparacoes() { // Método para obter o número total de comparações.
        return comparacoes; // Retorna o total de comparações.
    }

    public void resetComparacoes() { // Método para resetar o contador de comparações.
        this.comparacoes = 0; // Reseta o contador para zero.
    }
}

@FunctionalInterface
interface HashFunction { // Interface funcional para a função hash.
    int hash(String codigo, int tamanho); // Método para calcular o índice hash.
}

public class TabelaHashTeste {
    public static Registro[] gerarDados(int quantidade, long seed) { // Método para gerar dados aleatórios.
        Random random = new Random(seed); // Cria um gerador de números aleatórios com uma semente.
        Registro[] registros = new Registro[quantidade]; // Cria um array para armazenar registros.

        for (int i = 0; i < quantidade; i++) { // Itera até gerar a quantidade especificada.
            String codigo = String.format("%09d", random.nextInt(1000000000)); // Gera um código aleatório de 9 dígitos.
            registros[i] = new Registro(codigo); // Cria um novo registro com o código gerado.
        }

        return registros; // Retorna o array de registros.
    }

    public static void exportarResultadosParaCSV(String arquivo, int tamanho, String funcaoHash, int quantidade, long tempoInsercao, long tempoBusca, int colisoes, long totalComparacoes) { // Método para exportar resultados para CSV.
        try (PrintWriter writer = new PrintWriter(new FileWriter(arquivo, true))) { // Tenta abrir o arquivo para escrita (modo append).
            writer.printf("%d,%s,%d,%d,%d,%d,%d%n", tamanho, funcaoHash, quantidade, tempoInsercao, tempoBusca, colisoes, totalComparacoes); // Formata e escreve os resultados.
        } catch (IOException e) { // Captura exceções de I/O.
            e.printStackTrace(); // Imprime a pilha de erro.
        }
    }

    public static void medirDesempenho(TabelaHash tabela, Registro[] registros, String funcaoHash, int quantidade, String arquivo) { // Método para medir o desempenho da tabela hash.
        long inicio = System.nanoTime(); // Marca o tempo de início da inserção.
        for (Registro r : registros) { // Itera sobre todos os registros.
            tabela.adicionar(r); // Adiciona cada registro à tabela.
        }
        long tempoInsercao = System.nanoTime() - inicio; // Calcula o tempo total de inserção.

        long totalComparacoes = 0; // Inicializa o total de comparações.
        long tempoBusca = 0; // Inicializa o tempo total de busca.

        for (int i = 0; i < 5; i++) { // Realiza 5 buscas para avaliação.
            tabela.resetComparacoes(); // Reseta contagem de comparações.
            inicio = System.nanoTime(); // Marca o tempo de início da busca.
            for (Registro r : registros) { // Itera sobre todos os registros para buscar.
                tabela.buscar(r.getCodigo()); // Busca cada registro na tabela.
            }
            totalComparacoes += tabela.getComparacoes(); // Soma o total de comparações feitas.
            tempoBusca += (System.nanoTime() - inicio); // Acumula o tempo total de busca.
        }

        // Calcular tempo médio de busca
        tempoBusca /= 5; // Calcula o tempo médio de busca.

        System.out.println("Tempo de inserção: " + tempoInsercao + " ns"); // Exibe o tempo de inserção.
        System.out.println("Colisões: " + tabela.getColisoes()); // Exibe o número de colisões.
        System.out.println("Total de comparações em buscas: " + totalComparacoes); // Exibe o total de comparações.
        System.out.println("Tempo médio de busca: " + tempoBusca + " ns"); // Exibe o tempo médio de busca.

        // Exportar resultados para CSV
        exportarResultadosParaCSV(arquivo, tabela.getTamanho(), funcaoHash, quantidade, tempoInsercao, tempoBusca, tabela.getColisoes(), totalComparacoes); // Exporta os resultados para o arquivo CSV.
    }

    public static void main(String[] args) { // Método principal do programa.
        int[] tamanhos = {1000, 2000, 5000}; // Diferentes tamanhos de tabela hash.
        long seed = 12345; // Semente para gerar dados aleatórios.
        int[] quantidades = {10000, 50000, 20000}; // Quantidades de registros a serem gerados.
        String arquivoCSV = "resultados.csv"; // Nome do arquivo CSV para armazenar resultados.

        // Criar arquivo CSV e adicionar cabeçalho
        try (PrintWriter writer = new PrintWriter(new FileWriter(arquivoCSV))) { // Tenta abrir o arquivo para escrita.
            writer.println("Tamanho,FuncaoHash,Quantidade,TempoInsercao,TempoBusca,Colisoes,Comparacoes"); // Adiciona o cabeçalho ao CSV.
        } catch (IOException e) { // Captura exceções de I/O.
            e.printStackTrace(); // Imprime a pilha de erro.
        }

        for (int tamanho : tamanhos) { // Itera sobre os tamanhos definidos para a tabela hash.
            System.out.println("Tabela Hash de tamanho: " + tamanho); // Exibe o tamanho atual da tabela hash.
            for (int quantidade : quantidades) { // Itera sobre as quantidades de registros.
                Registro[] registros = gerarDados(quantidade, seed); // Gera registros aleatórios.

                System.out.println("Inserindo " + quantidade + " registros com hash resto..."); // Exibe a mensagem de inserção.
                TabelaHash tabelaResto = new TabelaHash(tamanho); // Cria uma tabela hash com a função de hash padrão (resto).
                medirDesempenho(tabelaResto, registros, "Resto", quantidade, arquivoCSV); // Mede o desempenho e exporta resultados.
                System.out.println(); // Linha em branco para separação.

                System.out.println("Inserindo " + quantidade + " registros com hash multiplicação..."); // Mensagem para hash multiplicação.
                TabelaHash tabelaMultiplicacao = new TabelaHash(tamanho); // Cria a tabela hash.
                tabelaMultiplicacao.setHashFunction(tabelaMultiplicacao::hashMultiplicacao); // Define a função hash de multiplicação.
                medirDesempenho(tabelaMultiplicacao, registros, "Multiplicacao", quantidade, arquivoCSV); // Mede desempenho.
                System.out.println(); // Linha em branco.

                System.out.println("Inserindo " + quantidade + " registros com hash dobramento..."); // Mensagem para hash dobramento.
                TabelaHash tabelaDobramento = new TabelaHash(tamanho); // Cria a tabela hash.
                tabelaDobramento.setHashFunction(tabelaDobramento::hashDobramento); // Define a função hash de dobramento.
                medirDesempenho(tabelaDobramento, registros, "Dobramento", quantidade, arquivoCSV); // Mede desempenho.
                System.out.println(); // Linha em branco.
            }
        }
    }
}
