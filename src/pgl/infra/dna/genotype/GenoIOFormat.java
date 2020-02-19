package pgl.infra.dna.genotype;

/**
 * The file format standards of genotype table
 */
public enum GenoIOFormat {
    /**
     * VCF format
     */
    VCF,
    /**
     * VCF format compressed in gz
     */
    VCF_GZ,
    /**
     * HDF5 format
     */
    HDF5;
}
