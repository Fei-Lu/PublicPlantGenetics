/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package format.dna;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import utils.FStringUtils;
import utils.IOFileFormat;
import utils.IOUtils;

/**
 * Holding FastA format sequence, providing functions of sorting, searching and collecting statistics.
 * Bases are compressed to 3 bits, representing 'A', 'C', 'G', 'T', 'N'. Specially designed for large Fasta sequence.
 * @author feilu
 */
public class FastaBit implements FastaInterface{
    FastaRecord[] records = null;
    private enum sortType {byName, byID, byLengthAscending, byLengthDescending}
    sortType sType = null;
    /**
     * Constructs a {@link format.dna.Fasta} from input file. The file should be either txt format or gz format.
     * @param infileS 
     */
    public FastaBit (String infileS) {
        if (infileS.endsWith(".gz")) {
            this.readFasta(infileS, IOFileFormat.TextGzip);
        }
        else {
            this.readFasta(infileS, IOFileFormat.Text);
        }
    }
    
    /**
     * Constructs a {code Fasta} from input file.
     * @param infileS
     * @param format 
     */
    public FastaBit (String infileS, IOFileFormat format) {
        this.readFasta(infileS, format);
    }
    
    @Override
    public void readFasta (String infileS, IOFileFormat format) {
        System.out.println("Reading Fasta file...");
        List<FastaRecord> fl = new ArrayList<>();
        try {
            BufferedReader br = null;
            if (format == IOFileFormat.Text) {
                br = IOUtils.getTextReader(infileS);
            }
            else if (format == IOFileFormat.TextGzip) {
                br = IOUtils.getTextGzipReader(infileS);
            }
            else {
                throw new UnsupportedOperationException("Invalid input format for the Fasta file");
            }
            String temp = null, name = null, seq = null;
            StringBuilder sb = new StringBuilder();
            FastaRecord fr;
            boolean first = true;
            int cnt = 1;
            while ((temp = br.readLine()) != null) {
                if (temp.startsWith(">")) {
                    if (first == false) {
                        seq = sb.toString();
                        fr = new FastaRecord(name, seq, cnt);
                        fl.add(fr);
                        sb = new StringBuilder();
                        if (cnt%1000000 == 0) {
                            System.out.println("Read "+String.valueOf(cnt)+" sequences");
                        }
                        cnt++;
                    }
                    name = temp.substring(1, temp.length());
                    first = false;
                }
                else {
                    sb.append(temp);
                }
            }
            if (!name.equals("")) {
                seq = sb.toString();
                fr = new FastaRecord(name, seq, cnt);
                fl.add(fr);
            }
            records = fl.toArray(new FastaRecord[fl.size()]);
            sType = sortType.byID;
            System.out.println(records.length + " sequences in the file " + infileS);
        }
        catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
    
    @Override
    public void writeFasta (String outfileS, boolean[] ifOut) {
        int cnt = 0;
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(outfileS), 65536);
            for (int i = 0; i < records.length; i++) {
                if (!ifOut[i]) continue;
                bw.write(">"+records[i].name);
                bw.newLine();
                bw.write(FStringUtils.getMultiplelineString(60, records[i].getSequence()));
                bw.newLine();
                cnt++;
            }
            bw.flush();
            bw.close();
            System.out.println(cnt+ " sequences are written in " + outfileS);
        }
        catch (Exception e) {
            System.out.println("Error while writing "+ outfileS);
        }
    }
    
    @Override
    public void writeFasta (String outfileS) {
        try {
            BufferedWriter bw = IOUtils.getTextWriter(outfileS);
            for (int i = 0; i < records.length; i++) {
                bw.write(">"+records[i].name);
                bw.newLine();
                bw.write(FStringUtils.getMultiplelineString(60, records[i].getSequence()));
                bw.newLine();
            }
            bw.flush();
            bw.close();
            System.out.println(records.length+ " sequences are written in " + outfileS);
        }
        catch (Exception e) {
            System.out.println("Error while writing "+ outfileS);
        }
    }
    
    @Override
    public int getN50 () {
        if (sType != sortType.byLengthDescending) this.sortByLengthDescending();
        long sum = this.getTotalSeqLength();
        long halfSum = sum/2;
        int current = 0;
        for (int i = 0; i < this.getSeqNumber(); i++) {
            current+=this.getSeqLength(i);
            if (current > halfSum) return i+1;
        }
        return -1;
    }
    
    @Override
    public int getL50 () {
        if (sType != sortType.byLengthDescending) this.sortByLengthDescending();
        long sum = this.getTotalSeqLength();
        long halfSum = sum/2;
        int current = 0;
        for (int i = 0; i < this.getSeqNumber(); i++) {
            current+=this.getSeqLength(i);
            if (current > halfSum) return this.getSeqLength(i);
        }
        return -1;
    }
    
    @Override
    public long getTotalSeqLength () {
        long sum = 0;
        for (int i = 0; i < this.getSeqNumber(); i++) {
            sum+=this.getSeqLength(i);
        }
        return sum;
    }
    
    @Override
    public int getSeqNumber () {
        return records.length;
    }
    
    @Override
    public int getIndexByName (String name) {
        if (this.sType != sortType.byName) {
            this.sortByName();
        }
        return Arrays.binarySearch(records, new FastaRecord(name,null,-1));
    }
    
    @Override
    public int getSeqLength (int index) {
        return records[index].getSequenceLength();
    }
    
    @Override
    public String[] getNames () {
        String[] names = new String[this.getSeqNumber()];
        for (int i = 0; i < names.length; i++) names[i] = this.getName(i);
        return names;
    }
    
    @Override
    public String getName (int index) {
        return records[index].name;
    }
    
    @Override
    public String getSeq (int index) {
        return records[index].getSequence();
    }
    
    @Override
    public String getSeq (int index, int startIndex, int endIndex) {
        return records[index].getSequence(startIndex, endIndex);
    }
    
    @Override
    public void setName (String newName, int index) {
        records[index].name = newName;
    }

    @Override
    public void sortByName () {
        Arrays.parallelSort(records, new sortByName());
    }
    
    /**
     * Sort the sequences of Fasta by ID
     */
    @Override
    public void sortByID () {
        Arrays.parallelSort (records, new sortByID());
    }
    
    @Override
    public void sortByLengthAscending () {
        Arrays.parallelSort (records, new sortByLengthAscending());
    }
    
    @Override
    public void sortByLengthDescending () {
        Arrays.parallelSort (records, new sortByLengthDescending());
    }
    
    @Override
    public boolean isThereN () {
        boolean value;
        for (int i = 0; i < records.length; i++) {
             value = records[i].isThereN();
             if (value) return true;
        }
        return false;
    }
    
    
    /**
     * Return if the fasta has non A, C, G, T, N base
     * @return 
     */
    public boolean isThereNonACGTBase () {
        boolean value;
        for (int i = 0; i < records.length; i++) {
             value = records[i].isThereNonACGTBase();
             if (value) return true;
        }
        return false;
    }
    
    @Override
    public boolean isThereNonACGTNBase() {
        return false;
    }
    
    private class FastaRecord extends Sequence3Bit {
        String name;
        int id;

        public FastaRecord (String name, String seq, int id) {
            super(seq);
            this.name = name;
            this.id = id;
        }
    }
    
    private class sortByID implements Comparator <FastaRecord> {
        @Override
        public int compare(FastaRecord o1, FastaRecord o2) {
            return o1.id - o2.id;
        }
    }
    
    private class sortByName implements Comparator <FastaRecord> {
        @Override
        public int compare (FastaRecord o1, FastaRecord o2) {
            return o1.name.compareTo(o2.name);
        }
    }
    
    private class sortByLengthAscending implements Comparator <FastaRecord> {
        @Override
        public int compare (FastaRecord o1, FastaRecord o2) {
            return o1.getSequenceLength() - o2.getSequenceLength();
        }
    }
    
    private class sortByLengthDescending implements Comparator <FastaRecord> {
        @Override
        public int compare (FastaRecord o1, FastaRecord o2) {
            return o2.getSequenceLength()-o1.getSequenceLength();
        }
    }
}
