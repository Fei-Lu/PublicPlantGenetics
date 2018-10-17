/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package format.dna.snp;

/**
 *
 * @author feilu
 */
public class SNP implements SNPInterface {
    short chr = Short.MIN_VALUE;
    int pos = Integer.MIN_VALUE;
    byte ref = Byte.MIN_VALUE;
    byte alt = Byte.MIN_VALUE;
    
    public SNP (short chr, int pos, char refAllele, char altAllele) {
        this.chr = chr;
        this.pos = pos;
        this.ref = SNPEncoder.alleleCharByteMap.get(refAllele);
        this.alt = SNPEncoder.alleleCharByteMap.get(altAllele);
    }
    
    public SNP (short chr, int pos, byte ref, byte alt) {
        this.chr = chr;
        this.pos = pos;
        this.ref = ref;
        this.alt = alt;
    }

    @Override
    public int compareTo(SNPInterface o) {
        if (this.getChromosome() == o.getChromosome()) {
            if (this.getPosition() == o.getPosition()) {
                return (int)this.getAlternativeAlleleByte() - (int)this.getAlternativeAlleleByte();
            }
            else if (this.getPosition() < o.getPosition()) {
                return -1;
            }
            return 1;
        }
        else if (this.getChromosome() < o.getChromosome()) {
            return -1;
        }
        return 1;
    }

    @Override
    public short getChromosome() {
        return this.chr;
    }

    @Override
    public int getPosition() {
        return this.pos;
    }

    @Override
    public byte getReferenceAlleleByte() {
        return this.ref;
    }

    @Override
    public char getReferenceAllele() {
        return SNPEncoder.alleleByteCharMap.get(this.getReferenceAlleleByte());
    }

    @Override
    public byte getAlternativeAlleleByte() {
        return this.alt;
    }

    @Override
    public char getAlternativeAllele() {
        return SNPEncoder.alleleByteCharMap.get(this.getAlternativeAlleleByte());
    }
}
