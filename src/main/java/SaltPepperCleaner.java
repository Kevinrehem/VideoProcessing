import java.text.DecimalFormat;
import java.util.*;

public class SaltPepperCleaner extends Thread {
    private static final List<Frame> taskBag = new ArrayList<>(); //De onde os cores vão pegar as tarefas
    private static byte[][][] fixedFrames; //Destino final do frame corrigido
    private static int originalBagSize;
    private Frame currentFrame; //o frame que será corrigido

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

    //Calcula a média baseado em uma lista de pixels
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


    //Carrega todos os frames para dentro de uma List
    public static void loadFrames(byte frames[][][]) {
        for (int i = 0; i < frames.length; i++) {
            taskBag.add(new Frame(i, frames[i]));
        }
        originalBagSize=taskBag.size();
        fixedFrames = new byte[frames.length][frames[0].length][frames[0][0].length];
    }

    //converte vector de frames tratados em matriz tridimensional e retorna essa matriz
    public static byte[][][] getFixedFrames(){
        return fixedFrames;
    }

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
}