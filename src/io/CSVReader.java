package io;
import index.BTreeIndex;
import manager.BlockManager;
import manager.FCBManager;
import metadata.MetadataHandler;
import model.FileControlBlock;

import javax.swing.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.HashMap;
import java.util.Map;
public class CSVReader {
    private BlockWriter writer;
    private FCBManager fcbManager;
    private BlockManager blockManager;
    private int dataSize=0;
    private int lineNumber = 1;
    public CSVReader(BlockWriter blockWriter) throws IOException {
        this.writer = blockWriter;
        this.fcbManager = new FCBManager();
        this.blockManager = new BlockManager();
    }

    public void readAndWriteCSV(RandomAccessFile database, String csvFileName) {
        try (BufferedReader reader = new BufferedReader(new FileReader(csvFileName))) {
            String line;
            boolean isFirstLine = true;
            String fileName = csvFileName; // 假设文件名与CSV文件名相同

            // 重置读取器到文件开头
            isFirstLine = true;
            FileControlBlock fcb = new FileControlBlock(fileName, 0, 0, 0, 0, 0);
            fcbManager.updateOrAddFCBInMetadata(database,fcb);
            BTreeIndex.resetNextId();

            while ((line = reader.readLine()) != null) {
                if (isFirstLine) {
                    isFirstLine = false;
                    continue;
                }
                Map<String, String> movieData = new HashMap<>();
                movieData.put("id", String.valueOf(lineNumber));
                movieData.put("data", line);

                dataSize += movieData.toString().getBytes().length;
//                System.out.println("Data size for line " + lineNumber + ": " + dataSize);

                writer.write(movieData);
                lineNumber++;
//                System.out.println( "lineNumber: " + lineNumber);
            }
//            writer.setFCBFilesize(dataSize);
//            writer.setFCBUsedBlocks();
            dataSize = 0;
//            writer.writeBitmapToHeader();
            MetadataHandler metadataHandler = new MetadataHandler(database);
            metadataHandler.updateBitmapInMetadata(blockManager.getBitmapAsBytes(),blockManager.getTotalBlocks());
            lineNumber=1;

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void close() throws IOException {
        writer.close();
    }
}