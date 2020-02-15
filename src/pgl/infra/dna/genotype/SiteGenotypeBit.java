package pgl.infra.dna.genotype;

import pgl.infra.dna.snp.BiSNP;
import pgl.infra.utils.PStringUtils;

import java.util.BitSet;
import java.util.List;

public class SiteGenotypeBit extends BiSNP {
    BitSet phase1 = null;
    BitSet phase2 = null;
    BitSet missing = null;
    byte taxaNumberResidual = Byte.MIN_VALUE;

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
