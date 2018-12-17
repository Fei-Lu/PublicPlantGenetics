/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package analysis.pipeline.grt;

import cern.colt.GenericSorting;
import cern.colt.Swapper;
import cern.colt.function.IntComparator;
import format.dna.BaseEncoder;
import gnu.trove.list.array.TIntArrayList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import utils.PArrayUtils;
import utils.Tuple;

/**
 *
 * @author feilu
 */
public class TagFinder {
    int paraLevel = 32;
    TagAnnotations tas = null;
    GroupIntSeqFinder[] gsfs = null;
    int ceaseSearchingSize = 3;
    
    public TagFinder (TagAnnotations tas) {
        this.tas = tas;
        this.initialize(tas);
    }
    
    public Tuple<int[], int[]> getMostSimilarTags (long[] tag, byte r1Length, byte r2Length, int groupIndex, int maxDivergence) {
        TIntArrayList r1IntSeqList = this.getR1IntSeq(tag, r1Length);
        TIntArrayList r2IntSeqList = this.getR2IntSeq(tag, r2Length);       
        int[] startEndIndex = null;
        int minSize = Integer.MAX_VALUE;
        boolean ifCease = false;
        for (int i = 0; i < r1IntSeqList.size(); i++) {
            int[] currentSE = gsfs[groupIndex].getStartEndIndex(r1IntSeqList.get(i));
            if (currentSE == null) continue;
            else {
                int currentSize = currentSE[1] - currentSE[0];
                if (currentSize < minSize) {
                    minSize = currentSize;
                    startEndIndex = currentSE;
                }
                if (minSize < ceaseSearchingSize) {
                    ifCease = true;
                    break;
                }
            }
        }
        if (!ifCease) {
            for (int i = 0; i < r2IntSeqList.size(); i++) {
                int[] currentSE = gsfs[groupIndex].getStartEndIndex(r2IntSeqList.get(i));
                if (currentSE == null) continue;
                else {
                    int currentSize = currentSE[1] - currentSE[0];
                    if (currentSize < minSize) {
                        minSize = currentSize;
                        startEndIndex = currentSE;
                    }
                }
                if (minSize < ceaseSearchingSize) {
                    ifCease = true;
                    break;
                }
            }
        }
        TIntArrayList mismatchList = new TIntArrayList();
        TIntArrayList tagIndicesList = new TIntArrayList();
        for (int i = startEndIndex[0]; i < startEndIndex[1]; i++) {
            int dbTagIndex = gsfs[groupIndex].getTagIndex(i);
            long[] dbTag = tas.getTag(groupIndex, dbTagIndex);
            int currentMismatch = 0;
            boolean toContinue = false;
            for (int j = 0; j < dbTag.length; j++) {
                byte diff = BaseEncoder.getSeqDifferences(dbTag[j], tag[j], maxDivergence);
                if (diff > maxDivergence) {
                    toContinue = true;
                    break;
                }
                currentMismatch+= diff;
            }
            if (toContinue) continue;
            if (currentMismatch > maxDivergence) continue;
            mismatchList.add(currentMismatch);
            tagIndicesList.add(dbTagIndex);
        }
        int[] mismatch = mismatchList.toArray();
        int[] tagIndices = tagIndicesList.toArray();
        Tuple<int[], int[]> t = new Tuple<>(mismatch, tagIndices);
        t.sortByFirstIntInt();
        mismatch = t.getFirstElement();
        tagIndices = t.getSecondElement();
        int minMismatch = mismatch[0];
        if (minMismatch > maxDivergence) return null;
        int size = 1;
        for (int i = 1; i < mismatch.length; i++) {
            if (mismatch[i] == minMismatch) size++;
            else break;
        }
        int[] mismatchArray = new int[size];
        int[] tagIndicesArray = new int[size];
        for (int i = 0; i < size; i++) {
            mismatchArray[i] = mismatch[i];
            tagIndicesArray[i] = tagIndices[i];
        }
        Tuple<int[], int[]> result = new Tuple<>(mismatchArray, tagIndicesArray);
        return result;
    }
    
