/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package analysis.pipeline.grt;

import cern.colt.GenericSorting;
import cern.colt.Swapper;
import cern.colt.function.IntComparator;
import format.dna.snp.SNP;
import format.position.ChrPos;
import gnu.trove.list.array.TByteArrayList;
import gnu.trove.list.array.TIntArrayList;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author feilu
 */
public class TagAnnotation implements Swapper, IntComparator {
    protected int groupIndex = -1;
    protected List<long[]> tagList = null;
    protected TByteArrayList r1LenList = null;
    protected TByteArrayList r2LenList = null;
    protected TIntArrayList readCountList = null;
    protected List<List<SNP>> SNPList = null;
    protected List<List<ChrPos>> allelePosList = null;
    protected List<TByteArrayList> alleleList = null;
    protected List<TByteArrayList[]> alleleRelativePosList = null;
    

    TagAnnotation (int groupIndex) {
        this.groupIndex = groupIndex;
        tagList = new ArrayList<>();
        r1LenList = new TByteArrayList();
        r2LenList = new TByteArrayList();
        readCountList = new TIntArrayList();
        SNPList = new ArrayList<>();
        allelePosList = new ArrayList<>();
        alleleList = new ArrayList<>();
        alleleRelativePosList = new ArrayList<>();
    }
    
    TagAnnotation (int tagLengthInLong, int groupIndex, int tagNumber, boolean ifSorted) {
        this.groupIndex = groupIndex;
        tagList = new ArrayList(tagNumber);
        r1LenList = new TByteArrayList(tagNumber);
        r2LenList = new TByteArrayList(tagNumber);
        readCountList = new TIntArrayList(tagNumber);
        SNPList = new ArrayList<>();
        allelePosList = new ArrayList<>();
        alleleList = new ArrayList<>();
        alleleRelativePosList = new ArrayList<>();
    }
    
    void appendTag (long[] tag, byte r1Len, byte r2Len, int readNumber) {
        tagList.add(tag);
        r1LenList.add(r1Len);
        r2LenList.add(r2Len);
        readCountList.add(readNumber);
        SNPList.add(new ArrayList<SNP>());
        allelePosList.add(new ArrayList<ChrPos>());
        alleleList.add(new TByteArrayList());
        TByteArrayList[] alleleRelativePos = new TByteArrayList[2];
        alleleRelativePos[0] = new TByteArrayList();
        alleleRelativePos[1] = new TByteArrayList();
        alleleRelativePosList.add(alleleRelativePos);
    }
    
    void appendTag (long[] tag, byte r1Len, byte r2Len, int readNumber, List<SNP> tagSNPList, List<ChrPos> tagAllelePosList, TByteArrayList tagAlleleList) {
        tagList.add(tag);
        r1LenList.add(r1Len);
        r2LenList.add(r2Len);
        readCountList.add(readNumber);
        SNPList.add(tagSNPList);
        allelePosList.add(tagAllelePosList);
        alleleList.add(tagAlleleList);
    }
    
    long[] getTag (int tagIndex) {
        return this.tagList.get(tagIndex);
    }
    
    byte getR1TagLength (int tagIndex) {
        return this.r1LenList.get(tagIndex);
    }
    
    byte getR2TagLength (int tagIndex) {
        return this.r2LenList.get(tagIndex);
    }
    
    List<SNP> getSNPOfTag (int tagIndex) {
        return this.SNPList.get(tagIndex);
    }
    
    List<ChrPos> getAllelePosOfTag (int tagIndex) {
        return this.allelePosList.get(tagIndex);
    }
    
    TByteArrayList getAlleleOfTag (int tagIndex) {
        return this.alleleList.get(tagIndex);
    }
    
    int getTotalReadNum () {
        int cnt = 0;
        for (int i = 0; i < this.getTagNumber(); i++) {
            cnt+=this.getReadNumber(i);
        }
        return cnt;
    }
    
    int getReadNumber (int tagIndex) {
        return this.readCountList.get(tagIndex);
    }
    
    int getTagNumber () {
        return this.tagList.size();
    }
    
    int getTagIndex (long[] tag) {
        return Collections.binarySearch(tagList, tag, TagUtils.tagCom);
    }  
    
    void setSNPOfTag (int tagIndex, List<SNP> tagSNPList) {
        this.SNPList.set(tagIndex, tagSNPList);
    }
    
    void setAlleleOfTag (int tagIndex, List<ChrPos> tagAllelePosList, TByteArrayList tagAlleleList, TByteArrayList[] alleleRelativePos) {
        this.allelePosList.set(tagIndex, tagAllelePosList);
        this.alleleList.set(tagIndex, tagAlleleList);
        this.alleleRelativePosList.set(tagIndex, alleleRelativePos);
    }
    
    void removeSNPOfTag (int tagIndex) {
        this.SNPList.get(tagIndex).clear();
    }
    
    void removeAlleleOfTag (int tagIndex) {
        this.allelePosList.get(tagIndex).clear();
        this.alleleList.get(tagIndex).clear();
    }
    
