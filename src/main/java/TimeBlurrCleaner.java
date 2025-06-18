import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public class TimeBlurrCleaner extends Thread{
    private final static List<FrameLine> taskBag = new ArrayList<>();
    private static byte[][][] fixedFrames;
    private static byte[][][] originalFrames;
    private FrameLine currentLine;

    /*Nesta função é realizado o tratamento, linha a linha, da nossa matriz tridimensional.
    A matriz será dividida em vários vetores unidimensionais de objetos tipo FrameLine, que serão
    tratados e devolvidos como um vetor byte[]*/
    private byte[] treatLine(){
        byte[] treatedLine = new byte[originalFrames[this.currentLine.getTime()][this.currentLine.getIndex()].length];
        for(int i = 15; i < originalFrames[this.currentLine.getTime()][this.currentLine.getIndex()].length-15; i++){
            boolean blurr = true;
            for(int j = i-15; j < i+15; j++){
                if(originalFrames[this.currentLine.getTime()][this.currentLine.getIndex()][j] < 122 ){
                    blurr = false;
                    break;
                }
            }
            if(blurr && this.currentLine.getPrevious(originalFrames)!=null && this.currentLine.getNext(originalFrames)!=null){
                for(int j = i-15; j < i+15; j++){
                    //System.out.println(this.currentLine.getPixelLine()[j] + " <-- " + this.currentLine.getPrevious().getPixelLine()[j]);
                    treatedLine[j] = calcCorrection(this.currentLine, j);
                    System.out.println(originalFrames[currentLine.getTime()][currentLine.getIndex()][j] + " <---- " + treatedLine[j]);
                }
            }else{
                for(int j = i-15; j < i+15; j++){
                    treatedLine[j] = originalFrames[this.currentLine.getTime()][this.currentLine.getIndex()][j];
                }
            }
        }
        return treatedLine;
    }

    //TODO...
    private byte calcCorrection(FrameLine frameLine, int index){
        if(frameLine.getPrevious(originalFrames) == null || frameLine.getNext(originalFrames) == null){
            return 0;
        }
        int media = 0;
        media += frameLine.getPrevious(originalFrames)[index];
        media += frameLine.getNext(originalFrames)[index];
        media /= 2;
        return (byte)media;

    }

    //retorna os frames corrigidos
    public static byte[][][] getFixedFrames(){
        return fixedFrames;
    }

    /*Remove o primeiro item da task bag, entrega a uma thread, trata o frame e o coloca dentro de fixedFrames
    na posição correspondente à que foi tirado.*/
    @Override
    public void run() {
        while(!taskBag.isEmpty()){
            synchronized(taskBag){
                if(!taskBag.isEmpty()){
                    this.currentLine = taskBag.removeFirst();
                    System.out.println(taskBag.size());
                }
            }
            if(this.currentLine != null){
                fixedFrames[currentLine.getTime()][currentLine.getIndex()] = this.treatLine();
            }
            this.currentLine = null;
        }
    }

    //Função para carregar todos os frames do vídeo dentro da bag of tasks
    public static void loadFrames(byte[][][] frames){
        originalFrames = frames;
        for(int i=0;i<frames.length;i++){
            for(int j=0;j<frames[i].length;j++){
                taskBag.add(new FrameLine(i, j, i-2, i+2));
            }
        }
        fixedFrames = new byte[frames.length][frames[0].length][frames[0][0].length];

    }

}

//aumentar o numero de frames para frente e para tras, implementar média do futuro e passado para correção e aumento do processamento
