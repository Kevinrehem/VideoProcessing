import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class SaltPepperCleaner extends Thread{
    private static List<byte[][]> taskBag = new ArrayList<>();
    private static List<byte[][]> fixedFrames = new ArrayList<>();
    private byte[][] currentFrame;

    private List<Byte> getNeighbours(byte[][] currentFrame, int i, int j){
        List<Byte> pixels = new ArrayList<>();
        if(currentFrame[i-1] != null){
            pixels.add(currentFrame[i-1][j]);
            if(j-1>=0){
                pixels.add(currentFrame[i-1][j-1]);
                pixels.add(currentFrame[i][j-1]);
            }
            if(j+1<currentFrame[i].length){
                pixels.add(currentFrame[i-1][j+1]);
                pixels.add(currentFrame[i][j+1]);
            }
        }
        if(currentFrame[i+1] != null){
            pixels.add(currentFrame[i+1][j]);
            if(j-1>=0) pixels.add(currentFrame[i+1][j-1]);
            if(j+1<currentFrame[i].length) pixels.add(currentFrame[i-1][j+1]);
        }
        Collections.sort(pixels);

        return pixels;
    }

    private byte calcMediana(List<Byte> pixels){
        byte mediana;
        int meio = pixels.size()/2;
        meio--;
        if(pixels.size()%2==0){
            mediana = (byte) ((pixels.get(meio)+pixels.get(meio+1))/2);
        }else{
            mediana = (byte) (pixels.get(meio));
        }
        return mediana;
    }

    private void treatFrame(){
        this.currentFrame = taskBag.removeFirst();
        List<Byte> pixels = new ArrayList<>();
        for (int i = 0; i< this.currentFrame.length; i++){
            for(int j = 0; j< this.currentFrame[i].length; j++){
                pixels = getNeighbours(this.currentFrame, i, j);
                byte mediana = calcMediana(pixels);
                if(this.currentFrame[i][j] < mediana-24 || this.currentFrame[i][j] > mediana+24) this.currentFrame[i][j] = mediana;
            }
        }
    }



    public static void loadFrames(byte frames[][][]){
        for(int i=0; i< frames.length; i++){
            taskBag.add(frames[i]);
        }
    }



}
