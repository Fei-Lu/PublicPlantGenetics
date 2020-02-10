package pgl.infra.dna.snp;

import pgl.infra.dna.allele.Allele;
import pgl.infra.dna.allele.AlleleType;
import pgl.infra.position.ChrPos;

public class BiSNP extends ChrPos {
    public Allele ref = null;
    public Allele alt = null;
    public String annotation = null;

    public BiSNP() {

    }

    public BiSNP(short chr, int pos, char refBase, char altBase) {
        super(chr, pos);
        this.setRefAllele(refBase);
        this.setAltAllele(altBase);
    }

    @Override
    public BiSNP setChromosome (short chr) {
        super.setChromosome(chr);
        return this;
    }

    @Override
    public BiSNP setPosition (int position) {
        super.setPosition(position);
        return this;
    }

    public BiSNP setRefAllele (char refBase) {
        this.ref = new Allele (refBase);
        ref.setAlleleType(AlleleType.Reference);
        return this;
    }

    public BiSNP setAltAllele (char altBase) {
        this.alt = new Allele (altBase);
        alt.setAlleleType(AlleleType.Alternative);
        return this;
    }

    @Override
    public BiSNP build () {
        return this;
    }
}
