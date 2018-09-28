/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package analysis.pipeline.libgbs;

import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import utils.IOUtils;

/**
 *
 * @author feilu
 */
public class TagCounts {
    int tagLengthInLong = -1;
    int offSet = 8;
    int groupIdentifierLength = 4;
    boolean ifSorted;
    int groupCount = -1;
    List<TagCount> tcList = null;

    
    public TagCounts (String infileS) {
        if (infileS.endsWith(".tp")) {
            this.readTPBinaryFile(infileS);
        }
        else if (infileS.endsWith(".tc")) {
            this.readBinaryFile(infileS);
        }
    }
    
    public boolean addTagCounts (TagCounts atc) {
        if (this.getTagLengthInLong() != atc.getTagLengthInLong()) return false;
        if (this.getGroupIdentiferOffset() != atc.getGroupIdentiferOffset()) return false;
        if (this.getGroupIdentiferLength() != atc.getGroupIdentiferLength()) return false;
        tcList.parallelStream().forEach(tc -> {
            tc.r1LenList.addAll(atc.tcList.get(tc.groupIndex).r1LenList);
            tc.r2LenList.addAll(atc.tcList.get(tc.groupIndex).r2LenList);
            tc.readCountList.addAll(atc.tcList.get(tc.groupIndex).readCountList);
            tc.tagList.addAll(atc.tcList.get(tc.groupIndex).tagList);
        });
        this.ifSorted = false;
        return true;
    }
    
