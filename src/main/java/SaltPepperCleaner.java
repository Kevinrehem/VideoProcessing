import java.util.*;

public class SaltPepperCleaner extends Thread {
    private static Vector<Frame> taskBag = new Vector<>(); //De onde os cores vão pegar as tarefas
    private static byte[][][] fixedFrames; //Destino final do frame corrigido
    private Frame currentFrame; //o frame que será corrigido
    private static Object key = new Object();

    //Devolve um Vector<> com todos os pixels vizinhos de um pixel cujo indice é passado como parametro
    private List<Byte> getNeighbours(byte[][] currentFrame, int i, int j) {
        List<Byte> pixels = new ArrayList<>();
        if (i > 0) {
            if (currentFrame[i - 1] != null) {
                pixels.add(currentFrame[i - 1][j]);
                if (j - 1 >= 0) {
                    pixels.add(currentFrame[i - 1][j - 1]);
                    pixels.add(currentFrame[i][j - 1]);
                }
                if (j + 1 < currentFrame[i].length && j >= 0) {
                    pixels.add(currentFrame[i - 1][j + 1]);
                    pixels.add(currentFrame[i][j + 1]);
                }
            }

        }
        if (i + 1 < currentFrame.length) {
            if (currentFrame[i + 1] != null) {
                pixels.add(currentFrame[i + 1][j]);
                if (j - 1 >= 0) pixels.add(currentFrame[i + 1][j - 1]);
                if (j + 1 < currentFrame[i].length && i > 0) pixels.add(currentFrame[i - 1][j + 1]);
            }
            Collections.sort(pixels);
            return pixels;
        }
        return pixels;
    }

    //Calcula a média baseado em uma lista de pixels
    private byte calcMedia(List<Byte> pixels) {
        if (pixels == null || pixels.isEmpty()) {
            return 0; // valor padrão
        }

        short media = 0;
        for(Byte it:pixels){
            media+=it;
        }
        media/=pixels.size();
        return (byte) media;
        /*Collections.sort(pixels);
        int size = pixels.size();
        int meio = size / 2;

        if (size % 2 == 0) {
            short mediana = (short) ((pixels.get(meio - 1) + pixels.get(meio)) / 2);
            return (byte) mediana;
        } else {
            return pixels.get(meio);
        }*/
    }


    //Método para tratar o frame atual, percorre a matriz de bytes e corrige os s
    private byte[][] treatFrame() {
        byte[][] frameResult = new byte[currentFrame.getFrame().length][currentFrame.getFrame()[0].length];
        List<Byte> neighbours = new ArrayList<>();
        for (int i = 0; i < this.currentFrame.getFrame().length; i++) {
            for (int j = 0; j < this.currentFrame.getFrame()[i].length; j++) {
                neighbours = getNeighbours(this.currentFrame.getFrame(), i, j);
                byte media = calcMedia(neighbours);
                if (this.currentFrame.getFrame()[i][j] < media - 165 || this.currentFrame.getFrame()[i][j] > media + 165){
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
                    this.currentFrame = taskBag.removeFirst();
                    System.out.println(taskBag.size());
                }
            }
            if(currentFrame!=null){
                fixedFrames[currentFrame.getIndex()] = this.treatFrame();
            }
            this.currentFrame=null;
        }
    }
}