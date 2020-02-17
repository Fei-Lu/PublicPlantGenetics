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
    
    public float getTaxonHeterozygosity (int taxonIndex);
    
    public float getSiteHeterozygoteFraction (int siteIndex);
    
    public byte getMinorAlleleByte(int siteIndex);
    
    public float getMinorAlleleFrequency (int siteIndex);
    
    public byte getMajorAlleleByte(int siteIndex);
    
    public float getMajorAlleleFrequency (int siteIndex);
    
    public byte getReferenceAlleleByte(int siteIndex);
    
    public float getReferenceAlleleFrequency (int siteIndex);
    
    public byte getAlternativeAlleleByte(int siteIndex);
    
    public float getAlternativeAlleleFrequency (int siteIndex);

    /**
     * Return the start index of a chromosome, inclusive
     * @param chromosome
     * @return -1 if the chromosome does not exist
     */
    public int getStartIndexOfChromosome (short chromosome);

    /**
     * Return the end index of a chromosome, exclusive
     * @param chromosome
     * @return -1 if chromosome does not exist
     */
    public int getEndIndexOfChromosome (short chromosome);

    public GenotypeTable getSubGenotypeTableBySite (int[] siteIndices);
    
    public GenotypeTable getSubGenotypeTableByTaxa (int[] taxaIndices);
    
}
