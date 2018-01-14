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
public interface RangesInterface {
    
    /**
     * Return a {@link format.range.Range} object
     * @param rangeIndex
     * @return 
     */
    public Range getRange (int rangeIndex);
    
    /**
     * Sort by range size
     */
    public void sortBySize ();
    
    /**
     * Sort by starting position of a range
     */
    public void sortByPosition ();
    
    /**
     * Insert a {@link format.range.Range} into the list
     * @param rangeIndex
     * @param r 
     */
    public void insertRange (int rangeIndex, Range r);
    
    /**
     * Remove a {@link format.range.Range} from the list
     * @param rangeIndex 
     */
    public void removeRange (int rangeIndex);
    
    /**
     * Set a {@link format.range.Range} in the list
     * @param rangeIndex
     * @param r 
     */
    public void setRange (int rangeIndex, Range r);
    
    /**
     * Return total number of {@link format.range.Range} in the list
     * @return 
     */
    public int getRangeNumber ();
}
