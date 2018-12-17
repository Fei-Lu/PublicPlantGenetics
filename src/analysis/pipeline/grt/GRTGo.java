/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package analysis.pipeline.grt;

import java.io.File;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import utils.Benchmark;
import utils.CLIInterface;

/**
 *
 * @author feilu
 */
public class GRTGo implements CLIInterface {
    Options options = new Options();
    HelpFormatter optionFormat = new HelpFormatter();
    String introduction = null;
    String mode = null;
    String workingDirS = null;
    String barcodeFileS = null;
    String libraryFqMapFileS = null;
    String enzymeCutterF = null;
    String enzymeCutterR = null;
    String bwaPath = null;
    String referenceFileS = null;
    int numThreads = 32;
    
    LibraryInfo li = null;
    String[] subDirS = {"tagsBySample","tagsLibrary","alignment", "rawGenotype", "filteredGenotype"};
    
    public GRTGo (String[] args) {
        this.createOptions();
        introduction = this.createIntroduction();
        this.retrieveParameters (args);
    }
    
    @Override
    public void retrieveParameters (String[] args) {
        long start = System.nanoTime();
        CommandLineParser parser = new DefaultParser();
        try {
            CommandLine line = parser.parse(options, args);
            mode = line.getOptionValue("m");
            workingDirS = line.getOptionValue("w");
            barcodeFileS = line.getOptionValue("b");
            libraryFqMapFileS = line.getOptionValue("f");
            enzymeCutterF = line.getOptionValue("ef");
            enzymeCutterR = line.getOptionValue("er");
            String temp = line.getOptionValue("t");
            int numCores = Runtime.getRuntime().availableProcessors();
            if (temp != null) {
                int inputThreads = Integer.parseInt(temp);
                numThreads = inputThreads;
                if (inputThreads < 0) numThreads = numCores;
            }
            if (numThreads > numCores) numThreads = numCores;
            this.referenceFileS = line.getOptionValue("g");
            this.bwaPath = line.getOptionValue("bwa");
        }
        catch(Exception e) {
            e.printStackTrace();
            System.exit(1); 
        }
        this.creatDirectories();
        if (mode == null) {
            this.printIntroductionAndUsage();
            System.exit(1); 
            return;
        }
        else if (mode.equals("pf")) {
            if (workingDirS == null || barcodeFileS == null || libraryFqMapFileS == null || enzymeCutterF == null || enzymeCutterR == null) {
                this.printIntroductionAndUsage();
                System.exit(1); 
                return;
            }
            li = new LibraryInfo(barcodeFileS, libraryFqMapFileS, enzymeCutterF, enzymeCutterF);
            String tagBySampleDirS = new File (this.workingDirS, this.subDirS[0]).getAbsolutePath();
            TagParser tp = new TagParser(li);
            tp.setThreads(numThreads);
            tp.parseFastq(tagBySampleDirS);
            tp.compressTagsBySample(tagBySampleDirS);
            System.out.println("Parsing fastq is complemeted in " + String.format("%.4f", Benchmark.getTimeSpanHours(start)) + " hours");
        }
        else if (mode.equals("mt")) {
            if (workingDirS == null) {
                this.printIntroductionAndUsage();
                System.exit(1); 
                return;
            }
            String tagBySampleDirS = new File (this.workingDirS, this.subDirS[0]).getAbsolutePath();
            String tagLibraryDirS = new File (this.workingDirS, this.subDirS[1]).getAbsolutePath();
            String mergedTagCountFileS = new File(tagLibraryDirS, "tag.tas").getAbsolutePath();
            new TagMerger(tagBySampleDirS, mergedTagCountFileS);
            System.out.println("Merging tags is complemeted in " + String.format("%.4f", Benchmark.getTimeSpanHours(start)) + " hours");
        }
        else if (mode.equals("at")) {
            if (workingDirS == null || this.referenceFileS == null || bwaPath == null) {
                this.printIntroductionAndUsage();
                System.exit(1); 
                return;
            }
            String tagLibraryDirS = new File (this.workingDirS, this.subDirS[1]).getAbsolutePath();
            String mergedTagAnnotationFileS = new File(tagLibraryDirS, "tag.tas").getAbsolutePath();
            String alignmentDirS = new File (this.workingDirS, this.subDirS[2]).getAbsolutePath();
            TagAligner ta = new TagAligner(referenceFileS, this.bwaPath, mergedTagAnnotationFileS, alignmentDirS);
            ta.setThreads(numThreads);
            System.out.println("Aligning tags is complemeted in " + String.format("%.4f", Benchmark.getTimeSpanHours(start)) + " hours");
        }
        else {
            this.printIntroductionAndUsage();
            System.exit(1); 
            return;
        }
    }
    
    private void creatDirectories () {
        File workingDir = new File(this.workingDirS);
        workingDir.mkdir();
        for (int i = 0; i < this.subDirS.length; i++) {
            File f = new File (this.workingDirS, subDirS[i]);
            f.mkdir();
        }
    }
    
    @Override
    public void createOptions () {
        options = new Options();
        options.addOption("m", true, "Analysis mode.");
        options.addOption("w", true, "Working directory, where sub-directories are created for analysis.");
        options.addOption("b", true, "The barcode file, where sample barcoding information is stored.");
        options.addOption("f", true, "The libraryFastqMap file, where corresponding fastq files can be found for each flowcell_lane_library-index combination.");
        options.addOption("ef", true, "Recognition sequence of restriction enzyme in R1, e.g GGATCC");
        options.addOption("er", true, "Recognition sequence of restriction enzyme in R2, e.g CCGG");
        options.addOption("t", true, "Number of threads. The default value is 32. The actual number of running threads is less than the number of cores regardless of the input value, but -1 means the number of all available cores");
        options.addOption("g", true, "The reference genome of the species. The indexing files should be included in the same directory of the reference genome.");
        options.addOption("bwa", true, "The path of bwa excutable file, e.g /Users/Software/bwa-0.7.15/bwa");
    }
    
    @Override
    public String createIntroduction () {
        StringBuilder sb = new StringBuilder();
        sb.append("\nThe program GRT.jar is designed to genotype seuqecing samples made from two-enzyme GBS systems. ");
        sb.append("By using a large set of diverse samples, it builds up a database of genetic variants of a species.\n");
        sb.append("The genotype of a tested sample can be retrieved from the database.\n\n");
        sb.append("Command line example:\n\n");
        sb.append("java -Xms20g -Xmx50g -GRT.jar " +
                  "-m pf "
                + "-w /Users/user1/Lib_GBS/pipeOutput/ "
                + "-b /Users/user1/Lib_GBS/source/20180601_GBSLibrarybarcode.txt "
                + "-f /Users/user1/Lib_GBS/source/LibraryFastqMap.txt "
                + "-ef GGATCC "
                + "-er CCGG\n");
        return sb.toString();
    }
    
    public static void main (String[] args) {
        new GRTGo (args);
    }

    @Override
    public void printIntroductionAndUsage() {
        System.out.println("Incorrect parameter input. Program stops.");
        System.out.println(introduction);
        optionFormat.printHelp("GRT.jar", options);
    }
    
}
