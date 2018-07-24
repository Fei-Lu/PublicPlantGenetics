/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package analysis.pipeline.libgbs;

import cern.colt.GenericSorting;
import cern.colt.Swapper;
import cern.colt.function.IntComparator;
import format.dna.BaseEncoder;
import gnu.trove.list.array.TByteArrayList;
import gnu.trove.list.array.TIntArrayList;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import utils.IOUtils;

/**
 *
 * @author feilu
 */
public class TagCount implements Swapper, IntComparator {
    protected int tagLengthInLong = 3;
    protected int tagNum = -1;
    protected long[][] tags = null;
    protected byte[] r1Len = null;
    protected byte[] r2Len = null;
    protected int[] readNum = null;
    
    public TagCount (String infileS) {
        this.readBinaryFile(infileS);
    }
    
    public void readBinaryFile (String infileS) {
        try {
            DataInputStream dis = IOUtils.getBinaryReader(infileS);
            this.tagLengthInLong = dis.readInt();
            tagNum = dis.readInt();
            if (tagNum == -1) tagNum = (int)((new File(infileS).length()-8)/(tagLengthInLong*2*8+2+4));
            tags = new long[tagNum][2*this.tagLengthInLong];
            r1Len = new byte[tagNum];
            r2Len = new byte[tagNum];
            readNum = new int[tagNum];
            for (int i = 0; i < tagNum; i++) {
                for (int j = 0; j < tags[0].length; j++) {
                    tags[i][j] = dis.readLong();
                }
                r1Len[i] = dis.readByte();
                r2Len[i] = dis.readByte();
                readNum[i] = dis.readInt();
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public void writeBinaryFile (String outfileS) {
        try {
            DataOutputStream dos = IOUtils.getBinaryWriter(outfileS);
            dos.writeInt(this.getTagLengthInLong());
            dos.writeInt(this.getTagNumber());
            int numOfLong = 2*this.tagLengthInLong;
            for (int i = 0; i < this.getTagNumber(); i++) {
                for (int j = 0; j < numOfLong; j++) {
                    dos.writeLong(tags[i][j]);
                }
                dos.writeByte(r1Len[i]);
                dos.writeByte(r2Len[i]);
                dos.writeInt(this.getReadNumber(i));
            }
            dos.flush();
            dos.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public int getTagLengthInLong () {
        return this.tagLengthInLong;
    }
    
    public int getTotalReadNum () {
        int cnt = 0;
        for (int i = 0; i < this.getTagNumber(); i++) {
            cnt+=this.getReadNumber(i);
        }
        return cnt;
    }
    
    public int getReadNumber (int index) {
        return this.readNum[index];
    }
    
    public int getTagNumber () {
        return this.tagNum;
    }
    
    @Override
    public void swap (int index1, int index2) {
        long temp;
        for (int i = 0; i < tagLengthInLong*2; i++) {
            temp = tags[index1][i];
            tags[index1][i] = tags[index2][i];
            tags[index2][i] = temp;
        }
        byte tl;
        tl = r1Len[index1];
        r1Len[index1] = r1Len[index2];
        r1Len[index2] = tl;
        tl = r2Len[index1];
        r2Len[index1] = r2Len[index2];
        r2Len[index2] = tl;
        int tc;
        tc = readNum[index1];
        readNum[index1] = readNum[index2];
        readNum[index2] = tc;
    }

    @Override
    public int compare (int index1, int index2) {
        for (int i = 0; i < tagLengthInLong*2; i++) {
            if (tags[index1][i] < tags[index2][i]) {
                return -1;
            }
            if (tags[index1][i] > tags[index2][i]) {
                return 1;
            }
        }
        return 0;
    }

    public void sort () {
        System.out.println("TagCount sort begins");
        GenericSorting.quickSort(0, this.getTagNumber(), this, this);
        System.out.println("TagCount sort ends");
    }
    
    protected void collapseCounts () {
        sort();//requires that the reads are sorted
        int collapsedRows = 0;
        for (int i = 0; i < this.getTagNumber()-1; i++) {
            if (readNum[i] == 0) continue;
            for (int j = i + 1; j < this.getTagNumber(); j++) {
                int index = this.compare(i, j);
                if (index < 0) break;
                else {
                    readNum[i]+=readNum[j];
                    collapsedRows+=readNum[j];
                    readNum[j] = 0;
                }
            }
        }
        int cnt = 0;
        List<long[]> tagList = new ArrayList();
        TByteArrayList r1LenList = new TByteArrayList();
        TByteArrayList r2LenList = new TByteArrayList();
        TIntArrayList readNumList = new TIntArrayList();
        for (int i = 0; i < this.getTagNumber(); i++) {
            if (this.getReadNumber(i) == 0) continue;
            tagList.add(tags[i]);
            r1LenList.add(r1Len[i]);
            r2LenList.add(r2Len[i]);
            readNumList.add(readNum[i]);
            cnt++;
        }
        tags = tagList.toArray(new long[tagList.size()][]);
        r1Len = r1LenList.toArray();
        r2Len = r2LenList.toArray();
        readNum = readNumList.toArray();
        this.tagNum = cnt;
        System.out.println("Tag rows collapsed after sorting:" + collapsedRows);
    }
}
