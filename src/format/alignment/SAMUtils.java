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
        String seq = l.get(9);
        String md = l.get(12).split(":")[2];
        Tuple<TByteArrayList, TIntArrayList> cigarOPPosIndex = getCigarOPAndPosIndex(cigar);
        TByteArrayList opList = cigarOPPosIndex.getFirstElement();
        TIntArrayList posIndexList = cigarOPPosIndex.getSecondElement();
        if (opList.get(0) == 83) {//S
            int length = Integer.parseInt(cigar.substring(0, posIndexList.get(0)));
            seq = seq.substring(length);
            cigar = cigar.substring(posIndexList.get(0)+1);
            if (opList.get(opList.size()-1) == 83 || opList.get(opList.size()-1) == 72) {
                cigarOPPosIndex = getCigarOPAndPosIndex(cigar);
                opList = cigarOPPosIndex.getFirstElement();
                posIndexList = cigarOPPosIndex.getSecondElement();
            }
        }
        else if (opList.get(0) == 72) {
            cigar = cigar.substring(posIndexList.get(0)+1);
            if (opList.get(opList.size()-1) == 83 || opList.get(opList.size()-1) == 72) {
                cigarOPPosIndex = getCigarOPAndPosIndex(cigar);
                opList = cigarOPPosIndex.getFirstElement();
                posIndexList = cigarOPPosIndex.getSecondElement();
            }
        }
        if (opList.get(opList.size()-1) == 83) {
            int length = Integer.parseInt(cigar.substring(posIndexList.get(opList.size()-2)+1, posIndexList.get(opList.size()-1)));
            seq = seq.substring(0, seq.length()-length);
            cigar = cigar.substring(0, posIndexList.get(opList.size()-2)+1);
        }
        else if (opList.get(opList.size()-1) == 72) {
            cigar = cigar.substring(0, posIndexList.get(opList.size()-2)+1);
        }
        List<SNP> snpList = new ArrayList();
        short chr = (short)Integer.parseInt(l.get(2));
        int startPos = Integer.parseInt(l.get(3));
        int currentPos = startPos-1;
        
