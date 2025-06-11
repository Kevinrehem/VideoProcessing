public class Frame {
    private int index;
    private byte frame[][];

    public Frame(int index, byte[][] frame) {
        this.index = index;
        this.frame = frame;
    }

    public int getIndex() {
        return index;
    }


    public byte[][] getFrame() {
        return frame;
    }

}
