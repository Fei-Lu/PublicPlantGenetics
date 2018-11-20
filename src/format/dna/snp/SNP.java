/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package format.dna.snp;

import format.position.ChrPos;
import java.util.Comparator;

/**
 *
 * @author feilu
 */
public class SNP extends ChrPos implements SNPInterface {
    byte ref = Byte.MIN_VALUE;
    byte alt = Byte.MIN_VALUE;
    
    public SNP (short chr, int pos, char refAllele, char altAllele) {
        super(chr, pos);
        this.ref = AlleleEncoder.alleleCharByteMap.get(refAllele);
        this.alt = AlleleEncoder.alleleCharByteMap.get(altAllele);
    }
    
    public SNP (short chr, int pos, byte ref, byte alt) {
        super(chr, pos);
        this.ref = ref;
        this.alt = alt;
    }

    @Override
    public byte getReferenceAlleleByte() {
        return this.ref;
    }

    @Override
    public char getReferenceAllele() {
        return AlleleEncoder.alleleByteCharMap.get(this.getReferenceAlleleByte());
    }

    @Override
    public byte getAlternativeAlleleByte() {
        return this.alt;
    }

    @Override
    public char getAlternativeAllele() {
        return AlleleEncoder.alleleByteCharMap.get(this.getAlternativeAlleleByte());
    }
    
    public static class CompareSNP implements Comparator<SNP> { 
        @Override
        public int compare(SNP a, SNP b) { 
            if (a.getChromosome() == b.getChromosome()) {
                if (a.getPosition() == b.getPosition()) {
                    return a.getAlternativeAlleleByte()-b.getAlternativeAlleleByte();
                }
                else if (a.getPosition() < b.getPosition()) return -1;
                return 1;
            }
            else if (a.getChromosome() < b.getChromosome()) return -1;
            return 1;
        } 
    }
}
