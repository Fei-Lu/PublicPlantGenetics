/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package format.dna.snp;

/**
 *
 * @author feilu
 */
public interface SNPInterface {
        
    public byte getReferenceAlleleByte ();
    
    public char getReferenceAllele ();
    
    public byte getAlternativeAlleleByte ();
    
    public char getAlternativeAllele ();
    
    
}
