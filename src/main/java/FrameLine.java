public class FrameLine {
    private byte [] pixelLine;
    private int time;
    private int index;
    private byte[] previous, next;

    public FrameLine(byte[] pixelLine, int time, int lineIndex, byte[] previous, byte[] next) {
        this.pixelLine = pixelLine;
        this.time = time;
        this.index = lineIndex;
        this.previous = previous;
        this.next = next;
    }

    public byte[] getNext() {
        return next;
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

    public byte[] getPrevious() {
        return previous;
    }
}
