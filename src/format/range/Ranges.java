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
 * Data structure for a list of {@link format.range.Range}, providing functions of sorting, searching, and merging, etc.
 * @author feilu
 */
public class Ranges implements RangesInterface {
    protected List<Range> ranges = null;
    protected List<String> header = null;

    //statistics of current range list, need to rebuild after write operations on ranges
    //0, unsorted; 1, by position; 2, by size; 3 by value
    protected int sortType = 0;
    protected int[] chrs = null;
    
    /**
     * Constructs a {@link format.range.Ranges} object from a list of {@link format.range.Range}
     * @param ranges 
     */
    public Ranges (List<Range> ranges) {
        this.ranges = ranges;
        this.initializeHeader();
    }
    
    /**
     * Constructs a {@link format.range.Ranges} object with custom parameters
     * @param infileS
     * @param format
     * @param ifWithHeader if the Ranges file has a header in it. True, the header will be used; False, will automatically add a header "Chr\tStart\tEnd".
     */
    public Ranges (String infileS, IOFileFormat format, boolean ifWithHeader) {
        this.readRangeFile(infileS, format, ifWithHeader);
    }
    
    /**
     * Constructs a {@link format.range.Ranges} object with default, txt file or file ending with ".gz", with a header
     * @param infileS 
     */
    public Ranges (String infileS) {
        if (infileS.endsWith(".gz")) {
            this.readRangeFile(infileS, IOFileFormat.TextGzip, true);
        }
        else {
            this.readRangeFile(infileS, IOFileFormat.Text, true);
        }
    }
    
