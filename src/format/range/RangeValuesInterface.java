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
public interface RangeValuesInterface {
    
    /**
     * Sort ranges by their values
     */
    public void sortByValue ();
    
    /**
     * Return the value of a range
     * @param rangeIndex
     * @return 
     */
    public double getValue (int rangeIndex);
    
    /**
     * Insert range and its value to the list
     * @param rangeIndex
     * @param r
     * @param value 
     */
    public void insertRangeValue (int rangeIndex, Range r, double value);
    
    /**
     * Remove range and its value from the list
     * @param rangeIndex 
     */
    public void removeRangeValue (int rangeIndex);
    
    /**
     * Set a range and its value
     * @param rangeIndex
     * @param value 
     */
    public void setRangeValue (int rangeIndex, double value);
    
    /**
     * Return a merged {@link format.range.RangeValues} object
     * @param ri
     * @return 
     */
    public RangeValues getMergedRangeValues (RangeValues ri);
    
    /**
     * Return a {@link format.range.RangeValues} object by chromosome
     * @param chr
     * @return 
     */
    public RangeValues getRangeValuesByChromosome(int chr);
    
    /**
     * Return a {@link format.range.RangeValues} object containing a position
     * @param chr
     * @param pos
     * @return 
     */
    public RangeValues getRangeValuesContainsPosition(int chr, int pos);
    
}
