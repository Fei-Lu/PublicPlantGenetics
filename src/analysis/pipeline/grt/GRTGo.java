/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package analysis.pipeline.grt;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
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
    
    public GRTGo (String[] args) {
        this.createOptions();
        introduction = this.createIntroduction();
        this.retrieveParameters (args);
    }
    
    @Override
    public void retrieveParameters (String[] args) {
        CommandLineParser parser = new DefaultParser();
        try {
            CommandLine line = parser.parse(options, args);
            mode = line.getOptionValue("m");
            workingDirS = line.getOptionValue("w");
            barcodeFileS = line.getOptionValue("b");
            libraryFqMapFileS = line.getOptionValue("f");
            enzymeCutterF = line.getOptionValue("ef");
            enzymeCutterR = line.getOptionValue("er");
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        if (mode == null) {
            this.printIntroductionAndUsage();
            return;
        }
        else if (mode.equals("pf")) {
            if (workingDirS == null || barcodeFileS == null || libraryFqMapFileS == null || enzymeCutterF == null || enzymeCutterR == null) {
                this.printIntroductionAndUsage();
               
                return;
            }
            
        }
        else {
            this.printIntroductionAndUsage();
            return;
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
    }
    
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
        System.out.println("Incorrect parameter input. Program quits.");
        System.out.println(introduction);
        optionFormat.printHelp("GRT.jar", options);
    }
    
}
