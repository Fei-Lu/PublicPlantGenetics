package pgl.infra.dna.snp;

import pgl.infra.dna.allele.Allele;
import pgl.infra.dna.allele.AlleleType;

public class TriSNP extends BiSNP {
    public Allele alt2 = null;

    public TriSNP(short chr, int pos, char refBase, char altBase, char alt2Base, String info) {
        super(chr, pos, refBase, altBase, info);
        this.setAlt2Allele(alt2Base);
    }

    public void setAlt2Allele (char altBase2) {
        this.alt2 = new Allele (altBase2);
        alt2.setAlleleType(AlleleType.Alternative);
    }
}
