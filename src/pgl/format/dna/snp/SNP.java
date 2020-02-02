/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pgl.format.dna.snp;

import pgl.format.position.ChrPos;
import gnu.trove.list.array.TByteArrayList;
import gnu.trove.set.hash.TByteHashSet;
import java.util.Comparator;

/**
 *
 * @author feilu
 */
public class SNP extends ChrPos implements SNPInterface {
    byte ref = Byte.MIN_VALUE;
    TByteArrayList alts = new TByteArrayList();
    
    public SNP (short chr, int pos, char refAllele, char altAllele) {
        super(chr, pos);
        this.ref = AlleleEncoder.alleleCharByteMap.get(refAllele);
        this.addAltAllele(altAllele);
    }
    
    public SNP (short chr, int pos, byte ref, byte alt) {
        super(chr, pos);
        this.ref = ref;
        this.addAltAlleleByte(alt);
    }
    
    public SNP (short chr, int pos, byte ref, TByteArrayList alts) {
        super(chr, pos);
        this.ref = ref;
        this.alts = alts;
    }
    
    public TByteArrayList getAltAlleleList () {
        return this.alts;
    }
    
    @Override
    public void removeDuplicatedAltAlleles () {
        TByteHashSet s = new TByteHashSet(alts);
        alts = new TByteArrayList(s);
        alts.sort();
    }
    
    @Override
    public void addAltAllele (char altAllele) {
        this.addAltAlleleByte(AlleleEncoder.alleleCharByteMap.get(altAllele));
    }
    
    @Override
    public void addAltAlleleByte (byte alt) {
        this.alts.add(alt);
        if (alts.size() == Byte.MAX_VALUE) this.removeDuplicatedAltAlleles();
    }
    
    @Override
    public void sortAltAlleles () {
        alts.sort();
    }
    
    @Override
    public int getAltAlleleIndex (byte alt) {
        return alts.binarySearch(alt);
    }
    
    @Override
    public byte getAltAlleleNumber () {
        return (byte)this.alts.size();
    }
    
    @Override
    public int getAltAlleleIndex (char altAllele) {
        return this.getAltAlleleIndex(AlleleEncoder.alleleCharByteMap.get(altAllele));
    }
    
    @Override
    public byte getRefAlleleByte() {
        return this.ref;
    }

    @Override
    public char getRefAllele() {
        return AlleleEncoder.alleleByteCharMap.get(this.getRefAlleleByte());
    }

    @Override
    public byte getAltAlleleByte(int altIndex) {
        return this.alts.get(altIndex);
    }

    @Override
    public char getAltAllele(int alleleIndex) {
        return AlleleEncoder.alleleByteCharMap.get(this.getAltAlleleByte(alleleIndex));
    }
}