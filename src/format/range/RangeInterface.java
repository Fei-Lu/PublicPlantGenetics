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
public interface RangeInterface {
    
    public int getRangeChromosome ();
    
    public int getRangeStart ();
    
    public int getRangeEnd ();
    
    public int getRangeSize ();
    
    public void setRangeChromosome (int chr);
    
    public void setRangeStart (int start);
    
    public void setRangeEnd (int end);
    
    public String getInfoString ();
    
    public boolean isOverlap (RangeInterface ri);
    
    public boolean isWithin (RangeInterface ri);
    
    public boolean isContain (RangeInterface ri);
    
    public boolean isIncluded (RangeInterface ri);
    
    public RangeInterface getIntersection (RangeInterface other);
    
}
