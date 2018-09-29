/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package analysis.pipeline.libgbs;

import java.io.BufferedReader;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import utils.IOUtils;

/**
 *
 * @author feilu
 */
public class LibGBSGo {
    String workingDirS = null;
    String barcodeFileS = null;
    String libraryFastqMapFileS = null;
    String referenceFileS = null;
    String bwaPath = null;
    String cutter1 = null;
    String cutter2 = null;
    String[] subDirS = {"tagsBySample","tagsLibrary","alignment", "rawGenotype", "filteredGenotype"};
    LibraryInfo li = null;
    
    public LibGBSGo (String parameterFileS) {
        this.initializeParameter(parameterFileS);
        //this.mkTagsBySample();
        //this.mergeTagCounts();
        this.alignTags();
        //this.mkTagDB();
    }
    
    public void mkTagDB () {
       String tagBySampleDirS = new File (this.workingDirS, this.subDirS[0]).getAbsolutePath();
       File[] fs = new File(tagBySampleDirS).listFiles();
       fs = IOUtils.listFilesEndsWith(fs, ".tc");
       int sum = 0;
       for (int i = 0; i < fs.length; i++) {
           TagCounts tc = new TagCounts(fs[i].getAbsolutePath());
           sum+=tc.getTotalReadNumber();
       }
       System.out.println(sum);
    }
    
    public void alignTags () {
        String tagLibraryDirS = new File (this.workingDirS, this.subDirS[1]).getAbsolutePath();
        String mergedTagCountFileS = new File(tagLibraryDirS, "tag.tc").getAbsolutePath();
        String alignmentDirS = new File (this.workingDirS, this.subDirS[2]).getAbsolutePath();
        new TagAligner(referenceFileS, this.bwaPath, mergedTagCountFileS, alignmentDirS);
    }
    
    public void mergeTagCounts () {
        String tagBySampleDirS = new File (this.workingDirS, this.subDirS[0]).getAbsolutePath();
        String tagLibraryDirS = new File (this.workingDirS, this.subDirS[1]).getAbsolutePath();
        String mergedTagCountFileS = new File(tagLibraryDirS, "tag.tc").getAbsolutePath();
        new TagMerger(tagBySampleDirS, mergedTagCountFileS);
    }
    
    public void mkTagsBySample () {
        li = new LibraryInfo(barcodeFileS, libraryFastqMapFileS, this.cutter1, this.cutter2);
        String tagBySampleDirS = new File (this.workingDirS, this.subDirS[0]).getAbsolutePath();
        TagParser tp = new TagParser(li);
        tp.parseFastq(tagBySampleDirS);
        tp.compressTagsBySample(tagBySampleDirS);
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
        this.referenceFileS = paList.get(3);
        this.bwaPath = paList.get(4);
        this.cutter1 = paList.get(5).toUpperCase();
        this.cutter2 = paList.get(6).toUpperCase();
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
