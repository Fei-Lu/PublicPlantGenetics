package pgl.infra.dna.genotype;

import pgl.infra.position.ChrPosInterface;

public interface GenotypeTable {

    public int getTaxaNumber ();

    public int getSiteNumber ();

    public String getTaxonName (int taxonIndex);

    public short getChromosome (int siteIndex);
    
    public int getPosition (int siteIndex);
    
    public void sortBySite ();
    
    public void sortByTaxa ();
    
    public int getTaxonIndex (String taxon);
    
    public int getSiteIndex ();
    
    public byte getGenotype (int siteIndex, int taxonIndex);
    
    public boolean isHeterozygous (int siteIndex, int taxonIndex);
    
    public boolean isHomozygous (int siteIndex, int taxonIndex);
    
    public boolean isMissing (int siteIndex, int taxonIndex);
    
    public int getMissingNumberBySite (int siteIndex);
    
    public int getMissingNumberByTaxon (int taxonIndex);
    
    public int getHomozygoteNumberBySite (int siteIndex);
    
    public int getHomozygoteNumberByTaxon (int taxonIndex);
    
    public int getHeterozygoteNumberBySite (int siteIndex);
    
    public int getHeterozygoteNumberByTaxon (int taxonIndex);
    
    public int getMinorAlleleFrequency (int siteIndex);
    
    public int getMajorAlleleFrequency (int siteIndex);
    
    public int getReferenceAlleleFrequency (int siteIndex);
    
    public int getAlternativeAlleleFrequency (int siteIndex);

    public int getStartIndexOfChromosome (int chromosome);

    public int getEndIndexOfChromosome (int chromosome);

    public int removeSite (int siteIndex);

    public int removeTaxon (int taxonIndex);
    
}
