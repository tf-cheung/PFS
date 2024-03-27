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
        System.out.println("Index data length: " + indexDataLength);
        int requiredBlocks = (int) Math.ceil((double) indexDataLength / Constants.BLOCK_SIZE);
        System.out.println("Required blocks: " + requiredBlocks);
        int[] allocatedBlocks = blockManager.allocateContiguousBlocks(requiredBlocks);
        System.out.println(blockManager.getAvailableBlocks());
        int currentBlockIndex = blockManager.allocateBlock(0);

        if (allocatedBlocks == null) {
            System.out.println("No contiguous blocks available");
            // 计算需要扩大的单位数
            int expandUnits = (int) Math.ceil((double) indexDataLength / Constants.FILE_INNIT_SIZE);
            System.out.println("Expand units: " + expandUnits);
            // 一次性扩大文件大小
            FileCreator fileCreator = new FileCreator();
            fileCreator.extendFile(database, expandUnits * Constants.FILE_INNIT_SIZE);
            allocatedBlocks = blockManager.allocateContiguousBlocks(requiredBlocks);

        }

        int startBlockIndex = allocatedBlocks[0];
        long indexStartPosition = (long) (startBlockIndex + Constants.HEADER_BLOCKS) * Constants.BLOCK_SIZE;
        System.out.println("Index start position: " + indexStartPosition);
        long indexEndPosition = indexStartPosition + indexDataLength;
        System.out.println("Index end position: " + indexEndPosition);

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
}
