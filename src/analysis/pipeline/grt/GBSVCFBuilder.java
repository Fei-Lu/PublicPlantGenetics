/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package analysis.pipeline.grt;

import format.dna.BaseEncoder;
import format.dna.snp.AlleleEncoder;
import format.dna.snp.genotype.AlleleDepth;
import format.dna.snp.genotype.VCFUtils;
import format.position.ChrPos;
import gnu.trove.list.array.TByteArrayList;
import gnu.trove.list.array.TIntArrayList;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Arrays;
import java.util.List;
import utils.IOUtils;
import utils.PArrayUtils;
import utils.PStringUtils;
import utils.Tuple;

/**
 *
 * @author feilu
 */
public class GBSVCFBuilder {
    TagAnnotations tas = null;
    SNPCounts sc = null;
    int numThreads = 32;
    int identityThreshold = 3;
    int maxAltNumber = 2;
    double sequencingAlignErrorRate = 0.05;
    
    public GBSVCFBuilder (TagAnnotations tas, SNPCounts sc) {
        this.tas = tas;
        this.sc = sc;
    }
    
    public void setThreads (int numThreads) {
        this.numThreads = numThreads;
    }
    
    public void setTagIdentifyThreshold (int identityThreshold) {
        this.identityThreshold = identityThreshold;
    }
    
    public void callGenotype (String tagBySampleDirS, String genotypeDirS) {
        File tempDir = new File(genotypeDirS, "temp");
        tempDir.mkdir();
        File genoDir = new File(genotypeDirS, "genotype");
        genoDir.mkdir();
        File[] sampleFiles = new File (tagBySampleDirS).listFiles();
        sampleFiles = IOUtils.listFilesEndsWith(sampleFiles, ".tas");
        Arrays.sort(sampleFiles);
        String[] sampleNames = new String[sampleFiles.length];       
        for (int i = 0; i < sampleNames.length; i++) {
            sampleNames[i] = sampleFiles[i].getName().replaceAll(".tas$", "");
        }
        int[][] indices = PArrayUtils.getSubsetsIndicesBySubsetSize(sampleFiles.length, this.numThreads);
        List<File> sampleFileList = Arrays.asList(sampleFiles);
        TagFinder tf = new TagFinder(tas);
        System.out.println("\nStart calling genotype of each individual sample...\n");
        for (int i = 0; i < indices.length; i++) {
            List<File> subFList = sampleFileList.subList(indices[i][0], indices[i][1]);
            subFList.parallelStream().forEach(f -> {
                String tempFileS = new File(tempDir, f.getName().replaceAll(".tas", ".gen")).getAbsolutePath();
                AlleleDepth[][] adt = this.initializeADTable();
                TagAnnotations ata = new TagAnnotations(f.getAbsolutePath());
                for (int j = 0; j < ata.getGroupNumber(); j++) {
                    for (int k = 0; k < ata.getTagNumber(j); k++) {
                        long[] tag = ata.getTag(j, k);
                        int readDepth = ata.getReadNumber(j, k);
                        byte r1Length = ata.getR1TagLength(j, k);
                        byte r2Length = ata.getR1TagLength(j, k);
                        Tuple<int[], int[]> result = tf.getMostSimilarTags(tag, r1Length, r2Length, j, identityThreshold);
                        if (result == null) continue;
                        int[] divergence = result.getFirstElement();
                        int[] tagIndices = result.getSecondElement();
                        int tagIndex = this.getTagIndex(divergence, tagIndices, j, tag);
                        if (tagIndex < 0) continue;
                        int alleleNumber = tas.getAlleleNumberOfTag(j, tagIndex);
                        if (alleleNumber == 0) continue;
                        short chr = tas.getAlleleOfTag(j, tagIndex).get(0).getChromosome();
                        int chrIndex = sc.getChrIndex(chr);
                        if (chrIndex < 0) continue;
                        for (int u = 0; u < alleleNumber; u++) {
                            AlleleInfo ai = tas.getAlleleOfTag(j, tagIndex).get(u);
                            int snpIndex = sc.getSNPIndex(chrIndex, new ChrPos(ai.getChromosome(), ai.getPosition()));
                            adt[chrIndex][snpIndex].addAllele(ai.getAllele());
                            adt[chrIndex][snpIndex].addDepth(readDepth);
                        }
                    }
                }
                for (int j = 0; j < adt.length; j++) {
                    for (int k = 0; k < adt[j].length; k++) {
                        adt[j][k].toArray();
                    }
                }
                this.writeTempGenotype(tempFileS, adt);
            });
        }
        this.writeGenotype(tempDir, sampleNames, genotypeDirS);
        File[] tempfs = tempDir.listFiles();
        for (int i = 0; i < tempfs.length; i++) tempfs[i].delete();
        tempDir.delete();
    }
    
