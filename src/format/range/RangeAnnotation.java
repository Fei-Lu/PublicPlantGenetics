/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package format.range;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author feilu
 */
public class RangeAnnotation<T> extends Range {
    public List<T> annotations = null;
    
    public RangeAnnotation(int chr, int start, int end) {
        super(chr, start, end);
        annotations = new ArrayList<>();
    }
    
    
}