    private void initialize (TagAnnotations tas) {
        System.out.println("Start building TagFinder from TagAnnotations");
        int groupNumber  = tas.getGroupNumber();
        gsfs = new GroupIntSeqFinder[groupNumber];
        List<TagAnnotation> taList = tas.taList;
        int[][] indices = PArrayUtils.getSubsetsIndicesBySubsetSize(taList.size(), this.paraLevel);
        for (int i = 0; i < indices.length; i++) {
            List<Integer> subIndexList = new ArrayList();
            int size = indices[i][1]-indices[i][0];
            for (int j = 0; j < size; j++) {
                subIndexList.add(j+indices[i][0]);
            }
            subIndexList.stream().forEach(currentIndex -> {
                TagAnnotation ta = taList.get(currentIndex);
                TIntArrayList intSeqList = new TIntArrayList();
                TIntArrayList indexList = new TIntArrayList();
                int tagNumber  = ta.getTagNumber();
                for (int j = 0; j < tagNumber; j++) {
                    long[] tag = ta.getTag(j);
                    byte r1Length = ta.getR1TagLength(j);
                    byte r2Length = ta.getR2TagLength(j);
                    TIntArrayList tagInt = this.getIntSeq(tag, r1Length, r2Length);                    
                    intSeqList.addAll(tagInt);
                    for (int k = 0; k < tagInt.size(); k++) indexList.add(j);
                }
                GroupIntSeqFinder gsf = new GroupIntSeqFinder(intSeqList, indexList);
                gsfs[currentIndex] = gsf;
            });
        }
        System.out.println("Finish building TagFinder");
    }
    
    private TIntArrayList getR2IntSeq (long[] tag, byte r2Length) {
        TIntArrayList seqList = new TIntArrayList();
        int[] intTag = BaseEncoder.getIntSeqsFromLongSeqs(tag);
        int n = r2Length/BaseEncoder.intChunkSize;
        for (int i = 0; i < n; i++) {
            seqList.add(intTag[i+intTag.length/2]);
        }
        return seqList;
    }
    
    private TIntArrayList getR1IntSeq (long[] tag, byte r1Length) {
        TIntArrayList seqList = new TIntArrayList();
        int[] intTag = BaseEncoder.getIntSeqsFromLongSeqs(tag);
        int n = r1Length/BaseEncoder.intChunkSize;
        for (int i = 0; i < n; i++) {
            seqList.add(intTag[i]);
        }
        return seqList;
    }
    
    private TIntArrayList getIntSeq (long[] tag, byte r1Length, byte r2Length) {
        TIntArrayList seqList = new TIntArrayList();
        int[] intTag = BaseEncoder.getIntSeqsFromLongSeqs(tag);
        int n = r1Length/BaseEncoder.intChunkSize;
        for (int i = 0; i < n; i++) {
            seqList.add(intTag[i]);
        }
        n = r2Length/BaseEncoder.intChunkSize;
        for (int i = 0; i < n; i++) {
            seqList.add(intTag[i+intTag.length/2]);
        }
        return seqList;
    }
}

class GroupIntSeqFinder {
    int[] intSeq;
    int[] tagIndices;
    
    public GroupIntSeqFinder (TIntArrayList intSeqList, TIntArrayList indexList) {
        this.intSeq = intSeqList.toArray();
        this.tagIndices = indexList.toArray();
        this.sort();
    }
    
    public int getTagIndex (int intSeqIndex) {
        return tagIndices[intSeqIndex];
    }
    
    public int[] getStartEndIndex (int seq) {
        int index = this.binarySearch(seq);
        if (index < 0) return null;
        int[] startEnd = new int[2];
        startEnd[0] = index;//inclusive
        startEnd[1] = index+1;//exclusive
        for (int i = index-1; i > -1; i--) {
            if (intSeq[i] == seq) {
                startEnd[0] = i;
            }
            else break;
        }
        for (int i = index+1; i < intSeq.length; i++) {
            startEnd[1] = i;
            if (intSeq[i] != seq) {
                break;
            }
            else {
                if (i == intSeq.length-1) startEnd[1] = intSeq.length;
            }
        }
        return startEnd;
    }
    
    public int binarySearch (int seq) {
        return Arrays.binarySearch(intSeq, seq);
    }
    
    public void sort () {
        GenericSorting.quickSort(0, intSeq.length, comp, swapper);
    }
    
    Swapper swapper = new Swapper() {
        @Override
        public void swap(int index1, int index2) {
            int temp = intSeq[index1];
            intSeq[index1] = intSeq[index2];
            intSeq[index2] = temp;
            temp = tagIndices[index1];
            tagIndices[index1] = tagIndices[index2];
            tagIndices[index2] = temp;
        }
    };
    
    IntComparator comp = new IntComparator() {
        @Override
        public int compare(int index1, int index2) {
            if (intSeq[index1] < intSeq[index2]) return -1;
            else if (intSeq[index1] > intSeq[index2]) return 1;
            else {
                if (tagIndices[index1] < tagIndices[index2]) return -1;
                else if (tagIndices[index1] > tagIndices[index2]) return 1;
                return 0;
            }
        }
    };
}
