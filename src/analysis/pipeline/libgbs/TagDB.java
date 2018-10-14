/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package analysis.pipeline.libgbs;

import format.alignment.SAMUtils;
import java.io.BufferedReader;
import utils.IOUtils;

/**
 *
 * @author feilu
 */
public class TagDB {
    
    public TagDB () {}
    
    public TagDB (String dbFileS) {
        
    }
    
    public void initializeDB (String mergedTagCountFileS) {
        
    }
    
    public void SNPCalling (String samFileS) {
        int mapQThresh = 30;
        try {
            BufferedReader br = IOUtils.getTextReader(samFileS);
            String temp = null;
            while ((temp = br.readLine()).startsWith("@SQ")){}
            while ((temp = br.readLine()) != null) {
                SAMUtils.getVariation(temp, mapQThresh);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        
    }
}
