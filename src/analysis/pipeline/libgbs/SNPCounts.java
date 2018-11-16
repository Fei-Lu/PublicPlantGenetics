/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package analysis.pipeline.libgbs;

import format.dna.snp.SNP;
import java.util.List;

/**
 *
 * @author feilu
 */
public class SNPCounts {
    List<SNPCount> chrSCList = null;
    
    public SNPCounts (TagAnnotations tas) {
        this.initilize(tas);
    }
    
    private void initilize (TagAnnotations tas) {
        
    }
}

class SNPCount extends SNP {
    int count = 0;
    public SNPCount(short chr, int pos, byte ref, byte alt) {
        super(chr, pos, ref, alt);
    }
}