    public void readBinaryFile (String infileS) {
        try {
            DataInputStream dis = IOUtils.getBinaryReader(infileS);
            this.tagLengthInLong = dis.readInt();
            this.offSet = dis.readInt();
            this.groupIdentifierLength = dis.readInt();
            ifSorted = dis.readBoolean();
            this.groupCount = (int)Math.pow(4, groupIdentifierLength);
            tcList = new ArrayList<>();
            for (int i = 0; i < groupCount; i++) {
                int tagNumber = dis.readInt();
                int groupIndex = dis.readInt();
                TagCount tc = new TagCount(this.tagLengthInLong, groupIndex, tagNumber, ifSorted);
                tcList.add(tc);
                for (int j = 0; j < tagNumber; j++) {
                    long[] tag = new long[this.tagLengthInLong*2];
                    for (int k = 0; k < tag.length; k++) {
                        tag[k] = dis.readLong();
                    }
                    byte r1Len = dis.readByte();
                    byte r2Len = dis.readByte();
                    int readNumber = dis.readInt();
                    tcList.get(i).appendTag(tag, r1Len, r2Len, readNumber);
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public void writeTextFile (String outfileS) {
        try {
            BufferedWriter bw = IOUtils.getTextWriter(outfileS);
            bw.write("TagLengthInLong:\t" + String.valueOf(this.getTagLengthInLong()));
            bw.newLine();
            bw.write("GroupIdentifierOffset:\t" + String.valueOf(this.getGroupIdentiferOffset()));
            bw.newLine();
            bw.write("GroupIdentifierLength:\t" + String.valueOf(this.getGroupIdentiferLength()));
            bw.newLine();
            for (int i = 0; i < this.getGroupNumber(); i++) {
                StringBuilder sb = new StringBuilder();
                sb.append("GroupIndex:\t").append(i).append("\nTagNumber:\t").append(this.getTagNumber(i));
                bw.write(sb.toString());
                bw.newLine();
                for (int j = 0; j < this.getTagNumber(i); j++) {
                    sb = new StringBuilder();
                    sb.append(this.getR1TagLength(i, j)).append("\t").append(this.getR2TagLength(i, j)).append("\t").append(this.getReadNumber(i, j)).append("\n");
                    String[] reads = TagUtils.getReadsFromTag(this.getTag(i, j), this.getR1TagLength(i, j), this.getR2TagLength(i, j));
                    sb.append(reads[0]).append("\t").append(reads[1]);
                    bw.write(sb.toString());
                    bw.newLine();
                } 
            }
            bw.flush();
            bw.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public void writeBinaryFile (String outfileS) {
        try {
            DataOutputStream dos = IOUtils.getBinaryWriter(outfileS);
            dos.writeInt(this.getTagLengthInLong());
            dos.writeInt(this.getGroupIdentiferOffset());
            dos.writeInt(this.groupIdentifierLength);
            dos.writeBoolean(this.ifSorted);
            for (int i = 0; i < this.getGroupNumber(); i++) {
                dos.writeInt(this.getTagNumber(i));
                dos.writeInt(i);
                for (int j = 0; j < tcList.get(i).getTagNumber(); j++) {
                    long[] tag = tcList.get(i).getTag(j);
                    for (int k = 0; k < tag.length; k++) {
                        dos.writeLong(tag[k]);
                    }
                    dos.writeByte(tcList.get(i).getR1TagLength(j));
                    dos.writeByte(tcList.get(i).getR2TagLength(j));
                    dos.writeInt(tcList.get(i).getReadNumber(j));
                }
            }
            dos.flush();
            dos.close();
            System.out.println(outfileS + " is written");
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void readTPBinaryFile (String infileS) {
        try {
            DataInputStream dis = IOUtils.getBinaryReader(infileS);
            this.tagLengthInLong = dis.readInt();
            int currentTagNum = dis.readInt();
            groupCount = (int)Math.pow(4, groupIdentifierLength);
            tcList = new ArrayList<>();
            for (int i = 0; i < groupCount; i++) {
                TagCount tc = new TagCount(this.tagLengthInLong, i);
                tcList.add(tc);
            }
            if (currentTagNum == -1) currentTagNum = (int)((new File(infileS).length()-8)/(tagLengthInLong*2*8+2+4));
            for (int i = 0; i < currentTagNum; i++) {
                long[] tag = new long[2*this.tagLengthInLong];
                byte r1Len = 0;
                byte r2Len = 0;
                int readCount = 0;
                int groupIndex = 0;
                for (int j = 0; j < tag.length; j++) {
                    tag[j] = dis.readLong();
                }
                r1Len = dis.readByte();
                r2Len = dis.readByte();
                readCount = dis.readInt();
                groupIndex = TagUtils.getGroupIndexFromTag(tag, offSet, groupIdentifierLength);
                tcList.get(groupIndex).appendTag(tag, r1Len, r2Len, readCount);
            }
            dis.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        this.sort();
    }
    
    public void sort () {
        System.out.println("TagCounts sort begins");
        tcList.parallelStream().forEach(tc -> {
            tc.sort();
        });
        System.out.println("TagCounts sort ends");
        this.ifSorted = true;
    }
    
    public boolean isSorted () {
        return this.ifSorted;
    }
    
    public int getMaxTagNumberInGroups () {
        int max = Integer.MIN_VALUE;
        for (int i = 0; i < this.getGroupNumber(); i++) {
            if (this.getTagNumber(i) > max) max = this.getTagNumber(i);
        }
        return max;
    }
    
    public int getReadNumber (int groupIndex, int tagIndex) {
        return tcList.get(groupIndex).getReadNumber(tagIndex);
    }
    
    public byte getR1TagLength (int groupIndex, int tagIndex) {
        return tcList.get(groupIndex).getR1TagLength(tagIndex);
    }
    
    public byte getR2TagLength (int groupIndex, int tagIndex) {
        return tcList.get(groupIndex).getR2TagLength(tagIndex);
    }
    
    public int getTagIndex (long[] tag, int groupIndex) {
        return tcList.get(groupIndex).getTagIndex(tag);
    }
    
    public int getGroupIndex (long[] tag) {
        return TagUtils.getGroupIndexFromTag(tag, this.getGroupIdentiferOffset(), this.getGroupIdentiferLength());
    }
    
    public long getTotalReadNumber () {
        long sum = 0; 
        for (int i = 0; i < this.getGroupNumber(); i++) {
            sum += tcList.get(i).getTotalReadNum();
        }
        return sum;
    }
    
    public int getTagNumber (int groupIndex) {
        return tcList.get(groupIndex).getTagNumber();
    }
    
    public long getTagNumber () {
        long sum = 0;
        for (int i = 0; i < this.getGroupNumber(); i++) {
            sum += tcList.get(i).getTagNumber();
        }
        return sum;
    }
    
    public int getGroupIdentiferLength () {
        return this.groupIdentifierLength;
    }
    
    public int getGroupIdentiferOffset () {
        return this.offSet;
    }
    
    public int getGroupNumber () {
        return this.groupCount;
    }
    
    public int getTagLengthInLong () {
        return this.tagLengthInLong;
    }
    
    public long[] getTag (int groupIndex, int tagIndex) {
        return tcList.get(groupIndex).getTag(tagIndex);
    }
    
    public void collapseCounts () {
        AtomicInteger acnt = new AtomicInteger();
        if (this.isSorted() == false) this.sort();
        tcList.parallelStream().forEach(tc -> {
            int cnt = tc.collapseCounts();
            acnt.addAndGet(cnt);
        });     
        System.out.println("Tag rows collapsed after sorting: " + acnt.get());
    }
}
