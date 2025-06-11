import java.util.Vector;

public class TimeBlurrCleaner extends Thread{
    private static Vector<FrameLine> taskBag = new Vector<>();

    public static void loadFrames(byte[][][] frames){
        for(int i=0;i<frames.length;i++){
            for(int j=0;j<frames[i].length;j++){
                FrameLine aux = new FrameLine(frames[i][j], i, new FrameLine(frames[i-1][j], i-1, null));
                taskBag.add(aux);
            }
        }

    }

}
