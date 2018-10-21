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
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
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
    int mapQThresh = 30;
    int maxMappingIntervalThresh = 1000;
    
    public TagDB (String dbFileS) {
        
    }
    
    public TagDB (String dbFileS, String mergedTagCountFileS) {        
        //this.initializeDB(dbFileS, mergedTagCountFileS);
    }
    
    private void initializeDB (String dbFileS, String mergedTagCountFileS) {
        TagCounts tc = new TagCounts(mergedTagCountFileS);
        System.out.println(tc.getTotalReadNumber());
        Connection conn = null;
        try {
            // db parameters
            String url = "jdbc:sqlite:"+dbFileS;
            // create a connection to the database
            conn = DriverManager.getConnection(url);
            System.out.println("Connection to SQLite has been established.");
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM runoob_tbl");
            
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }    
    }
    
    public void addAlignmentAndSNP (String samFileS) {
        System.out.println("Start adding alignments and raw SNPs to DB");
        try {
            BufferedReader br = IOUtils.getTextReader(samFileS);
            String temp = null;
            while ((temp = br.readLine()).startsWith("@SQ")){}
            String currentQuery = "";
            List<String> currentList = null;
            int queryCount = 0;
            List<SNP> tagSNPList = new ArrayList();
            long cnt = 0;
            int r1Start = Integer.MIN_VALUE;
            int r1End = Integer.MIN_VALUE;
            byte r1Strand = Byte.MIN_VALUE;
            int r2Start = Integer.MIN_VALUE;
            int r2End = Integer.MIN_VALUE;
            byte r2Strand = Byte.MIN_VALUE;
            while ((temp = br.readLine()) != null) {
                List<String> l = SAMUtils.getAlignElements(temp);
                List<SNP> snpList = SAMUtils.getVariants(l, mapQThresh);
                if (currentQuery.equals(l.get(0))) {
                    if (snpList != null) tagSNPList.addAll(snpList);
                    currentList = l;
                    queryCount++;
                }
                else {
                    if (!currentQuery.equals("")) {
                        if (queryCount == 2 && currentList.get(6).equals("=")) {
                            double len = Math.abs(Double.valueOf(currentList.get(8)));
                            if (len < maxMappingIntervalThresh) {
                                if (currentList.get(5).startsWith("*")) {
                                    r2Start = Integer.MIN_VALUE;
                                    r2End = Integer.MIN_VALUE;
                                    r2Strand = Byte.MIN_VALUE;
                                }
                                else {
                                    r2Start = Integer.parseInt(currentList.get(3));
                                    r2End = SAMUtils.getEndPos(currentList.get(5), r2Start);
                                    if (SAMUtils.isReverseAligned(Integer.parseInt(currentList.get(1)))) r2Strand = 0;
                                    else r2Strand = 1;
                                }
                                List<String> ll = PStringUtils.fastSplit(currentList.get(0), "_");                               
                                int groupIndex = Integer.parseInt(ll.get(0));
                                int tagIndex = Integer.parseInt(ll.get(1));
                                int tagCount = Integer.parseInt(ll.get(2));
                                cnt++;
                                if (cnt%10000000 == 0) System.out.println(String.valueOf(cnt) + " tags are properly aligned for SNP calling");
                                if (tagSNPList.size() != 0) {
                                    
                                    //add the tagSNPList to the database;
                                }
                            }
                        }
                    }
                    tagSNPList = new ArrayList();
                    if (snpList != null) tagSNPList.addAll(snpList);
                    
                    if (l.get(5).startsWith("*")) {
                        r1Start = Integer.MIN_VALUE;
                        r1End = Integer.MIN_VALUE;
                        r1Strand = Byte.MIN_VALUE;
                    }
                    else {
                        r1Start = Integer.parseInt(l.get(3));
                        r1End = SAMUtils.getEndPos(l.get(5), r1Start);
                        if (SAMUtils.isReverseAligned(Integer.parseInt(l.get(1)))) r1Strand = 0;
                        else r1Strand = 1;
                    }
                    
                    currentList = l;
                    currentQuery = currentList.get(0);
                    queryCount = 1;
                }                
            }
            if (queryCount == 2 && currentList.get(6).equals("=")) {
                double len = Math.abs(Double.valueOf(currentList.get(8)));
                if (len < maxMappingIntervalThresh) {
                    if (currentList.get(5).startsWith("*")) {
                        r2Start = Integer.MIN_VALUE;
                        r2End = Integer.MIN_VALUE;
                        r2Strand = Byte.MIN_VALUE;
                    }
                    else {
                        r2Start = Integer.parseInt(currentList.get(3));
                        r2End = SAMUtils.getEndPos(currentList.get(5), r2Start);
                        if (SAMUtils.isReverseAligned(Integer.parseInt(currentList.get(1)))) r2Strand = 0;
                        else r2Strand = 1;
                    }
                    List<String> ll = PStringUtils.fastSplit(currentList.get(0), "_");
                    int groupIndex = Integer.parseInt(ll.get(0));
                    int tagIndex = Integer.parseInt(ll.get(1));
                    int tagCount = Integer.parseInt(ll.get(2));
                    cnt++;
                    if (tagSNPList.size() != 0) {
                        //add the tagSNPList to the database;
                    }
                 }
            }
            br.close();
            System.out.println("A total of "+String.valueOf(cnt) + " tags are properly aligned for SNP calling");
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
