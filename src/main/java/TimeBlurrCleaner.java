import java.util.Vector;

public class TimeBlurrCleaner extends Thread{
    private static Vector<FrameLine> taskBag = new Vector<>();
    private static byte[][][] fixedFrames;
    private FrameLine currentLine;

    /*TODO... Aqui deve ser realizado o tratamento, linha a linha, da nossa matriz tridimensional.
    A matriz será dividida em vários vetores unidimensionais de objetos tipo FrameLine, que serão
    tratados e devolvidos como um vetor byte[]*/
    private byte[] treatLine(){
        byte[] treatedLine = new byte[currentLine.getPixelLine().length];
        return treatedLine;
    }


    /*Remove o primeiro item da task bag, entrega a uma thread, trata o frame e o coloca dentro de fixedFrames
    na posição correspondente à que foi tirado.*/
    @Override
    public void run() {
        while(!taskBag.isEmpty()){
            synchronized(taskBag){
                this.currentLine = taskBag.removeFirst();
                System.out.println(taskBag.size());
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
                FrameLine aux = new FrameLine(frames[i][j], i, j,new FrameLine(frames[i-1][j], i-1, j,null));
                taskBag.add(aux);
            }
        }
        fixedFrames = new byte[fixedFrames.length][frames[0].length][frames[0][0].length];

    }

}
