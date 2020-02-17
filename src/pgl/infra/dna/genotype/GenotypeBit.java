package pgl.infra.dna.genotype;

import pgl.PGLConstraints;
import pgl.infra.utils.IOUtils;
import pgl.infra.utils.PStringUtils;

import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import pgl.infra.position.ChrPos;

public class GenotypeBit implements GenotypeTable {
    List<String> taxaList = null;
    List<SiteGenotypeBit> genoList = null;

    public GenotypeBit () {
        
    }

    public GenotypeBit (String infileS, GenoIOFormat format) {
        if (format == GenoIOFormat.VCF) {
            this.buildFromVCF(infileS);
        }
        else if (format == GenoIOFormat.HDF5) {
            this.buildFromHDF5(infileS);
        }
    }
    
    @Override
    public int getTaxaNumber() {
        return taxaList.size();
    }

    @Override
    public String getTaxonName(int taxonIndex) {
        return taxaList.get(taxonIndex);
    }

    @Override
    public int getTaxonIndex(String taxon) {
        return Collections.binarySearch(taxaList, taxon);
    }
    
    @Override
    public int getSiteNumber () {
        return this.genoList.size();
    }
    
    @Override
    public short getChromosome(int siteIndex) {
        return genoList.get(siteIndex).getChromosome();
    }

    @Override
    public int getPosition(int siteIndex) {
        return genoList.get(siteIndex).getPosition();
    }

    @Override
    public void sortBySite() {
        Collections.sort(genoList);
    }

    @Override
    public void sortByTaxa() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public byte getGenotypeByte (int siteIndex, int taxonIndex) {
        return this.genoList.get(siteIndex).getGenotypeByte(taxonIndex);
    }

    @Override
    public boolean isHeterozygous(int siteIndex, int taxonIndex) {
        return this.genoList.get(siteIndex).isHeterozygous(taxonIndex);
    }

    @Override
    public boolean isHomozygous(int siteIndex, int taxonIndex) {
        return this.genoList.get(siteIndex).isHomozygous(taxonIndex);
    }

    @Override
    public boolean isMissing(int siteIndex, int taxonIndex) {
        return this.genoList.get(siteIndex).isMissing(siteIndex);
    }

    @Override
    public int getSiteIndex(short chromosome, int position) {
        ChrPos query = new ChrPos (chromosome, position);
        int index = Collections.binarySearch(genoList, query);
        return index;
    }

    @Override
    public int getMissingNumberBySite(int siteIndex) {
        return this.genoList.get(siteIndex).getMissingNumber();
    }

    @Override
    public int getMissingNumberByTaxon(int taxonIndex) {
        int cnt = 0;
        for (int i = 0; i < this.getSiteNumber(); i++) {
            if (this.genoList.get(i).isMissing(taxonIndex)) cnt++;
        }
        return cnt;
    }
    
    @Override
    public int getNonMissingNumberBySite(int siteIndex) {
        return this.getTaxaNumber()-this.getMissingNumberBySite(siteIndex);
    }

    @Override
    public int getNonMissingNumberByTaxon(int taxonIndex) {
        return this.getSiteNumber()-this.getMissingNumberByTaxon(taxonIndex);
    }


    @Override
    public int getHomozygoteNumberBySite(int siteIndex) {
        return this.genoList.get(siteIndex).getHomozygoteNumber();
    }

    @Override
    public int getHomozygoteNumberByTaxon(int taxonIndex) {
        int cnt = 0;
        for (int i = 0; i < this.getSiteNumber(); i++) {
            if (this.genoList.get(i).isHomozygous(taxonIndex)) cnt++;
        }
        return cnt;
    }

    @Override
    public int getHeterozygoteNumberBySite(int siteIndex) {
        return this.genoList.get(siteIndex).getHeterozygoteNumber();
    }

    @Override
    public int getHeterozygoteNumberByTaxon(int taxonIndex) {
        int cnt = 0;
        for (int i = 0; i < this.getSiteNumber(); i++) {
            if (this.genoList.get(i).isHeterozygous(taxonIndex)) cnt++;
        }
        return cnt;
    }
    
    @Override
    public float getTaxonHeterozygosity(int taxonIndex) {
        return (float)((double)this.getHeterozygoteNumberByTaxon(taxonIndex)/this.getNonMissingNumberByTaxon(taxonIndex));
    }

    @Override
    public float getSiteHeterozygoteFraction(int siteIndex) {
        return (float)((double)this.getHeterozygoteNumberBySite(siteIndex)/this.getNonMissingNumberBySite(siteIndex));
    }
    
    @Override
    public byte getMinorAlleleByte(int siteIndex) {
        return genoList.get(siteIndex).getMinorAlleleByte();
    }

    @Override
    public float getMinorAlleleFrequency(int siteIndex) {
        return genoList.get(siteIndex).getMinorAlleleFrequency();
    }
    
    @Override
    public byte getMajorAlleleByte(int siteIndex) {
        return genoList.get(siteIndex).getMinorAlleleByte();
    }
    
    @Override
    public float getMajorAlleleFrequency(int siteIndex) {
        return genoList.get(siteIndex).getMajorAlleleFrequency();
    }

    @Override
    public byte getReferenceAlleleByte(int siteIndex) {
        return genoList.get(siteIndex).getReferenceAlleleByte();
    }

