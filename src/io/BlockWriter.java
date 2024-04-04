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
                // 如果当前没有可用的块,则找到下一个空闲块
                currentBlockIndex = blockManager.findFirstFreeBlock();
                currentPosition = 0;
                blockManager.setBlockUsed( currentBlockIndex, true);
                usedBlocks++;
            }
            if (currentBlockIndex == -1 || Constants.BLOCK_SIZE - currentPosition < dataBytes .length - dataOffset) {
                // 如果当前块剩余空间不足,则分配新块
                currentBlockIndex = blockManager.allocateBlock(currentBlockIndex+1);
//                System.out.println("Allocated block: " + currentBlockIndex);
                if (currentBlockIndex == -1) {
                    FileCreator fileCreator = new FileCreator();
                    fileCreator.extendFile(file,blockManager, Constants.FILE_INNIT_SIZE);
                    currentBlockIndex = blockManager.allocateBlock(currentBlockIndex + 1);
                }

                currentPosition = 0;
                usedBlocks++; // 每分配一个新块,已使用的块数加 1

            }



            int remainingDataToWrite = dataBytes .length - dataOffset;
            int bytesToWrite = Math.min(Constants.BLOCK_SIZE - currentPosition, remainingDataToWrite);

            long position = (long) (currentBlockIndex + Constants.HEADER_BLOCKS) * Constants.BLOCK_SIZE + currentPosition;
            file.seek(position);
            file.write(dataBytes , dataOffset, bytesToWrite);

            currentPosition += bytesToWrite;
            dataOffset += bytesToWrite;
//            System.out.println("Data offset: " + dataOffset);

            int dataId = Integer.parseInt(rawData.get("id"));
            indexTree.put(dataId, currentBlockIndex);
//            System.out.println("Data ID: " + dataId + ", Block index: " + currentBlockIndex);
        }
    }



    /**
     * Calculates the current file size based on the current block index and position.
     * @return the calculated file size
     */
    public long calculateFileSize() {
        return (long) currentBlockIndex * Constants.BLOCK_SIZE + currentPosition;
    }


    /**
     * Updates the file size information in the head block of the file.
     * @throws IOException if an I/O error occurs
     */
    public void updateHeadBlock() throws IOException {
        long fileSize = calculateFileSize();
        System.out.println("File size: " + fileSize);

        // 从头块的开头开始读取内容
        file.seek(0);
        byte[] blockData = new byte[Constants.BLOCK_SIZE];
        int bytesRead = file.read(blockData);

        // 将字节数组转换为字符串
        String blockContent = new String(blockData, 0, bytesRead);

        // 查找 "File size:" 字符串的位置
        int fileSizeIndex = blockContent.indexOf("File size:");

        if (fileSizeIndex != -1) {
            // 如果找到了 "File size:" 字符串,将文件指针移动到该位置
            file.seek(fileSizeIndex);

            // 覆盖写入新的文件大小信息
            file.writeBytes("File size: " + fileSize);
        } else {
            // 如果没有找到 "File size:" 字符串,则在头块的末尾添加文件大小信息
            file.seek(bytesRead);
            file.writeBytes("File size: " + fileSize + "\n");
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
        if (blockId < 0 || blockId >= blockManager.getTotalBlocks()) {
            throw new IllegalArgumentException("Invalid block ID.");
        }
        byte[] blockData = new byte[Constants.BLOCK_SIZE];
        long position = (long) blockId * Constants.BLOCK_SIZE + Constants.HEADER_SIZE;
        file.seek(position);
        int bytesRead = file.read(blockData);
        if (bytesRead != -1) {
            String serializedData = new String(blockData, 0, bytesRead);
            //序列化后的数据
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
