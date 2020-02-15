/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pgl.infra.position;

/**
 *
 * @author feilu
 */
public interface ChrPosInterface extends Comparable<ChrPosInterface> {
    
    public short getChromosome ();
    
    public int getPosition ();

    public void setChromosome (short chr);

    public void setPosition (int position);
    
}
