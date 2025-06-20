
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.VideoWriter;
import org.opencv.videoio.Videoio;

public class VideoProcessing {

    /* Carrega a biblioteca nativa (via nu.pattern.OpenCV) assim que a classe é carregada na VM. */
    static {
        nu.pattern.OpenCV.loadLocally();
    }

    public static byte[][][] carregarVideo(String caminho) {
        System.out.println("Carregando o vídeo... " + caminho);

        VideoCapture captura = new VideoCapture(caminho);
        if (!captura.isOpened()) {
            System.out.println("Vídeo está sendo processado por outra aplicação");
        }
        
        //tamanho do frame
        int largura = (int) captura.get(Videoio.CAP_PROP_FRAME_WIDTH);
        int altura = (int) captura.get(Videoio.CAP_PROP_FRAME_HEIGHT);

        //não conhecço a quantidade dos frames (melhorar com outra lib) :(
        List<byte[][]> frames = new ArrayList<>();
           
        //matriz RGB mesmo preto e branco?? - uso na leitura do frame
        Mat matrizRGB = new Mat();
        
        //criando uma matriz temporária em escala de cinza
        Mat escalaCinza = new Mat(altura, largura, CvType.CV_8UC1); //1 única escala
        byte linha[] = new byte[largura];

        while (captura.read(matrizRGB)) {//leitura até o último frames
            
            //convertemos o frame atual para escala de cinza
            Imgproc.cvtColor(matrizRGB, escalaCinza, Imgproc.COLOR_BGR2GRAY);

            //criamos uma matriz para armazenar o valor de cada pixel (int estouro de memória)
            byte pixels[][] = new byte[altura][largura];
            for (int y = 0; y < altura; y++) {
                escalaCinza.get(y, 0, linha);
                for (int x = 0; x < largura; x++) {
                    pixels[y][x] = (byte)(linha[x] & 0xFF); //shift de correção - unsig
                }
            }
            frames.add(pixels);
        }
        captura.release();

        /* converte o array de frames em matriz 3D */
        byte cuboPixels[][][] = new byte[frames.size()][][];
        for (int i = 0; i < frames.size(); i++) {
            cuboPixels[i] = frames.get(i);
        }

        System.out.printf("Frames: %d   Resolução: %d x %d \n",
                cuboPixels.length, cuboPixels[0][0].length, cuboPixels[0].length);
        
        return cuboPixels;
    }

    public static void gravarVideo(byte pixels[][][],
            String caminho,
            double fps) {

        int qFrames = pixels.length;
        int altura = pixels[0].length;
        int largura = pixels[0][0].length;

        int fourcc = VideoWriter.fourcc('a', 'v', 'c', '1');   // identificação codec .mp4
        VideoWriter escritor = new VideoWriter(
                caminho, fourcc, fps, new Size(largura, altura), true);

        if (!escritor.isOpened()) {
            System.err.println("Erro ao gravar vídeo no caminho sugerido");
        }

        Mat matrizRgb = new Mat(altura, largura, CvType.CV_8UC3); //voltamos a operar no RGB (limitação da lib)
        
        byte linha[] = new byte[largura * 3];                // BGR intercalado

        for (int f = 0; f < qFrames; f++) {
            for (int y = 0; y < altura; y++) {
                for (int x = 0; x < largura; x++) {
                    byte g = (byte) pixels[f][y][x];
                    int i = x * 3;
                    linha[i] = linha[i + 1] = linha[i + 2] = g;     // cinza → B,G,R
                }
                matrizRgb.put(y, 0, linha);
            }
            escritor.write(matrizRgb);
        }
        escritor.release(); //limpando o buffer 
    }

    //chama os métodos da classe SaltPepperCleaner para iniciar tratamento do erro sal e pimenta
    public static byte[][][] removerSalPimenta(byte pixels[][][], int cores){
        /*System.out.println("Enter para continuar...");
        new Scanner(System.in).nextLine();*/
        System.out.println("Processamento remove ruído 1...");
        SaltPepperCleaner.loadFrames(pixels);


        SaltPepperCleaner vetCores[] = new SaltPepperCleaner[cores];
        for(int i = 0; i < vetCores.length; i++){
            vetCores[i] = new SaltPepperCleaner();
            vetCores[i].start();
        }

        for(SaltPepperCleaner it:vetCores){
            try {
                it.join();
            } catch (InterruptedException e) {
                System.err.println("Interrompido.");
            }
        }


        return SaltPepperCleaner.getFixedFrames();
    }

    //Chama os métodos da classe TimeBlurrCleaner para iniciar o tratamento do erro de borrões de tempo
    public static byte[][][] removerBorroesTempo(byte pixels[][][], int cores){

        /*System.out.println("Enter para continuar...");
        new Scanner(System.in).nextLine();*/
        System.out.println("Processamento remove ruído 2...");

        TimeBlurrCleaner.loadFrames(pixels);
        TimeBlurrCleaner vetCores[] = new TimeBlurrCleaner[cores];

        for(int i = 0; i < vetCores.length; i++){
            vetCores[i] = new TimeBlurrCleaner();
            vetCores[i].start();
        }

        for( TimeBlurrCleaner it:vetCores){
            try {
                it.join();
            } catch (InterruptedException e) {
                System.err.println("Interrompido.");
            }
        }


        System.out.println("Salvando...");
        return TimeBlurrCleaner.getFixedFrames();
    }

    public static void main(String[] args) {

        String caminhoVideo = "lib\\video.mp4";
        String caminhoGravar = "lib\\video-clean.mp4";
        double fps = 24.0; //isso deve mudar se for outro vídeo (avaliar metadados ???)
        System.out.print("Cores: ");
        int cores = new Scanner(System.in).nextInt();
        long start = System.currentTimeMillis();
        gravarVideo(removerBorroesTempo(removerSalPimenta(carregarVideo(caminhoVideo), cores), cores), caminhoGravar, fps);
        long end = System.currentTimeMillis();
        System.out.println("Término do processamento");
        System.out.println("Tempo de execução: " + (double)(end - start)/1000 + "s");
        System.out.println("Cores: " + cores);
    }
}
