import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class SaltPepperCleaner extends Thread {
    private static List<byte[][]> taskBag = new ArrayList<>(); //De onde os cores vão pegar as tarefas
    private static List<byte[][]> fixedFrames = new ArrayList<>(); //Destino final do frame corrigido
    private byte[][] currentFrame; //o frame que será corrigido
    private static Object key = new Object();

    //Devolve uma List<> com todos os pixels vizinhos de um pixel cujo indice é passado como parametro
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

    /*private byte calcMediana(List<Byte> pixels) {

        byte mediana = 0;
        if(!pixels.isEmpty()) {
            int meio = pixels.size() / 2;
            meio--;

            if (pixels.size() % 2 == 0) {
                mediana = (byte) ((pixels.get(meio) + pixels.get(meio + 1)) / 2);
            } else {
                mediana = (byte) (pixels.get(meio));
            }
        }
        return mediana;
    }*/

    //Método para tratar o frame atual, percorre a matriz de bytes e corrige os s
    private byte[][] treatFrame() {
        byte[][] frameResult = new byte[currentFrame.length][currentFrame[0].length];
        this.currentFrame = taskBag.removeFirst();
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

    @Override
    public void run() {
        this.treatFrame();
    }
}
