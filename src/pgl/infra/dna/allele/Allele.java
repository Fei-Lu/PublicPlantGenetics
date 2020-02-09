package pgl.infra.dna.allele;

/**
 * Class holding allele and its basic information
 * Basic information are stored in bits
 * @author feilu
 */
public class Allele {
    byte baseVal = -1;
    byte feature = 0;

    /**
     * Construct an empty object of {@link Allele}
     */
    public Allele () {

    }

    /**
     * Construct an object of {@link Allele}
     * @param c
     */
    public Allele (char c) {
        this.baseVal = AlleleEncoder.getAlleleByteFromBase(c);
    }

    /**
     * Construct an object of {@link Allele}
     * @param alleleByte
     */
    public Allele (byte alleleByte) {
        this.baseVal = alleleByte;
    }

    /**
     * Return the base of the allele
     * @return
     */
    public char getAlleleBase () {
        return AlleleEncoder.getAlleleBaseFromByte(this.baseVal);
    }

    /**
     * Return the byte code of the allele, see {@link AlleleEncoder}
     * @return
     */
    public byte getAlleleByte () {
        return this.baseVal;
    }

    /**
     * Return the code of allele feature
     * @return
     */
    public byte getAlleleFeature () {
        return this.feature;
    }

    /**
     * Set allele type and return, see {@link AlleleType}
     * @param at
     * @return
     */
    public Allele setAlleleType (AlleleType at) {
        this.feature = (byte)(feature | at.getFeature());
        return this;
    }

    /**
     * Remove allele type and return, see {@link AlleleType}
     * @param at
     * @return
     */
    public Allele removeAlleleType (AlleleType at) {
        this.feature = (byte)(feature & (~at.getFeature()));
        return this;
    }

    /**
     * Reset all allele types to false, see {@link AlleleType}
     * @return
     */
    public Allele resetAlleleTypeToDefault () {
        this.feature = 0;
        return this;
    }

    /**
     * Build an object using set-builder method
     * @return
     */
    public Allele build () {
        return this;
    }

    /**
     * Return the allele is a specific type, see {@link AlleleType}
     * @param at
     * @return
     */
    public boolean isAlleleTypeOf (AlleleType at) {
        if ((this.feature & at.getFeature()) == 0) return false;
        return true;
    }
}