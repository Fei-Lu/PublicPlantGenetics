/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package format.alignment;

import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import utils.IOUtils;

/**
 *
 * @author feilu
 */
public class SAMSEAlignment {
    SEAlignRecord[] sars = null;
    
    public SAMSEAlignment () {}
    
    public void readFromBWAMEM (String inputFileS) {
        System.out.println("Reading SAM format alignment (BWA-MEM) from: " + inputFileS);
        List<SEAlignRecord> rList = new ArrayList();
        try {
            BufferedReader br;
            if (inputFileS.endsWith(".gz")) {
                br = IOUtils.getTextGzipReader(inputFileS);
            } else {
                br = br = IOUtils.getTextReader(inputFileS);
            }
            while(br.readLine().startsWith("@PG")==false) {};
            String inputStr = null;
            int cnt = 0;
            
            Set<String> querySet = new HashSet();
            
            while((inputStr = br.readLine())!=null) {
                SEAlignRecord sar = SAMUtils.getSEAlignRecord(inputStr);
                rList.add(sar);
                cnt++;
                if (cnt%500000 == 0) System.out.println("Read in " + String.valueOf(cnt) + " lines");
                
                querySet.add(sar.getQuery());
                
            }
            
            String[] queries = querySet.toArray(new String[querySet.size()]);
            Arrays.sort(queries);
            int[] counts = new int[queries.length];
            for (int i = 0; i < rList.size(); i++) {
                int index = Arrays.binarySearch(queries, rList.get(i).getQuery());
                counts[index]++;
            }
            for (int i = 0; i < counts.length; i++) {
                if (counts[i] == 2) continue;
                System.out.println(counts[i]+"\t"+queries[i]);
            }
            
            sars = rList.toArray(new SEAlignRecord[rList.size()]);
            br.close();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
        //this.sortByQuery();
        System.out.println("SAMSEAlignment object has "+String.valueOf(sars.length) + " alignment records");
    }
}
