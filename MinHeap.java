
/**
 * The MinHeap class implements a min-heap data structure for usage to store
 * data for the purposes of replacement selection sort and multi-way merge sort.
 * 
 * @author bleavitt24
 * @author kingtran
 * @version 4.22.2020
 */
public class MinHeap {
    @SuppressWarnings("rawtypes")
    private Comparable[] heap; // Pointer to the heap array
    private int size; // maximum size of the heap
    private int n; // Number of things now in heap


    /**
     * MinHeap 3-arg constructor
     * 
     * @param h
     *            the pointer to the heap array
     * @param num
     *            the number of things in the heap
     * @param max
     *            the maximum size of the heap
     */
    @SuppressWarnings("rawtypes")
    public MinHeap(Comparable[] h, int num, int max) {
        heap = h;
        n = num;
        size = max;
        buildheap();
    }


    /**
     * Gets the size of the heap
     * 
     * @return n the heap size
     */
    public int heapsize() {
        return n;
    }


    /**
     * Returns the maximum size of the heap
     * 
     * @return size the max heap size
     */
    public int heapMaxSize() {
        return size;
    }


    /**
     * Returns true if the current position is a leaf in the heap
     * 
     * @param pos
     *            the current position
     * @return true if the current position in the leaf is a heap, false if not
     */
    private boolean isLeaf(int pos) {
        return (pos >= n / 2) && (pos < n);
    }


    /**
     * Returns the position of the left child of the current position
     * 
     * @param pos
     *            the current position
     * @return the position of the left child of the current position
     */
    private int leftchild(int pos) {
        if (pos >= n / 2) {
            return -1;
        }
        return 2 * pos + 1;
    }



    /**
     * Returns the position of the parent of the current position
     * 
     * @param pos
     *            the current position
     * @return the position of the parent of the current position
     */
    private int parent(int pos) {
        if (pos <= 0) {
            return -1;
        }
        return (pos - 1) / 2;
    }


    /**
     * Insert method for the MinHeap
     * 
     * @param key
     *            the type of the MinHeap to compare with
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void insert(Comparable key) {
        if (n >= size) {
            System.out.println("Heap is full");
            // throw new Exception();
            return;
        }
        int curr = n++;
        heap[curr] = key; // Start at end of heap
        // Now sift up until curr's parent's key > curr's key
        while ((curr != 0) && (heap[curr].compareTo(heap[parent(curr)]) < 0)) {
            swap(curr, parent(curr));
            curr = parent(curr);
        }
    }


    /**
     * Heapify the contents of the heap
     */
    private void buildheap() {
        // if (n != 0) {
        for (int i = n / 2 - 1; i >= 0; i--) {
            siftdown(i);
        }
        // }
    }


    /**
     * Puts an element in the heap to the correct place
     * 
     * @param pos
     *            the current position
     */
    @SuppressWarnings("unchecked")
    private void siftdown(int pos) {
        if ((pos < 0) || (pos >= n)) {
            return; // Illegal position
        }
        while (!isLeaf(pos)) {
            int j = leftchild(pos);
            if ((j < (n - 1)) && (heap[j].compareTo(heap[j + 1]) > 0)) {
                j++; // j is now index of child with greater value
            }
            if (heap[pos].compareTo(heap[j]) <= 0) {
                return;
            }
            swap(pos, j);
            pos = j; // Move down
        }
    }


    /**
     * Remove and return the minimum value
     * 
     * @return the minimum value in the heap
     */
    @SuppressWarnings("rawtypes")
    public Comparable removemin() {
        if (n == 0) {
            return -1; // Removing from empty heap
        }
        swap(0, --n); // Swap minimum with last value
        siftdown(0); // Put new heap root val in correct place
        return heap[n];
    }


    /**
     * Remove and return the value at the specified position
     * 
     * @param pos
     *            the current position
     * @return the value at the current position
     */
    @SuppressWarnings("rawtypes")
    public Comparable remove(int pos) {
        if ((pos < 0) || (pos >= n)) {
            return -1; // Illegal heap position
        }
        if (pos == (n - 1)) {
            n--; // Last element, no work to be done
        }
        else {
            swap(pos, --n); // Swap with last value
            update(pos);
        }
        return heap[n];
    }


    /**
     * Modify the value at the current position
     * 
     * @param pos
     *            the current position
     * @param newVal
     *            the new value
     */
    @SuppressWarnings("rawtypes")
    public void modify(int pos, Comparable newVal) {
        if ((pos < 0) || (pos >= n)) {
            return; // Illegal heap position
        }
        heap[pos] = newVal;
        update(pos);
    }


    /**
     * The value at pos has been changed, restore the heap property
     * 
     * @param pos
     *            the current position
     */
    @SuppressWarnings("unchecked")
    private void update(int pos) {
        // If it is a big value, push it up
        while ((pos > 0) && (heap[pos].compareTo(heap[parent(pos)]) < 0)) {
            swap(pos, parent(pos));
            pos = parent(pos);
        }
        siftdown(pos); // If it is little, push down
    }


    /**
     * Swap method for the heap
     * 
     * @param pos1
     *            this position
     * @param pos2
     *            other position
     */
    @SuppressWarnings("rawtypes")
    private void swap(int pos1, int pos2) {
        Comparable temp = heap[pos1];
        heap[pos1] = heap[pos2];
        heap[pos2] = temp;
    }


    /**
     * Returns the heap array
     * 
     * @return the heap array
     */
    @SuppressWarnings("rawtypes")
    public Comparable[] getArr() {
        return heap;
    }


    /**
     * To string method for the heap
     * 
     * @return the toString of the heap
     */
    public String toString() {
        StringBuilder builder = new StringBuilder();
        int currLevel = 0;
        for (int i = 0; i < n; i++) {
            if (i + 1 == Math.pow(2, currLevel + 1)) {
                currLevel++;
                builder.append("\n");
            }
            builder.append(heap[i] + " ");
        }
        return builder.toString();
    }


    /**
     * Hide the minimum value for replacement selection sort
     */
    public void hideMin() {
        swap(0, n - 1);
        n--;
        size--;
        siftdown(0);
    }


    /**
     * Decrement the maximum heap size for replacement selection sort
     */
    public void decrementMaxSize() {
        if (n < size) {
            size--;
        }
    }


    /**
     * Get the minimum value in the heap
     * 
     * @return the minimum value in the heap
     */
    @SuppressWarnings("rawtypes")
    public Comparable getMin() {
        return heap[0];
    }


    /**
     * Swaps the smallest value in the heap with the last value in the heap for
     * replacement selection srot
     * 
     * @param numVals
     *            the current value in the heap
     */
    public void swapVals(int numVals) {
        for (int i = 0; i < numVals; i++) {
            swap(i, heap.length - i - 1);
        }
    }
}
