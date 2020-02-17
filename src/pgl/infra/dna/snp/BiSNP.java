package pgl.infra.dna.snp;

import pgl.infra.dna.allele.Allele;
import pgl.infra.dna.allele.AlleleType;
import pgl.infra.position.ChrPos;

public class BiSNP extends ChrPos {
    public Allele reference = null;
    public Allele alternative = null;
    public String info = null;

    public BiSNP () {
        
    }
    
    public BiSNP(short chr, int pos, char refBase, char altBase, String info) {
        super(chr, pos);
        this.initializeRefAllele(refBase);
        this.initializeAltAllele(altBase);
        this.setSNPInfo(info);
    }

    private void initializeRefAllele(char refBase) {
        this.reference = new Allele (refBase);
        reference.setAlleleType(AlleleType.Reference);
    }

    private void initializeAltAllele(char altBase) {
        this.alternative = new Allele (altBase);
        alternative.setAlleleType(AlleleType.Alternative);
    }

    public void setReferenceAlleleType (AlleleType at) {
        reference.setAlleleType(at);
    }

    public void setAlternativeAlleleType (AlleleType at) {
        alternative.setAlleleType(at);
    }

    public void removeReferenceAlleleType (AlleleType at) {
        reference.removeAlleleType(at);
    }

    public void removeAlternativeAlleleType (AlleleType at) {
        alternative.removeAlleleType(at);
    }

    public boolean isReferenceAlleleTypeOf (AlleleType at) {
        return reference.isAlleleTypeOf(at);
    }

    public boolean isAlternativeAlleleTypeOf (AlleleType at) {
        return alternative.isAlleleTypeOf(at);
    }

    public byte getReferenceAlleleByte() {
        return reference.getAlleleByte();
    }

    public byte getAlternativeAlleleByte() {
        return alternative.getAlleleByte();
    }

    public void setSNPInfo (String info) {
        this.info = info;
    }
}
