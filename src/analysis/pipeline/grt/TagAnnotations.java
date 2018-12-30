/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package analysis.pipeline.grt;

import format.alignment.SAMUtils;
import format.dna.snp.AlleleEncoder;
import format.dna.snp.SNP;
import format.position.ChrPos;
import gnu.trove.list.array.TByteArrayList;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import utils.IOUtils;
import utils.PStringUtils;
import utils.Tuple;
import utils.Tuple3;

/**
 *
 * @author feilu
 */
public class TagAnnotations {
    int tagLengthInLong = Integer.MIN_VALUE;
    int offSet = 8;
    int groupIdentifierLength = 6;
    boolean ifSorted = false;
    int groupCount = -1;
    List<TagAnnotation> taList = null;
    
    public TagAnnotations (String infileS) {
        if (infileS.endsWith(".tp")) {
            this.readTPBinaryFile(infileS);
        }
        else if (infileS.endsWith(".tas")) {
            this.readBinaryFile(infileS);
        }
    }
    
    private void readTPBinaryFile (String infileS) {
        System.out.println("Reading TagAnnotations file from " + infileS);
        try {
            DataInputStream dis = IOUtils.getBinaryReader(infileS);
            this.tagLengthInLong = dis.readInt();
            int currentTagNum = dis.readInt();
            groupCount = (int)Math.pow(4, groupIdentifierLength);
            taList = new ArrayList<>();
            for (int i = 0; i < groupCount; i++) {
                TagAnnotation ta = new TagAnnotation(i);
                taList.add(ta);
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
                taList.get(groupIndex).appendTag(tag, r1Len, r2Len, readCount);
            }
            dis.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        this.sort();
    }
    
    public void writeFastqFile (String r1FastqFileS, String r2FastqFileS) {
        System.out.println("Writing fastq file from TagAnnotations");
        String polyQ = "????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????";
        try {
            System.out.println("Writing fastq file from TagAnnotations");
            BufferedWriter bw1 = IOUtils.getTextWriter(r1FastqFileS);
            BufferedWriter bw2 = IOUtils.getTextWriter(r2FastqFileS);
            StringBuilder sb = new StringBuilder();
            String identifier = null;
            String[] reads = null;
            long cnt = 0;
            for (int i = 0; i < this.getGroupNumber(); i++) {
                for (int j = 0; j < this.getTagNumber(i); j++) {
                    sb = new StringBuilder();
                    sb.append("@").append(i).append("_").append(j).append("_").append(this.getReadNumber(i, j));
                    identifier = sb.toString();
                    bw1.write(identifier); bw1.newLine();          
                    bw2.write(identifier); bw2.newLine();
                    reads = TagUtils.getReadsFromTag(this.getTag(i, j), this.getR1TagLength(i, j), this.getR2TagLength(i, j));
                    bw1.write(reads[0]);bw1.newLine();
                    bw2.write(reads[1]);bw2.newLine();
                    bw1.write("+");bw1.newLine();
                    bw2.write("+");bw2.newLine();
                    bw1.write(polyQ.substring(0, this.getR1TagLength(i, j)));bw1.newLine();
                    bw2.write(polyQ.substring(0, this.getR2TagLength(i, j)));bw2.newLine();
                    cnt++;
                    if (cnt%10000000 == 0) System.out.println(String.valueOf(cnt) + " tags have been converted to Fastq");
                }
            }
            bw1.flush();bw1.close();
            bw2.flush();bw2.close();
            System.out.println("Fastq files are written to " + String.valueOf(r1FastqFileS) + " " + String.valueOf(r2FastqFileS));
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
     
    public void readBinaryFile (String infileS) {
        try {
            System.out.println("Reading TagAnnotations file from " + infileS);
            DataInputStream dis = IOUtils.getBinaryReader(infileS);
            this.tagLengthInLong = dis.readInt();
            this.offSet = dis.readInt();
            this.groupIdentifierLength = dis.readInt();
            ifSorted = dis.readBoolean();
            this.groupCount = (int)Math.pow(4, groupIdentifierLength);
            taList = new ArrayList<>();
            long cnt = 0;
            for (int i = 0; i < groupCount; i++) {
                int tagNumber = dis.readInt();
                int groupIndex = dis.readInt();
                TagAnnotation ta = new TagAnnotation(this.tagLengthInLong, groupIndex, tagNumber, ifSorted);               
                for (int j = 0; j < tagNumber; j++) {
                    cnt++;
                    long[] tag = new long[this.tagLengthInLong*2];
                    for (int k = 0; k < tag.length; k++) {
                        tag[k] = dis.readLong();
                    }
                    byte r1Len = dis.readByte();
                    byte r2Len = dis.readByte();
                    int readNumber = dis.readInt();
                    byte snpNumber = dis.readByte();
                    List<SNP> snpList = new ArrayList<>();
                    for (int k = 0; k < snpNumber; k++) {
                        short chr = dis.readShort();
                        int pos = dis.readInt();
                        byte ref = dis.readByte();
                        byte altNumber = dis.readByte();
                        TByteArrayList alts = new TByteArrayList();
                        for (int u = 0; u < altNumber; u++) {
                            alts.add(dis.readByte());
                        }
                        snpList.add(new SNP(chr, pos, ref, alts));
                    }
                    byte alleleNumber = dis.readByte();
                    List<ChrPos> posList = new ArrayList<>();
                    TByteArrayList tagAlleleList = new TByteArrayList();
                    for (int k = 0; k < alleleNumber; k++) {
                        posList.add(new ChrPos(dis.readShort(), dis.readInt()));
                        tagAlleleList.add(dis.readByte());
                    }
                    ta.appendTag(tag, r1Len, r2Len, readNumber, snpList, posList, tagAlleleList);
                    if (cnt%10000000 == 0) System.out.println("Reading in "+String.valueOf(cnt)+" tags");
                }
                taList.add(ta);
            }
            System.out.println(String.valueOf(cnt) + " tags are in " + infileS);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public void writeBinaryFile (String outfileS) {
        System.out.println("Writing TagAnnotationss file to " + outfileS);
        try {
            DataOutputStream dos = IOUtils.getBinaryWriter(outfileS);
            dos.writeInt(this.getTagLengthInLong());
            dos.writeInt(this.getGroupIdentiferOffset());
            dos.writeInt(this.groupIdentifierLength);
            dos.writeBoolean(this.ifSorted);
            long cnt = 0;
            for (int i = 0; i < this.getGroupNumber(); i++) {
                dos.writeInt(this.getTagNumber(i));
                dos.writeInt(i);
                for (int j = 0; j < taList.get(i).getTagNumber(); j++) {
                    long[] tag = taList.get(i).getTag(j);
                    for (int k = 0; k < tag.length; k++) {
                        dos.writeLong(tag[k]);
                    }
                    dos.writeByte(this.getR1TagLength(i, j));
                    dos.writeByte(this.getR2TagLength(i, j));
                    dos.writeInt(this.getReadNumber(i, j));
                    byte snpNumber = this.getSNPNumberOfTag(i, j);
                    dos.writeByte(snpNumber);
                    List<SNP> snpList = this.getSNPOfTag(i, j);
                    for (int k = 0; k < snpNumber; k++) {
                        SNP s = snpList.get(k);
                        dos.writeShort(s.getChromosome());
                        dos.writeInt(s.getPosition());
                        dos.writeByte(s.getRefAlleleByte());  
                        dos.writeByte(s.getAltAlleleNumber());
                        for (int u = 0; u < s.getAltAlleleNumber(); u++) {
                            dos.writeByte(s.getAltAlleleByte(u));
                        }
                    }
                    byte alleleNumber = this.getAlleleNumberOfTag(i, j);
                    dos.writeByte(alleleNumber);
                    List<ChrPos> posList = this.getAllelePosOfTag(i, j);
                    TByteArrayList tagAlleleList = this.getAlleleOfTag(i, j);
                    for (int k = 0; k < alleleNumber; k++) {
                        dos.writeShort(posList.get(k).getChromosome());
                        dos.writeInt(posList.get(k).getPosition());
                        dos.writeByte(tagAlleleList.get(k));
                    }                  
                    cnt++;
                    if (cnt%10000000 == 0) System.out.println(String.valueOf(cnt) + " tags ouput to " + outfileS);
                }
            }
            dos.flush();
            dos.close();
            System.out.println(String.valueOf(cnt) + " tags are written to " + outfileS);
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
                    sb.append(reads[0]).append("\t").append(reads[1]).append("\n");
                    List<SNP> snpList = this.getSNPOfTag(i, j);
                    sb.append("SNPNumber:\t").append(snpList.size());
                    sb.append("\nSNPs:");
                    for (int k = 0; k < snpList.size(); k++) {
                        SNP s = snpList.get(k);
                        sb.append("\t|").append(k).append("->").append(s.getChromosome()).append("\t").append(s.getPosition()).append("\t").append(s.getRefAllele()).append("\t");
                        for (int u = 0; u < s.getAltAlleleNumber(); u++) {
                            sb.append(s.getAltAllele(u)).append("/");
                        }
                        sb.deleteCharAt(sb.length()-1);
                    }
                    sb.append("\n");
                    TByteArrayList alleleList = this.getAlleleOfTag(i, j);
                    List<ChrPos> posList = this.getAllelePosOfTag(i, j);
                    sb.append("AlleleNumber:\t").append(snpList.size());
                    sb.append("\nAlleles:");
                    for (int k = 0; k < posList.size(); k++) {
                        sb.append("\t|").append(k).append("->").append(posList.get(k).getChromosome()).append("\t").append(posList.get(k).getPosition())
                                .append("\t").append(AlleleEncoder.alleleByteCharMap.get(alleleList.get(k)));
                    }
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
    
    public void callAllele (String samFileS, SNPCounts sc, int mapQThresh, int maxMappingIntervalThresh) {
        System.out.println("Start adding alleles to DB");
        try {
            BufferedReader br = IOUtils.getTextGzipReader(samFileS);
            String temp = null;
            while ((temp = br.readLine()).startsWith("@SQ")){}
            int queryCount = 0;
            List<ChrPos> tagAllelePosList = new ArrayList<>();
            TByteArrayList tagAlleleList = new TByteArrayList();
            TByteArrayList[] alleleRelativePos = new TByteArrayList[2];
            for (int i = 0; i < alleleRelativePos.length; i++) {
                alleleRelativePos[i] = new TByteArrayList();
            }
            long cnt = 0;
            long snpCnt = 0;
            while ((temp = br.readLine()) != null) {
                List<String> l = SAMUtils.getAlignElements(temp);
                if (Integer.parseInt(l.get(1)) > 2000) continue; //remove supplement alignment to have a pair of alignments for PE reads
                queryCount++;
                Tuple3 <List<ChrPos>, TByteArrayList, TByteArrayList> alleles = SAMUtils.getAllelesWithSubstitutionPos(l, mapQThresh, sc);
                if (queryCount == 1) {
                    if (alleles != null) {
                        tagAllelePosList.addAll(alleles.getFirstElement());
                        tagAlleleList.addAll(alleles.getSecondElement());
                        alleleRelativePos[0] = alleles.getThirdElement();
                    }
                }
                else if (queryCount == 2) {
                    if (l.get(6).equals("=")) {
                        double len = Math.abs(Double.valueOf(l.get(8)));
                        if (len < maxMappingIntervalThresh) {
                            if (alleles != null) {
                                tagAllelePosList.addAll(alleles.getFirstElement());
                                tagAlleleList.addAll(alleles.getSecondElement());
                                alleleRelativePos[1] = alleles.getThirdElement();
                            }
                            cnt++;
                            if (cnt%10000000 == 0) System.out.println(String.valueOf(cnt) + " tags are properly aligned for SNP calling");                           
                            if (tagAllelePosList.size() != 0) {
                                List<String> ll = PStringUtils.fastSplit(l.get(0), "_");                               
                                int groupIndex = Integer.parseInt(ll.get(0));
                                int tagIndex = Integer.parseInt(ll.get(1));
                                this.setAlleleOfTag(groupIndex, tagIndex, tagAllelePosList, tagAlleleList, alleleRelativePos);
                                this.sortAlleleListByPosition(groupIndex, tagIndex);
                                snpCnt++;
                            }
                        }
                    }
                    queryCount = 0;
                    tagAllelePosList = new ArrayList<>();
                    tagAlleleList = new TByteArrayList();
                    for (int i = 0; i < alleleRelativePos.length; i++) {
                        alleleRelativePos[i] = new TByteArrayList();
                    }
                }
            }
            br.close();
            System.out.println("A total of "+String.valueOf(cnt) + " tags are properly aligned for allele calling");
            System.out.println("A total of "+String.valueOf(snpCnt) + " tags have allele calls");
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public void callSNP (String samFileS, int mapQThresh, int maxMappingIntervalThresh, int maxDivergence) {
        System.out.println("Start adding raw SNPs to DB");
        maxDivergence++;
        try {
            BufferedReader br = IOUtils.getTextGzipReader(samFileS);
            String temp = null;
            while ((temp = br.readLine()).startsWith("@SQ")){}
            int queryCount = 0;
            List<SNP> tagSNPList = new ArrayList();
            long cnt = 0;
            long snpCnt = 0;
            while ((temp = br.readLine()) != null) {
                List<String> l = SAMUtils.getAlignElements(temp);
                if (Integer.parseInt(l.get(1)) > 2000) continue; //remove supplement alignment to have a pair of alignments for PE reads
                queryCount++;
                List<SNP> snpList = SAMUtils.getVariants(l, mapQThresh);
                if (queryCount == 1) {
                    if (snpList != null) tagSNPList.addAll(snpList);
                }
                else if (queryCount == 2) {
                    if (l.get(6).equals("=")) {
                        double len = Math.abs(Double.valueOf(l.get(8)));
                        if (len < maxMappingIntervalThresh) {
                            if (snpList != null) tagSNPList.addAll(snpList);
                            cnt++;
                            if (cnt%10000000 == 0) System.out.println(String.valueOf(cnt) + " tags are properly aligned for SNP calling");                           
                            if (tagSNPList.size() > 0 && tagSNPList.size() < maxDivergence) {
                                List<String> ll = PStringUtils.fastSplit(l.get(0), "_");                               
                                int groupIndex = Integer.parseInt(ll.get(0));
                                int tagIndex = Integer.parseInt(ll.get(1));
                                Collections.sort(tagSNPList);        
                                setSNPOfTag(groupIndex, tagIndex, tagSNPList);
                                snpCnt++;
                            }
                        }
                    }
                    queryCount = 0;
                    tagSNPList = new ArrayList();
                }
            }
            br.close();
            System.out.println("A total of "+String.valueOf(cnt) + " tags are properly aligned for SNP calling");
            System.out.println("A total of "+String.valueOf(snpCnt) + " tags have SNP calls");
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public SNPCounts getSNPCounts () {
        return new SNPCounts(this);
    }
    
    public boolean addTagAnnotations (TagAnnotations ata) {
        if (this.getTagLengthInLong() != ata.getTagLengthInLong()) return false;
        if (this.getGroupIdentiferOffset() != ata.getGroupIdentiferOffset()) return false;
        if (this.getGroupIdentiferLength() != ata.getGroupIdentiferLength()) return false;
        taList.parallelStream().forEach(ta -> {
            ta.r1LenList.addAll(ata.taList.get(ta.groupIndex).r1LenList);
            ta.r2LenList.addAll(ata.taList.get(ta.groupIndex).r2LenList);
            ta.readCountList.addAll(ata.taList.get(ta.groupIndex).readCountList);
            ta.tagList.addAll(ata.taList.get(ta.groupIndex).tagList);
            ta.SNPList.addAll(ata.taList.get(ta.groupIndex).SNPList);
            ta.allelePosList.addAll(ata.taList.get(ta.groupIndex).allelePosList);
            ta.alleleList.addAll(ata.taList.get(ta.groupIndex).alleleList);
        });
        this.ifSorted = false;
        return true;
    }
    
    public void sortSNPListByPosition (int groupIndex, int tagIndex) {
        this.taList.get(groupIndex).sortSNPListByPosition(tagIndex);
    }
    
    public void sortAlleleListByPosition (int groupIndex, int tagIndex) {
        this.taList.get(groupIndex).sortAlleleListByPosition(tagIndex);
    }
    
    public void sort () {
        System.out.println("TagAnnotations sort begins");
        taList.parallelStream().forEach(ta -> {
            ta.sort();
        });
        System.out.println("TagAnnotations sort ends");
        this.ifSorted = true;
    }
    
    public boolean isSorted () {
        return this.ifSorted;
    }
    
    public int getMaxTagNumberAcrossGroups () {
        int max = Integer.MIN_VALUE;
        for (int i = 0; i < this.getGroupNumber(); i++) {
            if (this.getTagNumber(i) > max) max = this.getTagNumber(i);
        }
        return max;
    }
    
    public int getReadNumber (int groupIndex, int tagIndex) {
        return taList.get(groupIndex).getReadNumber(tagIndex);
    }
    
    public byte getR1TagLength (int groupIndex, int tagIndex) {
        return taList.get(groupIndex).getR1TagLength(tagIndex);
    }
    
    public byte getR2TagLength (int groupIndex, int tagIndex) {
        return taList.get(groupIndex).getR2TagLength(tagIndex);
    }
    
    public int getTagIndex (long[] tag, int groupIndex) {
        return taList.get(groupIndex).getTagIndex(tag);
    }
    
    public int getGroupIndex (long[] tag) {
        return TagUtils.getGroupIndexFromTag(tag, this.getGroupIdentiferOffset(), this.getGroupIdentiferLength());
    }
    
    public long getTotalReadNumber () {
        long sum = 0; 
        for (int i = 0; i < this.getGroupNumber(); i++) {
            sum += taList.get(i).getTotalReadNum();
        }
        return sum;
    }
    
    public int getTagNumber (int groupIndex) {
        return taList.get(groupIndex).getTagNumber();
    }
    
    public long getTagNumber () {
        long sum = 0;
        for (int i = 0; i < this.getGroupNumber(); i++) {
            sum += taList.get(i).getTagNumber();
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
        return taList.get(groupIndex).getTag(tagIndex);
    }
    
    public byte getSNPNumberOfTag (int groupIndex, int tagIndex) {
        return (byte)this.getSNPOfTag(groupIndex, tagIndex).size();
    }
    
    public List<SNP> getSNPOfTag (int groupIndex, int tagIndex) {
        return this.taList.get(groupIndex).getSNPOfTag(tagIndex);
    } 
    
    public byte getAlleleNumberOfTag (int groupIndex, int tagIndex) {
        return (byte)this.getAlleleOfTag(groupIndex, tagIndex).size();
    }
    
    public TByteArrayList getAlleleOfTag (int groupIndex, int tagIndex) {
        return this.taList.get(groupIndex).getAlleleOfTag(tagIndex);
    }
    
    public List<ChrPos> getAllelePosOfTag (int groupIndex, int tagIndex) {
        return this.taList.get(groupIndex).getAllelePosOfTag(tagIndex);
    }
    
    public void setSNPOfTag (int groupIndex, int tagIndex, List<SNP> tagSNPList) {
        taList.get(groupIndex).setSNPOfTag(tagIndex, tagSNPList);
    }
    
    public void setAlleleOfTag (int groupIndex, int tagIndex, List<ChrPos> tagAllelePosList, TByteArrayList tagAlleleList, TByteArrayList[] alleleRelativePos) {
        taList.get(groupIndex).setAlleleOfTag(tagIndex, tagAllelePosList, tagAlleleList, alleleRelativePos);
    }
    
    public void removeSNPOfTag (int groupIndex, int tagIndex) {
        taList.get(groupIndex).removeSNPOfTag(tagIndex);
    }
    
    public void removeAlleleOfTag (int groupIndex, int tagIndex) {
        taList.get(groupIndex).removeAlleleOfTag(tagIndex);
    }
    
    public void removeAllSNP () {
        for (int i = 0; i < this.getGroupNumber(); i++) {
            for (int j = 0; j < this.getTagNumber(i); j++) {
                this.removeSNPOfTag(i, j);
            }
        }
    }
    
    public void removeAllAllele () {
        for (int i = 0; i < this.getGroupNumber(); i++) {
            for (int j = 0; j < this.getTagNumber(i); j++) {
                this.removeAlleleOfTag(i, j);
            }
        }
    }
    
    public void collapseCounts (int minReadCount) {
        System.out.println("Start collapsing read counts of TagAnnotations with "+this.getTagNumber()+" tags.");
        AtomicInteger acnt = new AtomicInteger();
        AtomicInteger gcnt = new AtomicInteger();
        AtomicInteger pcnt = new AtomicInteger();
        if (this.isSorted() == false) this.sort();
        NumberFormat defaultFormat = NumberFormat.getPercentInstance();
        defaultFormat.setMinimumFractionDigits(1);
	int step = (int)(this.getGroupNumber()*0.2);
        taList.parallelStream().forEach(ta -> {
            int cnt = ta.collapseCounts(minReadCount);
            acnt.addAndGet(cnt);
            int count = gcnt.addAndGet(1);            
            if (count%step == 0) {
                System.out.println("Colapsed " + defaultFormat.format(0.1*pcnt.addAndGet(2)));
            }
        });     
        System.out.println("Collapsing tags complected. Tag rows collapsed after sorting: " + acnt.get());
    }
}