    protected void initializeHeader () {
        header = new ArrayList<>();
        header.add("Chr");header.add("Start");header.add("End");
    }
    
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
            while ((temp = br.readLine()) != null) {
                current = PStringUtils.fastSplit(temp);
                Range r = new Range(Short.parseShort(current.get(0)), Integer.parseInt(current.get(1)), Integer.parseInt(current.get(2)));
                ranges.add(r);
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
                bw.write(this.getRange(i).getInfoString());
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
                bw.write(this.getRange(i).getInfoString());
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
    
    protected void resetStatistics () {
        this.chrs = null;
        this.sortType = 0;
    }
    
    @Override
    public void sortBySize() {
        GenericSorting.quickSort(0, this.getRangeNumber(), compBySize, swapper);
        this.sortType = 2;
    }

    @Override
    public void sortByStartPosition() {
        GenericSorting.quickSort(0, this.getRangeNumber(), compByStartPosition, swapper);
        this.sortType = 1;
    }

    /**
     * Remove a {@link format.range.Range} from the list
     * @param rangeIndex 
     */
    @Override
    public void removeRange(int rangeIndex) {
        ranges.remove(rangeIndex);
        this.chrs = null;
    }

    /**
     * Insert a {@link format.range.Range} into the list
     * @param rangeIndex
     * @param r 
     */
    @Override
    public void insertRange(int rangeIndex, Range r) {
        ranges.add(rangeIndex, r);
        this.resetStatistics();
    }

    /**
     * Set a {@link format.range.Range} in the list
     * @param rangeIndex
     * @param r 
     */
    @Override
    public void setRange(int rangeIndex, Range r) {
        ranges.set(rangeIndex, r);
        this.resetStatistics();
    }

    private Swapper swapper = new Swapper() {
        @Override
        public void swap(int a, int b) {
            Range temp = getRange(a);
            setRange(a, getRange(b));
            setRange(b, temp);
        }
    };
    
    protected IntComparator compBySize = new IntComparator() {
        @Override
        public int compare(int a, int b) {
            int sA = getRange(a).getRangeSize();
            int sB = getRange(b).getRangeSize();
            return sA-sB;
        }
    };
    
    protected IntComparator compByStartPosition = new IntComparator() {
        @Override
        public int compare(int a, int b) {
            int chrA = getRangeChromosome(a);
            int chrB = getRangeChromosome(b);
            if (chrA == chrB) {
                int sA = getRange(a).getRangeStart();
                int sB = getRange(b).getRangeStart();
                return sA-sB;
            }
            else {
                return chrA-chrB;
            }
        }
    };

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
    public Ranges getRangesByChromosome(int chr) {
        int startIndex = this.getStartIndexOfChromosome(chr);
        if (startIndex == -1) return null;
        int endIndex = this.getEndIndexOfChromosome(chr);
        if (endIndex == -1) return null;
        List<Range> l = new ArrayList<>();
        for (int i = startIndex; i < endIndex; i++) {
            l.add(this.getRange(i));
        }
        return new Ranges(l);
    }

    @Override
    public Ranges getRangesContainsPosition(int chr, int pos) {
        int[] indices = this.getRangesIndicesContainsPosition(chr, pos);
        List<Range> l = new ArrayList();
        for (int i = 0; i < indices.length; i++) {
            l.add(this.getRange(indices[i]));
        }
        return new Ranges(l);
    }
    
    @Override
    public int[] getRangesIndicesContainsPosition (int chr, int pos) {
        if (sortType != 1) this.sortByStartPosition();
        TIntArrayList indexList = new TIntArrayList();
        Range query = new Range(chr, pos, pos+1);
        int hit = Collections.binarySearch(ranges, query);
        if (hit < 0) hit = -hit-1;
        while (this.getRange(hit).isContain(chr, pos)) {
            indexList.add(hit);
        }
        return indexList.toArray();
    }
    
    @Override
    public Ranges getNonOverlapRanges() {
        int[] chromosomes = this.getChromosomes();
        int[] mins = new int[chromosomes.length];
        int[] maxs = new int[chromosomes.length];
        for (int i = 0; i < mins.length; i++) {
            mins[i] = Integer.MAX_VALUE;
            maxs[i] = Integer.MIN_VALUE;
        }
        for (int i = 0; i < this.getRangeNumber(); i++) {
            int index = Arrays.binarySearch(chromosomes, this.getRangeChromosome(i));
            int v = this.getRangeStart(i);
            if (v < mins[index]) mins[index] = v;
            v = this.getRangeEnd(i);
            if (v > maxs[index]) maxs[index] = v;
        }
        List<Range> rList = new ArrayList<>();
        for (int i = 0; i < chromosomes.length; i++) {
            System.out.println("Start collasping chromosome " + String.valueOf(chromosomes[i]));
            int base = mins[i];
            int length = maxs[i] - mins[i];
            byte[] status = new byte[length];
            int startIndex = this.getStartIndexOfChromosome(chromosomes[i]);
            int endIndex = this.getEndIndexOfChromosome(chromosomes[i]);
            for (int j = startIndex; j < endIndex; j++) {
                for (int k = this.getRangeStart(j); k < this.getRangeEnd(j); k++) {
                    status[k-base] = 1;
                }
            }
            int current = 0;
            while (current < length) {
                if (status[current] != 0) {
                    int start = current+base;
                    while (current < length && status[current] == 1) {
                        current++;
                    }
                    int end = current+base;
                    rList.add(new Range(chromosomes[i], start, end));
                }
                current++;
            }
        }
        return new Ranges(rList);
    }

    @Override
    public int getFirstRangeIndex(int chr, int pos) {
        if (sortType != 1) this.sortByStartPosition();
        Range query = new Range(chr, pos, pos+1);
        int hit = Collections.binarySearch(ranges, query);
        int index;
        if (hit < 0) {
            index = -hit-1;
            if (this.getRange(index).isContain(chr, pos)) return index;
            return hit;
        }
        return hit;
    }

    @Override
    public Ranges getMergedRanges(Ranges rs) {
        List<Range> newList = new ArrayList<>(ranges);
        for (int i = 0; i < rs.getRangeNumber(); i++) {
            newList.add(rs.getRange(i));
        }
        return new Ranges(newList);
    }

    @Override
    public void addRange(Range r) {
        ranges.add(r);
        this.resetStatistics();
    }

    @Override
    public void addRanges(Ranges rs) {
        for (int i = 0; i < rs.getRangeNumber(); i++) {
            ranges.add(rs.getRange(i));
        }
        this.resetStatistics();
    }

    @Override
    public List<Range> getRangeList() {
        return new ArrayList<Range>(this.ranges);
    }
}
