/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package analysis.pipeline.libgbs;

import java.io.File;
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
        fs = IOUtils.listFilesEndsWith(fs, ".tc");
        TagCounts tc = new TagCounts(fs[0].getAbsolutePath());
        boolean ifCollapsed = false;
        for (int i = 1; i < fs.length; i++) {
            ifCollapsed = false;
            TagCounts atc = new TagCounts(fs[i].getAbsolutePath());
            tc.addTagCounts(atc);
            if (tc.getMaxTagNumberInGroups() < collapseThreshold) continue;
            tc.collapseCounts();
            ifCollapsed = true;
        }
        if (!ifCollapsed) {
            tc.collapseCounts();
        }
        tc.writeBinaryFile(outputFileS);
    }
}
