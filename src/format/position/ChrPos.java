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
public class ChrPos implements Comparable<ChrPos> {
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
    public int compareTo(ChrPos o) {
        if (this.getChromosome() == o.getChromosome()) {
            if (this.getPosition() == o.getPosition()) return 0;
            else if (this.getPosition() < o.getPosition()) return -1;
            return 1;
        }
        else if (this.getChromosome() < o.getChromosome()) return -1;
        return 1;
    }
}
