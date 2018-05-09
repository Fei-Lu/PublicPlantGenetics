/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package format.tree;

/**
 *
 * @author feilu
 */
public class NodeWHeight {
    
    String name = null;
    
    double height = 0;
    
    public NodeWHeight (String name, double height) {
        this.name = name;
        this.height = height;
    }
    
    public String getName () {
        return name;
    }
    
    public double getHeight () {
        return height;
    }
}
