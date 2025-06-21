# Processamento paralelo para tratamento de arquivos de vídeo

## **1. Introdução**

Dado um arquivo de vídeo com diversos chuviscos e borrões, devemos implementar uma solução, usando Processamento Paralelo (em mais de um núcleo do processador) para realizar correções adotando quaisquer métodos que venham a ser mais convenientes. 

### 1.1. Técnicas e tecnologias

- Linguagem Java
  - Build: Maven 23.0
  - Parelelismo: Threads  
  - Processamento de imagem: OpenCV

## **2. Implementação**

Foram adotadas duas metodologias distintas para cada 
tipo de tratamento de vídeo, uma para limpar os erros 
de chuvisco, também conhecido como "Salt and Pepper" 
ou "Sal e Pimenta", e outra para tratar os borrões de 
tempo que aparecem pontualmente pelo vídeo. Para ambos 
os casos foi necessária a criação de classes separadas 
para armazenar dados importantes acerca dos dados tratados 
para que eles pudessem retornar ao exato mesmo lugar de 
onde foram retirados uma vez que fossem corrigidos.

### 2.1. Sal e Pimenta (SaltPepperCleaner) 

- Para a classe que implementa a correção do erro Salt and Pepper, foi
entregue a cada um dos núcleos do processador um objeto da classe Frame, 
que armazena toda a matriz de pixels de um frame, além da sua posição 
original no vídeo, para onde o frame deve ser devolvido após tratamento.
```java
public class Frame {
    private int index;
    private byte frame[][];

    public Frame(int index, byte[][] frame) {
        this.index = index;
        this.frame = frame;
    }

    public int getIndex() {
        return index;
    }


    public byte[][] getFrame() {
        return frame;
    }

}

```

- A correção é calculada através da média dos pixels vizinhos ao pixel que 
deve ser tratado. 
```java
private byte calcMedia(List<Byte> pixels) {
        if (pixels == null || pixels.isEmpty()) {
            return 0; // valor padrão
        }

        int soma = 0;
        for(Byte it:pixels){
            soma+=it;
        }
        soma/=pixels.size();
        byte media = (byte) soma;
        return media;
    }
```
- A decisão de quais pixels devem ou não ser tratados 
é feita estabelecendo um limite de distância de valor entre 
o pixel sendo analisado e a média do valor de todos os seus pixels vizinhos.
```java
//Método para tratar o frame atual, percorre a matriz de bytes e corrige os outliers
private byte[][] treatFrame() {
    byte[][] frameResult = new byte[currentFrame.getFrame().length][currentFrame.getFrame()[0].length];
    List<Byte> neighbours = new ArrayList<>();
    for (int i = 2; i < this.currentFrame.getFrame().length-2; i++) {
        for (int j = 2; j < this.currentFrame.getFrame()[i].length-2; j++) {
            neighbours = getNeighbours(this.currentFrame.getFrame(), i, j);
            byte media = calcMedia(neighbours);
            if (this.currentFrame.getFrame()[i][j] < media - 190 || this.currentFrame.getFrame()[i][j] > media + 190){
                //System.out.println(this.currentFrame.getFrame()[i][j] + " <-- " + media);
                frameResult[i][j] = media;
            }else {
                frameResult[i][j] = this.currentFrame.getFrame()[i][j];
            }
        }
    }
    return frameResult;
}
```
- Para pegar as coordenadas dos pixels adjacentes ao que 
estamos analisando, utilizamos uma abordagem de iterar ao redor 
do pixel ignorando-o com um if
```java
    //Devolve um List<Byte> com todos os pixels vizinhos de um pixel cujo indice é passado como parametro
    private List<Byte> getNeighbours(byte[][] currentFrame, int i, int j) {
        List<Byte> pixels = new ArrayList<>();

        for (int di = -2; di <= 2; di++) {
            for (int dj = -2; dj <= 2; dj++) {
                // Ignora o próprio pixel
                if (di == 0 && dj == 0) continue;

                int ni = i + di;
                int nj = j + dj;
                pixels.add(currentFrame[ni][nj]);
                // Verifica se os índices estão dentro dos limites da matriz

            }
        }

        return pixels;
    }
```
Esta classe trabalha com paralelismo entregando a cada núcleo do 
processador um frame inteiro, que ele deve tratar e devolver a um
array estático da classe SaltPepperCleaner, em sua devida posição
original

