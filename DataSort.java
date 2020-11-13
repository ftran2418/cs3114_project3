import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Sorter class performs replacement selection sort and multi-way merge sort to
 * produce a single run to the run file with all the records sorted in ascending
 * order
 * 
 * @author bleavitt24
 * @author kingtran
 * @version 4.22.2020
 */
public class DataSort {

    private MinHeap heap;

    private int currentBlock;
    private Reader reader;
    private byte[] input;
    private byte[] output;

    // 2D array to store the records into an input buffer for each run
    private byte[][] runInputs;

    // keeps track of the original run values
    private int[] originalRuns;

    // keeps track of the offset values
    private int[] offsets;

    // keeps track of how many merges are complete
    private int currentMergesComplete;

    // store how many merges are needed to perform
    private int numOfMerges;

    // stores how many records have been used
    private int[] numRecsUsed;

    // stores the current position in run
    private int[] currPos;

    // store how many runs are needed
    private int numRunsNeeded;

    private int runCounter;
    private int outputOffset;

    private RandomAccessFile outputFile;

    // store the original number of counts
    private ArrayList<Integer> runCounts;

    private StringBuilder builder;

    // Temp Vars
    private float max;
    private float min;
    private boolean writeToOriginal;

    /**
     * Sorter 1-arg constructor
     * 
     * @param fileName
     *            the file to sort
     * @throws IOException
     *             if error with file
     */
    public DataSort(String fileName) throws IOException {
        try {
            reader = new Reader(fileName);
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        int numBlocks = reader.numBlocks();
        currentBlock = 0;
        @SuppressWarnings("rawtypes")
        Comparable[] heapArr = new Comparable[16384];
        heap = new MinHeap(heapArr, 0, 16384);

        input = new byte[8192];
        output = new byte[8192];

        builder = new StringBuilder();
        runCounter = 0;
        outputOffset = 0;
        runCounts = new ArrayList<Integer>();

        outputFile = new RandomAccessFile("Sampledata.bin", "rw");
        max = 0;
        min = 9999999;

        // call to replacement selection sort
        fillHeap();
        while (currentBlock < numBlocks) {
            reader.getNextInput(input);
            replacementSort(0);
            currentBlock++;
        }
        emptyHeap();

        //*********************************************************************
        // get the original number of counts for indexing later on
       /* offsets = new int[8];
        numRecsUsed = new int[8];
        runInputs = new byte[8][8192];
        currPos = new int[8]; */
        
        offsets = new int[4];
        numRecsUsed = new int[4];
        runInputs = new byte[4][8192];
        currPos = new int[4];

        currentBlock = 0;
        // current Num of merges completed in this round
        max = 0;
        min = 99999999;
        writeToOriginal = true;

        // call to multi-way merge sort
        while (runCounts.size() > 1) {
            outputOffset = 0;

            // store the original runs from AList to an array
            originalRuns = new int[runCounts.size()];
            for (int i = 0; i < originalRuns.length; i++) {
                for (int j = 0; j < i; j++) {
                    originalRuns[i] += runCounts.get(j);
                }
            }

            // reset heap
            heap = new MinHeap(heapArr, 0, 16384);

            // calculate number of merges needed
            numOfMerges = (runCounts.size() - 1) / 4 + 1;
            currentMergesComplete = 0;
            ArrayList<Integer> newRunCounts = new ArrayList<Integer>();

            // update the new run counts after 1 merge has been completed
            for (int i = 0; i < numOfMerges; i++) {
                int numOfRunsNeeded = numOfMerges - 1 == i
                    ? runCounts.size() - i * 4
                    : 4;
                int sum = 0;
                for (int j = 0; j < numOfRunsNeeded; j++) {
                    sum += runCounts.get(i * 4 + j);
                }
                newRunCounts.add(sum);
            }

            // reset the file pointer
            reader.setOffset(0);
            outputFile.seek(0);
            multiWayMerge();
            runCounts = newRunCounts;

        }

        // Check if it ended in the run file
        if (writeToOriginal) {

            // Write from runFile to originalFile
            outputFile.seek(0);
            reader.setOffset(0);
            for (int i = 0; i < numBlocks; i++) {
                outputFile.read(input);
                reader.writeToFile(input);

            }
        }
        reader.setOffset(0);

        // append to string builder the specified format the spec required us to
        // print out
        for (int i = 0; i < numBlocks; i++) {
            reader.getNextInput(input);
            if (i % 5 == 0 && i != 0) {
                builder.append("\n");
            }
            Record record = new Record(Arrays.copyOfRange(input, 0, 8), 0);
            builder.append(record.toString() + " ");
        }

        System.out.println(builder.toString());

    }


    /**
     * toString method for the Sorter class
     * 
     * @return the toString representation of the records
     */
    public String sorterToString() {
        return builder.toString();
    }


    /**
     * MultiWayMerge performs multi-way merge on all the runs in the file until
     * there is a singular run left sorted in ascending order
     * 
     * @throws IOException
     *             if there are errors in the file
     */
    private void multiWayMerge() throws IOException {

        // until all runs have been exhausted of blocks/records
        while (currentMergesComplete < numOfMerges) {
            int checkShizzle = outputOffset;
            // fill heap with one block from each run
            fillHeapMerge();
            for (int i = 0; i < numRunsNeeded; i++) {
                getNextInputBlock(i, runInputs[i]);
            }
            while (heap.heapsize() > 0) {

                // removes smallest record and puts it to output buffer
                Record record = (Record)heap.removemin();
                recordToOutput(outputOffset, record);

                outputOffset += 8;

                if (record.getKeyFloat() > max) {
                    max = record.getKeyFloat();
                }
                if (record.getKeyFloat() < min) {
                    min = record.getKeyFloat();
                }

                // determine if the block has no more records left
                if (currPos[record.getFlag()] >= numRecsUsed[record.getFlag()]
                    * 8) {
                    getNextInputBlock(record.getFlag(), runInputs[record
                        .getFlag()]);

                }

                // iff output buffer is full, dump into file
                if (outputOffset == 8192) {

                    if (writeToOriginal) {
                        reader.writeToFile(output);
                    }
                    else {
                        outputFile.write(output);
                    }
                    emptyOutput(checkShizzle);
                    checkShizzle = 0;

                    outputOffset = 0;
                }

                // if the run still has records left, insert them into the heap
                if (numRecsUsed[record.getFlag()] != 0) {
                    Record ins = new Record(Arrays.copyOfRange(runInputs[record
                        .getFlag()], currPos[record.getFlag()], currPos[record
                            .getFlag()] + 8), record.getFlag());
                    // System.out.println(ins.getKeyDouble());
                    heap.insert(ins);
                    currPos[record.getFlag()] += 8;
                }
            }

            currentMergesComplete++;
        }

        writeToOriginal = !writeToOriginal;
    }


    /**
     * Private method to get the next block for the input buffer
     * 
     * @param flag
     *            the flag of the record
     * @param inputArr
     *            the input buffer
     */
    private void getNextInputBlock(int flag, byte[] inputArr) {
        try {

            // reading from output file and loading it into the buffer
            if (writeToOriginal) {
                outputFile.seek(originalRuns[currentMergesComplete * 4 + flag]
                    * 8 + offsets[flag]);
                outputFile.read(inputArr);
            }
            else {
                reader.setOffset(originalRuns[currentMergesComplete * 4 + flag]
                    * 8 + offsets[flag]);
                reader.getNextInput(inputArr);
            }

            int numBytesToGet = (runCounts.get(currentMergesComplete * 4 + flag)
                * 8) - offsets[flag] < 8192
                    ? (runCounts.get(currentMergesComplete * 4 + flag) * 8)
                        - offsets[flag]
                    : 8192;
            numRecsUsed[flag] = numBytesToGet / 8;
            currPos[flag] = 0;
            offsets[flag] += numBytesToGet;

        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * Fill heap with one block from every run (at most 8 runs)
     */
    private void fillHeapMerge() {

        try {

            // find out how many runs are needed to perform sort on
            numRunsNeeded = numOfMerges - 1 == currentMergesComplete
                ? runCounts.size() - currentMergesComplete * 4
                : 4;

            for (int j = 0; j < numRunsNeeded; j++) {

                // determine what file to read from
                if (writeToOriginal) {
                    outputFile.seek(originalRuns[currentMergesComplete * 4 + j]
                        * 8);
                    outputFile.read(runInputs[j]);
                }
                else {
                    reader.setOffset(originalRuns[currentMergesComplete * 4 + j]
                        * 8);
                    reader.getNextInput(runInputs[j]);
                }
                int numBytesToGet = runCounts.get(currentMergesComplete * 4 + j)
                    * 8 < 8192
                        ? runCounts.get(currentMergesComplete * 4 + j) * 8
                        : 8192;
                for (int i = 0; i + 8 <= numBytesToGet; i += 8) {
                    heap.insert(new Record(Arrays.copyOfRange(runInputs[j], i, i
                        + 8), j));
                }
                offsets[j] = numBytesToGet;

                numRecsUsed[j] = numBytesToGet / 8;
                currentBlock++;
            }

        }
        catch (IOException e) {
            e.printStackTrace();
        }

    }


    /**
     * Private method to fill the heap
     */
    private void fillHeap() {
        for (int i = 0; i < 16; i++) {
            try {
                reader.getNextInput(input);
            }
            catch (IOException e) {
                e.printStackTrace();
            }
            for (int j = 0; j + 8 <= input.length; j = j + 8) {
                
                if(j == 8186) {
                    System.out.println("HELLO");
                }
                heap.insert(new Record(Arrays.copyOfRange(input, j, j + 8),
                    0));

            }

        }
        currentBlock += 16;

    }


    /**
     * Private method to handle replacement selection sort, will write to output
     * file all the sorted runs in the file
     * 
     * @param inputOffset
     *            the input offset
     * @throws IOException
     *             if file has errors
     */
    private void replacementSort(int inputOffset) throws IOException {
        int currInputOffset = inputOffset;

        // perform the replacement selection sort if these conditions are still
        // valid
        while (inputOffset + 8 <= input.length && heap.heapMaxSize() > 0) {
            Record record = (Record)heap.removemin();
            if (record.getKeyFloat() > max) {
                max = record.getKeyFloat();
            }
            if (record.getKeyFloat() < min) {
                min = record.getKeyFloat();
            }

            // put the smallest value record to the output buffer
            recordToOutput(outputOffset, record);
            outputOffset += 8;
            runCounter++;

            // determine if need to handle for the special case to hide values
            Record inputRecord = new Record(Arrays.copyOfRange(input,
                inputOffset, inputOffset + 8), 0);
            boolean hideInput = false;
            for (int j = currInputOffset; j < outputOffset; j = j + 8) {
                Record outputRecord = new Record(Arrays.copyOfRange(output, j, j
                    + 8), 0);
                if (inputRecord.compareTo(outputRecord) < 0) {
                    hideInput = true;
                    break;
                }
            }

            // insert record into the heap and also hide it if hideInput is true
            // for special case
            heap.insert(inputRecord);
            if (hideInput) {
                heap.hideMin();
            }
            inputOffset += 8;
        }

        // if output buffer is full dump to output file
        if (outputOffset == 8192) {
            outputFile.write(output);
            emptyOutput(currInputOffset);
            outputOffset = 0;
        }

        // if heap size is 0, create a new heap and call replacementSort again
        else if (heap.heapMaxSize() == 0) {
            heap = new MinHeap(heap.getArr(), 16384, 16384);
            runCounts.add(runCounter);
            runCounter = 0;
            replacementSort(inputOffset);
        }
    }


    /**
     * Stores the record to the output buffer
     * 
     * @param offset
     *            the offset to store at
     * @param record
     *            the record to store
     */
    private void recordToOutput(int offset, Record record) {
        byte[] temp = record.getAll();
        for (int i = 0; i < temp.length; i++) {
            output[offset + i] = temp[i];
        }
    }


    /**
     * Empties the heap and writes it to the output file and handles hidden
     * values as well from replacement selection sort
     * 
     * @throws IOException
     *             if file contains errors
     */
    private void emptyHeap() throws IOException {
        int hiddenVals = 16384 - heap.heapMaxSize();

        // add to runCounts AList to store number of records for current run
        runCounts.add(heap.heapMaxSize() + runCounter);
        if (hiddenVals != 0) {
            runCounts.add(hiddenVals);
        }

        // Known that output Buffer is empty since there are no more inputs and
        // the Heap has exactly 8 blocks of Records
        outputOffset = 0;
        while (heap.heapMaxSize() > 0) {
            Record record = (Record)heap.removemin();
            if (record.getKeyFloat() > max) {
                max = record.getKeyFloat();
            }
            if (record.getKeyFloat() < min) {
                min = record.getKeyFloat();
            }
            recordToOutput(outputOffset, record);
            heap.decrementMaxSize();
            outputOffset += 8;
            if (outputOffset == 8192) {
                emptyOutput(0);
                outputOffset = 0;
                outputFile.write(output);
            }
        }

        // dealing with hidden values and print them to output file
        heap.swapVals(hiddenVals);
        heap = new MinHeap(heap.getArr(), hiddenVals, 16384);
        int currentOutputOffset = outputOffset;
        while (heap.heapsize() > 0) {
            Record record = (Record)heap.removemin();
            if (record.getKeyFloat() > max) {
                max = record.getKeyFloat();
            }
            if (record.getKeyFloat() < min) {
                min = record.getKeyFloat();
            }
            recordToOutput(outputOffset, record);
            outputOffset += 8;

            // dump if output offset is full
            if (outputOffset == 8192) {
                emptyOutput(currentOutputOffset);
                outputOffset = 0;
                outputFile.write(output);
            }
        }
    }


    /**
     * Private method to check if the records were printed in order for each
     * run, if not, display "BUG"
     * 
     * @param inputOffset
     *            the inputOffset
     */
    private void emptyOutput(int inputOffset) {
        // CURRENTLY ONLY TESTS
        for (int i = 0; i < inputOffset - 8; i = i + 8) {
            Record lower = new Record(Arrays.copyOfRange(output, i, i + 8), 0);
            Record upper = new Record(Arrays.copyOfRange(output, i + 8, i
                + 16), 0);
            if (upper.getKeyFloat() - lower.getKeyFloat() < 0) {
                System.out.println("BUGBUGBUGBUGBUGBUGBUGBUGBUGBUGBUGBUGBUG");
            }
        }
        for (int i = inputOffset; i < output.length - 8; i = i + 8) {
            Record lower = new Record(Arrays.copyOfRange(output, i, i + 8), 0);
            Record upper = new Record(Arrays.copyOfRange(output, i + 8, i
                + 16), 0);
            if (upper.getKeyFloat() - lower.getKeyFloat() < 0) {
                System.out.println("BUGBUGBUGBUGBUGBUGBUGBUGBUGBUGBUGBUGBUG");
            }
        }
    }
}
