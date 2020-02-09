/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pgl.infra.dna.allele;

import com.koloboke.collect.map.hash.HashByteCharMap;
import com.koloboke.collect.map.hash.HashByteCharMaps;
import com.koloboke.collect.map.hash.HashCharByteMap;
import com.koloboke.collect.map.hash.HashCharByteMaps;

/**
 * Class encoding alleles and site genotype from A, C, G, T, D, I, N
 * @author feilu
 */
public class AlleleEncoder {

    /**
     * Alleles in char, D is deletion, I is insertion, N is missing either biological or technical
     */
    public static final char[] alleleBases = {'A', 'C', 'G', 'T', 'D', 'I', 'N'};
    /**
     * Alleles in AscII code
     */
    public static final byte[] alleleAscIIs = {65, 67, 71, 84, 68, 73, 78};
    /**
     * Alleles in byte code
     */
    public static final byte[] alleleBytes = {0, 1, 2, 3, 4, 5, 6};

    /**
     * Converter from char to allele byte
     */
    public static final HashCharByteMap alleleBaseByteMap =
            HashCharByteMaps.getDefaultFactory().withDefaultValue((byte)-1).newImmutableMap(alleleBases, alleleBytes);

    /**
     * Converter from allele byte to char
     */
    public static final HashByteCharMap alleleByteBaseMap =
            HashByteCharMaps.getDefaultFactory().withDefaultValue('!').newImmutableMap(alleleBytes, alleleBases);

    /**
     * Return an allele byte from char
     * @param c
     * @return
     */
    public static byte getAlleleByteFromBase(char c) {
        return alleleBaseByteMap.get(c);
    }

    /**
     * Return an allele char from byte
     * @param b
     * @return
     */
    public static char getAlleleBaseFromByte(byte b) {
        return alleleByteBaseMap.get(b);
    }

    /**
     * Return a genotype byte from allele bytes of two homologous chromosomes
     * @param b1
     * @param b2
     * @return
     */
    public static byte getGenotypeByte (byte b1, byte b2) {
        return (byte)((b1<<4)+b2);
    }

    /**
     * Return a genotype byte from allele chars of two homologous chromosomes
     * @param c1
     * @param c2
     * @return
     */
    public static byte getGenotypeByte (char c1, char c2) {
        return getGenotypeByte(getAlleleByteFromBase(c1), getAlleleByteFromBase(c2));
    }

    /**
     * Return the allele byte of the 1st homologous chromosome
     * @param g
     * @return
     */
    public static byte getAlleleByte1FromGenotypeByte (byte g) {
        return (byte)(g>>>4);
    }

    /**
     * Return the allele byte of the 2nd homologous chromosome
     * @param g
     * @return
     */
    public static byte getAlleleByte2FromGenotypeByte (byte g) {
        return (byte)(g&15);
    }

    /**
     * Return the allele char of the 1st homologous chromosome
     * @param g
     * @return
     */
    public static char getAlleleBase1FromGenotypeByte(byte g) {
        return getAlleleBaseFromByte(getAlleleByte1FromGenotypeByte(g));
    }

    /**
     * Return the allele char of the 2nd homologous chromosome
     * @param g
     * @return
     */
    public static char getAlleleBase2FromGenotypeByte(byte g) {
        return getAlleleBaseFromByte(getAlleleByte2FromGenotypeByte(g));
    }
}