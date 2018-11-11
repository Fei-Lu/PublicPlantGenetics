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
public class ChrPos<T extends ChrPos> implements Comparable<T>{
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
        if (this.chr == o.chr) {
            if (pos == o.pos) return 0;
            else if (pos < o.pos) return -1;
            return 1;
        }
        else if (chr < o.chr) return -1;
        return 1;
    }
}
