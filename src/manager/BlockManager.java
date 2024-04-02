package manager;
import constants.Constants;
import metadata.MetadataHandler;
import utils.Tools;


import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Vector;

/**
 * BlockManager 类用于管理文件系统中的块分配和释放。
 * 它使用位图(bitmap)来跟踪块的使用情况,并提供了一系列方法来分配、释放和查询块的状态。
 *
 * 主要功能:
 * 1. 初始化位图,将所有块标记为未使用状态。
 * 2. 分配一个未使用的块,并将其标记为已使用状态。
 * 3. 释放一个已使用的块,并将其标记为未使用状态。
 * 4. 在 BlockWriter 扩容时,更新 BlockManager 的位图和块计数。
 * 5. 提供方法获取总块数、剩余空间以及将位图转换为字节数组。
 *
 * 同步:
 * BlockManager 类中的 expand, allocateBlock 和 freeBlock 方法被声明为同步方法,
 * 以确保在多线程环境下对位图和块的分配与释放操作是线程安全的。
 *
 * 构造方法:
 * 1. BlockManager(): 无参构造方法,用于初始化 BlockManager 对象。
 * 2. BlockManager(int fileSize): 带参构造方法,根据文件大小初始化 BlockManager 对象。
 *
 * 私有方法:
 * 1. initializeBitmap(): 初始化位图,将所有块标记为未使用状态。
 *
 * 同步方法:
 * 1. expand(long newFileSize): 在 BlockWriter 扩容时,更新 BlockManager 的位图和块计数。
 * 2. allocateBlock(int startIndex): 分配一个未使用的块,并将其标记为已使用状态。
 * 3. freeBlock(int blockIndex): 释放一个已使用的块,并将其标记为未使用状态。
 *
 * 公共方法:
 * 1. isBlockUsed(int blockIndex): 判断一个块是否被使用。
 * 2. setBlockUsed(int blockIndex, boolean used): 设置一个块的使用状态。
 * 3. getTotalBlocks(): 获取总块数。
 * 4. getBitmapAsBytes(): 将位图转换为字节数组。
 * 5. getRemainingSpace(RandomAccessFile file): 获取文件的剩余空间。
 * 6. printBitmap(): 打印位图信息,用于调试。
 */

public class BlockManager {
    private Vector<Boolean> bitmap; // 用于跟踪块的分配情况
    private int totalBlocks; // 当前文件的总块数
    private RandomAccessFile file;



    // 构造方法
    public BlockManager() {
        this.totalBlocks = (Constants.FILE_INNIT_SIZE - Constants.HEADER_SIZE) / Constants.BLOCK_SIZE;
        this.bitmap = new Vector<>(totalBlocks);
        initializeBitmap();
    }

    public BlockManager(RandomAccessFile file) throws IOException {
        this.file = file;
        MetadataHandler metadataHandler = new MetadataHandler(file);
        long fileSize = metadataHandler.readDatabaseSize();
        this.totalBlocks = (int) ((fileSize - Constants.HEADER_SIZE) / Constants.BLOCK_SIZE);

        Vector<Boolean> bitmapFromMetadata = metadataHandler.readBitmapFromMetadata();
        if (bitmapFromMetadata != null && bitmapFromMetadata.size() == totalBlocks) {
            // 如果元数据中存在有效的bitmap,则使用它
            this.bitmap = bitmapFromMetadata;
        } else {
            // 如果元数据中不存在有效的bitmap,则创建一个新的bitmap
            this.bitmap = new Vector<>(totalBlocks);
            initializeBitmap();
        }

    }

    private void initializeBitmap() {
        for (int i = 0; i < totalBlocks; i++) {
            bitmap.add(false);
        }
    }

    public synchronized int allocateBlock(int startIndex) throws IOException {
//        MetadataHandler metadataHandler = new MetadataHandler(file);
//        metadataHandler.readBitmapFromMetadata();


//        System.out.println("startIndex: " + startIndex);
        for (int i = startIndex; i < totalBlocks; i++) {
            if (!bitmap.get(i)) {
                bitmap.set(i, true);
                return i;
            }
        }
        return -1;
    }


