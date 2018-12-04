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
import gnu.trove.list.array.TIntArrayList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import utils.PArrayUtils;

/**
 *
 * @author feilu
 */
public class TagFinder {
    int paraLevel = 32;
    GroupIntSeqFinder[] gsfs = null;
    
    public TagFinder (TagAnnotations tas) {
        this.initialize(tas);
    }
    
    public void findMostSimilarTag (long[] tag, byte r1Length, byte r2Length, int groupIndex) {
        TIntArrayList r1IntSeqList = this.getR1IntSeq(tag, r1Length);
        TIntArrayList r2IntSeqList = this.getR2IntSeq(tag, r1Length);
        int[] startEndIndex = null;
        int minSize = Integer.MAX_VALUE;
        for (int i = 0; i < r1IntSeqList.size(); i++) {
            int[] currentSE = gsfs[groupIndex].getStartEndIndices(r1IntSeqList.get(i));
            if (currentSE == null) continue;
            else {
                int currentSize = currentSE[1] - currentSE[0];
                if (currentSize < minSize) {
                    minSize = currentSize;
                    startEndIndex = currentSE;
                }
            }
        }
        for (int i = startEndIndex[0]; i < startEndIndex[1]; i++) {
            
            
            
        }
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
    int[] indices;
    
    public GroupIntSeqFinder (TIntArrayList intSeqList, TIntArrayList indexList) {
        this.intSeq = intSeqList.toArray();
        this.indices = indexList.toArray();
        this.sort();
    }
    
    public int[] getStartEndIndices (int seq) {
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
            temp = indices[index1];
            indices[index1] = indices[index2];
            indices[index2] = temp;
        }
    };
    
    IntComparator comp = new IntComparator() {
        @Override
        public int compare(int index1, int index2) {
            if (intSeq[index1] < intSeq[index2]) return -1;
            else if (intSeq[index1] > intSeq[index2]) return 1;
            else {
                if (indices[index1] < indices[index2]) return -1;
                else if (indices[index1] > indices[index2]) return 1;
                return 0;
            }
        }
    };
}