//        startPos = 1;
//        currentPos = 1-1;
//        md = "5A4G11C30A35^A7";
        for (int i = 0; i < md.length(); i++) {
            char c = md.charAt(i);
            if (Character.isDigit(c)) {
                int j;
                for (j = i + 1; j < md.length(); j++) {
                    if (!Character.isDigit(md.charAt(j))) {
                        break;
                    }
                }
                currentPos = currentPos + Integer.parseInt(md.substring(i, j));
                i = j-1;
            }
            else if (Character.isLetter(c)){
                currentPos++;
                char alt = seq.charAt(currentPos-startPos);
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
    
    
    
    
    
    
    
    
    
    
    
    
    
    /**Parses an input line from a SAM file to determine all variants.
     * @param inputLine A line from a SAM file.
     * @return  A 3 X (number of variants) array holding all SNPs,
     * insertions and deletions from both the CIGAR field and the MD:Z: field. */
    public static String[][] getVariants(String inputLine) {
        String tagSequence = null;
        String cigarString = null;
        String mdField = null;
        String[] inputLineFields = inputLine.split("\t");
        String[][] mdFieldVariants;
        String[][] cigarStringVariants;
        int recordedVariantCount;
        int totalVariantCount = 0;

        for (int i = 0; i < inputLineFields.length; i++) {	//Loop through columns of each input line
            if (inputLineFields[i].matches("MD.*")) {
                mdField = inputLineFields[i].substring(5);
            } //Get MD Field
            if (i == 5) {
                tagSequence = inputLineFields[i];
            } //Get tag sequence
            if (i == 4) {
                cigarString = inputLineFields[i];
            } //Get CIGAR string
        }

        mdFieldVariants = parseMDField(mdField, tagSequence);		//Retrieve variants from MD field and CIGAR string.  If there were no variants,
        cigarStringVariants = parseCIGAR(cigarString, tagSequence);	//these methods will return NULL.  So this is checked before concatenating the arrays.

        if (mdFieldVariants != null) {
            totalVariantCount += mdFieldVariants[0].length;
        }
        if (cigarStringVariants != null) {
            totalVariantCount += cigarStringVariants[0].length;
        }

        String[][] totalVariants = new String[3][totalVariantCount];	//Make new array to hold all variants
        for (int i = 0; i < 3; i++) {	//Loop through rows
            recordedVariantCount = 0; //How many variants have been transferred into each row
            while (recordedVariantCount < totalVariantCount) { //Loop through columns
                if (mdFieldVariants != null) {
                    for (int j = 0; j < mdFieldVariants[0].length; j++) {	    //Fill with variants from md Field
                        totalVariants[i][j] = mdFieldVariants[i][j];
                        recordedVariantCount++;
                    }
                }

                if (cigarStringVariants != null) {
                    for (int j = 0; j < cigarStringVariants[0].length; j++) {    //Fill with variants from CIGAR string
                        totalVariants[i][j + recordedVariantCount] = cigarStringVariants[i][j];
                        recordedVariantCount++;
                    }
                }
            }
        }
        if (mdFieldVariants == null && cigarStringVariants == null) {
            return null;
        } else {
            return totalVariants;
        }
    }

    /**Uses a CIGAR code and the start coordinate of a BWA alignment to find the 
     * corresponding end coordinate.  Adjusts start coordinate if beginning of alignment 
     * was soft clipped.
     * @param cigarString   
     * @param startCoordinate */
    public static int[] adjustCoordinates(String cigarString, int startCoordinate) {
        String currentCharacter;
        String currDigits = "";
        boolean firstLetter = true;
        int endCoordinate = startCoordinate - 1;
        for (int cigarStringPosition = 0; cigarStringPosition < cigarString.length(); cigarStringPosition++) { //Loop through CIGAR string
            currentCharacter = cigarString.substring(cigarStringPosition, cigarStringPosition + 1);

            // M = Previous bases were present in both query & reference.
            // S/H = Previous bases were present in query & reference, but were
            //       dropped from reference during alignment to increase score
            //       (can only occur at one or both ends).
            // I = Previous bases were present only in query.
            // D = Previous bases were present only in reference.
            if (currentCharacter.matches("[0-9]")) {
                currDigits = currDigits.concat(currentCharacter); //Concatenate digits of number
            } else if (currentCharacter.matches("[MmDd]")) {
                endCoordinate += Integer.parseInt(currDigits);
                currDigits = "";
                firstLetter = false;
            } else if (currentCharacter.matches("[SsHh]")) {
                if (firstLetter) {
                    int span = Integer.parseInt(currDigits);
                    startCoordinate -= span;
                    endCoordinate = startCoordinate + span - 1;  // assumes that these soft clipped bases contain no indels
                    currDigits = "";
                    firstLetter = false;
                } else {
                    endCoordinate += Integer.parseInt(currDigits);
                    currDigits = "";
                }
            } else if (currentCharacter.matches("[Ii]")) {
                currDigits = "";
                firstLetter = false;
            }
        }
        int[] coords = {startCoordinate, endCoordinate};
        return coords;
    }

    /**Uses a CIGAR code and the start coordinate of a BWA alignment to find the 
     *corresponding end coordinate.
     *@param cigarString   
     *@param startCoordinate */
    public static long endCoordinate(String cigarString, long startCoordinate) {
        byte cigarStringPosition;
        String currentCharacter;
        String currDigits = "";
        long endCoordinate = startCoordinate - 1;
        for (cigarStringPosition = 0; cigarStringPosition < cigarString.length(); cigarStringPosition++) { //Loop through CIGAR string
            currentCharacter = cigarString.substring(cigarStringPosition, cigarStringPosition + 1);

            //M = Previous bases were present in both query & reference.
            //S = Previous bases were present in query & reference, but were
            //    dropped from reference during alignment to increase score.
            //I = Previous bases were present only in query.
            //D = Previous bases were present only in reference.
            if (currentCharacter.matches("[0-9]")) {
                currDigits = currDigits.concat(currentCharacter); //Concatenate digits of number
            } else if (currentCharacter.matches("[MmDdSs]")) {

                endCoordinate += Integer.parseInt(currDigits);
                currDigits = "";
            } else if (currentCharacter.matches("[Ii]")) {
                currDigits = "";
            }
        }
        return endCoordinate;
    }

    /**Searches a CIGAR string for insertions in the
     * reference sequence.
     * @param cigarString The text of the CIGAR string.
     * @param tagSequence The sequence of the CIGAR string's corresponding tag.
     * @return variant A 3x(number of variants) array containging:<br>
     * [0]The character "+"
     * [1]The character "-"
     * [2]The position of the reference insertion relative to the tag.*/
    public static String[][] parseCIGAR(String cigarString, String tagSequence) {
        byte cigarStringPosition;
        String currentCharacter;
        String matchingBaseCount = "";
        int tagSequencePosition = 0;
        String refHaplotype = "";
        String tagHaplotype = "";
        String[] variantPosition = new String[64];
        int variantCount = 0;

        try {
            for (cigarStringPosition = 0; cigarStringPosition < cigarString.length(); cigarStringPosition++) { //Loop through CIGAR string
                currentCharacter = cigarString.substring(cigarStringPosition, cigarStringPosition + 1);
                if (currentCharacter.matches("[0-9]")) {
                    matchingBaseCount = matchingBaseCount.concat(currentCharacter); //Concatenate digits of number
                }
                if (currentCharacter.matches("[A-Za-z]")) {
                    tagSequencePosition += Integer.parseInt(matchingBaseCount);
                    matchingBaseCount = "";
                    if (currentCharacter.matches("[I]")) { //An "I" indicates a deletion
                        refHaplotype = refHaplotype.concat("+");
                        tagHaplotype = tagHaplotype.concat("-");
                        variantPosition[variantCount] = Integer.toString(tagSequencePosition);
                        variantCount++;
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("There was an exception in parseCIGAR: " + e);
        }

        //Construct array of return values
        String[][] returnValue = new String[3][variantCount];
        if (variantCount == 0) {	//If there are no variants, return NULL
            returnValue = null;
        } else {
            for (int i = 0; i < variantCount; i++) {
                returnValue[0][i] = refHaplotype.substring(i, i + 1);
                returnValue[1][i] = tagHaplotype.substring(i, i + 1);
                returnValue[2][i] = variantPosition[i];
            }
        }
        return returnValue;
    }

    /**Decodes the MD: field in a SAM file to determine the location of
     * polymorphisms and whether they are mismatches or deletions from the
     * reference sequence.
     *@param mdField The text of the MD: field, NOT including the MD:Z: prefix!
     *@param tagSequence The sequence of the tag corresponding to mdField.
     *@return variant A 3x(number of variants) array containing:<br>
     *[0] Reference haplotype<br>
     *[1] Tag haplotype<br>
     *[2] Position in tag<br>
     * If there are no polymorphisms in the alignment, the method returns NULL.  */
    public static String[][] parseMDField(String mdField, String tagSequence) {

        String matchingBaseCount = "";
        byte tagSequencePosition = 0;  //pointer to loop through tag sequence
        byte mdFieldPosition;
        boolean deletion = false; //True if a letter indicates a deletion rather than a substitution
        byte deletionLength = 0;
        int variantCount = 0; //Number of variants found in current tag
        String tagHaplotype = "";
        String refHaplotype = "";
        String[] variantPosition = new String[tagSequence.length()]; //There can't be more polymorphisms than bases

        try {
            for (mdFieldPosition = 0; mdFieldPosition < mdField.length(); mdFieldPosition++) { //Loop through MD field
                if (mdField.substring(mdFieldPosition, mdFieldPosition + 1).equals("^")) {
                    deletion = true;
                } //A caret indicates a deletion

                if (mdField.substring(mdFieldPosition, mdFieldPosition + 1).matches("[AGCTagct]")) { //A letter indicates a polymorphism
                    tagSequencePosition += Integer.parseInt(matchingBaseCount); //If we came here from a number, move tag pointer
                    matchingBaseCount = ""; //...then reset.

                    if (deletion) {
                        deletionLength++; //
                    } else {
                        //		    System.out.println(mdField.substring(mdFieldPosition, mdFieldPosition+1));
                        refHaplotype = refHaplotype.concat(mdField.substring(mdFieldPosition, mdFieldPosition + 1)); //Record ref haplotype
                        tagHaplotype = tagHaplotype.concat(tagSequence.substring(tagSequencePosition, tagSequencePosition + 1)); //Record tag haplotype
                        variantPosition[variantCount] = Integer.toString(tagSequencePosition); //Record position in tag
                        variantCount++;
                        tagSequencePosition++;	//Keep moving tag pointer only if we are not in a deletion
                    }
                }

                if (mdField.substring(mdFieldPosition, mdFieldPosition + 1).matches("[0-9]")) {
                    if (deletion) {
                        refHaplotype = refHaplotype.concat("-"); //A number during a deletion indicates a deletion from the reference...
                        tagHaplotype = tagHaplotype.concat("+"); //...Or alternatively, an insertion in the tag
                        variantPosition[variantCount] = Integer.toString(tagSequencePosition);
                        variantCount++;
                        deletionLength = 0;
                        deletion = false;
                    }
                    matchingBaseCount = matchingBaseCount.concat(mdField.substring(mdFieldPosition, mdFieldPosition + 1)); //Concatenate digits of number
                }
            }
        } catch (Exception e) {
            System.out.println("There was an exception in parseMDField: " + e);
        }
        //Construct array of return values
        String[][] returnValue = new String[3][variantCount];
        if (variantCount == 0) {	//If there are no variants, return NULL
            returnValue = null;
        } else {
            for (int i = 0; i < variantCount; i++) {
                returnValue[0][i] = refHaplotype.substring(i, i + 1);
                returnValue[1][i] = tagHaplotype.substring(i, i + 1);
                returnValue[2][i] = variantPosition[i];
            }
        }
        return returnValue;
    }
}
