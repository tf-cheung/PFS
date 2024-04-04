package manager;

import constants.Constants;
import index.BTreeIndex;
import io.FileCreator;
import metadata.MetadataHandler;
import model.FileControlBlock;
import utils.ApplicationContext;
import utils.SerializationUtils;

import java.io.IOException;
import java.io.RandomAccessFile;

public class IndexManager {
    public BTreeIndex readIndexFromFile(RandomAccessFile database, String fileName) throws IOException {
        FCBManager fcbManager = new FCBManager();
        FileControlBlock fcb = fcbManager.findFCBByFileName(database,fileName);


        int startPosition = (int) fcb.getIndexStartPosition();
        int endPosition = (int) fcb.getIndexEndPosition();

        // 计算索引数据的长度
        int indexDataLength = (int) (endPosition -startPosition);
        // 读取索引数据
        byte[] indexData = new byte[indexDataLength];
        database.seek(startPosition);
        database.read(indexData);
//        System.out.println("Read index data: " + Arrays.toString(indexData));

        // 反序列化字节数组为 index.BTreeIndex 对象
        SerializationUtils serializationUtils = new SerializationUtils();

        BTreeIndex indexTree = serializationUtils.deserializeIndexTree(indexData);
//        System.out.println("Deserialized index.BTreeIndex:");
//        System.out.println(indexTree.toString());
        return indexTree;
    }


    public void writeIndexToFile(RandomAccessFile database, BlockManager blockManager, BTreeIndex indexTree) throws IOException {
        FCBManager fcbManager = new FCBManager();
        SerializationUtils serializationUtils = new SerializationUtils();

        // 将BTreeIndex转换为字节数组
        byte[] indexData = serializationUtils.serializeIndexTree(indexTree);
        int indexDataLength = indexData.length;
        int requiredBlocks = (int) Math.ceil((double) indexDataLength / Constants.BLOCK_SIZE);
        int[] allocatedBlocks = blockManager.allocateContiguousBlocks(requiredBlocks);
        int currentBlockIndex = blockManager.allocateBlock(0);

        if (allocatedBlocks == null) {
            // 计算需要扩大的单位数
            int expandUnits = (int) Math.ceil((double) indexDataLength / Constants.FILE_INNIT_SIZE);
            // 一次性扩大文件大小
            FileCreator fileCreator = new FileCreator();
            fileCreator.extendFile(database,blockManager, expandUnits * Constants.FILE_INNIT_SIZE);
            allocatedBlocks = blockManager.allocateContiguousBlocks(requiredBlocks);

        }

        int startBlockIndex = allocatedBlocks[0];
        long indexStartPosition = (long) (startBlockIndex + Constants.HEADER_BLOCKS) * Constants.BLOCK_SIZE;
        long indexEndPosition = indexStartPosition + indexDataLength;
        database.seek(indexStartPosition);
        database.write(indexData);

        String fileName = ApplicationContext.getCsvFileName();
        FileControlBlock fcb = fcbManager.findFCBByFileName(database,fileName);
        fcb.setIndexStartPosition(indexStartPosition);
        fcb.setIndexEndPosition(indexEndPosition);

        fcbManager.updateOrAddFCBInMetadata(database,fcb);
        MetadataHandler metadataHandler = new MetadataHandler(database);


        metadataHandler.updateBitmapInMetadata(blockManager.getBitmapAsBytes(),blockManager.getTotalBlocks());

    }

    public void removeIndexForFile(RandomAccessFile database, BlockManager blockManager, String fileName) throws IOException {
        FCBManager fcbManager = new FCBManager();
        FileControlBlock fcb = fcbManager.findFCBByFileName(database, fileName);


        if (fcb != null) {
            int startBlockIndex = (int) ((fcb.getIndexStartPosition() - Constants.HEADER_SIZE) / Constants.BLOCK_SIZE);
            int endBlockIndex = (int) ((fcb.getIndexEndPosition() - Constants.HEADER_SIZE) / Constants.BLOCK_SIZE);

            // 释放索引占用的块
            blockManager.releaseContiguousBlocks(startBlockIndex, endBlockIndex - startBlockIndex + 1);

            // 清空索引占用的块
            byte[] emptyData = new byte[Constants.BLOCK_SIZE];
            for (int i = startBlockIndex; i <= endBlockIndex; i++) {
                long position = (long) (i + Constants.HEADER_BLOCKS) * Constants.BLOCK_SIZE;
                database.seek(position);
                database.write(emptyData);
            }

            // 更新 FCB 中的索引位置信息
            fcb.setIndexStartPosition(0);
            fcb.setIndexEndPosition(0);
            fcbManager.updateOrAddFCBInMetadata(database, fcb);

            // 更新位图信息
            MetadataHandler metadataHandler = new MetadataHandler(database);
            metadataHandler.updateBitmapInMetadata(blockManager.getBitmapAsBytes(), blockManager.getTotalBlocks());
        }
    }
}
