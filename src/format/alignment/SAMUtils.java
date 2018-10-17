/*
 * SAMUtils
 */
package format.alignment;

import format.dna.snp.SNP;
import gnu.trove.list.array.TByteArrayList;
import gnu.trove.list.array.TIntArrayList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import utils.PStringUtils;
import utils.Tuple;

/**
 *
 * @author Fei Lu
 */
public class SAMUtils {
    /**
     * CIGAR consume operators are '=', 'D', 'M', 'N', 'X'
     */
    public static final byte[] consumeCigarOPByte = {61, 68, 77, 78, 88};
    
    /**
     * Return a {@link format.alignment.SEAlignRecord} object from an input alignment
     * @param inputStr
     * @return 
     */
    public static SEAlignRecord getSEAlignRecord (String inputStr) {
        return getSEAlignRecord (PStringUtils.fastSplit(inputStr));
    }
    
    /**
     * Return a {@link format.alignment.SEAlignRecord} object from elements of an input alignment
     * @param l
     * @return 
     */
    private static SEAlignRecord getSEAlignRecord (List<String> l) {
        int flag = Integer.parseInt(l.get(1));
        String hit = null;
        byte strand = Byte.MIN_VALUE;
        int startPos = Integer.MIN_VALUE;
        int endPos = Integer.MIN_VALUE;
        short mapQ = Short.MIN_VALUE;
        short alnMatchNumber = Short.MIN_VALUE;
        short editDistance = Short.MIN_VALUE;
        if (l.get(5).equals("*")) {
            //continue;
        }
        else {
            hit = l.get(2);
            if (((flag >> 4) & 1) == 1) strand = 0;
            else strand = 1;
            startPos = Integer.parseInt(l.get(3));
            String cigar = l.get(5);
            Tuple<TByteArrayList, TIntArrayList> cigarOpPosIndex = getCigarOPAndPosIndex (cigar);
            endPos = getEndPos(cigar, cigarOpPosIndex, startPos);
            mapQ = Short.parseShort(l.get(4));
            alnMatchNumber  = getAlignMatchNumberInCigar (cigar, cigarOpPosIndex);
            editDistance = Short.valueOf(l.get(11).split(":")[2]);
        }
        SEAlignRecord sar = new SEAlignRecord (l.get(0), hit, startPos, endPos, strand, mapQ, alnMatchNumber, editDistance);
        return sar;
    }
    
    /**
     * Return the end position of reference from an alignment
     * Return -1 if the query is not aligned, in which cigar is *
     * @param cigar
     * @param startPos
     * @return 
     */
    public static int getEndPos (String cigar, int startPos) {
        Tuple<TByteArrayList, TIntArrayList> opPosIndex = getCigarOPAndPosIndex(cigar);
        return getEndPos(cigar, opPosIndex, startPos);
    }
    
    /**
     * Return the end position of reference from an alignment
     * Return -1 if the query is not aligned, in which cigar is *
     * @param cigar
     * @param opPosIndex
     * @param startPos
     * @return 
     */
    private static int getEndPos (String cigar, Tuple<TByteArrayList, TIntArrayList> opPosIndex, int startPos) {
        if (opPosIndex == null) return -1;
        byte[] op = opPosIndex.getFirstElement().toArray();
        int[] posIndex = opPosIndex.getSecondElement().toArray();
        int endPos = startPos - 1;
        for (int i = 0; i < op.length; i++) {
            int index = Arrays.binarySearch(consumeCigarOPByte, op[i]);
            if (index < 0) continue;
            if (i == 0) {
                endPos += Integer.valueOf(cigar.substring(0, posIndex[i]));
            }
            else {
                endPos += Integer.valueOf(cigar.substring(posIndex[i-1]+1, posIndex[i]));
            }
        }
        return endPos;
    }
    
    /**
     * Return total length of alignment match in CIGAR, including both sequence match and mismatch
     * @param cigar
     * @param cigarOpPosIndex
     * @return 
     */
    private static short getAlignMatchNumberInCigar (String cigar, Tuple<TByteArrayList, TIntArrayList> cigarOpPosIndex) {
        byte[] op = cigarOpPosIndex.getFirstElement().toArray();
        int[] posIndex = cigarOpPosIndex.getSecondElement().toArray();
        int len = 0;
        for (int i = 0; i < op.length; i++) {
            if (op[i] != 77) continue;
            if (i == 0) {
                len += Integer.valueOf(cigar.substring(0, posIndex[i]));
            }
            else {
                len += Integer.valueOf(cigar.substring(posIndex[i-1]+1, posIndex[i]));
            }
        }
        return (short)len;
    }
    
    /**
     * Return operators and their position index of CIGAR in a {@link utils.Tuple} format
     * @param cigar
     * @return 
     */
    private static Tuple<TByteArrayList, TIntArrayList> getCigarOPAndPosIndex (String cigar) {
        if (cigar.startsWith("*")) return null;
        TByteArrayList opList = new TByteArrayList();
        TIntArrayList posList = new TIntArrayList();
        byte[] cigarB = cigar.getBytes();
        for (int i = 0; i < cigar.length(); i++) {
            if (cigarB[i] > 64) {
                opList.add(cigarB[i]);
                posList.add(i);
            }
        }
        return new Tuple<TByteArrayList, TIntArrayList> (opList, posList);
    }
    
