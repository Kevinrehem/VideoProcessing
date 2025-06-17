import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public class TimeBlurrCleaner extends Thread{
    private final static List<FrameLine> taskBag = new ArrayList<>();
    private static byte[][][] fixedFrames;
    private FrameLine currentLine;

    /*Nesta função é realizado o tratamento, linha a linha, da nossa matriz tridimensional.
    A matriz será dividida em vários vetores unidimensionais de objetos tipo FrameLine, que serão
    tratados e devolvidos como um vetor byte[]*/
    private byte[] treatLine(){
        byte[] treatedLine = new byte[this.currentLine.getPixelLine().length];
        for(int i = 15; i < this.currentLine.getPixelLine().length-15; i+=30){
            boolean blurr = true;
            for(int j = i-15; j < i+15; j++){
                if(this.currentLine.getPixelLine()[j] < 122 ){
                    blurr = false;
                }
            }
            if(blurr && this.currentLine.getPrevious()!=null && this.currentLine.getNext()!=null){
                for(int j = i-15; j < i+15; j++){
                    //System.out.println(this.currentLine.getPixelLine()[j] + " <-- " + this.currentLine.getPrevious().getPixelLine()[j]);
                    treatedLine[j] = calcCorrection(this.currentLine, j);
                    System.out.println(this.currentLine.getPixelLine()[j] + " <---- " + treatedLine[j]);
                }
            }else{
                for(int j = i-15; j < i+15; j++){
                    treatedLine[j] = this.currentLine.getPixelLine()[j];
                }
            }
        }
        return treatedLine;
    }

    //TODO...
    private byte calcCorrection(FrameLine frameLine, int index){
        if(frameLine.getPrevious() == null || frameLine.getNext() == null){
            return 0;
        }
        int media = 0;
        media += frameLine.getPrevious()[index];
        media += frameLine.getNext()[index];
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
        for(int i=0;i<frames.length;i++){
            for(int j=0;j<frames[i].length;j++){
                FrameLine aux;
                if(i>2 && i<frames.length-2){
                    aux = new FrameLine(frames[i][j], i, j, frames[i-2][j], frames[i+2][j]);
                }else {
                    aux = new FrameLine(frames[i][j], i, j,null, null);
                }
                taskBag.add(aux);
            }
        }
        fixedFrames = new byte[frames.length][frames[0].length][frames[0][0].length];

    }

}

//aumentar o numero de frames para frente e para tras, implementar média do futuro e passado para correção e aumento do processamento
