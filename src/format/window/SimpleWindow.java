package format.window;

import gnu.trove.list.array.TIntArrayList;

import java.util.Arrays;

public class SimpleWindow {
    //incluside
    int[] starts = null;
    //exclusive
    int[] ends = null;
    double[] windowValues = null;
    int windowSize = Integer.MIN_VALUE;
    int windowStep = Integer.MIN_VALUE;
    int chrLength = Integer.MIN_VALUE;

    public SimpleWindow (int chrLength, int windowSize, int windowStep) {
        this.chrLength = chrLength;
        this.windowSize = windowSize;
        this.windowStep = windowStep;
        this.initialize();
    }

    public SimpleWindow (int chrLength, int windowSize) {
        this.chrLength = chrLength;
        this.windowSize = windowSize;
        this.windowStep = windowSize;
        this.initialize();
    }

    /**
     * Add count of specified positions in windows
     * @param positions
     */
    public void addCount (int[] positions) {
        int firstIndex = -1;
        int lastIndex = -1;
        for (int i = 0; i < positions.length; i++) {
            firstIndex = this.getFirstWindowIndex(positions[i]);
            lastIndex = this.getLastWindowIndex(positions[i]);
            for (int j = firstIndex; j < lastIndex; j++) {
                this.windowValues[j]++;
            }
        }
    }

    /**
     * Add values of specified positions in windows
     * @param positions
     * @param values
     */
    public void addIntValues (int[] positions, int[] values) {
        int firstIndex = -1;
        int lastIndex = -1;
        for (int i = 0; i < positions.length; i++) {
            firstIndex = this.getFirstWindowIndex(positions[i]);
            lastIndex = this.getLastWindowIndex(positions[1]);
            for (int j = firstIndex; j < lastIndex; j++) {
                this.windowValues[j]+=values[i];
            }
        }
    }

    /**
     * Add values of specified positions in windows
     * @param positions
     * @param values
     */
    public void addFloatValues (int[] positions, float[] values) {
        int firstIndex = -1;
        int lastIndex = -1;
        for (int i = 0; i < positions.length; i++) {
            firstIndex = this.getFirstWindowIndex(positions[i]);
            lastIndex = this.getLastWindowIndex(positions[1]);
            for (int j = firstIndex; j < lastIndex; j++) {
                this.windowValues[j]+=values[i];
            }
        }
    }

    /**
     * Add values of specified positions in windows
     * @param positions
     * @param values
     */
    public void addDoubleValues (int[] positions, double[] values) {
        int firstIndex = -1;
        int lastIndex = -1;
        for (int i = 0; i < positions.length; i++) {
            firstIndex = this.getFirstWindowIndex(positions[i]);
            lastIndex = this.getLastWindowIndex(positions[1]);
            for (int j = firstIndex; j < lastIndex; j++) {
                this.windowValues[j]+=values[i];
            }
        }
    }

    /**
     * Return the values in windows
     * @return
     */
    public int[] getWindowValuesInt () {
        int[] values = new int[this.windowValues.length];
        for (int i = 0; i < this.windowValues.length; i++) {
            values[i] = (int)this.windowValues[i];
        }
        return values;
    }

    /**
     * Return the values in windows
     * @return
     */
    public float[] getWindowValuesFloat () {
        float[] values = new float[this.windowValues.length];
        for (int i = 0; i < this.windowValues.length; i++) {
            values[i] = (float)this.windowValues[i];
        }
        return values;
    }

    /**
     * Clear window values to 0
     */
    public void clearWindowValues () {
        this.windowValues = new double[this.starts.length];
    }

    /**
     * Return the values in windows
     * @return
     */
    public double[] getWindowValuesDouble () {
        return this.windowValues;
    }

    /**
     * Return the starting positions of windows, inclusive
     * @return
     */
    public int[] getWindowStarts () {
        return this.starts;
    }

    /**
     * Return the ending positions of windows, exclusive
     * @return
     */
    public int[] getWindowEnds () {
        return this.ends;
    }

    /**
     * Return the first index of first window containing the position, inclusive
     * @param position
     * @return
     */
    public int getFirstWindowIndex (int position) {
        int index = Arrays.binarySearch(this.ends, position);
        if (index < 0) index = -index-1;
        else index++;
        return index;
    }

    /**
     * Return the last index of first window containing the position, exclusive
     * @param position
     * @return
     */
    public int getLastWindowIndex (int position) {
        int index = Arrays.binarySearch(this.starts, position);
        if (index < 0) index = -index-2;
        return index+1;
    }

    private void initialize () {
        TIntArrayList startList = new TIntArrayList();
        TIntArrayList endList = new TIntArrayList();
        int start = 1;
        int end = start+windowSize;
        while (start < chrLength) {
            startList.add(start);
            endList.add(end);
            start+=windowStep;
            end = start+windowSize;
        }
        this.starts = startList.toArray();
        this.ends = endList.toArray();
        this.windowValues = new double[ends.length];
    }
}
