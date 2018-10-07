/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package format.alignment;

/**
 * Holding information of single end read alignment from BWA-MEM
 * @author feilu
 */
public class SEAlignRecord {
    /**Query sequence*/
    String query = null;
    /**Chromosome*/
    String hit = null;
    /**Reference starting position of query sequence, inclusive*/
    int startPos = Integer.MIN_VALUE;
    /**Reference ending position of query sequence, inclusive*/
    int endPos = Integer.MIN_VALUE;
    /**Strand of alignment, + = 1, - = 0*/
    byte strand = Byte.MIN_VALUE;
    /**Mapping quality*/
    short mappingQuality = Short.MIN_VALUE;
    /**The length of matched range*/
    short alignMatchNumber = Short.MIN_VALUE;
    /**Mismatch number in the matched range*/
    short editDistance = Short.MIN_VALUE;
    
    public SEAlignRecord (String query) {
        this.query = query;
    }
    
    public SEAlignRecord (String query, String hit, int startPos, int endPos, byte strand, short mappingQuality, short alignMatchNumber, short editDistance) {
        this.query = query;
        this.hit = hit;
        this.startPos = startPos;
        this.endPos = endPos;
        this.strand = strand;
        this.mappingQuality = mappingQuality;
        this.alignMatchNumber = alignMatchNumber;
        this.editDistance = editDistance;
    }
    
    public boolean isAligned () {
        if (this.getHit() == null) return false;
        return true;
    }
    
    /**
     * Return query of an alignment
     * @return
     */
    public String getQuery () {
        return this.query;
    }
    
    /**
     * Return hit of an alignment, e.g. chromosome name of the reference genome
     * @return NULL if not aligned
     */
    public String getHit () {
        return this.hit;
    }
    
    /**
     * Return the 1-based starting position of an alignment
     * @return Integer.MIN_VALUE if now aligned
     */
    public int getStartPos () {
        return this.startPos;
    }
    
    /**
     * Return the 1-based ending position of an alignment
     * @return Integer.MIN_VALUE if now aligned
     */
    public int getEndPos () {
        return this.endPos;
    }
    
    /**
     * Return the strand of an alignment, 1 representing plus, 0 representing minus
     * @return Byte.MIN_VALUE if now aligned
     */
    public byte getStrand () {
        return this.strand;
    }
    
    /**
     * Return the mapping quality of an alignment
     * Note: 255 indicates the mapping quality is not available
     * @return Short.MIN_VALUE if now aligned
     */
    public short getMappingQuality () {
        return this.mappingQuality;
    }
    
    /**
     * Return the total number of alignment matched base in CIGAR string, including both sequence match and mismatch  
     * @return Short.MIN_VALUE if now aligned
     */
    public short getAlignMatchNumber () {
        return this.alignMatchNumber;
    }
    
    /**
     * Return number of mismatched base, relative to the reference genome
     * @return Short.MIN_VALUE if now aligned
     */
    public short getEditDistance () {
        return this.editDistance;
    }
}
