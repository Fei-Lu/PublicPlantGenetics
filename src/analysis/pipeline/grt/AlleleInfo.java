/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package analysis.pipeline.grt;

import format.position.ChrPos;

/**
 *
 * @author feilu
 */
public class AlleleInfo extends ChrPos {
    byte allele = -1;
    byte end = -1; // paired-end, 1 or 2
    byte relaPos = -1;
    
    public AlleleInfo(short chr, int pos) {
        super(chr, pos);
    }
    
    public AlleleInfo (short chr, int pos, byte allele, byte end, byte relaPos) {
        super(chr, pos);
        this.allele = allele;
        this.end = end;
        this.relaPos = relaPos;
    }
    
    public byte getAllele () {
        return allele;
    }
    
    public byte getEnd () {
        return end;
    }
    
    public byte getRelativePosition () {
        return relaPos;
    }
}
