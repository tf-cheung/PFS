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

    public void writeBitmapToHeader() throws IOException {
        byte[] bitmapBytes = blockManager.getBitmapAsBytes();
        file.seek(Constants.BITMAP_OFFSET);
        file.write(bitmapBytes);
    }

    public void write(BlockManager blockManager, Map<String, String> rawData) throws IOException {
        String serializedData = serializationUtils.serializeData(rawData);
        byte[] dataBytes  = serializedData.getBytes();
        int dataOffset = 0;
        while (dataOffset < dataBytes .length) {

            if (currentBlockIndex == -1) {
                // 如果当前没有可用的块,则找到下一个空闲块
                currentBlockIndex = blockManager.findFirstFreeBlock();
                System.out.println("没有空闲块。。。");
                System.out.println("currentBlockIndex: " + currentBlockIndex);
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
                    fileCreator.extendFile(file, Constants.FILE_INNIT_SIZE);
                    currentBlockIndex = blockManager.allocateBlock(currentBlockIndex + 1);
                    System.out.println("Allocated block: " + currentBlockIndex);
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


    public long calculateFileSize() {
        return (long) currentBlockIndex * Constants.BLOCK_SIZE + currentPosition;
    }
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
    public String readData(int blockId, int movieId) throws IOException {
        if (blockId < 0 || blockId >= blockManager.getTotalBlocks()) {
            throw new IllegalArgumentException("Invalid block ID.");
        }
        byte[] blockData = new byte[Constants.BLOCK_SIZE];
        long position = (long) blockId * Constants.BLOCK_SIZE + Constants.HEADER_SIZE;
        file.seek(position);
        int bytesRead = file.read(blockData);
//        System.out.println("Block data: " + Arrays.toString(blockData));

        if (bytesRead != -1) {
            String serializedData = new String(blockData, 0, bytesRead);
            //序列化后的数据
//            System.out.println("Serialized data: " + serializedData);
            Map<String, String> movieData = serializationUtils.deserializeData(serializedData);
//            System.out.println("====================================");
//            System.out.println(movieData.get("1"));
//            for (String id : movieData.keySet()) {
//                System.out.println("ID: " + id + ", Data: " + movieData.get(id));
//            }
//            if (movieData.get(String.valueOf(movieId)).getMovieId().equals(String.valueOf(movieId))) {
//                return movieData.get(String.valueOf(movieId));
//            }


            return movieData.get(String.valueOf(movieId));
        }

        return null;
    }

    public void expandFileSize() throws IOException {
        long currentSize = file.length();
        file.setLength(currentSize+1024*1024);
        updateFileSizeInMetadata(currentSize + 1024 * 1024);
    }

    private void updateFileSizeInMetadata(long newSize) throws IOException {
        // 将文件指针移动到元数据中总大小字段的偏移量
        file.seek(Constants.DATABASE_SIZE_OFFSET);
        // 将更新后的文件大小写入元数据
        file.writeLong(newSize);
    }

    //reset BtreeIndex
    public void resetIndexTree() {
        indexTree = new BTreeIndex();
    }
    public void addFCB(FileControlBlock fcb) {
        this.fcb = fcb;
        fcbList.add(fcb);
        //print List<FileControlBlock> fcbList;
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


    //test
    public void printBlockContent(int blockIndex) throws IOException {
        if (blockIndex < 0 || blockIndex >= blockManager.getTotalBlocks()) {
            System.out.println("Invalid block index.");
            return;
        }

        byte[] blockData = new byte[Constants.BLOCK_SIZE];
        long position = (long) blockIndex * Constants.BLOCK_SIZE;
        file.seek(position);
        int bytesRead = file.read(blockData);

        if (bytesRead != -1) {
            String blockContent = new String(blockData, 0, bytesRead);
            System.out.println("Block " + blockIndex + " content:");
            System.out.println(blockContent);
        } else {
            System.out.println("Block " + blockIndex + " is empty.");
        }
    }

    //打印整个Block的数据
    public byte[] extractDataFromBlocks(int[] blockIndices) throws IOException {
        ByteArrayOutputStream dataStream = new ByteArrayOutputStream();

        for (int blockIndex : blockIndices) {
            if (blockIndex < 0 || blockIndex >= blockManager.getTotalBlocks()) {
                throw new IllegalArgumentException("Invalid block index: " + blockIndex);
            }

            byte[] blockData = new byte[Constants.BLOCK_SIZE];
            long position = (long) blockIndex * Constants.BLOCK_SIZE;
            file.seek(position);
            int bytesRead = file.read(blockData);

            if (bytesRead != -1) {
                dataStream.write(blockData, 0, bytesRead);
            }
        }

        return dataStream.toByteArray();
    }

    //给定文件名和所需大小，扩展文件大小
    public static void extendFile(String fileName, long requiredSize) throws IOException {
        File file = new File(fileName);

        // 使用RandomAccessFile访问文件
        try (RandomAccessFile raf = new RandomAccessFile(file, "rw")) {
            long currentSize = raf.length();
            // 如果剩余空间不足，则扩充文件大小
            while (currentSize - raf.getFilePointer() < requiredSize) {
                currentSize += Constants.FILE_INNIT_SIZE;
                raf.setLength(currentSize);
            }
        }
    }


}
