package pgl.infra.dna.snp;

import pgl.infra.dna.allele.Allele;
import pgl.infra.dna.allele.AlleleType;
import pgl.infra.position.ChrPos;

public class BiSNP extends ChrPos {
    public Allele ref = null;
    public Allele alt = null;
    public String info = null;

    public BiSNP () {
        
    }
    
    public BiSNP(short chr, int pos, char refBase, char altBase, String info) {
        super(chr, pos);
        this.setRefAllele(refBase);
        this.setAltAllele(altBase);
        this.setSNPInfo(info);
    }

    public void setRefAllele (char refBase) {
        this.ref = new Allele (refBase);
        ref.setAlleleType(AlleleType.Reference);
    }

    public void setAltAllele (char altBase) {
        this.alt = new Allele (altBase);
        alt.setAlleleType(AlleleType.Alternative);
    }

    public byte getRefAlleleByte () {
        return ref.getAlleleByte();
    }

    public byte getAltAlleleByte () {
        return alt.getAlleleByte();
    }

    public void setSNPInfo (String info) {
        this.info = info;
    }
}
