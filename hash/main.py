import pandas as pd
import matplotlib.pyplot as plt

# Carregar os dados do CSV
#Do jeito que estava
dados = pd.read_csv('resultados.csv', header=None, names=['Tamanho', 'FuncaoHash', 'Quantidade', 'TempoInsercao', 'TempoBusca', 'Colisoes', 'Comparacoes'])

#Do jeito que eu coloquei
dados = pd.read_csv('resultados.csv')

# Criar gráficos
for funcao in dados['FuncaoHash'].unique():
    subset = dados[dados['FuncaoHash'] == funcao]
    
    plt.figure(figsize=(10, 5))
    plt.plot(subset['Quantidade'], subset['TempoInsercao'], label='Tempo de Inserção')
    plt.plot(subset['Quantidade'], subset['TempoBusca'], label='Tempo de Busca')
    
    plt.title(f'Desenvolvimento para {funcao}')
    plt.xlabel('Quantidade de Registros')
    plt.ylabel('Tempo (ns)')
    plt.legend()
    plt.grid()
    plt.savefig(f'grafico_{funcao}.png')  # Salvar o gráfico como imagem
    plt.show()
