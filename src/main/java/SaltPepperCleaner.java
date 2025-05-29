import java.util.ArrayList;
import java.util.List;

public class SaltPepperCleaner extends Thread{
    private static List<byte[][]> taskBag = new ArrayList<>();
    private static List<byte[][]> fixedFrames = new ArrayList<>();
    private byte[][] currentFrame;

    private void treatFrame(){
        this.currentFrame = taskBag.removeFirst();
        for (int i = 0; i< currentFrame.length; i++){
            for(int j = 0; j< currentFrame[i].length; j++){
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

            }
        }
    }



    public static void loadFrames(byte frames[][][]){
        for(int i=0; i< frames.length; i++){
            taskBag.add(frames[i]);
        }
    }



}
