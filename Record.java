import java.nio.ByteBuffer;

/**
 * Record class to store and keep track of all the records in the file
 * 
 * @author aaronn
 * @author ftbaohan
 * @version
 */
public class Record implements Comparable<Record> {

    private byte[] key;
    private byte[] data;
    private int flag;


    /**
     * Record 2-arg constructor
     * 
     * @param rec
     *            the record to be assessed
     * @param flag
     *            what run the record came from
     */
    public Record(byte[] rec, int flag) {
        key = new byte[4];
        data = new byte[4];
        for (int i = 0; i < 4; i++) {
            data[i] = rec[i];
            key[i] = rec[i + 4];
        }
        this.flag = flag;
    }


    /**
     * Getter method for the flag of the record
     * 
     * @return flag the record's flag
     */
    public int getFlag() {
        return flag;
    }


    /**
     * Setter method for the flag of the record
     * 
     * @param newFlag
     *            the new flag
     */
    public void setFlag(int newFlag) {
        flag = newFlag;
    }


    /**
     * Getter method for the key
     * 
     * @return the key
     */
    public byte[] getKey() {
        return key;
    }


    /**
     * Setter method for key
     * 
     * @param newKey
     *            the value to set key to
     */
    public void setKey(byte[] newKey) {
        key = newKey;
    }


    /**
     * Getter method for the data
     * 
     * @return the record ID data
     */
    public byte[] getData() {
        return data;
    }


    /**
     * Setter method for data
     * 
     * @param newData
     *            the new record ID data
     */
    public void setData(byte[] newData) {
        data = newData;
    }


    /**
     * Gets the key and the data in a single array
     * 
     * @return all the bytes in the record
     */
    public byte[] getAll() {
        byte[] temp = new byte[8];
        for (int i = 0; i < 4; i++) {
            temp[i] = data[i];
            temp[i + 4] = key[i];
        }
        return temp;
    }


    /**
     * Gets float for key bytes
     * 
     * @return float value for the key
     */
    public float getKeyFloat() {
        return ByteBuffer.wrap(key).getFloat();
    }
    
    /**
     * Setter method for data and key
     * 
     * @param newRec
     *            the new record to be set
     */
    public void setAll(byte[] newRec) {
        for (int i = 0; i < 4; i++) {
            data[i] = newRec[i];
            key[i] = newRec[i + 4];
        }
    }


    /**
     * Gets int for record ID
     * 
     * @return int value for the record ID
     */
    public int getDataInt() {
        return ByteBuffer.wrap(data).getInt();
    }



    /**
     * Compare method for records
     * 
     * @param other
     *            the other Record to be compared to
     * @return return 0 if equal, -1 if other is greater, and 1 if the this is
     *         greater
     */
    @Override
    public int compareTo(Record other) {
        
        /*
         * if (ByteBuffer.wrap(key).getDouble() - ByteBuffer.wrap(other.getKey())
            .getDouble() > 0) {
            return 1;
        }
        if (ByteBuffer.wrap(key).getDouble() - ByteBuffer.wrap(other.getKey())
            .getDouble() < 0) {
            return -1;
        }
        return 0;
        */
        
        if (ByteBuffer.wrap(key).getFloat() - ByteBuffer.wrap(other.getKey())
            .getFloat() == 0) {
            return 0;
        }
        if (ByteBuffer.wrap(key).getFloat() - ByteBuffer.wrap(other.getKey())
            .getFloat() < 0) {
            return -1;
        }
        return 1;
    }


    /**
     * toString method for the Record class
     * 
     * @return the toString representation of a record
     */
    public String toString() {
        return getDataInt() + " " + getKeyFloat();
    }
}
