import java.sql.SQLOutput;
import java.util.*;

public class SaltPepperCleaner extends Thread {
    private static Vector<byte[][]> taskBag = new Vector<>(); //De onde os cores vão pegar as tarefas
    private static Vector<byte[][]> fixedFrames = new Vector<>(); //Destino final do frame corrigido
    private byte[][] currentFrame; //o frame que será corrigido
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

    //Calcula a mediana baseado em uma lista de pixels
    private byte calcMediana(List<Byte> pixels) {
        if (pixels == null || pixels.isEmpty()) {
            return 0; // valor padrão
        }

        Collections.sort(pixels);
        int size = pixels.size();
        int meio = size / 2;

        if (size % 2 == 0) {
            return (byte) ((pixels.get(meio - 1) + pixels.get(meio)) / 2);
        } else {
            return pixels.get(meio);
        }
    }


    //Método para tratar o frame atual, percorre a matriz de bytes e corrige os s
    private byte[][] treatFrame() {
        byte[][] frameResult = new byte[currentFrame.length][currentFrame[0].length];
        List<Byte> neighbours = new ArrayList<>();
        for (int i = 0; i < this.currentFrame.length; i++) {
            for (int j = 0; j < this.currentFrame[i].length; j++) {
                neighbours = getNeighbours(this.currentFrame, i, j);
                byte mediana = calcMediana(neighbours);
                if (this.currentFrame[i][j] < mediana - 24 || this.currentFrame[i][j] > mediana + 24){
                   frameResult[i][j] = mediana;
                }else {
                    frameResult[i][j] = this.currentFrame[i][j];
                }
            }
        }
        return frameResult;
    }


    //Carrega todos os frames para dentro de uma List
    public static void loadFrames(byte frames[][][]) {
        for (int i = 0; i < frames.length; i++) {
            taskBag.add(frames[i]);
        }
    }

    //converte vector de frames tratados em matriz tridimensional e retorna essa matriz
    public static byte[][][] getFixedFrames(){
        byte fixedVideo[][][] = new byte[fixedFrames.size()][fixedFrames.get(0).length][fixedFrames.get(0)[0].length];
        for (int i = 0; i < fixedVideo.length; i++){
            fixedVideo[i] = fixedFrames.get(i);
        }
        return fixedVideo;
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
                fixedFrames.add(this.treatFrame());
            }
            this.currentFrame=null;
        }
    }
}
