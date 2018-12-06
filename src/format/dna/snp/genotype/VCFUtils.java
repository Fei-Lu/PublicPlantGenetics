/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package format.dna.snp.genotype;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 *
 * @author feilu
 */
public class VCFUtils {
    
    public static String getVCFHeader (String[] taxaNames) {
        StringBuilder sb = new StringBuilder();
        sb.append("#CHR\tPOS\tID\tREF\tALT\tQUAL\tFILTER\tINFO\tFORMAT");
        for (int i = 0; i < taxaNames.length; i++) {
            sb.append("\t").append(taxaNames[i]);
        }
        return sb.toString();
    }
    
    public static String getVCFAnnotation () {
        StringBuilder sb = new StringBuilder();
        sb.append("##fileformat=VCFv4.2\n");
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss.SSS");
        Date dt = new Date();
        String S = sdf.format(dt);
        sb.append("##fileDate=").append(S.split(" ")[0]).append("\n");
        sb.append("##INFO=<ID=DP,Number=1,Type=Integer,Description=\"").append("Total depth").append("\">\n");
        sb.append("##INFO=<ID=AD,Number=2+,Type=Integer,Description=\"").append("Allele depth").append("\">\n");
        sb.append("##INFO=<ID=NZ,Number=1,Type=Integer,Description=\"").append("Number of individuals with alleles present").append("\">\n");
        sb.append("##INFO=<ID=AP,Number=2+,Type=Integer,Description=\"").append("Number of individuals in which an allele is present").append("\">\n");
        sb.append("##INFO=<ID=DI,Number=2,Type=Integer,Description=\"").append("Number of deletion and insertion type").append("\">\n");
        return sb.toString();
    }
}
