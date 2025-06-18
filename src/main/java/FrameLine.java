public class FrameLine {
    private int time;
    private int index;
    private final int previous, next;

    public FrameLine(int time, int lineIndex, int previous, int next) {
        this.time = time;
        this.index = lineIndex;
        this.previous = previous;
        this.next = next;
    }

    public byte[] getNext(byte[][][] allFrames) {
        return (next< allFrames.length) ? allFrames[this.next][this.index] : null;
    }

    public int getTime() {
        return time;
    }

    public int getIndex() {
        return index;
    }

    public byte[] getPrevious(byte[][][] allFrames) {
        return (previous>=0) ? allFrames[this.previous][this.index] : null;
    }
}
