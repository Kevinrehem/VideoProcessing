public class FrameLine {
    private byte [] pixelLine;
    private int time;
    private int index;
    private FrameLine previous, next;

    public FrameLine(byte[] pixelLine, int time, int lineIndex, FrameLine previous, FrameLine next) {
        this.pixelLine = pixelLine;
        this.time = time;
        this.index = lineIndex;
        this.previous = previous;
        this.next = next;
    }

    public FrameLine getNext() {
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

    public FrameLine getPrevious() {
        return previous;
    }
}
