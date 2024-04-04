package utils;


import java.io.*;
import java.util.HashMap;
import java.util.Map;

import index.BTreeIndex;


public class SerializationUtils {
    /**
     * Serializes the index tree to a byte array.
     * @param indexTree the index tree to serialize
     * @return the serialized index tree as a byte array
     * @throws IOException if an I/O error occurs
     */
    public byte[] serializeIndexTree(BTreeIndex indexTree) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bos);

        // 写入 index.BTreeIndex 的元数据
        dos.writeInt(indexTree.size()); // 写入索引树中的键值对数量
        dos.writeInt(indexTree.height()); // 写入索引树的高度

        // 递归序列化索引树的节点
        serializeNode(dos, indexTree.root, indexTree.height());

        dos.flush();
//        System.out.println("Serialized index tree: " + Arrays.toString(bos.toByteArray()));
        return bos.toByteArray();
    }

    /**
     * Serializes a node of the index tree to the data output stream.
     * @param dos the data output stream
     * @param node the node to serialize
     * @param height the height of the node
     * @throws IOException if an I/O error occurs
     */
    public void serializeNode(DataOutputStream dos, BTreeIndex.Node node, int height) throws IOException {
        // 写入节点的元数据
        dos.writeInt(node.id); // 写入节点的唯一标识符
//        System.out.println("Node id: " + node.id);
        dos.writeInt(node.m); // 写入节点中的键值对数量
//        System.out.println("Node m: " + node.m);

        // 写入节点中的键值对
        for (int i = 0; i < node.m; i++) {
            BTreeIndex.Entry entry = node.children[i];
            dos.writeInt(entry.key); // 写入键
//            System.out.println("Entry key: " + entry.key);
            dos.writeInt(entry.val); // 写入值
//            System.out.println( "Entry val: " + entry.val);
            if (height > 0) {
                // 如果不是叶子节点,递归序列化子节点
                serializeNode(dos, entry.next, height - 1);
            }
        }
    }

    /**
     * Deserializes the index tree from a byte array.
     * @param data the serialized index tree data
     * @return the deserialized index tree
     * @throws IOException if an I/O error occurs
     */
    public BTreeIndex deserializeIndexTree(byte[] data) throws IOException {
        ByteArrayInputStream bis = new ByteArrayInputStream(data);
        DataInputStream dis = new DataInputStream(bis);

        // 读取 index.BTreeIndex 的元数据
        int size = dis.readInt(); // 读取索引树中的键值对数量
        int height = dis.readInt(); // 读取索引树的高度

        BTreeIndex indexTree = new BTreeIndex();
        indexTree.root = deserializeNode(dis, height);
        indexTree.height = height;
        indexTree.n = size;

        return indexTree;
    }

    /**
     * Deserializes a node of the index tree from the data input stream.
     * @param dis the data input stream
     * @param height the height of the node
     * @return the deserialized node
     * @throws IOException if an I/O error occurs
     */
    public BTreeIndex.Node deserializeNode(DataInputStream dis, int height) throws IOException {
        // Read the metadata of the node
        int id = dis.readInt();
        int m = dis.readInt();

        BTreeIndex.Node node = new BTreeIndex.Node(m);
        node.id = id;

        // Read the key-value pairs of the node
        for (int i = 0; i < m; i++) {
            int key = dis.readInt(); // read the key
            int val = dis.readInt(); // read the value

            BTreeIndex.Entry entry;
            if (height > 0) {
                // If it's not a leaf node, recursively deserialize the child node
                BTreeIndex.Node child = deserializeNode(dis, height - 1);
                entry = new BTreeIndex.Entry(key, val, child);
            } else {
                entry = new BTreeIndex.Entry(key, val, null);
            }

            node.children[i] = entry;
        }

        return node;
    }


    /**
     * Serializes the movie data to a string representation.
     * @param movieData the movie data map
     * @return the serialized movie data as a string
     */
    public String serializeData(Map<String, String> movieData) {
        StringBuilder sb = new StringBuilder();
        sb.append("id:").append(movieData.get("id")).append(";");
        sb.append("data:").append(movieData.get("data")).append(";");
        return sb.toString();
    }

    /**
     * Deserializes the movie data from a string representation.
     * @param serializedData the serialized movie data string
     * @return the deserialized movie data as a map
     */
    public Map<String, String> deserializeData(String serializedData) {
        Map<String, String> movieDataMap = new HashMap<>();

        String[] pairs = serializedData.split(";id:");

        for (String pair : pairs) {
            if (!pair.trim().isEmpty()) {
                // Split the pair into "id" and "data"
                String[] idDataPair = pair.split(";", 2);

                if (idDataPair.length == 2) {
                    // Extract the "id" and "data" values
                    String id = idDataPair[0].replace("id:", "").trim();
                    String movieData = idDataPair[1].replace("data:", "").trim();

                    // Add the values to the movie data map
                    movieDataMap.put(id, movieData);
                }
            }
        }

        return movieDataMap;
    }

}
