/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package analysis.pipeline.libgbs;

import format.table.RowTable;
import gnu.trove.list.array.TIntArrayList;
import java.io.DataOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *
 * @author feilu
 */
public class LaneInfo {
    
    String[] lanes = null;
    String[][] taxaNames = null;
    String[][] barcodeR1 = null;
    String[][] barcodeR2 = null;
    HashMap<String, Set<String>>[] barcodeR1TaxaMaps = null;
    HashMap<String, Set<String>>[] barcodeR2TaxaMaps = null;
    String[] laneFastqsR1 = null;
    String[] laneFastqsR2 = null;
    String cutter1 = null;
    String cutter2 = null;
    
    public LaneInfo (String barcodeFileS, String laneFastqMapFileS, String cutter1, String cutter2) {
        this.parseBarcode(barcodeFileS, laneFastqMapFileS);
        this.cutter1 = cutter1;
        this.cutter2 = cutter2;
    }
    
    public int getLaneNumber () {
        return lanes.length;
    }
    
    public String[] getLaneArray () {
        String[] na = new String[lanes.length];
        System.arraycopy(lanes, 0, na, 0, lanes.length);
        return na;
    }
    
    public String getCutter1 () {
        return this.cutter1;
    }
    
    public String getCutter2 () {
        return this.cutter2;
    }
    
    public String getLaneName (int index) {
        return lanes[index];
    }
    
    public String[] getTaxaNames (int index) {
        return taxaNames[index];
    }
    
    public String[] getLaneBarcodeR1 (int index) {
        String[] na = new String[barcodeR1[index].length];
        System.arraycopy(barcodeR1[index], 0, na, 0, barcodeR1[index].length);
        return na;
    }
    
    public String[] getLaneBarcodeR2 (int index) {
        String[] na = new String[barcodeR2[index].length];
        System.arraycopy(barcodeR2[index], 0, na, 0, barcodeR2[index].length);
        return na;
    }
    
    public HashMap<String, Set<String>> getbarcodeR1TaxaMap (int index) {
        return barcodeR1TaxaMaps[index];
    }
    
    public HashMap<String, Set<String>> getbarcodeR2TaxaMap (int index) {
        return barcodeR2TaxaMaps[index];
    }
    
    public String getFastqFileSR1 (int index) {
        return laneFastqsR1[index];
    }
    
    public String getFastqFileSR2 (int index) {
        return laneFastqsR2[index];
    }
    