    public static List<SNP> getVariation (String inputStr, int mapQThresh) {
        List<String> l = PStringUtils.fastSplit(inputStr);
        String cigar = l.get(5);
        if (cigar.startsWith("*")) return null;
        if (Integer.parseInt(l.get(4)) < mapQThresh) return null;
        short chr = (short)Integer.parseInt(l.get(2));
        int startPos = Integer.parseInt(l.get(3));
        String seq = l.get(9);
        String md = l.get(12).split(":")[2];
        Tuple<TByteArrayList, TIntArrayList> cigarOPPosIndex = getCigarOPAndPosIndex(cigar);
        TByteArrayList opList = cigarOPPosIndex.getFirstElement();
        TIntArrayList posIndexList = cigarOPPosIndex.getSecondElement();
        if (opList.get(0) == 83) {//S
            int length = Integer.parseInt(cigar.substring(0, posIndexList.get(0)));
            startPos = startPos+length-1;
            seq = seq.substring(length);
            cigar = cigar.substring(posIndexList.get(0)+1);
            if (opList.get(opList.size()-1) == 83 || opList.get(opList.size()-1) == 72) {
                cigarOPPosIndex = getCigarOPAndPosIndex(cigar);
                opList = cigarOPPosIndex.getFirstElement();
                posIndexList = cigarOPPosIndex.getSecondElement();
            }
        }
        else if (opList.get(0) == 72) {//H
            int length = Integer.parseInt(cigar.substring(0, posIndexList.get(0)));
            startPos = startPos+length-1;
            cigar = cigar.substring(posIndexList.get(0)+1);
            if (opList.get(opList.size()-1) == 83 || opList.get(opList.size()-1) == 72) {
                cigarOPPosIndex = getCigarOPAndPosIndex(cigar);
                opList = cigarOPPosIndex.getFirstElement();
                posIndexList = cigarOPPosIndex.getSecondElement();
            }
        }
        if (opList.get(opList.size()-1) == 83) {//S
            int length = Integer.parseInt(cigar.substring(posIndexList.get(opList.size()-2)+1, posIndexList.get(opList.size()-1)));
            seq = seq.substring(0, seq.length()-length);
            cigar = cigar.substring(0, posIndexList.get(opList.size()-2)+1);
        }
        else if (opList.get(opList.size()-1) == 72) {//H
            cigar = cigar.substring(0, posIndexList.get(opList.size()-2)+1);
        }
        List<SNP> snpList = new ArrayList();
        int currentPos = startPos-1;
        int currentAltPos = 0;
        if (opList.contains((byte)73)) {//I 18M1I31M1D46M
            for (int i = 0; i < cigar.length(); i++) {
                char c = cigar.charAt(i);
                if (Character.isDigit(c)) {
                    int j;
                    for (j = i + 1; j < cigar.length(); j++) {
                        if (!Character.isDigit(cigar.charAt(j))) {
                            break;
                        }
                    }
                    int length = Integer.parseInt(cigar.substring(i, j));
                    char cop = cigar.charAt(j);
                    if (cop == 'I') {
                        char alt = 'I';
                        char ref = seq.charAt(currentAltPos-1);
                        snpList.add(new SNP(chr, currentPos, ref, alt));
                        StringBuilder sb = new StringBuilder(seq);
                        sb.delete(currentAltPos, currentAltPos+length);
                        seq = sb.toString();
//                        System.out.println(seq);
//                        int a = 3;
//                        currentAltPos += length;
                    }
                    else if (cop == 'D' || cop == 'N') {
                        currentPos += length;
                    }
                    else { //M,=,X
                        currentPos += length;
                        currentAltPos+=length;
                    }
                    i = j-1;
                }
                else  {
                    //do nothing
                }
            }
        }
        currentPos = startPos-1;
        currentAltPos = 0;   
        for (int i = 0; i < md.length(); i++) {
            char c = md.charAt(i);
            if (Character.isDigit(c)) {
                int j;
                for (j = i + 1; j < md.length(); j++) {
                    if (!Character.isDigit(md.charAt(j))) {
                        break;
                    }
                }
                int length = Integer.parseInt(md.substring(i, j));
                currentPos += length;
                currentAltPos += length;
                i = j-1;
            }
            else if (Character.isLetter(c)){
                currentPos++;
                currentAltPos++;
                char alt = seq.charAt(currentAltPos-1);
                snpList.add(new SNP(chr, currentPos, c, alt));
            }
            else if (c == '^'){
                int j;
                for (j = i + 1; j < md.length(); j++) {
                    if (!Character.isLetter(md.charAt(j))) break;
                }
                String deletionS = md.substring(i+1, j);
                char alt = 'D';
                char ref = md.charAt(i+1);
                snpList.add(new SNP(chr, currentPos+1, ref, alt));
                currentPos += deletionS.length();
                i = j -1;
            }
            else {
                System.out.println(c);
            }
        }
        Collections.sort(snpList);
        return snpList;
    }
}