    private void writeGenotype (File tempDir, String[] sampleNames, String genotypeDirS) {
        System.out.println("Start merging individual genotype into VCF by chromosomes");
        File genoDir = new File(genotypeDirS, "genotype");
        File[] fs = genoDir.listFiles();
        for (int i = 0; i < fs.length; i++) fs[i].delete();
        genoDir.mkdir();
        Arrays.sort(sampleNames);
        int chrNumber = sc.getChromosomeNumber();
        String[] outfiles = new String[chrNumber];
        for (int i = 0; i < outfiles.length; i++) {
            short chr = sc.getChromosome(i);
            outfiles[i] = new File (genoDir, "chr"+PStringUtils.getNDigitNumber(3, chr)+".vcf").getAbsolutePath();
        }
        int ii = 0;
        int jj = 0;
        int kk = 0;
        try {
            FileChannel[] fcs = new FileChannel[sampleNames.length]; 
            ByteBuffer[] bbs = new ByteBuffer[fcs.length];
            for (int i = 0; i < fcs.length; i++) {
                String inputFileS = new File(tempDir, sampleNames[i]+".gen").getAbsolutePath();
                Tuple<FileChannel, ByteBuffer> iot = IOUtils.getNIOChannelBufferReader(inputFileS, 65536);
                fcs[i] = iot.getFirstElement();
                bbs[i] = iot.getSecondElement();
            }
            for (int i = 0; i < fcs.length; i++) {
                fcs[i].read(bbs[i]);
                bbs[i].flip();
            }
            String annotation = VCFUtils.getVCFAnnotation();
            String header = VCFUtils.getVCFHeader(sampleNames);
            for (int i = 0; i < sc.getChromosomeNumber(); i++) {
                BufferedWriter bw = IOUtils.getTextWriter(outfiles[i]);
                bw.write(annotation);
                bw.write(header);
                bw.newLine();
                for (int j = 0; j < sc.getSNPNumberOnChromosome(i); j++) {
                    int[] depth = new int[6];
                    AlleleDepth[] sampleAD = new AlleleDepth[fcs.length];
                    for (int k = 0; k < fcs.length; k++) {
                        if (k == 51) {
                            int a = 3;
                        }
                        ii = i;jj = j;kk = k;
                        short chrIndex = bbs[k].getShort();
                        if (chrIndex == Short.MIN_VALUE) {
                            bbs[k].clear();
                            fcs[k].read(bbs[k]);
                            bbs[k].flip();
                            chrIndex = bbs[k].getShort();
                        }
                        else if (chrIndex == -1) {
                            
                        }
                        int posIndex = bbs[k].getInt();
                        if (chrIndex != i || posIndex != j) {
                            bbs[k].position(bbs[k].position()-6);
                            sampleAD[k] = new AlleleDepth();
                        }
                        else {
                            sampleAD[k] = new AlleleDepth();
                            byte alleleNumber = bbs[k].get();
                            for (int u = 0; u < alleleNumber; u++) {
                                byte cAllele = bbs[k].get();
                                int cDepth = bbs[k].getInt();
                                sampleAD[k].addAllele(cAllele);
                                sampleAD[k].addDepth(cDepth);
                                depth[cAllele]+=cDepth;
                            }
                        }
                        sampleAD[k].toArray();
                    }
                    TByteArrayList alleleList = new TByteArrayList();
                    TIntArrayList depthList = new TIntArrayList();
                    for (int k = 0; k < depth.length; k++) {
                        if (depth[k] == 0) continue;
                        alleleList.add(AlleleEncoder.alleleBytes[k]);
                        depthList.add(depth[k]);
                    }
                    AlleleDepth siteAD = new AlleleDepth(alleleList.toArray(), depthList.toArray());
                    AlleleDepth altAD = siteAD.getAltAlleleDepth(sc.getRefAlleleByteOfSNP(i, j));
                    altAD.sortByDepthDesending();
                    StringBuilder sb = new StringBuilder();
                    sb.append(sc.getChromosome(i)).append("\t").append(sc.getPositionOfSNP(i, j)).append("\t")
                            .append(sc.getChromosome(i)).append("-").append(sc.getPositionOfSNP(i, j)).append("\t")
                            .append(AlleleEncoder.alleleByteCharMap.get(sc.getRefAlleleByteOfSNP(i, j))).append("\t");
                    int altNum = altAD.getAlleleNumber();
                    if (altNum > this.maxAltNumber) altNum = this.maxAltNumber;
                    for (int k = 0; k < altNum; k++) {
                        sb.append(AlleleEncoder.alleleByteCharMap.get(altAD.getAllele(k))).append(",");
                    }
                    sb.deleteCharAt(sb.length()-1).append("\t.\t.\t");
                    sb.append("DP=").append(VCFUtils.getTotalDepth(sampleAD)).append(";AD=").append(VCFUtils.getAlleleTotalDepth(sampleAD, sc.getRefAlleleByteOfSNP(i, j))).append(",");
                    for (int k = 0; k < altNum; k++) {
                        sb.append(VCFUtils.getAlleleTotalDepth(sampleAD, altAD.getAllele(k))).append(",");
                    }
                    sb.deleteCharAt(sb.length()-1).append(";NS=").append(VCFUtils.getNumberOfTaxaWithAlleles(sampleAD)).append(";AP=").append(VCFUtils.getNumberOfTaxaWithAllele(sampleAD, sc.getRefAlleleByteOfSNP(i, j))).append(",");
                    for (int k = 0; k < altNum; k++) {
                        sb.append(VCFUtils.getNumberOfTaxaWithAllele(sampleAD, altAD.getAllele(k))).append(",");
                    }
                    sb.deleteCharAt(sb.length()-1);
                    sb.append("\tGT:AD:PL");
                    for (int k = 0; k < sampleAD.length; k++) {
                        int n = altNum+1;
                        int[] readCount = new int[n];
                        readCount[0] = sampleAD[k].getDepth(sc.getRefAlleleByteOfSNP(i, j));
                        for (int u = 0; u < altNum; u++) {
                            readCount[u+1] = sampleAD[k].getDepth(altAD.getAllele(u));
                        }
                        sb.append("\t").append(VCFUtils.getGenotype(readCount, sequencingAlignErrorRate));
                    }
                    bw.write(sb.toString());
                    bw.newLine();   
                }
                bw.flush();
                bw.close();
                System.gc();
            }
            for (int i = 0; i < fcs.length; i++) {
                fcs[i].close();
            }
        }
        catch (Exception e) {
            System.out.println(ii+"\t"+jj+"\t"+kk);
            e.printStackTrace();
        }
        System.out.println("VCF genotype is output to " + genotypeDirS);
    }
    
