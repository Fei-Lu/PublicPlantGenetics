package pgl.infra.dna.genotype;

import java.util.List;

public class GenotypeAbstract implements GenotypeTable {
    List<String> taxaList = null;


    @Override
    public int getTaxaNumber() {
        return taxaList.size();
    }

    @Override
    public int getSiteNumber() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    
}