```java
@Override
public void run() {
    while (!taskBag.isEmpty()){
        synchronized (taskBag){
            if(!taskBag.isEmpty()){
                //entrega um frame da taskBag estática para o objeto SaltPepperCleaner
                this.currentFrame = taskBag.removeFirst();
                DecimalFormat df = new DecimalFormat("#.##");
                if((taskBag.size()/originalBagSize)%500==0){
                    //exibe a porcentagem de frames já tratados
                    System.out.println(df.format((1.0-(double)taskBag.size()/originalBagSize)*100) + "%");
                }
            }
        }
        if(currentFrame!=null){
            //aloca numa matriz estática de resultado o frame tratado
            fixedFrames[currentFrame.getIndex()] = this.treatFrame();
        }
        this.currentFrame=null; //reseta o valor de current frame para null para evitar conflitos de acesso
    }
}
```
- ilustração do fluxo da classe
![Exemplo de fluxo da classe SaltPepperCleaner](https://iili.io/FxhPX4I.png)

### 2.2. Borrões de Tempo (TimeBlurrCleaner)

- Para a classe que implementa a correção do erro Time Blurr,
foi entregue a cada núcleo do processados uma linha de um 
frame, gerando uma task bag consideravelmente maior, as informações 
de qual frame essa linha foi retirada e qual seu index no frame são
armazenadas numa classe FrameLine que possui referências para o frame
que está duas posições atrás na matriz tridimensional e também para o 
frame duas posições à frente. Essa classe faz mais referência ao invés
de criar duplicatas de dados, pois o programa estava apresentando estouro
de memória
```java
public class FrameLine {
    private int time;
    private int index;
    private final int previous, next;

    public FrameLine(int time, int lineIndex, int previous, int next) {
        this.time = time;
        this.index = lineIndex;
        this.previous = previous;
        this.next = next;
    }

    public byte[] getNext(byte[][][] allFrames) {
        return (next< allFrames.length) ? allFrames[this.next][this.index] : null;
    }

    public int getTime() {
        return time;
    }

    public int getIndex() {
        return index;
    }

    public byte[] getPrevious(byte[][][] allFrames) {
        return (previous>=0) ? allFrames[this.previous][this.index] : null;
    }
}

```

- A correção é calculada através da média do valor do pixel na 
mesma posição em um frame futuro e em um frame passado. 
```java
private byte calcCorrection(FrameLine frameLine, int index){
    if(frameLine.getPrevious(originalFrames) == null 
            || frameLine.getNext(originalFrames) == null){
        return 0;
    }
    int media = 0;
    media += frameLine.getPrevious(originalFrames)[index];
    media += frameLine.getNext(originalFrames)[index];
    media /= 2;
    return (byte)media;

}
```

A decisão de quais pixels devem ou não ser tratados 
foi por um critério de proximidade à cor branca em uma 
quantidade grande de pixels em sequência.
```java
private byte[] treatLine(){
  byte[] treatedLine = new byte[originalFrames[this.currentLine.getTime()][this.currentLine.getIndex()].length];
  /* verifica em um bloco de 30 pixels da linha atual, se todos os valores dos pixels são maiores que 122,
   * que representaria uma mancha branca no vídeo e trata a linha quando identifica o erro */
  for(int i = 15; i < originalFrames[this.currentLine.getTime()][this.currentLine.getIndex()].length-15; i++){
    boolean blurr = true;
    for(int j = i-15; j < i+15; j++){
      if(originalFrames[this.currentLine.getTime()][this.currentLine.getIndex()][j] < 122 ){
        blurr = false;
        break;
      }
    }
    
    //tratamento da linha
    if(blurr && this.currentLine.getPrevious(originalFrames)!=null 
            && this.currentLine.getNext(originalFrames)!=null){
      for(int j = i-15; j < i+15; j++){
        treatedLine[j] = calcCorrection(this.currentLine, j);
      }
    }else{
      for(int j = i-15; j < i+15; j++){
        treatedLine[j] = originalFrames[this.currentLine.getTime()][this.currentLine.getIndex()][j];
      }
    }
  }
  return treatedLine;
}
```

Olhamos para 
um pixel e para os 15 pixels antes e depois dele, se todos
os pixels estiverem muito próximos da cor branca, aplicamos 
a correção em todos, com a média desses mesmos pixels dois
frames à frente e atrás

- Ilustração do fluxo da classe TimeBlurrCleaner
![Exemplo de fluxo da classe TimeBlurrCleaner](https://iili.io/Fxk9g29.png)


## **3. Resultados e conclusão**

As principais dificuldades encontradas foram o estouro (overflow) do cálculo
de média, o tratamento do estouro de memória que ocorreu por conta do 
tamanho da matriz tridimensional que estava sendo passada como parâmetro 
para as classes TimeBlurrCleaner e SaltPepperCleaner.

Pudemos observar uma melhoria no vídeo, mas foi aquém das expectativas,
a segunda fase do processamento apesar de estar paralelizada se demonstrou
menos eficiente, consumiu quantidades excessivas de memória, o que nos fez 
ter que pensar em formas de contornar os estouros que estava causando

### Configuração do ambiente
| Componente     | Modelo                                             |
|----------------|----------------------------------------------------|
| Processador    | Intel Xeon E3-2650v3 (10C/20T, 2.3GHz, 20MB Cache) |
| Memória        | 16GB DDR4 1866MHz (2x 8GB)                         |
| Placa de vídeo | GeForce GTX 1650 4GB                               |
| Fonte          | Corsair 600W 80+ Bronze                            |
| S.O.           | Windows 10 Pro x64                                 |

### Configuração de execução
| Cores          | Teste 1  | Teste 2  | Teste 3  | Tempo médio de execução |
|----------------|----------|----------|----------|-------------------------|
| 1 (Sequencial) | 470.199s | 448.801s | 472.850s | 463.950s                |
| 2              | 346.785s | 338.873s | 340.658s | 342.105s                |
| 4              | 306.122s | 309.812s | 311.643s | 309.192s                |
| 8              | 310.789s | 250.831s | 320.239s | 293.953s                |
| 16             | 274.106s | 276.981s | 330.463s | 293.850s                |
| 32             | 282.077s | 280.392s | 281.224s | 281.231s                |


## 4. Bibliografia
- Java Programming Language. Disponível em: <https://docs.oracle.com/javase/8/docs/technotes/guides/language/>.

