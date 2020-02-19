package pgl.infra.dna.genotype;

import pgl.infra.utils.IOUtils;
import sun.nio.ch.IOUtil;

import java.io.BufferedWriter;

public class GenotypeExport {

    public static void output(GenotypeTable gt, String outfileS, GenoIOFormat format) {
        if (format == GenoIOFormat.VCF) {
            toVCF(gt, outfileS,false);
        }
        else if (format == GenoIOFormat.VCF_GZ) {
            toVCF(gt, outfileS,true);
        }
        else if (format == GenoIOFormat.HDF5) {

        }
    }

    private static void toVCF (GenotypeTable gt, String outfileS, boolean ifGZ) {
        BufferedWriter bw = null;
        if (ifGZ) bw = IOUtils.getTextGzipWriter(outfileS);
        else bw = IOUtils.getTextWriter(outfileS);
        try {
            bw.write(VCFUtils.getVCFAnnotation());
            bw.write(VCFUtils.getVCFHeader(gt.getTaxaNames()));
            bw.newLine();
            for (int i = 0; i < gt.getSiteNumber(); i++) {
                bw.write(gt.getUnphasedVCFRecord(i));
                bw.newLine();
            }
            bw.flush();
            bw.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
