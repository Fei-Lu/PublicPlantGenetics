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
import utils.IOUtils;

/**
 *
 * @author feilu
 */
public class TagCounts {
    int tagLengthInLong = -1;
    int offSet = 8;
    int groupIdentifierLength = 3;
    int groupCount = -1;
    TagCount[] tcs = null;
    
    public TagCounts (String infileS) {
        if (infileS.endsWith(".tp")) {
            this.readTPBinaryFile(infileS);
        }
        else if (infileS.endsWith(".tc")) {
            this.readBinaryFile(infileS);
        }
    }
    
    public void readBinaryFile (String infileS) {
        try {
            DataInputStream dis = IOUtils.getBinaryReader(infileS);
            this.tagLengthInLong = dis.readInt();
            this.offSet = dis.readInt();
            this.groupIdentifierLength = dis.readInt();
            this.groupCount = (int)Math.pow(4, groupIdentifierLength);
            tcs = new TagCount[this.groupCount];
            for (int i = 0; i < groupCount; i++) {
                int tagNumber = dis.readInt();
                boolean ifSorted = dis.readBoolean();
                tcs[i] = new TagCount(this.tagLengthInLong, tagNumber, ifSorted);
                for (int j = 0; j < tagNumber; j++) {
                    long[] tag = new long[this.tagLengthInLong*2];
                    for (int k = 0; k < tag.length; k++) {
                        tag[k] = dis.readLong();
                    }
                    byte r1Len = dis.readByte();
                    byte r2Len = dis.readByte();
                    int readNumber = dis.readInt();
                    tcs[i].appendTag(tag, r1Len, r2Len, readNumber);
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
            for (int i = 0; i < this.getGroupNumber(); i++) {
                dos.writeInt(this.getTagNumber(i));
                dos.writeBoolean(tcs[i].ifSorted);
                for (int j = 0; j < tcs[i].getTagNumber(); j++) {
                    long[] tag = tcs[i].getTag(j);
                    for (int k = 0; k < tag.length; k++) {
                        dos.writeLong(tag[k]);
                    }
                    dos.writeByte(tcs[i].getR1TagLength(j));
                    dos.writeByte(tcs[i].getR2TagLength(j));
                    dos.writeInt(tcs[i].getReadNumber(j));
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
            tcs = new TagCount[groupCount];
            for (int i = 0; i < tcs.length; i++) {
                tcs[i] = new TagCount(this.tagLengthInLong);
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
                tcs[groupIndex].appendTag(tag, r1Len, r2Len, readCount);
            }
            dis.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        for (int i = 0; i < this.getGroupNumber(); i++) {
            tcs[i].sort();
        }
    }
    
    public void sort () {
        System.out.println("TagCounts sort begins");
        for (int i = 0; i < this.getGroupNumber(); i++) {
            tcs[i].sort();
        }
        System.out.println("TagCounts sort ends");
    }
    
    public int getReadNumber (int groupIndex, int tagIndex) {
        return tcs[groupIndex].getReadNumber(tagIndex);
    }
    
    public byte getR1TagLength (int groupIndex, int tagIndex) {
        return tcs[groupIndex].getR1TagLength(tagIndex);
    }
    
    public byte getR2TagLength (int groupIndex, int tagIndex) {
        return tcs[groupIndex].getR2TagLength(tagIndex);
    }
    
    public int getTagIndex (long[] tag, int groupIndex) {
        return tcs[groupIndex].getTagIndex(tag);
    }
    
    public int getGroupIndex (long[] tag) {
        return TagUtils.getGroupIndexFromTag(tag, this.getGroupIdentiferOffset(), this.getGroupIdentiferLength());
    }
    
    public long getTotalReadNumber () {
        long sum = 0; 
        for (int i = 0; i < this.getGroupNumber(); i++) {
            sum += tcs[i].getTotalReadNum();
        }
        return sum;
    }
    
    public int getTagNumber (int groupIndex) {
        return tcs[groupIndex].getTagNumber();
    }
    
    public long getTagNumber () {
        long sum = 0;
        for (int i = 0; i < this.getGroupNumber(); i++) {
            sum += tcs[i].getTagNumber();
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
        return tcs[groupIndex].getTag(tagIndex);
    }
    
    public void collapseCounts () {
        int sum = 0;
        for (int i = 0; i < tcs.length; i++) {
            sum += tcs[i].collapseCounts();
        }
        System.out.println("Tag rows collapsed after sorting: " + sum);
    }
}
