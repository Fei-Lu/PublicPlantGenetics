package pgl.infra.dna.genotype;

public interface GenotypeTable {

    public int getTaxaNumber ();

    public int getSiteNumber ();

    public String getTaxonName (int taxonIndex);

    public short getChromosome (int siteIndex);
    
    public int getPosition (int siteIndex);
    
    public void sortBySite ();
    
    public void sortByTaxa ();
    
    public int getTaxonIndex (String taxon);
    
    public int getSiteIndex (short chromosome, int position);
    
    public byte getGenotypeByte(int siteIndex, int taxonIndex);
    
    public boolean isHeterozygous (int siteIndex, int taxonIndex);
    
    public boolean isHomozygous (int siteIndex, int taxonIndex);
    
    public boolean isMissing (int siteIndex, int taxonIndex);
    
    public int getMissingNumberBySite (int siteIndex);
    
    public int getMissingNumberByTaxon (int taxonIndex);
    
    public int getNonMissingNumberBySite (int siteIndex);
    
    public int getNonMissingNumberByTaxon (int taxonIndex);
    
    public int getHomozygoteNumberBySite (int siteIndex);
    
    public int getHomozygoteNumberByTaxon (int taxonIndex);
    
    public int getHeterozygoteNumberBySite (int siteIndex);
    
    public int getHeterozygoteNumberByTaxon (int taxonIndex);
    
    public double getTaxonHeterozygosity (int taxonIndex);
    
    public double getSiteHeterozygoteFraction (int siteIndex);
    
    public double getMinorAlleleFrequency (int siteIndex);
    
    public double getMajorAlleleFrequency (int siteIndex);
    
    public double getReferenceAlleleFrequency (int siteIndex);
    
    public double getAlternativeAlleleFrequency (int siteIndex);

    public int getStartIndexOfChromosome (int chromosome);

    public int getEndIndexOfChromosome (int chromosome);

    public int removeSite (int siteIndex);

    public int removeTaxon (int taxonIndex);
    
    public GenotypeTable getSubGenotypeTableBySite (int[] siteIndices);
    
    public GenotypeTable getSubGenotypeTableByTaxa (int[] taxaIndices);
    
}
