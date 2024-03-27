package utils;

public class ApplicationContext {
    private static String csvFileName;
    private static String dbFileName;

    public static String getCsvFileName() {
        return csvFileName;
    }

    public static void setCsvFileName(String csvFileName) {
        ApplicationContext.csvFileName = csvFileName;
    }

    public static String getDbFileName() {
        return dbFileName;
    }

    public static void setDbFileName(String dbFileName) {
        ApplicationContext.dbFileName = dbFileName;
    }
}
