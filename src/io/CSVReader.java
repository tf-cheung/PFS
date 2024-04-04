package io;
import index.BTreeIndex;
import manager.BlockManager;
import manager.FCBManager;
import metadata.MetadataHandler;
import model.FileControlBlock;
import utils.Tools;

import javax.swing.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Date;
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
        this.blockManager = blockWriter.getBlockManager();
    }

    /**
     * Reads data from a CSV file and writes it to the database using the BlockWriter.
     * It also updates the FileControlBlock (FCB) and metadata.
     * @param database the RandomAccessFile representing the database
     * @param csvFileName the name of the CSV file to read from
     */
    public void readAndWriteCSV(RandomAccessFile database, String csvFileName) {
        try (BufferedReader reader = new BufferedReader(new FileReader(csvFileName))) {
            String line;
            boolean isFirstLine = true;  // To skip the header line in CSV if present.
            String fileName = csvFileName; // Use the CSV file name as the file name in the database.

            isFirstLine = true; // To skip the header line in CSV if present.

            int startBlock=blockManager.findFirstFreeBlock(); // Find the first free block to start writing the data.
            Date date = new Date();
            // The FCB contains metadata about the file such as the file name, start block, file size, etc.
            FileControlBlock fcb = new FileControlBlock(fileName, startBlock, 0, 0, 0, 0,date);
            // Update the FCB in the metadata.
            fcbManager.updateOrAddFCBInMetadata(database,fcb);
            // Reset the BTreeIndex to start from the beginning.
            BTreeIndex.resetNextId();

            // Read each line from the CSV file and write it to the database.
            while ((line = reader.readLine()) != null) {
                if (isFirstLine) {
                    isFirstLine = false;
                    continue;
                }
                Map<String, String> data = new HashMap<>();
                data.put("id", String.valueOf(lineNumber));
                data.put("data", line);
                dataSize += data.toString().getBytes().length; // Calculate the size of the data to be written.
                writer.write(blockManager,data);// Write the data to the database using the BlockWriter.
                lineNumber++; // Increment the line number.
            }

            MetadataHandler metadataHandler = new MetadataHandler(database);
            fcb.setFileSize(dataSize); // Update the file size in the FCB.
            fcb.setUsedBlocks(blockManager.getUsedBlocks()); // Update the used blocks in the FCB.
            fcbManager.updateOrAddFCBInMetadata(database,fcb); // Update the FCB in the metadata.

            dataSize = 0; // Reset the data size.
            metadataHandler.updateBitmapInMetadata(blockManager.getBitmapAsBytes(),blockManager.getTotalBlocks()); // Update the bitmap in the metadata.
            lineNumber=1; // Reset the line number.

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Closes the BlockWriter.
     * @throws IOException if an I/O error occurs
     */
    public void close() throws IOException {
        writer.close();
    }
}