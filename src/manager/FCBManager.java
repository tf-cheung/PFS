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



    /**
     * Writes the FCBList to the metadata of the file.
     * @param file the RandomAccessFile representing the file
     * @param fcbList the list of FileControlBlocks to write
     * @throws IOException if an I/O error occurs
     */
    private void writeFCBListToMetadata(RandomAccessFile file, List<FileControlBlock> fcbList) throws IOException {

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        DataOutputStream dataOutputStream = new DataOutputStream(outputStream);
        // Write the length of the FCB list
        dataOutputStream.writeInt(fcbList.size());

        // Serialize each FCB object to a byte array and write it to the data output stream
        for (FileControlBlock fcb : fcbList) {
            byte[] fcbBytes = fcb.toBytes();
            dataOutputStream.writeInt(fcbBytes.length);
            dataOutputStream.write(fcbBytes);
        }
        // Write the FCB list bytes to the file
        byte[] fcbListBytes = outputStream.toByteArray();
        file.seek(Constants.FCB_LIST_OFFSET);
        file.write(fcbListBytes);

    }

    /**
     * Updates or adds an FCB in the metadata of the file.
     * @param file the RandomAccessFile representing the file
     * @param fcb the FileControlBlock to update or add
     * @throws IOException if an I/O error occurs
     */
    public void updateOrAddFCBInMetadata(RandomAccessFile file, FileControlBlock fcb) throws IOException {
        List<FileControlBlock> fcbList = readFCBListFromMetadata(file);
        // Check if the FCB already exists in the list
        boolean fcbExists = false;
        for (int i = 0; i < fcbList.size(); i++) {
            FileControlBlock existingFCB = fcbList.get(i);
            // If the FCB with the same file name already exists, update it
            if (existingFCB.getFileName().equals(fcb.getFileName())) {
                fcbList.set(i, fcb);
                fcbExists = true;
                break;
            }
        }
        if (!fcbExists) {
            fcbList.add(fcb);
        }
        // Write the updated FCB list to the metadata
        writeFCBListToMetadata(file, fcbList);
    }

    /**
     * Reads the FCBList from the metadata of the file.
     * @param file the RandomAccessFile representing the file
     * @return the list of FileControlBlocks read from the metadata
     * @throws IOException if an I/O error occurs
     */
    public List<FileControlBlock> readFCBListFromMetadata(RandomAccessFile file) throws IOException {
        List<FileControlBlock> fcbList = new ArrayList<>();

        // Seek to the FCB list offset in the file
        file.seek(Constants.FCB_LIST_OFFSET);

        // Read the FCB list bytes from the file
        byte[] fcbListBytes = new byte[Constants.FCB_LIST_SIZE];
        file.read(fcbListBytes);

        // Create an input stream to read the FCB list bytes
        ByteArrayInputStream inputStream = new ByteArrayInputStream(fcbListBytes);
        DataInputStream dataInputStream = new DataInputStream(inputStream);

        // Read the number of FCB objects in the list
        int fcbListSize = dataInputStream.readInt();

        // Read each FCB object from the data input stream
        for (int i = 0; i < fcbListSize; i++) {
            int fcbBytesLength = dataInputStream.readInt();
            byte[] fcbBytes = new byte[fcbBytesLength];
            dataInputStream.read(fcbBytes);
            FileControlBlock fcb = FileControlBlock.fromBytes(fcbBytes);
            fcbList.add(fcb);
        }
        return fcbList;
    }

    /**
     * Finds and returns the FCB with the specified file name.
     * @param file the RandomAccessFile representing the file
     * @param fileName the name of the file to find the FCB for
     * @return the FileControlBlock with the specified file name, or null if not found
     * @throws IOException if an I/O error occurs
     */
    public FileControlBlock findFCBByFileName(RandomAccessFile file, String fileName) throws IOException {
        fcbList = readFCBListFromMetadata(file);
        // Iterate through the FCB list to find the FCB with the specified file name
        for (FileControlBlock fcb : fcbList) {
            if (fcb.getFileName().equals(fileName)) {
                return fcb;
            }
        }
        return null;
    }

    /**
     * Removes the specified FCB from the metadata of the file.
     * @param file the RandomAccessFile representing the file
     * @param fcbToRemove the FileControlBlock to remove
     * @throws IOException if an I/O error occurs
     */
    public void removeFCBFromMetadata(RandomAccessFile file, FileControlBlock fcbToRemove) throws IOException {
        List<FileControlBlock> fcbList = readFCBListFromMetadata(file);
        // Iterate through the FCB list to find the FCB to remove
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