    private void parseBarcode (String barcodeFileS, String laneFastqMapFileS) {
        RowTable<String> t = new RowTable<>(barcodeFileS);
        Set<String> s = new HashSet<>();
        for (int i = 0; i < t.getRowNumber(); i++) {
            StringBuilder sb = new StringBuilder();
            sb.append(t.getCell(i, 1)).append("_").append(t.getCell(i, 2));
            s.add(sb.toString());
        }
        lanes = s.toArray(new String[s.size()]);
        Arrays.sort(lanes);
        taxaNames = new String[lanes.length][];
        barcodeR1 = new String[lanes.length][];
        barcodeR2 = new String[lanes.length][];
        barcodeR1TaxaMaps = new HashMap[lanes.length];
        barcodeR2TaxaMaps = new HashMap[lanes.length];
        
        laneFastqsR1 = new String[lanes.length];
        laneFastqsR2 = new String[lanes.length];
        for (int i = 0; i < lanes.length; i++) {
            List<String> nameList = new ArrayList<>();
            List<String> barcodeR1List = new ArrayList<>();
            List<String> barcodeR2List = new ArrayList<>();
            for (int j = 0; j < t.getRowNumber(); j++) {
                StringBuilder sb = new StringBuilder();
                sb.append(t.getCell(j, 1)).append("_").append(t.getCell(j, 2));
                if (!sb.toString().equals(lanes[i])) continue;
                sb = new StringBuilder();
                sb.append(t.getCell(j, 0)).append("_").append(t.getCell(j, 1)).append("_").append(t.getCell(j, 2)).append("_").append(t.getCell(j, 3));
                nameList.add(sb.toString());
                barcodeR1List.add(t.getCell(j, 5));
                barcodeR2List.add(t.getCell(j, 6)); 
            }
            taxaNames[i] = nameList.toArray(new String[nameList.size()]);
            barcodeR1[i] = barcodeR1List.toArray(new String[nameList.size()]);
            barcodeR2[i] = barcodeR2List.toArray(new String[nameList.size()]);
            barcodeR1TaxaMaps[i] = new HashMap<>();
            barcodeR2TaxaMaps[i] = new HashMap<>();
            for (int j = 0; j < barcodeR1List.size(); j++) {
                s = new HashSet<>();
                barcodeR1TaxaMaps[i].put(barcodeR1List.get(j), s);
                s = new HashSet<>();
                barcodeR2TaxaMaps[i].put(barcodeR2List.get(j), s);
            }
            for (int j = 0; j < taxaNames[i].length; j++) {
                s = barcodeR1TaxaMaps[i].get(barcodeR1[i][j]);
                s.add(taxaNames[i][j]);
                barcodeR1TaxaMaps[i].put(barcodeR1[i][j], s);
                s = barcodeR2TaxaMaps[i].get(barcodeR2[i][j]);
                s.add(taxaNames[i][j]);
                barcodeR2TaxaMaps[i].put(barcodeR2[i][j], s);
            }
        }
        t = new RowTable<>(laneFastqMapFileS);
        List<String> lList = new ArrayList();
        for (int i = 0; i < t.getRowNumber(); i++) {
            StringBuilder sb = new StringBuilder();
            sb.append(t.getCell(i, 0)).append("_").append(t.getCell(i, 1));
            lList.add(sb.toString());
        }
        Collections.sort(lList);
        TIntArrayList availableIndexList = new TIntArrayList();
        for (int i = 0; i < lanes.length; i++) {
            int index = Collections.binarySearch(lList, lanes[i]);
            if (index < 0) {
                System.out.println(lanes[i] + " does not have corresponding fastqs");
            }
            else {
                if (t.getCell(index, 2).equals("NA") || t.getCell(index, 3).equals("NA")) {
                    System.out.println(lanes[i] + " does not have corresponding fastqs");
                }
                else {
                    this.laneFastqsR1[i] = t.getCell(index, 2);
                    this.laneFastqsR2[i] = t.getCell(index, 3);
                    availableIndexList.add(i);
                }
            } 
        }
        List<String> laneList = new ArrayList();
        List<String[]> taxaNameList = new ArrayList();
        List<String[]> barcodeR1List = new ArrayList();
        List<String[]> barcodeR2List = new ArrayList();
        List<HashMap<String, Set<String>>> r1MapList = new ArrayList();
        List<HashMap<String, Set<String>>> r2MapList = new ArrayList();
        List<String> laneFqR1List = new ArrayList();
        List<String> laneFqR2List = new ArrayList();
        int[] aIndex = availableIndexList.toArray();
        for (int i = 0; i < aIndex.length; i++) {
            laneList.add(lanes[aIndex[i]]);
            taxaNameList.add(taxaNames[aIndex[i]]);
            barcodeR1List.add(barcodeR1[aIndex[i]]);
            barcodeR2List.add(barcodeR1[aIndex[i]]);
            r1MapList.add(barcodeR1TaxaMaps[aIndex[i]]);
            r2MapList.add(barcodeR2TaxaMaps[aIndex[i]]);
            laneFqR1List.add(laneFastqsR1[aIndex[i]]);
            laneFqR2List.add(laneFastqsR2[aIndex[i]]);
        }
        
        lanes = laneList.toArray(new String[laneList.size()]);
        taxaNames = taxaNameList.toArray(new String[taxaNameList.size()][]);
        barcodeR1 = barcodeR1List.toArray(new String[barcodeR1List.size()][]);
        barcodeR2 = barcodeR2List.toArray(new String[barcodeR2List.size()][]);
        barcodeR1TaxaMaps = r1MapList.toArray(new HashMap[r1MapList.size()]);
        barcodeR2TaxaMaps = r2MapList.toArray(new HashMap[r2MapList.size()]);
        laneFastqsR1 = laneFqR1List.toArray(new String[laneFqR1List.size()]);
        laneFastqsR2 = laneFqR2List.toArray(new String[laneFqR2List.size()]);
        
        System.out.println(lanes.length+" lanes will be paralell processd. They are:");
        int cnt = 0;
        for (int i = 0; i < lanes.length; i++) {
            StringBuilder sb = new StringBuilder();
            sb.append(lanes[i]).append(" with ").append(this.taxaNames[i].length).append(" samples");
            System.out.println(sb.toString());
            cnt+=this.taxaNames[i].length;
        }
        System.out.println("A total of " + String.valueOf(cnt) + " samples are in the current batch");
    }
    
}
