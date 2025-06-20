# Processamento paralelo para tratamento de arquivos de vídeo

## **1. Proposta**

Dado um arquivo de vídeo com diversos chuviscos e borrões, devemos implementar uma solução, usando Processamento Paralelo (em mais de um núcleo do processador) para realizar correções adotando quaisquer métodos que venham a ser mais convenientes. 

### 1.1. Técnicas e tecnologias

- Linguagem Java
  - Compilador Maven 23.0
  - Java Threads  
  - OpenCV

## **2. Implementação**

Foram adotadas duas metodologias distintas para cada tipo de tratamento de vídeo, uma para limpar os erros de chuvisco, também conhecido como "Salt and Pepper" ou "Sal e Pimenta", e outra para tratar os borrões de tempo que aparecem pontualmente pelo vídeo. Para ambos os casos foi necessária a criação de classes separadas para armazenar dados importantes acerca dos dados tratados para que eles pudessem retornar ao exato mesmo lugar de onde foram retirados uma vez que fossem corrigidos.

### 2.1. Sal e Pimenta (SaltPepperCleaner) 

Para a classe que implementa a correção do erro Salt and Pepper, foi entregue a cada um dos núcleos do processador um objeto da classe Frame, que armazena toda a matriz de pixels de um frame, além da sua posição original no vídeo, para onde o frame deve ser devolvido após tratamento. A correção é calculada através da média dos pixels vizinhos ao pixel que deve ser tratado. 

A decisão de quais pixels devem ou não ser tratados foi estabelecida através de um threshold de distância de valor entre o pixel sendo análisado e a média do valor de todos os pixels vizinhos, quando a distância do valor é maior que 190, esse pixel é ajustado para o valor da média. Esse valor de 190 foi decidido a partir de testagem intensiva.

![Exemplo de fluxo da classe SaltPepperCleaner](/assets/images/SaltPepperCleanerFlux.png)

### 2.2. Borrões de Tempo (TimeBlurrCleaner)

Para a classe que implementa a correção do erro Time Blurr, foi entregue a cada núcleo do processados uma linha de um frame, gerando uma task bag consideravelmente maior. A correção é calculada através da média do valor do pixel na mesma posição em um frame futuro e em um frame passado. 

A decisão de quais pixels devem ou não ser tratados foi por um critério de proximidade à cor branca em uma quantidade grande de pixels em sequência. Olhamos para um pixel e para os 15 pixels antes e depois dele, se todos os pixels estiverem muito próximos da cor branca, aplicamos a correção em todos, com a média desses mesmos pixels dois frames à frente e atrás


## **3. Resultados e conclusão**

As principais dificuldades encontradas foram o estouro (overflow) do cálculo de média, o tratamento do estouro de memória que ocorreu por conta do tamanho da matriz tridimensional que estava sendo passada como parâmetro para as classes TimeBlurrCleaner e SaltPepperCleaner.


### Configuração do ambiente
|Componente|Modelo|
|----------|------|
|Processador|Intel Xeon E3-2650v3 2.3GHz 10 Núcleos, 20 Threads, 25MB Cache|
|Memória| 2x 8GB 2133MHz|
|Placa de vídeo| GeForce GTX 1650 4GB|
|Fonte| Corsair 600W 80+ Bronze|

### Configuração de execução
|Configuração   |Tempo de execução|
|---------------|-----------------|
|Sequencial     |600s             |
|2              |300s             |
|4              |150s             |
|8              |75s              |
|16             |37s              |
|32             |18s              |