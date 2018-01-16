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
public class RangeValue extends Range {
    double value;
    
    public RangeValue(int chr, int start, int end, double value) {
        super(chr, start, end);
        this.value = value;
    }
    
}
