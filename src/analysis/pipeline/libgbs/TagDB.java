/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package analysis.pipeline.libgbs;

import format.alignment.SAMUtils;
import format.dna.snp.SNP;
import gnu.trove.list.array.TDoubleArrayList;
import graphcis.r.DensityPlot;
import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import utils.IOUtils;
import utils.PStringUtils;

/**
 *
 * @author feilu
 */
public class TagDB {
    
    public TagDB () {}
    
    public TagDB (String dbFileS) {
        
    }
    
    public void initializeDB (String mergedTagCountFileS) {
        
    }
    
    public void SNPCalling (String samFileS) {
        int mapQThresh = 30;
        int maxMappingIntervalThresh = 1000;
        TDoubleArrayList lenList = new TDoubleArrayList();
        try {
            BufferedReader br = IOUtils.getTextReader(samFileS);
            String temp = null;
            while ((temp = br.readLine()).startsWith("@SQ")){}
            String currentQuery = "";
            List<String> currentList = null;
            int queryCount = 0;
            List<SNP> tagSNPList = new ArrayList();
            int cnt = 0;
            while ((temp = br.readLine()) != null) {
                List<String> l = SAMUtils.getAlignElements(temp);
                List<SNP> snpList = SAMUtils.getVariants(l, mapQThresh);
                if (currentQuery.equals(l.get(0))) {
                    if (snpList != null) tagSNPList.addAll(snpList);
                    queryCount++;
                }
                else {
                    if (!currentQuery.equals("")) {
                        if (queryCount == 2 && currentList.get(6).equals("=")) {
                            double len = Math.abs(Double.valueOf(currentList.get(8)));
                            if (len < maxMappingIntervalThresh) {
                                List<String> ll = PStringUtils.fastSplit(currentList.get(0), "_");
                                int groupIndex = Integer.parseInt(ll.get(0));
                                int tagIndex = Integer.parseInt(ll.get(1));
                                int tagCount = Integer.parseInt(ll.get(2));
                                cnt+=2;
                                if (tagSNPList.size() == 0) continue;
                                //add the tagSNPList to the database;
                               
                            }
                        }
                    }
                    tagSNPList = new ArrayList();
                    if (snpList != null) tagSNPList.addAll(snpList);
                    currentList = l;
                    currentQuery = currentList.get(0);
                    queryCount = 1;
                }
                if (queryCount == 2 && currentList.get(6).equals("=")) {
                double len = Math.abs(Double.valueOf(currentList.get(8)));
                if (len < maxMappingIntervalThresh) {
                    List<String> ll = PStringUtils.fastSplit(currentList.get(0), "_");
                    int groupIndex = Integer.parseInt(ll.get(0));
                    int tagIndex = Integer.parseInt(ll.get(1));
                    int tagCount = Integer.parseInt(ll.get(2));
                    cnt+=2;
                    if (tagSNPList.size() == 0) continue;
                    //add the tagSNPList to the database;

                    }
                }
                br.close();
            }
            System.out.println(cnt);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
