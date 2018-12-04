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
import utils.PArrayUtils;

/**
 *
 * @author feilu
 */
public class GBSVCFBuilder {
    TagAnnotations tas = null;
    SNPCounts sc = null;
    int paraLevel = 32;
    
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
        int[][] indices = PArrayUtils.getSubsetsIndicesBySubsetSize(sampleFiles.length, this.paraLevel);
        List<File> sampleFileList = Arrays.asList(sampleFiles);
        TagFinder tf = new TagFinder(tas);
        for (int i = 0; i < indices.length; i++) {
            List<File> subFList = sampleFileList.subList(indices[0][0], indices[0][1]);
            subFList.parallelStream().forEach(f -> {
                TagAnnotations ata = new TagAnnotations(f.getAbsolutePath());
                for (int j = 0; j < ata.getGroupNumber(); j++) {
                    for (int k = 0; k < ata.getTagNumber(j); k++) {
                        long[] tag = ata.getTag(j, k);
                        byte r1Length = ata.getR1TagLength(j, k);
                        byte r2Length = ata.getR1TagLength(j, k);
                        tf.findMostSimilarTag(tag, r1Length, r2Length, j);
                    }
                }
            });
        }
    }
}
