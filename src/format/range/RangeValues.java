/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package format.range;

import cern.colt.GenericSorting;
import cern.colt.Swapper;
import cern.colt.function.IntComparator;
import com.koloboke.collect.set.hash.HashIntSet;
import com.koloboke.collect.set.hash.HashIntSets;
import gnu.trove.list.array.TDoubleArrayList;
import gnu.trove.list.array.TIntArrayList;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import utils.IOFileFormat;
import utils.IOUtils;
import utils.PStringUtils;

/**
 * Data structure for a list of {@link format.range.Range} with values, providing functions of sorting, searching, and merging, etc.
 * @author feilu
 */
public class RangeValues extends Ranges implements RangeValuesInterface {
    protected TDoubleArrayList vList = null;
    
    /**
     * Constructs a {@link format.range.Ranges} object from a list of {@link format.range.Range}
     * @param ranges 
     * @param values 
     */
    public RangeValues (List<Range> ranges, double[] values) {
        super(ranges);
        vList = new TDoubleArrayList(values);
        this.initializeHeader();
    }
    
    /**
     * Constructs a {@link format.range.Ranges} object with custom parameters
     * @param infileS
     * @param format
     * @param ifWithHeader if the Ranges file has a header in it. True, the header will be used; False, will automatically add a header "Chr\tStart\tEnd".
     */
    public RangeValues (String infileS, IOFileFormat format, boolean ifWithHeader) {
        super(infileS, format, ifWithHeader);
        this.readRangeFile(infileS, format, ifWithHeader);
    }
    
    /**
     * Constructs a {@link format.range.Ranges} object with default, txt file or file ending with ".gz", with a header
     * @param infileS 
     */
    public RangeValues (String infileS) {
        super(infileS);
    }
    
    @Override
    protected void initializeHeader () {
        header = new ArrayList<>();
        header.add("Chr");header.add("Start");header.add("End");
    }
    
