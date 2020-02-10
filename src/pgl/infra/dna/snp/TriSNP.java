package pgl.infra.dna.snp;

import pgl.infra.dna.allele.Allele;
import pgl.infra.dna.allele.AlleleType;
import pgl.infra.position.ChrPos;

public class TriSNP extends BiSNP {
    public Allele alt2 = null;

    public TriSNP() {

    }

    public TriSNP(short chr, int pos, char refBase, char altBase, char alt2Base) {
        super(chr, pos, refBase, altBase);
        this.setAlt2Allele(alt2Base);
    }

    @Override
    public TriSNP setChromosome (short chr) {
        super.setChromosome(chr);
        return this;
    }

    @Override
    public TriSNP setPosition (int position) {
        super.setPosition(position);
        return this;
    }

    @Override
    public TriSNP setRefAllele (char refBase) {
        super.setRefAllele(refBase);
        return this;
    }

    @Override
    public TriSNP setAltAllele (char altBase) {
        super.setAltAllele(altBase);
        return this;
    }

    public TriSNP setAlt2Allele (char altBase2) {
        this.alt2 = new Allele (altBase2);
        alt2.setAlleleType(AlleleType.Alternative);
        return this;
    }

    @Override
    public TriSNP build () {
        return this;
    }
}