    private void wirteTextTempGenotype (String tempFileS, AlleleDepth[][] adt) {
        try {
            BufferedWriter bw = IOUtils.getTextWriter(tempFileS);
            bw.write("ChrIndex\tSNPIndex\tAlleleIndex\tAllele\tDepth");
            bw.newLine();
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < adt.length; i++) {
                for (int j = 0; j < adt[i].length; j++) {
                    int alleleNumber  = adt[i][j].getAlleleNumber();
                    for (int k = 0; k < alleleNumber; k++) {
                        sb = new StringBuilder();
                        sb.append(i).append("\t").append(j).append("\t").append(k).append("\t").append(adt[i][j].getAllele(k)).append("\t").append(adt[i][j].getDepth(k));
                        bw.write(sb.toString());
                        bw.newLine();
                    }
                }
            }
            bw.flush();
            bw.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void writeTempGenotype (String tempFileS, AlleleDepth[][] adt) {
        try {
            Tuple<FileChannel, ByteBuffer> iot = IOUtils.getNIOChannelBufferWriter(tempFileS, 65536);
            FileChannel fc = iot.getFirstElement();
            ByteBuffer bb = iot.getSecondElement();
            for (int i = 0; i < adt.length; i++) {
                for (int j = 0; j < adt[i].length; j++) {
                    int alleleNumber = adt[i][j].getAlleleNumber();
                    if (alleleNumber == 0) continue;
                    bb.putShort((short)i);
                    bb.putInt(j);
                    bb.put((byte)alleleNumber);
                    for (int k = 0; k < alleleNumber; k++) {
                        bb.put(adt[i][j].getAllele(k));
                        bb.putInt(adt[i][j].getDepth(k));
                    }
                    int remain = bb.remaining();
                    if (remain < 50) {
                        bb.putShort(Short.MIN_VALUE);
                        bb.putInt(Integer.MIN_VALUE);
                        bb.flip();
                        bb.limit(bb.capacity());
                        fc.write(bb);
                        bb.clear();
                    }
                }
            }
            if (bb.hasRemaining()) {
                bb.putShort((short)-1);
                bb.putInt(Integer.MIN_VALUE);
                bb.flip();
                bb.limit(bb.capacity());
                fc.write(bb);
                bb.clear();
            }
            fc.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private AlleleDepth[][] initializeADTable () {
        AlleleDepth[][] adt = new AlleleDepth[sc.getChromosomeNumber()][];
        for (int i = 0; i < adt.length; i++) {
            adt[i] = new AlleleDepth[sc.getSNPNumberOnChromosome(i)];
            for (int j = 0; j < adt[i].length; j++) {
                adt[i][j] = new AlleleDepth();
            }
        }
        return adt;
    }
    
    private int getTagIndex (int[] divergence, int[] tagIndices, int groupIndex, long[] queryTag) {
        byte[][] query = new byte[2][];
        for (int i = 0; i < 2; i++) {
            query[i] = BaseEncoder.getByteArrayFromLongs(Arrays.copyOfRange(queryTag, i*tas.getTagLengthInLong(), (i+1)*tas.getTagLengthInLong()));
            //System.out.println(BaseEncoder.getSequenceFromLongs(Arrays.copyOfRange(queryTag, i*tas.getTagLengthInLong(), (i+1)*tas.getTagLengthInLong())));
        }
        for (int i = 0; i < tagIndices.length; i++) {
            int cnt = 0;
            List<AlleleInfo> ai = tas.getAlleleOfTag(groupIndex, tagIndices[i]);
            byte[][] dbTag = new byte[2][];
            for (int j = 0; j < dbTag.length; j++) {
                dbTag[j] = BaseEncoder.getByteArrayFromLongs(Arrays.copyOfRange(tas.getTag(groupIndex, tagIndices[i]), j*tas.getTagLengthInLong(), (j+1)*tas.getTagLengthInLong()));
                //System.out.println(BaseEncoder.getSequenceFromLongs(Arrays.copyOfRange(tas.getTag(groupIndex, tagIndices[i]), j*tas.getTagLengthInLong(), (j+1)*tas.getTagLengthInLong())));
            }
            for (int j = 0; j < ai.size(); j++) {
                byte end = ai.get(j).getEnd();
                byte base = ai.get(j).getBase();
                byte pos = ai.get(j).getRelativePosition();
                if (query[end-1][pos-1] == base) cnt++;
            }
            if (cnt == ai.size()) return tagIndices[i];
        }
        return Integer.MIN_VALUE;
    }
}

