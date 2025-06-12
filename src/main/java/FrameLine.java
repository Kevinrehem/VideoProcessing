public class FrameLine {
    private byte [] pixelLine;
    private int time;
    private int index;
    private FrameLine previous;

    public FrameLine(byte[] pixelLine, int time, int lineIndex, FrameLine previous) {
        this.pixelLine = pixelLine;
        this.time = time;
        this.index = lineIndex;
        this.previous = previous;
    }

    public byte[] getPixelLine() {
        return pixelLine;
    }

    public int getTime() {
        return time;
    }

    public int getIndex() {
        return index;
    }

    public FrameLine getPrevious() {
        return previous;
    }
}