    @Override
    protected void readRangeFile (String infileS, IOFileFormat format, boolean ifWithHeader) {
        try {
            BufferedReader br = null;
            if (format == IOFileFormat.Text) {
                br = IOUtils.getTextReader(infileS);
            }
            else if (format == IOFileFormat.TextGzip) {
                br = IOUtils.getTextGzipReader(infileS);
            }
            else {
                throw new UnsupportedOperationException("Unsupported format for input");
            }
            String temp = null;
            List<String> current = null;
            if (!ifWithHeader) {
                temp = br.readLine();
                current = PStringUtils.fastSplit(temp);
                header = current;
            }
            else this.initializeHeader();
            ranges = new ArrayList<>();
            vList = new TDoubleArrayList();
            while ((temp = br.readLine()) != null) {
                current = PStringUtils.fastSplit(temp);
                Range r = new Range(Short.parseShort(current.get(0)), Integer.parseInt(current.get(1)), Integer.parseInt(current.get(2)));
                ranges.add(r);
                vList.add(Double.parseDouble(current.get(3)));
            }
            br.close();
        }
        catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
    
    /**
     * Write to a {@link format.range.Ranges} file of specified format
     * @param outfileS
     * @param format 
     */
    @Override
    public void writeTextFile (String outfileS, IOFileFormat format) {
        try {
            BufferedWriter bw = null;
            if (format == IOFileFormat.Text) {
                bw = IOUtils.getTextWriter(outfileS);
            }
            else if (format == IOFileFormat.TextGzip) {
                bw = IOUtils.getTextGzipWriter(outfileS);
            }
            else {
                throw new UnsupportedOperationException("Unsupported format for input");
            }
            StringBuilder sb = new StringBuilder(header.get(0));
            for (int i = 1; i < header.size(); i++) {
                sb.append("\t").append(header.get(i));
            }
            bw.write(sb.toString());
            bw.newLine();
            for (int i = 0; i < this.getRangeNumber(); i++) {
                sb = new StringBuilder(this.getRange(i).getInfoString());
                sb.append("\t").append(vList.get(i));
                bw.write(sb.toString());
                bw.newLine();
            }
            bw.flush();
            bw.close();
            System.out.println("Table is written to " + outfileS);
        }
        catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
    
    /**
     * Select ranges and write to a {@link format.range.Ranges} file with specified format
     * @param outfileS
     * @param format
     * @param ifOut 
     */
    @Override
    public void writeTextFile (String outfileS, IOFileFormat format, boolean[] ifOut) {
        try {
            BufferedWriter bw = null;
            if (format == IOFileFormat.Text) {
                bw = IOUtils.getTextWriter(outfileS);
            }
            else if (format == IOFileFormat.TextGzip) {
                bw = IOUtils.getTextGzipWriter(outfileS);
            }
            else {
                throw new UnsupportedOperationException("Unsupported format for input");
            }
            StringBuilder sb = new StringBuilder(header.get(0));
            for (int i = 1; i < header.size(); i++) {
                sb.append("\t").append(header.get(i));
            }
            bw.write(sb.toString());
            bw.newLine();
            for (int i = 0; i < this.getRangeNumber(); i++) {
                if (!ifOut[i]) continue;
                sb = new StringBuilder(this.getRange(i).getInfoString());
                sb.append("\t").append(vList.get(i));
                bw.write(sb.toString());
                bw.newLine();
            }
            bw.flush();
            bw.close();
            System.out.println("Table is written to " + outfileS);
        }
        catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
    
    @Override
    public void sortByValue () {
        GenericSorting.quickSort(0, this.getRangeNumber(), compBySize, swapper);
        this.sortType = 3;
    }
    
    @Override
    public void removeRange(int rangeIndex) {
        this.removeRangeValue(rangeIndex);
    }
    
    /**
     * Insert Double.NaN as the value when the range list is implemented in {@link format.range.RangeValues}
     * @param rangeIndex
     * @param r 
     */
    @Override
    public void insertRange(int rangeIndex, Range r) {
        ranges.add(rangeIndex, r);
        vList.insert(rangeIndex, Double.NaN);
        this.resetStatistics();
    }
    
    /**
     * Set Double.NaN as the value when the range list is implemented in {@link format.range.RangeValues}
     * @param rangeIndex
     * @param r 
     */
    @Override
    public void setRange(int rangeIndex, Range r) {
        ranges.set(rangeIndex, r);
        this.setRangeValue(rangeIndex, Double.NaN);
        this.resetStatistics();
    }

    private Swapper swapper = new Swapper() {
        @Override
        public void swap(int a, int b) {
            Range temp = getRange(a);
            setRange(a, getRange(b));
            setRange(b, temp);
            double t = getValue(a);
            vList.set(a, getValue(b));
            vList.set(b, t);
        }
    };
    
    protected IntComparator compByValue = new IntComparator() {
        @Override
        public int compare(int a, int b) {
            double vA = getValue(a);
            double vB = getValue(b);
            if (vA < vB) {
                return -1;
            }
            else if (vA > vB) {
                return 1;
            }
            return 0;
        }
    };
    
    @Override
    public double getValue (int rangeIndex) {
        return this.vList.getQuick(rangeIndex);
    }
    
    @Override
    public Range getRange(int rangeIndex) {
        return ranges.get(rangeIndex);
    }

    @Override
    public int getRangeNumber() {
        return ranges.size();
    }

    @Override
    public int getStartIndexOfChromosome(int chr) {
        if (sortType != 1) this.sortByStartPosition();
        Range query = new Range(chr, Integer.MIN_VALUE, Integer.MIN_VALUE);
        int hit  = Collections.binarySearch(ranges, query);
        if (hit < 0) {
            int index = -hit-1;
            if (this.getRangeChromosome(index) == chr) return index;
            return hit;
        }
        return hit;
    }

    @Override
    public int getEndIndexOfChromosome(int chr) {
        if (sortType != 1) this.sortByStartPosition();
        Range query = new Range(chr+1, Integer.MIN_VALUE, Integer.MIN_VALUE);
        int hit  = Collections.binarySearch(ranges, query);
        if (hit < 0) {
            int index = -hit-1;
            if (this.getRangeChromosome(index) == chr) return index;
            return hit;
        }
        return hit;
    }

    @Override
    public int[] getChromosomes() {
        if (this.chrs != null) return chrs;
        HashIntSet s = HashIntSets.getDefaultFactory().newMutableSet();
        for (int i = 0; i < this.getRangeNumber(); i++) {
            s.add(this.getRangeChromosome(i));
        }
        this.chrs = s.toArray(chrs);
        Arrays.sort(chrs);
        return chrs;
    }

    @Override
    public int getChromosomeNumber() {
        return this.getChromosomes().length;
    }

    @Override
    public int getRangeStart(int rangeIndex) {
        return this.getRange(rangeIndex).getRangeStart();
    }

    @Override
    public int getRangeEnd(int rangeIndex) {
        return this.getRange(rangeIndex).getRangeEnd();
    }

    @Override
    public int getRangeChromosome(int rangeIndex) {
        return this.getRange(rangeIndex).getRangeChromosome();
    }

    @Override
    public RangeValues getRangeValuesByChromosome(int chr) {
        int startIndex = this.getStartIndexOfChromosome(chr);
        if (startIndex == -1) return null;
        int endIndex = this.getEndIndexOfChromosome(chr);
        if (endIndex == -1) return null;
        List<Range> l = new ArrayList<>();
        TDoubleArrayList dList = new TDoubleArrayList();
        for (int i = startIndex; i < endIndex; i++) {
            l.add(this.getRange(i));
            dList.add(this.getValue(i));
        }
        return new RangeValues(l, dList.toArray());
    }


    @Override
    public RangeValues getRangeValuesContainsPosition(int chr, int pos) {
        int[] indices = this.getRangesIndicesContainsPosition(chr, pos);
        List<Range> l = new ArrayList();
        TDoubleArrayList dList = new TDoubleArrayList();
        for (int i = 0; i < indices.length; i++) {
            l.add(this.getRange(indices[i]));
            dList.add(this.getValue(indices[i]));
        }
        return new RangeValues(l, dList.toArray());
    }
    

    @Override
    public RangeValues getMergedRangeValues(RangeValues rs) {
        List<Range> newList = new ArrayList<>(ranges);
        TDoubleArrayList newDList = new TDoubleArrayList(vList);
        for (int i = 0; i < rs.getRangeNumber(); i++) {
            newList.add(rs.getRange(i));
            newDList.add(rs.getValue(i));
        }
        return new RangeValues(newList, newDList.toArray());
    }
    
    /**
     * Add Double.NaN as the value when the range list is implemented in {@link format.range.RangeValues}
     * @param r 
     */
    @Override
    public void addRange(Range r) {
        ranges.add(r);
        vList.add(Double.NaN);
        this.resetStatistics();
    }
    
    /**
     * Add Double.NaN as the value when the range list is implemented in {@link format.range.RangeValues}
     * @param rs 
     */
    @Override
    public void addRanges(Ranges rs) {
        for (int i = 0; i < rs.getRangeNumber(); i++) {
            ranges.add(rs.getRange(i));
            vList.add(Double.NaN);
        }
        this.resetStatistics();
    }

    @Override
    public void insertRangeValue(int rangeIndex, Range r, double value) {
        ranges.add(rangeIndex, r);
        vList.insert(rangeIndex, value);
        this.resetStatistics();
    }

    @Override
    public void removeRangeValue(int rangeIndex) {
        ranges.remove(rangeIndex);
        vList.removeAt(rangeIndex);
        this.chrs = null;
    }

    @Override
    public void setRangeValue(int rangeIndex, double value) {
        vList.setQuick(rangeIndex, value);
        if (this.sortType == 3) this.resetStatistics();
    }
    
    /**
     * Return a {@link format.range.Ranges} object
     * @return 
     */
    public Ranges toRanges () {
        return new Ranges(this.ranges);
    }
}
