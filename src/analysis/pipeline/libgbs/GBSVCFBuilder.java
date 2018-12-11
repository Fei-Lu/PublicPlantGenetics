/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package analysis.pipeline.libgbs;

import cern.colt.GenericSorting;
import cern.colt.Swapper;
import cern.colt.function.IntComparator;
import format.dna.snp.AlleleEncoder;
import format.dna.snp.genotype.AlleleDepth;
import format.dna.snp.genotype.VCFUtils;
import format.position.ChrPos;
import gnu.trove.list.array.TByteArrayList;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.set.hash.TByteHashSet;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
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
    int paraLevel = 32;
    int maxDivergence = 5;
    int maxAltNumber = 2;
    
    public GBSVCFBuilder (TagAnnotations tas, SNPCounts sc) {
        this.tas = tas;
        this.sc = sc;
    }
    
    public void callGenotype (String tagBySampleDirS, String genotypeDirS) {
        File tempDir = new File(genotypeDirS, "temp");
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
            List<File> subFList = sampleFileList.subList(indices[i][0], indices[i][1]);
            subFList.stream().forEach(f -> {
                String tempFileS = new File(tempDir, f.getName().replaceAll(".tas", ".gen")).getAbsolutePath();
                AlleleDepth[][] adt = this.initializeADTable();
                TagAnnotations ata = new TagAnnotations(f.getAbsolutePath());
                for (int j = 0; j < ata.getGroupNumber(); j++) {
                    for (int k = 0; k < ata.getTagNumber(j); k++) {
                        long[] tag = ata.getTag(j, k);
                        int readDepth = ata.getReadNumber(j, k);
                        byte r1Length = ata.getR1TagLength(j, k);
                        byte r2Length = ata.getR1TagLength(j, k);
                        Tuple<int[], int[]> result = tf.getMostSimilarTags(tag, r1Length, r2Length, j, maxDivergence);
                        if (result == null) continue;
                        int[] divergence = result.getFirstElement();
                        int[] tagIndices = result.getSecondElement();
                        int tagIndex = this.getTagIndex(divergence, tagIndices);
                        TByteArrayList alleleList = tas.getAlleleOfTag(j, tagIndex);
                        if (alleleList.size() == 0) continue;
                        List<ChrPos> chrPosList = tas.getAllelePosOfTag(j, tagIndex);
                        short chr = chrPosList.get(0).getChromosome();
                        int chrIndex = sc.getChrIndex(chr);
                        for (int u = 0; u < chrPosList.size(); u++) {
                            int snpIndex = sc.getSNPIndex(chrIndex, chrPosList.get(u));
                            adt[chrIndex][snpIndex].addAllele(alleleList.get(u));
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
    }
    
    private void writeGenotype (File tempDir, String[] sampleNames, String genotypeDirS) {
        File genoDir = new File(genotypeDirS, "genotype");
        genoDir.mkdir();
        Arrays.sort(sampleNames);
        int chrNumber = sc.getChromosomeNumber();
        String[] outfiles = new String[chrNumber];
        for (int i = 0; i < outfiles.length; i++) {
            short chr = sc.getChromosome(i);
            outfiles[i] = new File (genoDir, PStringUtils.getNDigitNumber(3, chr)+".vcf").getAbsolutePath();
        }
        try {
            DataInputStream[] dis = new DataInputStream[sampleNames.length];
            for (int i = 0; i < dis.length; i++) {
                String inputFileS = new File(tempDir, sampleNames[i]+".gen").getAbsolutePath();
                dis[i] = IOUtils.getBinaryReader(inputFileS, 4096);
            }
            BufferedWriter[] bws = new BufferedWriter[outfiles.length];
            String annotation = VCFUtils.getVCFAnnotation();
            String header = VCFUtils.getVCFHeader(sampleNames);
            for (int i = 0; i < bws.length; i++) {
                bws[i] = IOUtils.getTextWriter(outfiles[i]);
                bws[i].write(annotation);
                bws[i].write(header);
                bws[i].newLine();
            }
            for (int i = 0; i < sc.getChromosomeNumber(); i++) {
                for (int j = 0; j < sc.getSNPNumberOnChromosome(i); j++) {
                    int[] depth = new int[6];
                    AlleleDepth[] sampleAD = new AlleleDepth[dis.length];
                    for (int k = 0; k < dis.length; k++) {
                        sampleAD[k] = new AlleleDepth();
                        int alleleNumber = dis[k].readInt();
                        for (int u = 0; u < alleleNumber; u++) {
                            byte cAllele = dis[k].readByte();
                            int cDepth = dis[k].readInt();
                            sampleAD[k].addAllele(cAllele);
                            sampleAD[k].addDepth(cDepth);
                            depth[cAllele]+=cDepth;
                        }
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
                    sb.deleteCharAt(sb.length()-1);
                    
                    bws[i].write(sb.toString());
                    bws[i].newLine();
                        
                }
            }
            
            for (int i = 0; i < dis.length; i++) {
                dis[i].close();
            }
            for (int i = 0; i < bws.length; i++) {
                bws[i].flush();
                bws[i].close();
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void writeTempGenotype (String tempFileS, AlleleDepth[][] adt) {
        try {
            DataOutputStream dos = IOUtils.getBinaryWriter(tempFileS);
            for (int i = 0; i < adt.length; i++) {
                for (int j = 0; j < adt[i].length; j++) {
                    int alleleNumber  = adt[i][j].getAlleleNumber();
                    dos.writeInt(alleleNumber);
                    for (int k = 0; k < alleleNumber; k++) {
                        dos.writeByte(adt[i][j].getAllele(k));
                        dos.writeInt(adt[i][j].getDepth(k));
                    }
                }
            }
            dos.flush();
            dos.close();
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
    
    private int getTagIndex (int[] divergence, int[] tagIndices) {
        return tagIndices[0];
    }
}

