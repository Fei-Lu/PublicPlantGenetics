/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package analysis.pipeline.libgbs;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import utils.IOUtils;

/**
 *
 * @author feilu
 */
public class GBSVCFBuilder {
    TagAnnotations tas = null;
    SNPCounts sc = null;
    
    public GBSVCFBuilder (TagAnnotations tas, SNPCounts sc) {
        this.tas = tas;
        this.sc = sc;
    }
    
    public void callGenotype (String tagBySampleDirS, String genotypeFileS) {
        File tempDir = new File(genotypeFileS, "temp");
        tempDir.mkdir();
        File[] sampleFiles = new File (tagBySampleDirS).listFiles();
        sampleFiles = IOUtils.listFilesEndsWith(sampleFiles, ".tas");
        Arrays.sort(sampleFiles);
        String[] sampleNames = new String[sampleFiles.length];
        for (int i = 0; i < sampleNames.length; i++) {
            sampleNames[i] = sampleFiles[i].getName().replaceAll(".tas$", "");
            System.out.println(sampleNames[i]);
        }
        List<File> fList = Arrays.asList(sampleFiles);
        fList.parallelStream().forEach(f -> {
            TagAnnotations ata = new TagAnnotations(f.getAbsolutePath());
            
        });
    }
}
