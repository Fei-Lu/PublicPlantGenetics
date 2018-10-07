/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package utils;

/**
 *
 * @author feilu
 */
public class Tuple<T, S> {
    T first = null;
    S second = null;
    
    public Tuple (T first, S second) {
        this.first = first;
        this.second = second;
    }
    
    public T getFirstElement () {
        return first;
    }
    
    public S getSecondElement () {
        return second;
    }
}
