/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package utils;

import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 *
 * @author feilu
 */
public class CrossMapUtils {
    public static String myPythonPath = "/Library/Frameworks/Python.framework/Versions/2.7/bin/python";
    public static String myCrossMapPath = "/Library/Frameworks/Python.framework/Versions/2.7/bin/CrossMap.py";
    public static String myMaizeChainPath = "/Users/feilu/Documents/database/maize/crossMap/AGPv3_to_AGPv4.chain.gz";
    String crossMapPath = null;
    String pythonPath = null;
    String inputBedFileS = null;
    String outputBedFileS = null;
    String chainFilePath = null;
    
    public CrossMapUtils (String pythonPath, String crossMapPath, String chainFilePath, String inputBedFileS, String outputBedFileS) {
        this.crossMapPath = crossMapPath;
        this.chainFilePath = chainFilePath;
        this.pythonPath = pythonPath;
        this.inputBedFileS = inputBedFileS;
        this.outputBedFileS = outputBedFileS;
    }
    
    public CrossMapUtils (String inputBedFileS, String outputBedFileS) {
        this.inputBedFileS = inputBedFileS;
        this.outputBedFileS = outputBedFileS;
    }
    
    public void setMaizeV3ToV4 () {
        this.crossMapPath = myCrossMapPath;
        this.chainFilePath = myMaizeChainPath;
        this.pythonPath = myPythonPath;
    }
    
    public void convert () {
        StringBuilder sb = new StringBuilder(this.pythonPath);
        sb.append(" ").append(this.crossMapPath).append(" bed ").append(this.chainFilePath).append(" ").append(this.inputBedFileS).append(" ").append(this.outputBedFileS);
        try{
            String command = sb.toString();
            System.out.println(command);
            Runtime run = Runtime.getRuntime();
            Process p = run.exec(command);
            BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String temp = null;
            while ((temp = br.readLine()) != null) {
                System.out.println("temp");
                
            }
            p.waitFor();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
