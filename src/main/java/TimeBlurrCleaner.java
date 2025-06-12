import java.util.Vector;

public class TimeBlurrCleaner extends Thread{
    private final static Vector<FrameLine> taskBag = new Vector<>();
    private static byte[][][] fixedFrames;
    private FrameLine currentLine;

    /*TODO... Aqui deve ser realizado o tratamento, linha a linha, da nossa matriz tridimensional.
    A matriz será dividida em vários vetores unidimensionais de objetos tipo FrameLine, que serão
    tratados e devolvidos como um vetor byte[]*/
    private byte[] treatLine(){
        byte[] treatedLine = new byte[this.currentLine.getPixelLine().length];
        for(int i = 20; i < this.currentLine.getPixelLine().length-20; i++){
            boolean blurr = true;
            for(int j = i-20; j < i+20; j++){
                if(this.currentLine.getPixelLine()[j] < this.currentLine.getPixelLine()[i]-3
                || this.currentLine.getPixelLine()[j] > this.currentLine.getPixelLine()[i]+3){
                    blurr = false;
                }
            }
            if(blurr && this.currentLine.getPrevious()!=null){
                for(int j = i-20; j < i+20; j++){
                    treatedLine[j] = this.currentLine.getPrevious().getPixelLine()[j];
                }
            }else{
                for(int j = i-20; j < i+20; j++){
                    treatedLine[j] = this.currentLine.getPixelLine()[j];
                }
            }
        }
        return treatedLine;
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
                if(i>0){
                    aux = new FrameLine(frames[i][j], i, j,new FrameLine(frames[i-1][j], i-1, j,null));
                }else {
                    aux = new FrameLine(frames[i][j], i, j,null);
                }
                taskBag.add(aux);
            }
        }
        fixedFrames = new byte[frames.length][frames[0].length][frames[0][0].length];

    }

}
