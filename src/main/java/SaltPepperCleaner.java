import java.util.ArrayList;
import java.util.List;

public class SaltPepperCleaner extends Thread{
    private static List<byte[][]> taskBag = new ArrayList<>();

    public static void loadFrames(byte frames[][][]){
        for(int i=0; i< frames.length; i++){
            taskBag.add(frames[i]);
        }
    }
}
