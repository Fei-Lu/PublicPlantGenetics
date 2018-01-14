/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package format.range;

/**
 *
 * @author feilu
 */
public class Range extends RangeAbstract {
    
    public Range(int chr, int start, int end) {
        super(chr, start, end);
    }

    @Override
    public RangeInterface getIntersection(RangeInterface ri) {
        if (!this.isOverlap(ri)) return null;
        if (this.start < ri.getRangeStart()) return new Range(this.chr, ri.getRangeStart(), this.end < ri.getRangeEnd()? this.end : ri.getRangeEnd());
        return new Range(this.chr, this.start, this.end < ri.getRangeEnd()? this.end : ri.getRangeEnd());
    }

}