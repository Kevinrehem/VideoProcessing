public class FrameLine {
    private byte [] pixelLine;
    private int index;
    private FrameLine previous;

    public FrameLine(byte[] pixelLine, int index, FrameLine previous) {
        this.pixelLine = pixelLine;
        this.index = index;
        this.previous = previous;
    }

    public byte[] getPixelLine() {
        return pixelLine;
    }

    public int getIndex() {
        return index;
    }

    public FrameLine getPrevious() {
        return previous;
    }
}
