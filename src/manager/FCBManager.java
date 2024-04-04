package manager;

import constants.Constants;
import model.FileControlBlock;

import java.io.*;
import java.util.ArrayList;
import java.util.List;


public class FCBManager {
    private List<FileControlBlock> fcbList;
    private  FileControlBlock fcb;

    public FCBManager() {
        this.fcbList = new ArrayList<>();
    }

    public FCBManager( FileControlBlock fcb ) {
        this.fcb = fcb;
        this.fcbList = new ArrayList<>();
    }

    public FCBManager(List<FileControlBlock> fcbList) {
        this.fcbList = fcbList;
    }

    private void addFCB(FileControlBlock fcb) {
        fcbList.add(fcb);
    }


    //把一个新的FCB写到METADATA中的FCBList中
    private void writeFCBListToMetadata(RandomAccessFile file, List<FileControlBlock> fcbList) throws IOException {

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        DataOutputStream dataOutputStream = new DataOutputStream(outputStream);

        // 写入FCB列表的长度
        dataOutputStream.writeInt(fcbList.size());

        // 写入每个FCB对象的字节数组
        for (FileControlBlock fcb : fcbList) {
            byte[] fcbBytes = fcb.toBytes();
            dataOutputStream.writeInt(fcbBytes.length);
            dataOutputStream.write(fcbBytes);
        }
        // 将字节数组写入到文件的元数据区域
        byte[] fcbListBytes = outputStream.toByteArray();
        file.seek(Constants.FCB_LIST_OFFSET);
        file.write(fcbListBytes);

    }

    public void updateOrAddFCBInMetadata(RandomAccessFile file, FileControlBlock fcb) throws IOException {
        List<FileControlBlock> fcbList = readFCBListFromMetadata(file);

        boolean fcbExists = false;
        for (int i = 0; i < fcbList.size(); i++) {
            FileControlBlock existingFCB = fcbList.get(i);
            if (existingFCB.getFileName().equals(fcb.getFileName())) {
                fcbList.set(i, fcb);
                fcbExists = true;
                break;
            }
        }

        if (!fcbExists) {
            fcbList.add(fcb);
        }

        writeFCBListToMetadata(file, fcbList);
    }

    public List<FileControlBlock> readFCBListFromMetadata(RandomAccessFile file) throws IOException {
        List<FileControlBlock> fcbList = new ArrayList<>();

        // 移动文件指针到FCB列表的起始位置
        file.seek(Constants.FCB_LIST_OFFSET);

        // 读取FCB列表的字节数组
        byte[] fcbListBytes = new byte[Constants.FCB_LIST_SIZE];
        file.read(fcbListBytes);

        // 反序列化字节数组为FCB对象列表
        ByteArrayInputStream inputStream = new ByteArrayInputStream(fcbListBytes);
        DataInputStream dataInputStream = new DataInputStream(inputStream);

        // 读取FCB列表的长度
        int fcbListSize = dataInputStream.readInt();

        // 读取每个FCB对象的字节数组并还原为FCB对象
        for (int i = 0; i < fcbListSize; i++) {
            int fcbBytesLength = dataInputStream.readInt();
            byte[] fcbBytes = new byte[fcbBytesLength];
            dataInputStream.read(fcbBytes);
            FileControlBlock fcb = FileControlBlock.fromBytes(fcbBytes);
            fcbList.add(fcb);
        }
        return fcbList;
    }


    public FileControlBlock findFCBByFileName(RandomAccessFile file, String fileName) throws IOException {
        fcbList = readFCBListFromMetadata(file);

        for (FileControlBlock fcb : fcbList) {
//            System.out.println("fcb.getFileName() = " + fcb.getFileName() + " fileName = " + fileName);
            if (fcb.getFileName().equals(fileName)) {
//                System.out.println("Found FCB: " + fileName);
                return fcb;
            }
        }
        return null;
    }
    public void removeFCBFromMetadata(RandomAccessFile file, FileControlBlock fcbToRemove) throws IOException {
        List<FileControlBlock> fcbList = readFCBListFromMetadata(file);
        for (FileControlBlock fcb : fcbList) {
            if (fcb.getFileName().equals(fcbToRemove.getFileName())) {
                System.out.println("Removing FCB: " + fcb.getFileName());
                fcbList.remove(fcb);
                break;
            }
        }
        writeFCBListToMetadata(file, fcbList);
    }

}
