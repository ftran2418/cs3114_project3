import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * Parser class parses and stores the bytes using RAF
 * 
 * @author kingtran
 * @author bleavitt24
 * @version 4.25.2020
 */
public class Reader {

    // RAF can use seek, read, getLength(), getCurrentFilePosition
    private MinHeap heap;
    private RandomAccessFile file;
    private final int blockSizeBytes = 8192;
    private long currOffset;

    /**
     * Parser 1-arg constructor
     * @param fileName the file to be parsed
     * @throws FileNotFoundException if file not found
     */
    public Reader(String fileName) throws FileNotFoundException {
        // psuedocode for heap
        String args = fileName;
        file = new RandomAccessFile(args, "rw");
        currOffset = 0;

    }

    /**
     * Gets the number of blocks that are in the file
     * 
     * @return the number of blocks in the file
     * @throws IOException if file is missing, corrupt, etc
     */
    public int numBlocks() throws IOException {
        int len = (int)((file.length()) / (blockSizeBytes));
        return len;
    }


    /**
     * Gets the next input based on the offset of the file
     * 
     * @param buffer
     *            the buffer to fill
     * @return byte[] with one block of values in it to input
     * @throws IOException
     *             if file is not found
     */
    public byte[] getNextInput(byte[] buffer) throws IOException {
        // byte[] buffer = new byte[8192];
        file.seek(currOffset);
        file.read(buffer);
        currOffset += buffer.length;
        return buffer;
    }

    /**
     * Set the offset of the file pointer
     * 
     * @param offset the offset to be sit to
     */
    public void setOffset(int offset) {
        currOffset = offset;
        try {
            file.seek(offset);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Fills the input buffers for multi-way merge
     * 
     * @param buffer the buffer 
     * @param offset the offset for the file pointer
     * @return the filled buffer
     * @throws IOException if error in file
     */
    public byte[] fillMergeInputBuffers(byte[] buffer, int offset)
        throws IOException {
        file.seek(offset);
        file.read(buffer);
        return buffer;
    }


    /**
     * Writes the output buffer to the file
     * 
     * @param output the output buffer
     */
    public void writeToFile(byte[] output) {
        try {
            file.seek(currOffset);
            file.write(output);
            currOffset += output.length;
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * removes all Records in heap until heap is empty or new output buffer is
     * full
     * (Use if only 1 run in file (8 blocks))
     * 
     * @return the new Output buffer
     */
    public byte[] removeAllHeap() {
        byte[] outputBuff = new byte[8192];
        for (int i = 0; i < outputBuff.length && heap.heapsize() > 0; i = i
            + 8) {
            byte[] next = ((Record)heap.removemin()).getAll();
            for (int j = 0; j < next.length; j++) {
                outputBuff[i + j] = next[j];
            }
        }
        return outputBuff;
    }

}
