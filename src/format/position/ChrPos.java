/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package format.position;

/**
 *
 * @author feilu
 * @param <T>
 */
public class ChrPos implements Comparable {
    short chr;
    int pos;
    
    public ChrPos (short chr, int pos) {
        this.chr = chr;
        this.pos = pos;
    }
    
    public short getChromosome () {
        return chr;
    }
    
    public int getPosition() {
        return pos;
    }

    @Override
    public int compareTo(Object o) {
        ChrPos oo = (ChrPos)o;
        if (this.getChromosome() == oo.getChromosome()) {
            if (this.getPosition() == oo.getPosition()) return 0;
            else if (this.getPosition() < oo.getPosition()) return -1;
            return 1;
        }
        else if (this.getChromosome() < oo.getChromosome()) return -1;
        return 1;
    }
}