    @Override
    public void swap (int index1, int index2) {
        long[] temp = tagList.get(index1);
        tagList.set(index1, tagList.get(index2));
        tagList.set(index2, temp);
        byte tl = r1LenList.get(index1);
        r1LenList.set(index1, r1LenList.get(index2));
        r1LenList.set(index2, tl);
        tl = r2LenList.get(index1);
        r2LenList.set(index1, r2LenList.get(index2));
        r2LenList.set(index2, tl);
        int tc = readCountList.get(index1);
        readCountList.set(index1, readCountList.get(index2));
        readCountList.set(index2, tc);
        List<SNP> tempSNP = SNPList.get(index1);
        SNPList.set(index1, SNPList.get(index2));
        SNPList.set(index2, tempSNP);
        List<ChrPos> tempPos = allelePosList.get(index1);
        allelePosList.set(index1, allelePosList.get(index2));
        allelePosList.set(index2, tempPos);
        TByteArrayList tb = alleleList.get(index1);
        alleleList.set(index1, alleleList.get(index2));
        alleleList.set(index2, tb);
        TByteArrayList[] tp = alleleRelativePosList.get(index1);
        alleleRelativePosList.set(index1, alleleRelativePosList.get(index2));
        alleleRelativePosList.set(index2, tp);
    }

    @Override
    public int compare (int index1, int index2) {
        for (int i = 0; i < this.tagList.get(0).length; i++) {
            if (tagList.get(index1)[i] < tagList.get(index2)[i]) {
                return -1;
            }
            if (tagList.get(index1)[i] > tagList.get(index2)[i]) {
                return 1;
            }
        }
        return 0;
    }
    
    void sortAlleleListByPosition (int tagIndex) {
        PosAllele pa = new PosAllele(this.allelePosList.get(tagIndex), this.alleleList.get(tagIndex));
        pa.sort();
        this.allelePosList.set(tagIndex, pa.pList);
        this.alleleList.set(tagIndex, pa.aList);
    }
    
    class PosAllele {
        List<ChrPos> pList = null;
        TByteArrayList aList = null;
        
        public PosAllele (List<ChrPos> pList, TByteArrayList aList) {
            this.pList = pList;
            this.aList = aList;
        }
        
        public void sort () {
            GenericSorting.quickSort(0, pList.size(), posComp, posSwapper);
        }
        
        Swapper posSwapper = new Swapper() {
            @Override
            public void swap(int index1, int index2) {
                ChrPos tempPos = pList.get(index1);
                pList.set(index1, pList.get(index2));
                pList.set(index2, tempPos);
                byte temp = aList.get(index1);
                aList.set(index1, aList.get(index2));
                aList.set(index2, temp);
            }
        };

        IntComparator posComp = new IntComparator() {
            @Override
            public int compare(int index1, int index2) {
                return pList.get(index1).compareTo(pList.get(index2));
            }
        };
    }
    
    
    void sortSNPListByPosition (int tagIndex) {
        Collections.sort(this.SNPList.get(tagIndex));
    }
    
    void sort () {
        //System.out.println("TagCount sort begins");
        GenericSorting.quickSort(0, this.getTagNumber(), this, this);
        //System.out.println("TagCount sort ends");
    }
    
    protected int collapseCounts (int minReadCount) {
        int collapsedRows = 0;
        for (int i = 0; i < this.getTagNumber()-1; i++) {
            if (this.readCountList.get(i) == 0) continue;
            for (int j = i + 1; j < this.getTagNumber(); j++) {
                int index = this.compare(i, j);
                if (index < 0) break;
                else {
                    int sum = readCountList.get(i)+readCountList.get(j);
                    readCountList.set(i, sum);
                    collapsedRows++;
                    readCountList.set(j, 0);
                }
            }
        }
        List<long[]> aTagList = new ArrayList<>();
        TByteArrayList aR1LenList = new TByteArrayList();
        TByteArrayList aR2LenList = new TByteArrayList();
        TIntArrayList aReadCountList = new TIntArrayList();
        List<List<SNP>> aSNPList = new ArrayList<>();
        List<List<ChrPos>> aAllelePosList = new ArrayList<>();
        List<TByteArrayList> aAlleleList = new ArrayList<>();
        for (int i = 0; i < this.getTagNumber(); i++) {
            if (readCountList.get(i) < minReadCount) continue;
            aTagList.add(tagList.get(i));
            aR1LenList.add(r1LenList.get(i));
            aR2LenList.add(r2LenList.get(i));
            aReadCountList.add(readCountList.get(i));
            aSNPList.add(SNPList.get(i));
            aAllelePosList.add(allelePosList.get(i));
            aAlleleList.add(alleleList.get(i));
        }
        tagList = aTagList;
        r1LenList = aR1LenList;
        r2LenList = aR2LenList;
        readCountList = aReadCountList;
        SNPList = aSNPList;
        allelePosList = aAllelePosList;
        alleleList = aAlleleList;
        aTagList = null;
        aR1LenList = null;
        aR2LenList = null;
        aReadCountList = null;
        aSNPList = null;
        aAllelePosList = null;
        aAlleleList = null;
        return collapsedRows;
    }
}
