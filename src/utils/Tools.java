package utils;

import java.util.Vector;

public class Tools {

    public static void printBitmap(Vector<Boolean> bitmap) {
        for (int i = 0; i < bitmap.size(); i++) {
            System.out.println("Block " + i + ": " + (bitmap.get(i) ? "Used" : "Free"));
        }
    }
    public static byte[] getBitmapAsBytes(Vector<Boolean> bitmap) {
        int totalBlocks = bitmap.size();
        int byteCount = (int) Math.ceil(totalBlocks / 8.0);
        byte[] bytes = new byte[byteCount];

        for (int i = 0; i < totalBlocks; i++) {
            if (bitmap.get(i)) {
                bytes[i / 8] |= (1 << (i % 8));
            }
        }

        return bytes;
    }
}
