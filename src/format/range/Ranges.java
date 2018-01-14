/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package format.range;

import cern.colt.Swapper;
import cern.colt.function.IntComparator;
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
 * 
 * @author feilu
 */
public class Ranges implements RangesInterface {
    List<Range> ranges = null;
    String[] header = {"Chr", "Start", "End"};
    
    /**
     * Constructs a {@link format.range.Ranges} object from a list of {@link format.range.Range}
     * @param ranges 
     */
    public Ranges (List<Range> ranges) {
        this.ranges = ranges;
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
            String a;
        }
    }
    
    private void readRangeFile (String infileS, IOFileFormat format, boolean ifWithHeader) {
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
                header = current.toArray(new String[current.size()]);
            }
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
            StringBuilder sb = new StringBuilder(header[0]);
            for (int i = 1; i < header.length; i++) {
                sb.append("\t").append(header[i]);
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
            StringBuilder sb = new StringBuilder(header[0]);
            for (int i = 1; i < header.length; i++) {
                sb.append("\t").append(header[i]);
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
    
    @Override
    public void sortBySize() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void sortByPosition() {
        Collections.sort(ranges);
    }

    @Override
    public void removeRange(int rangeIndex) {
        ranges.remove(rangeIndex);
    }

    @Override
    public void insertRange(int rangeIndex, Range r) {
        ranges.add(rangeIndex, r);
    }

    @Override
    public void setRange(int rangeIndex, Range r) {
        ranges.set(rangeIndex, r);
    }

    protected Swapper swapper = new Swapper() {
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

    @Override
    public Range getRange(int rangeIndex) {
        return ranges.get(rangeIndex);
    }

    @Override
    public int getRangeNumber() {
        return ranges.size();
    }
    
}
