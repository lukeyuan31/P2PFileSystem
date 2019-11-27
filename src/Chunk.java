public class Chunk {
    private int chunkNum;

    //for each chunk the length is 100kb
    private int chunkLength=102400;
    private byte[] buffer;

    public Chunk(int chunkNum){
        this.chunkNum=chunkNum;
        this.buffer = new byte[chunkLength];
    }


}
