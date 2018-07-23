/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package analysis.pipeline.libgbs;

import format.dna.BaseEncoder;
import java.io.DataInputStream;
import java.io.File;
import utils.IOUtils;

/**
 *
 * @author feilu
 */
public class TagCount {
    int chunkSize = 3;
    int tagNum = -1;
    long[][] tags = null;
    byte[] r1Len = null;
    byte[] r2Len = null;
    int[] tagCount = null;
    
    public TagCount (String infileS) {
        this.readBinaryFile(infileS);
    }
    
    private void readBinaryFile (String infileS) {
        try {
            DataInputStream dis = IOUtils.getBinaryReader(infileS);
            this.chunkSize = dis.readInt();
            tagNum = dis.readInt();
            if (tagNum == -1) tagNum = (int)((new File(infileS).length()-8)/(chunkSize*BaseEncoder.longChunkSize+2+4));
            tags = new long[tagNum][2*this.chunkSize];
            r1Len = new byte[tagNum];
            r2Len = new byte[tagNum];
            tagCount = new int[tagNum];
            for (int i = 0; i < tagNum; i++) {
                for (int j = 0; j < tags[0].length; j++) {
                    tags[i][j] = dis.readLong();
                }
                r1Len[i] = dis.readByte();
                r2Len[i] = dis.readByte();
                tagCount[i] = dis.readInt();
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
