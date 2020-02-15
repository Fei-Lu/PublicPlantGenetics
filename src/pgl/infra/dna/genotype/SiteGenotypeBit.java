package pgl.infra.dna.genotype;

import pgl.infra.dna.snp.BiSNP;
import pgl.infra.utils.PStringUtils;

import java.util.BitSet;
import java.util.List;

public class SiteGenotypeBit extends BiSNP {
    BitSet majorP = null;
    BitSet minorP = null;
    BitSet missingP = null;
    byte taxaNumberResidual = Byte.MIN_VALUE;

    public SiteGenotypeBit () {
        
    }
    
    public SiteGenotypeBit (short chr, int pos, char refBase, char altBase, String info, BitSet majorP, BitSet minorP, BitSet missingP, int taxaNumber) {
        super(chr, pos, refBase, altBase, info);
        this.majorP = majorP;
        this.minorP = minorP;
        this.missingP = missingP;
        this.setTaxaNumber(taxaNumber);
    }

    public void setTaxaNumber (int taxaNumber) {

    }

    public int getTaxaNumber () {
        return -1;
    }

    public int getWordNumber () {
        return -1;
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
        BitSet majorP = new BitSet(taxaNumber);
        BitSet minorP = new BitSet(taxaNumber);
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
                minorP.set(i);
            }
            else majorP.set(i);
            if (values[2] == 49) {
                minorP.set(i);
            }
            else majorP.set(i);
        }
        //SiteGenotypeBit sgb = new SiteGenotypeBit(chr, pos, refBase, altBase, info, majorP, minorP, missingP, taxaNumber);
        SiteGenotypeBit sgb = new SiteGenotypeBit(chr, pos, refBase, altBase, null, majorP, minorP, missingP, taxaNumber);
        return sgb;
    }
}
