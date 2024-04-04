package io;
import manager.BlockManager;
import constants.Constants;
import index.BTreeIndex;

import java.io.*;
import java.util.*;

import metadata.MetadataHandler;
import model.FileControlBlock;
import utils.SerializationUtils;
import utils.Tools;

public class BlockWriter {
    private RandomAccessFile file;
    private BlockManager blockManager; // BlockManager 实例
    private int currentBlockIndex = -1;; // 当前块索引，-1 表示尚未分配
    private int currentPosition = 0; // 当前块内的写入位置
    private BTreeIndex indexTree;
    private List<FileControlBlock> fcbList;
    private FileControlBlock fcb;
    private int usedBlocks = 0; // 已使用的块数
    private final SerializationUtils serializationUtils;


    public BlockWriter(RandomAccessFile file, BlockManager blockManager) throws IOException {
        this.file = file;
        this.blockManager = blockManager;
        this.indexTree = new BTreeIndex();
        this.fcbList = new ArrayList<>();
        this.serializationUtils = new SerializationUtils();
    }


    /**
     * Writes the bitmap bytes to the header of the file.
     * @throws IOException if an I/O error occurs
     */
    public void writeBitmapToHeader() throws IOException {
        byte[] bitmapBytes = blockManager.getBitmapAsBytes();
        file.seek(Constants.BITMAP_OFFSET);
        file.write(bitmapBytes);
    }

    /**
     * Writes the serialized data to the blocks. It allocates new blocks if the current block is full or not available.
     * It also updates the BTreeIndex with the data ID and block index.
     * @param blockManager the BlockManager instance
     * @param rawData the raw data to be written
     * @throws IOException if an I/O error occurs
     */
    public void write(BlockManager blockManager, Map<String, String> rawData) throws IOException {
        String serializedData = serializationUtils.serializeData(rawData);
        byte[] dataBytes  = serializedData.getBytes();
        int dataOffset = 0;
        while (dataOffset < dataBytes .length) {

            if (currentBlockIndex == -1) {
                // Find the next free block in the block manager for writing.
                currentBlockIndex = blockManager.findFirstFreeBlock();
                currentPosition = 0;    // Reset the current position within the block to the start since it's a new block.
                blockManager.setBlockUsed( currentBlockIndex, true);    // Mark the found block as used in the block manager.
                usedBlocks++;   // Increment the count of used blocks.

            }
            // If there's no block available or the current block doesn't have enough space for the remaining data...
            if (currentBlockIndex == -1 || Constants.BLOCK_SIZE - currentPosition < dataBytes .length - dataOffset) {
                // Allocate a new block, attempting to do so sequentially by using the next block index.
                currentBlockIndex = blockManager.allocateBlock(currentBlockIndex+1);
                // If allocation fails (returns -1), extend the file to create more blocks.
                if (currentBlockIndex == -1) {
                    FileCreator fileCreator = new FileCreator();
                    fileCreator.extendFile(file,blockManager, Constants.FILE_INNIT_SIZE);
                    // After extending, attempt to allocate a block again.
                    currentBlockIndex = blockManager.allocateBlock(currentBlockIndex + 1);
                }
                currentPosition = 0;    // Reset the position within the new block to start at the beginning.
                usedBlocks++;   // Increment the used blocks counter since a new block is allocated.
            }

            // Calculate the remaining amount of data that needs to be written.
            int remainingDataToWrite = dataBytes .length - dataOffset;
            // Determine the amount of data to write in this iteration, limited by block size or remaining data size.
            int bytesToWrite = Math.min(Constants.BLOCK_SIZE - currentPosition, remainingDataToWrite);
            // Calculate the file position to start writing, accounting for header blocks and current block position.
            long position = (long) (currentBlockIndex + Constants.HEADER_BLOCKS) * Constants.BLOCK_SIZE + currentPosition;
            // Seek to the calculated position in the file.
            file.seek(position);
            // Write the determined bytes from the data array to the file at the current position.
            file.write(dataBytes , dataOffset, bytesToWrite);
            // Update the current position within the block after writing.
            currentPosition += bytesToWrite;
            // Update the data offset to reflect the amount of data written.
            dataOffset += bytesToWrite;
            // Extract the data ID from the raw data map and parse it to an integer.
            int dataId = Integer.parseInt(rawData.get("id"));
            // Update the index tree with the data ID and the block index where it's stored.
            indexTree.put(dataId, currentBlockIndex);
        }
    }

    /**
     * Reads the data from the specified block and returns the deserialized data for the given movie ID.
     * @param blockId the block ID
     * @param movieId the movie ID
     * @return the deserialized data for the given movie ID
     * @throws IOException if an I/O error occurs
     */
    public String readData(int blockId, int movieId) throws IOException {
        // Check if the provided blockId is out of the file system's range.
        if (blockId < 0 || blockId >= blockManager.getTotalBlocks()) {
            // If the blockId is invalid, throw an exception.
            System.out.println("ID out of range");
        }
        byte[] blockData = new byte[Constants.BLOCK_SIZE];
        // Calculate the position in the file from which to start reading, considering the block ID and header size.
        long position = (long) blockId * Constants.BLOCK_SIZE + Constants.HEADER_SIZE;
        file.seek(position);
        // Read the block data into the byte array and capture the number of bytes read.
        int bytesRead = file.read(blockData);
        if (bytesRead != -1) {
            String serializedData = new String(blockData, 0, bytesRead);
            // Deserialize the string back into a map to retrieve structured data.
            Map<String, String> movieData = serializationUtils.deserializeData(serializedData);
            return movieData.get(String.valueOf(movieId));
        }

        return null;
    }

    public void close() throws IOException {
        if (file != null) {
            file.close();
        }
    }


    //getters
    public int getUsedBlocks() {
        return usedBlocks;
    }
    public int getCurrentBlockIndex() {
        return currentBlockIndex;
    }
    public FileControlBlock getFCB() {
        return fcb;
    }
    public List<FileControlBlock> getFCBList() {
        return fcbList;
    }
    public BTreeIndex getIndexTree() {
        return indexTree;
    }
    public BlockManager getBlockManager() {
        return blockManager;
    }


    //setters
    public void setIndexTree(BTreeIndex indexTree) {
        this.indexTree = indexTree;
    }
    public void setFCBList(List<FileControlBlock> fcbList) {
        this.fcbList = fcbList;
    }
    public void setFCB(FileControlBlock fcb) {
        this.fcb = fcb;
    }
    public void setCurrentBlockIndex(int currentBlockIndex) {
        this.currentBlockIndex = currentBlockIndex;
    }
    public void setUsedBlocks(int usedBlocks) {
        this.usedBlocks = usedBlocks;
    }
    public void setFCBFilesize(int fileSize) {
        fcb.setFileSize(fileSize);
    }

    public void setFCBUsedBlocks() {
        fcb.setUsedBlocks(usedBlocks);
        System.out.println("Used blocks: " + usedBlocks);
    }

    public void setFCBStartBlock() {
        if(currentBlockIndex != -1){
            fcb.setStartBlock(currentBlockIndex);

        }else{
            fcb.setStartBlock(0);
        }
    }

    public void setBlockManager(BlockManager blockManager) {
        this.blockManager = blockManager;
    }

    public void clearBlocks(int startBlock, int numBlocks) throws IOException {
        byte[] emptyData = new byte[Constants.BLOCK_SIZE];
        for (int i = startBlock; i < startBlock + numBlocks; i++) {
            long position = (long) (i + Constants.HEADER_BLOCKS) * Constants.BLOCK_SIZE;
            file.seek(position);
            file.write(emptyData);
        }
    }
}