    @Override
    public float getReferenceAlleleFrequency(int siteIndex) {
        return genoList.get(siteIndex).getReferenceAlleleFrequency();
    }
    
    @Override
    public byte getAlternativeAlleleByte(int siteIndex) {
        return genoList.get(siteIndex).getAlternativeAlleleByte();
    }

    @Override
    public float getAlternativeAlleleFrequency(int siteIndex) {
        return genoList.get(siteIndex).getReferenceAlleleFrequency();
    }

    @Override
    public int getStartIndexOfChromosome(short chromosome) {
        int index = this.getSiteIndex(chromosome, Integer.MIN_VALUE);
        if (index < 0) {
            index = -index - 1;
            if (index < this.getSiteNumber() && this.getChromosome(index) == chromosome) return index;
            return -1;
        }
        else {
            while (index > 0 && this.getChromosome(index-1) == chromosome) {
                index--;
            }
            return index;
        }
    }

    @Override
    public int getEndIndexOfChromosome(short chromosome) {
        int index = this.getSiteIndex(chromosome, Integer.MAX_VALUE);
        if (index < 0) {
            index = -index - 2;
            if (this.getChromosome(index) == chromosome) return index+1;
            else return -1;
        }
        else {
            while ((index+1) < this.getSiteNumber() && this.getChromosome(index+1) == chromosome) {
                index++;
            }
            return index+1;
        }
    }
    
    @Override
    public GenotypeTable getSubGenotypeTableBySite(int[] siteIndices) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public GenotypeTable getSubGenotypeTableByTaxa(int[] taxaIndices) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    
    
    
    
    
    
    
    
    
    private void buildFromHDF5 (String infileS) {


    }

    private void buildFromVCF (String infileS) {
        try {
            List<String> vcfAnnotationList = new ArrayList<>();
            BufferedReader br = null;
            if (infileS.endsWith(".gz")) {
                br = IOUtils.getTextGzipReader(infileS);
            }
            else {
                br = IOUtils.getTextReader(infileS);
            }
            String temp = null;
            while ((temp = br.readLine()).startsWith("##")) {
                vcfAnnotationList.add(temp);
            }
            this.taxaList = new ArrayList<>();
            List<String> l = new ArrayList<>();
            l = PStringUtils.fastSplit(temp);
            for (int i = 9; i < l.size(); i++) {
                taxaList.add(l.get(i));
            }
            ExecutorService pool = Executors.newFixedThreadPool(PGLConstraints.parallelLevel);
            List<Future<SGBBlockVCF>> resultList = new ArrayList<>();
            int siteCount = 0;
            int startIndex = 0;
            List<String> lines = new ArrayList ();
            StringBuilder sb = new StringBuilder();
            while ((temp = br.readLine()) != null) {
                lines.add(temp);
                if (lines.size()%SGBBlockVCF.blockSize == 0) {
                    SGBBlockVCF sgb = new SGBBlockVCF(lines, startIndex);
                    Future<SGBBlockVCF> result = pool.submit(sgb);
                    resultList.add(result);
                    startIndex+=lines.size();
                    lines = new ArrayList<>();
                }
                siteCount++;
                if (siteCount%1000000 == 0) {
                    sb.setLength(0);
                    sb.append("Read in ").append(siteCount).append(" SNPs from ").append(infileS);
                    System.out.println(sb.toString());
                }
            }
            br.close();
            if (lines.size() != 0) {
                SGBBlockVCF sgb = new SGBBlockVCF(lines, startIndex);
                Future<SGBBlockVCF> result = pool.submit(sgb);
                resultList.add(result);
            }
            pool.shutdown();
            pool.awaitTermination(Long.MAX_VALUE, TimeUnit.MICROSECONDS);
            SiteGenotypeBit[] sgbArray = new SiteGenotypeBit[siteCount];
            for (int i = 0; i < resultList.size(); i++) {
                SGBBlockVCF block = resultList.get(i).get();
                for (int j = 0; j < block.actBlockSize; j++) {
                    sgbArray[block.getStartIndex()+i] = block.getSiteGenotypes()[j];
                }
            }
            genoList = Arrays.asList(sgbArray);           
            sb.setLength(0);
            sb.append("A total of ").append(this.getSiteNumber()).append(" SNPs are in ").append(infileS).append("\n");
            sb.append("Genotype table is successfully built");
            System.out.println(sb.toString());
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    
}

class SGBBlockVCF implements Callable<SGBBlockVCF> {
    public static int blockSize = 4096;
    List<String> lines = null;
    int startIndex = Integer.MIN_VALUE;
    int actBlockSize = Integer.MIN_VALUE;
    SiteGenotypeBit[] sgbArray = null;

    public SGBBlockVCF (List<String> lines, int startIndex) {
        this.lines = lines;
        this.startIndex = startIndex;
        this.actBlockSize = lines.size();
    }

    public int getStartIndex () {
        return this.startIndex;
    }

    public SiteGenotypeBit[] getSiteGenotypes () {
        return this.sgbArray;
    }

    @Override
    public SGBBlockVCF call() throws Exception {
        this.sgbArray = new SiteGenotypeBit[this.actBlockSize];
        for (int i = 0; i < this.actBlockSize; i++) {
            sgbArray[i] = SiteGenotypeBit.buildFromVCFLine(lines.get(i));
        }
        lines = null;
        return this;
    }
}
