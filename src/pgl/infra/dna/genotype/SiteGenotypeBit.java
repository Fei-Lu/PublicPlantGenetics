package pgl.infra.dna.genotype;

import pgl.infra.dna.allele.AlleleEncoder;
import pgl.infra.dna.allele.AlleleType;
import pgl.infra.dna.snp.BiSNP;
import pgl.infra.utils.PStringUtils;

import java.util.BitSet;
import java.util.List;

public class SiteGenotypeBit extends BiSNP {
    //Bit set of the 1st homologous chromosome, 1 is alt, 0 is ref
    BitSet phase1 = null;
    //Bit set of the 2nd homologous chromosome, 1 is alt, 0 is ref
    BitSet phase2 = null;
    //Bit set of the missing genotype at a specific site, 1 is missing
    BitSet missing = null;
    
    float maf = Float.MIN_VALUE;
    
    public SiteGenotypeBit () {
        
    }
    
    public SiteGenotypeBit (short chr, int pos, char refBase, char altBase, String info, BitSet phase1, BitSet phase2, BitSet missing, int taxaNumber) {
        super(chr, pos, refBase, altBase, info);
        this.phase1 = phase1;
        this.phase2 = phase2;
        this.missing = missing;
    }

    public int getTaxaNumber () {
        return phase1.length();
    }

    public byte getGenotypeByte (int taxonIndex) {
        if (isMissing(taxonIndex)) return AlleleEncoder.genotypeMissingByte;
        byte ref = this.getReferenceAlleleByte();
        byte alt = this.getAlternativeAlleleByte();
        byte b1 = AlleleEncoder.alleleMissingByte;
        byte b2 = AlleleEncoder.alleleMissingByte;
        if (isPhase1Alternative(taxonIndex)) b1 = alt;
        else b1 = ref;
        if (isPhase2Alternative(taxonIndex)) b2 = alt;
        else b2 = ref;
        return AlleleEncoder.getGenotypeByte(b1, b2);
    }

    public boolean isMissing (int taxonIndex) {
        if (missing.get(taxonIndex)) return true;
        return false;
    }

    public boolean isHeterozygous (int taxonIndex) {
        if (this.isMissing(taxonIndex)) return false;
        if (isPhase1Alternative(taxonIndex) == isPhase2Alternative(taxonIndex)) return false;
        return true;
    }

    public boolean isHomozygous (int taxonIndex) {
        if (this.isMissing(taxonIndex)) return false;
        if (isPhase1Alternative(taxonIndex) == isPhase2Alternative(taxonIndex)) return true;
        return false;
    }

    public int getMissingNumber () {
        return missing.cardinality();
    }

    public int getNonMissingNumber () {
        return this.getTaxaNumber()-this.getMissingNumber();
    }

    public boolean isPhase1Alternative (int taxonIndex) {
        if (this.isMissing(taxonIndex)) return false;
        return phase1.get(taxonIndex);
    }

    public boolean isPhase2Alternative (int taxonIndex) {
        if (this.isMissing(taxonIndex)) return false;
        return phase2.get(taxonIndex);
    }

    public boolean isPhase1Reference (int taxonIndex) {
        if (this.isMissing(taxonIndex)) return false;
        if (this.isPhase1Alternative(taxonIndex)) return false;
        return true;
    }

    public boolean isPhase2Reference (int taxonIndex) {
        if (this.isMissing(taxonIndex)) return false;
        if (this.isPhase2Alternative(taxonIndex)) return false;
        return true;
    }
    
    public int getHeterozygoteNumber () {
        BitSet phase1C = phase1.get(0, phase1.length());
        phase1C.xor(phase2);
        return phase1C.cardinality();
    }
    
    public int getHomozygoteNumber () {
        return this.getNonMissingNumber()-this.getHeterozygoteNumber();
    }

    public int getAlternativeAlleleNumber () {
        return this.phase1.cardinality()+this.phase2.cardinality();
    }

    public float getAlternativeAlleleFrequency () {
        if (this.isAlternativeAlleleTypeOf(AlleleType.Minor)) return this.maf;
        if (this.isAlternativeAlleleTypeOf(AlleleType.Major)) return (float)(1-this.maf);
        return (float)((double)this.getAlternativeAlleleNumber()/(this.getNonMissingNumber()*2));
    }

