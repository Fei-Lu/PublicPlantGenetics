/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package format.dna.snp;

import com.koloboke.collect.map.hash.HashByteByteMap;
import com.koloboke.collect.map.hash.HashByteByteMaps;
import com.koloboke.collect.map.hash.HashByteCharMap;
import com.koloboke.collect.map.hash.HashByteCharMaps;
import com.koloboke.collect.map.hash.HashCharByteMap;
import com.koloboke.collect.map.hash.HashCharByteMaps;

/**
 *
 * @author feilu
 */
public class AlleleEncoder {
    // 'N' indicates both biolocial missing and technoligical missing
    public static final char[] alleles = {'A', 'C', 'G', 'T', 'D', 'I', 'N'};
    public static final byte[] alleleBytes = {0, 1, 2, 3, 4, 5, 6};
    
    public static final HashCharByteMap alleleCharByteMap = 
            HashCharByteMaps.getDefaultFactory().withDefaultValue((byte)-1).newImmutableMap(alleles, alleleBytes);
    
    public static final HashByteCharMap alleleByteCharMap = 
            HashByteCharMaps.getDefaultFactory().withDefaultValue('!').newImmutableMap(alleleBytes, alleles);
    
}
