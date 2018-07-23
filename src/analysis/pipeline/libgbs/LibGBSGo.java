/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package analysis.pipeline.libgbs;

import java.io.BufferedReader;
import java.io.File;
import java.util.ArrayList;
import utils.IOUtils;

/**
 *
 * @author feilu
 */
public class LibGBSGo {
    String workingDirS = null;
    String barcodeFileS = null;
    String libraryFastqMapFileS = null;
    String cutter1 = null;
    String cutter2 = null;
    String[] subDirS = {"tagsBySample","tagsLibrary","alignment", "rawGenotype", "filteredGenotype"};
    LibraryInfo li = null;
    
    public LibGBSGo (String parameterFileS) {
        this.initializeParameter(parameterFileS);
        this.mkTagsBySample();
    }
    
    public void mkTagsBySample () {
        li = new LibraryInfo(barcodeFileS, libraryFastqMapFileS, this.cutter1, this.cutter2);
        String tagBySampleDirS = new File (this.workingDirS, this.subDirS[0]).getAbsolutePath();
        String tagLibraryDirS = new File (this.workingDirS, this.subDirS[1]).getAbsolutePath();
        TagParser tp = new TagParser(li);
        tp.parseFastq(tagBySampleDirS);
        tp.compressTagsBySample(tagBySampleDirS);
        tp.mergeTagsBySample();
    }
    
    public void initializeParameter (String parameterFileS) {
        ArrayList<String> paList = new ArrayList();
        try {
            boolean check = false;
            BufferedReader br = IOUtils.getTextReader(parameterFileS);
            if (!br.readLine().equals("Author: Fei Lu")) check = true;
            if (!br.readLine().equals("Email: flu@genetics.ac.cn; dr.lufei@gmail.com")) check = true;
            if (!br.readLine().equals("Homepage: http://plantgeneticslab.weebly.com/")) check = true;
            if (check) {
                System.out.println("Please keep the author information, or the program quits.");
            }
            String temp = null;
            while ((temp = br.readLine()) != null) {
                if (temp.startsWith("!Parameter")) {
                    paList.add(br.readLine());
                }
            }
            br.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        this.workingDirS = paList.get(0);
        this.barcodeFileS = paList.get(1);
        this.libraryFastqMapFileS = paList.get(2);
        this.cutter1 = paList.get(3).toUpperCase();
        this.cutter2 = paList.get(4).toUpperCase();
        File workingDir = new File(this.workingDirS);
        workingDir.mkdir();
        for (int i = 0; i < this.subDirS.length; i++) {
            File f = new File (this.workingDirS, subDirS[i]);
            f.mkdir();
        }
    }
    
    public static void main (String[] args) {
       new LibGBSGo (args[0]);
    }
    
}