    public int getReferenceAlleleNumber () {
        return this.getNonMissingNumber()*2-this.getAlternativeAlleleNumber();
    }

    public float getReferenceAlleleFrequency () {
        if (this.isReferenceAlleleTypeOf(AlleleType.Minor)) return this.maf;
        if (this.isReferenceAlleleTypeOf(AlleleType.Major)) return (float)(1-this.maf);
        return (float)((double)this.getReferenceAlleleNumber()/(this.getNonMissingNumber()*2));
    }

    public byte getMinorAlleleByte () {
        if (this.reference.isAlleleTypeOf(AlleleType.Minor)) return this.getReferenceAlleleByte();
        if (this.alternative.isAlleleTypeOf(AlleleType.Minor)) return this.getAlternativeAlleleByte();
        this.getMinorAlleleFrequency();
        return this.getMinorAlleleByte();
    }

    public int getMinorAlleleNumber () {
        if (this.reference.isAlleleTypeOf(AlleleType.Minor)) return this.getReferenceAlleleNumber();
        if (this.alternative.isAlleleTypeOf(AlleleType.Minor)) return this.getAlternativeAlleleNumber();
        this.getMinorAlleleFrequency();
        return this.getMinorAlleleNumber();
    }

    public float getMinorAlleleFrequency () {
        if (this.maf != Float.MIN_VALUE) return this.maf;
        float altFre = this.getAlternativeAlleleFrequency();
        if (altFre < 0.5) {
            this.setAlternativeAlleleType(AlleleType.Minor);
            this.setReferenceAlleleType(AlleleType.Major);
        }
        else {
            this.setReferenceAlleleType(AlleleType.Minor);
            this.setAlternativeAlleleType(AlleleType.Major);
            altFre = (float)(1 - altFre);
        }
        this.maf = altFre;
        return altFre;
    }

    public byte getMajorAlleleByte () {
        if (this.reference.isAlleleTypeOf(AlleleType.Major)) return this.getReferenceAlleleByte();
        if (this.alternative.isAlleleTypeOf(AlleleType.Major)) return this.getAlternativeAlleleByte();
        this.getMinorAlleleFrequency();
        return this.getMajorAlleleByte();
    }

    public int getMajorAlleleNumber () {
        if (this.reference.isAlleleTypeOf(AlleleType.Major)) return this.getReferenceAlleleNumber();
        if (this.alternative.isAlleleTypeOf(AlleleType.Major)) return this.getAlternativeAlleleNumber();
        this.getMinorAlleleFrequency();
        return this.getMajorAlleleNumber();
    }

    public float getMajorAlleleFrequency () {
        if (this.maf != Float.MIN_VALUE) return (float)(1-this.maf);
        return (float)(1-this.getMinorAlleleFrequency());
    }
    
    public static SiteGenotypeBit buildFromVCFLine (String line) {
        List<String> l = PStringUtils.fastSplit(line);
        List<String> ll = null;
        String current = null;
        short chr = Short.parseShort(l.get(0));
        int pos = Integer.parseInt(l.get(1));
        char refBase = l.get(3).charAt(0);
        char altBase = l.get(4).charAt(0);
        String info = l.get(7);
        int taxaNumber = l.size()-9;
        BitSet phase1 = new BitSet(taxaNumber);
        BitSet phase2 = new BitSet(taxaNumber);
        BitSet missingP = new BitSet(taxaNumber);
        byte[] values = null;
        for (int i = 0; i < taxaNumber; i++) {
            current = l.get(i+9);
            if (current.startsWith(".")) {
                missingP.set(i);
                continue;
            }
            ll = PStringUtils.fastSplit(current, ":");
            values = ll.get(0).getBytes();
            if (values[0] == 49) {
                phase1.set(i);
            }
            if (values[2] == 49) {
                phase2.set(i);
            }
        }
        //SiteGenotypeBit sgb = new SiteGenotypeBit(chr, pos, refBase, altBase, info, majorP, minorP, missingP, taxaNumber);
        SiteGenotypeBit sgb = new SiteGenotypeBit(chr, pos, refBase, altBase, null, phase1, phase2, missingP, taxaNumber);
        return sgb;
    }
}