    public synchronized int[] allocateContiguousBlocks(int numBlocks) throws IOException {
        int startIndex = 0;
        int contiguousBlocks = 0;
        int[] allocatedBlocks = new int[numBlocks];



        for (int i = 0; i < totalBlocks; i++) {
            if (!bitmap.get(i)) {
                if (contiguousBlocks == 0) {
                    startIndex = i;
                }
                contiguousBlocks++;
                if (contiguousBlocks == numBlocks) {
                    // 找到了足够的连续块
                    for (int j = 0; j < numBlocks; j++) {
                        bitmap.set(startIndex + j, true);
                        allocatedBlocks[j] = startIndex + j;
                    }
                    return allocatedBlocks;
                }
            } else {
                contiguousBlocks = 0;
            }
        }

        // 没有找到足够的连续块
        return null;
    }

    public synchronized void expand(RandomAccessFile file, long newFileSize) throws IOException {

        long newTotalBlocks = (newFileSize-Constants.HEADER_SIZE) / Constants.BLOCK_SIZE;
        int oldBitmapSize = bitmap.size();

        // 创建一个新的位图向量
        Vector<Boolean> newBitmap = new Vector<>((int) newTotalBlocks);

        // 将原有位图中的块状态复制到新位图中
        for (int i = 0; i < oldBitmapSize; i++) {
            newBitmap.add(bitmap.get(i));
        }

        // 将新增的块标记为未使用状态
        for (int i = oldBitmapSize; i < newTotalBlocks; i++) {
            newBitmap.add(false);
        }

        // 用新位图替换原有位图
        this.bitmap = newBitmap;
        this.totalBlocks = (int) newTotalBlocks;

        MetadataHandler metadataHandler = new MetadataHandler(file);
        byte[] updatedBitmapBytes = getBitmapAsBytes();
        metadataHandler.updateBitmapInMetadata(updatedBitmapBytes,totalBlocks);
//        Tools.printBitmap(getBitmap(file));

    }

    public synchronized void freeBlock(int blockIndex) {
        if (blockIndex >= 0 && blockIndex < totalBlocks) {
            bitmap.set(blockIndex, false);
        }
    }


    //getter
    public int getTotalBlocks() {
        return totalBlocks;
    }
    public byte[] getBitmapAsBytes() {
        int byteCount = (int) Math.ceil(totalBlocks / 8.0);
        byte[] bytes = new byte[byteCount];

        for (int i = 0; i < totalBlocks; i++) {
            if (bitmap.get(i)) {
                bytes[i / 8] |= (1 << (i % 8));
            }
        }

        return bytes;
    }

    public int getUsedBlocks() {
        int usedBlocks = 0;
        for (boolean isUsed : bitmap) {
            if (isUsed) {
                usedBlocks++;
            }
        }
        return usedBlocks;
    }

    public long getRemainingSpace(RandomAccessFile file) throws IOException {
        int usedBlocks = 0;
        for (boolean isUsed : bitmap) {
            if (isUsed) {
                usedBlocks++;
            }
        }
        long usedSpace = (long) usedBlocks * Constants.BLOCK_SIZE;
        return file.length() - usedSpace- Constants.HEADER_SIZE;
    }
    //setter
    public void setBlockUsed(int blockIndex, boolean used) {
        bitmap.set(blockIndex, used);
    }

    //tools
    public void printBlockUsage() throws IOException {
        System.out.println("Block usage:");
        for (int i = 0; i < totalBlocks; i++) {
            System.out.println("Block " + i + ": " + (isBlockUsed(i) ? "Used" : "Free"));
        }
    }
    public int findFirstFreeBlock() throws IOException {
        for (int i = 0; i < totalBlocks; i++) {
            if (!isBlockUsed(i)) {
                System.out.println("First free block: " + i);
                return i;
            }
        }
        return -1;
    }

//    public boolean isBlockUsed(int blockIndex) throws IOException {
//        MetadataHandler metadataHandler = new MetadataHandler(file);
//        Vector<Boolean> bitmap = metadataHandler.readBitmapFromMetadata();
//        return bitmap.get(blockIndex);
//    }

    public boolean isBlockUsed(int blockIndex) throws IOException {
//        MetadataHandler metadataHandler = new MetadataHandler(file);
//        Vector<Boolean> bitmap = metadataHandler.readBitmapFromMetadata();
        return bitmap.get(blockIndex);
    }


    public int getAvailableBlocks() {
        int availableBlocks = 0;
        for (boolean isUsed : bitmap) {
            if (!isUsed) {
                availableBlocks++;
            }
        }
        return availableBlocks;
    }
}
