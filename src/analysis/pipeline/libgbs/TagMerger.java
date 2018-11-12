/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package analysis.pipeline.libgbs;

import java.io.File;
import java.util.Arrays;
import utils.IOUtils;

/**
 *
 * @author feilu
 */
class TagMerger {
    String inputDirS = null;
    String outputFileS = null;
    int collapseThreshold = (int)(Integer.MAX_VALUE * 0.75);
    public TagMerger (String inputDirS, String outputFileS) {
        this.mergeTagCounts(inputDirS, outputFileS);
    }
    
    public void mergeTagCounts (String inputDirS, String outputFileS) {
        File[] fs = new File(inputDirS).listFiles();
        fs = IOUtils.listFilesEndsWith(fs, ".tas");
        Arrays.sort(fs);
        System.out.println("Merging "+String.valueOf(fs.length)+" individual TagAnnotations files");
        TagAnnotations ta = new TagAnnotations(fs[0].getAbsolutePath());
        boolean ifCollapsed = false;
        int cnt = 0;
        for (int i = 1; i < fs.length; i++) {
            ifCollapsed = false;
            TagAnnotations ata = new TagAnnotations(fs[i].getAbsolutePath());
            ta.addTagAnnotations(ata);
            cnt++;
            if (cnt%100 == 0) System.out.println(String.valueOf(cnt) + "TagAnnotations files have been merged");
            if (ta.getMaxTagNumberAcrossGroups() < collapseThreshold) continue;
            ta.collapseCounts();
            ifCollapsed = true;
        }
        if (!ifCollapsed) {
            ta.collapseCounts();
        }
        System.out.println("A total of " + String.valueOf(fs.length) + " TagAnnotations files are merged");
        System.out.println(String.valueOf(ta.getTagNumber()) + " tags are devided into " + String.valueOf(ta.getGroupNumber()) + " tag groups");
        ta.writeBinaryFile(outputFileS);
    }
}
