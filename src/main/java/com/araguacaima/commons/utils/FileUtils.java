/*
 * Copyright 2017 araguacaima
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.araguacaima.commons.utils;

import com.araguacaima.commons.utils.file.FileUtilsFilenameFilter;
import com.araguacaima.commons.utils.file.FileUtilsFilenameFilterImpl;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.IterableUtils;
import org.apache.commons.collections4.Predicate;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.net.ftp.FTPClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Simple helper for basic file operations.
 */
@SuppressWarnings("ResultOfMethodCallIgnored")

public class FileUtils extends org.apache.commons.io.FileUtils {

    public static final int DEFAULT_FILTER_TYPE;
    public static final int DEFAULT_SEARCH_TYPE;
    public static final String EXTENSION_GSG = "gsg"; // Test
    public static final String EXTENSION_TXT = "txt";
    public static final String EXTENSION_XML = "xml";
    public static final int FILTER_INCLUDE_BOTH_HIDDEN_OR_NOT = -1;
    public static final int FILTER_TYPE_ALL = 0;
    public static final int FILTER_TYPE_NONE = 2;
    public static final int FILTER_TYPE_SOME = 1;
    public static final int FILTER_TYPE_UNKNOWN = -1;
    public static final int RECURSION_LEVEL_ALL = -1;
    public static final int RECURSION_LIMIT = 50;
    public static final int SEARCH_EXCLUDE_TYPE_ALL = -1;
    public static final int SEARCH_EXCLUDE_TYPE_COMPRESSED_FILE = 2;
    public static final int SEARCH_INCLUDE_ONLY_PATH = 3;
    public static final int SEARCH_INCLUDE_TYPE_ALL = 0;
    public static final int SEARCH_INCLUDE_TYPE_COMPRESSED_FILE = 1;
    private static final FileUtilsFilenameFilterCompare FILE_UTIL_FILENAME_FILTER_COMPARE = new
            FileUtilsFilenameFilterCompare();
    private static final FileUtilsFilenameFilterCompareIgnoreCase FILE_UTIL_FILENAME_FILTER_COMPARE_IGNORE_CASE = new
            FileUtilsFilenameFilterCompareIgnoreCase();
    private static final FileUtilsFilenameFilterImpl hiddenFilter = new FileUtilsFilenameFilterImpl() {
        public boolean accept(File dir, String name) {
            return (new File(dir.getPath() + File.separator + name)).isHidden();
        }
    };
    private static final FileUtilsFilenameFilterImpl notHiddenFilter = new FileUtilsFilenameFilterImpl() {
        public boolean accept(File dir, String name) {
            return !(new File(dir.getPath() + File.separator + name)).isHidden();
        }
    };
    private final static Logger log = LoggerFactory.getLogger(FileUtils.class);
    public static int DEFAULT_RECURSION_LEVEL;

    static {
        DEFAULT_FILTER_TYPE = FILTER_TYPE_ALL;
        DEFAULT_RECURSION_LEVEL = RECURSION_LEVEL_ALL;
        DEFAULT_SEARCH_TYPE = SEARCH_INCLUDE_TYPE_ALL;
    }

    public final String DEFAULT_PATH = "/";
    private DateUtils dateUtils = DateUtils.getInstance();
    private String filterCriterion;
    private NotNullsLinkedHashSet<FileUtilsFilenameFilter> filters = new NotNullsLinkedHashSet<>();
    private JarUtils jarUtils = JarUtils.getInstance();
    private NumberUtils numberUtils = NumberUtils.getInstance();
    private int recursionLevel;
    private int searchType;
    private StringUtils stringUtils = StringUtils.getInstance();

    public FileUtils() {
        setSearchType(DEFAULT_SEARCH_TYPE);
    }

    public FileUtils(int recursionLevel) {
        setSearchType(DEFAULT_SEARCH_TYPE);
        setRecursionLevel(recursionLevel);
        DEFAULT_RECURSION_LEVEL = recursionLevel;
    }

    /**
     * Bind the information stored in a file to a collection of objets that represents it. Each line in the file
     * corresponds with an object
     *
     * @param sourcefile     File name for searching about objects to bind
     * @param orderedFields  Fields of the object that will be associated to the file read
     * @param fieldSeparator Separator for identifying each field
     * @param classToBind    Class to bind objects
     * @return A collection of objects resulting from binding source file with incoming class
     * @throws Exception If it's not possible to read the file or if internal information could not be ssociated to
     *                   the desired class
     */

    public static Collection<Object> bindRecordsFromFileToObject(String sourcefile,
                                                                 Collection<String> orderedFields,
                                                                 char fieldSeparator,
                                                                 Class classToBind)
            throws Exception {
        return bindRecordsFromFileToObject(new File(sourcefile), orderedFields, fieldSeparator, classToBind);
    }

    /**
     * @param file           File for searching about objects to bind
     * @param orderedFields  Fields of the object that will be associated to the file read
     * @param fieldSeparator Separator for identifying each field
     * @param classToBind    Class to bind objects
     * @return A collection of objects resulting from binding source file with incoming class
     * @throws Exception If it's not possible to read the file or if internal information could not be ssociated to
     *                   the desired class
     */

    public static Collection<Object> bindRecordsFromFileToObject(File file,
                                                                 Collection<String> orderedFields,
                                                                 char fieldSeparator,
                                                                 Class classToBind)
            throws Exception {
        RandomAccessFile randomAccessFile = new RandomAccessFile(file, "r");
        String buffer;
        Collection<Object> result = new ArrayList<>();
        while ((buffer = randomAccessFile.readLine()) != null) {
            final Iterator<String> bufferSplitted = Arrays.asList(buffer.split(Character.toString(fieldSeparator)))
                    .iterator();
            final Object objectToBind = classToBind.newInstance();
            IterableUtils.forEach(orderedFields, methodName -> {
                try {
                    String value = bufferSplitted.next();
                    BeanUtils.setProperty(objectToBind, methodName, value);
                } catch (IllegalAccessException | InvocationTargetException e) {
                    e.printStackTrace();
                }
            });
            result.add(objectToBind);
        }
        return result;
    }

    public static File getFileFromClassPath(String fileName) {
        final URL resource = FileUtils.class.getClassLoader().getResource(fileName);
        if (resource != null) {
            return new File(resource.getPath());
        }
        return null;
    }

    public static FileUtilsFilenameFilterCompare getFileUtilsFilenameFilterCompare() {
        return FILE_UTIL_FILENAME_FILTER_COMPARE;
    }

    public static FileUtilsFilenameFilterCompareIgnoreCase getFileUtilsFilenameFilterCompareIgnoreCase() {
        return FILE_UTIL_FILENAME_FILTER_COMPARE_IGNORE_CASE;
    }

    public static FileUtilsFilenameFilter<File> getHiddenFilter() {
        return hiddenFilter;
    }

    public static FileUtilsFilenameFilter<File> getNotHiddenFilter() {
        return notHiddenFilter;
    }

    /**
     * Obtains the String staring from the initial position until refered word
     *
     * @param sourcefile    The path for the file to searching for
     * @param startPosition The relative initial position to start to searching for
     * @param searchFor     The String to be searched
     * @return total number of occurences of the String especified in searchFor parameter in a text file identified
     * by sourcefile
     * @throws Exception If any error occur
     */

    public static String getStringFromStartPositionEnclosedBy(String sourcefile, long startPosition, String searchFor)
            throws Exception {
        File file = new File(sourcefile);
        RandomAccessFile randomAccessFile = new RandomAccessFile(file, "r");
        long fileLength = randomAccessFile.length();
        StringBuilder buffer = new StringBuilder(StringUtils.EMPTY);
        if (startPosition < fileLength) {
            randomAccessFile.seek(startPosition);
            boolean recordStartingPointFound = false;
            long i = startPosition - 1;

            for (; i > 0 && !recordStartingPointFound; randomAccessFile.seek(i--)) {
                Character character = randomAccessFile.readChar();
                buffer.insert(0, character);
                if (buffer.toString().startsWith(searchFor)) {
                    recordStartingPointFound = true;
                }
            }
            recordStartingPointFound = false;
            i = startPosition;
            for (; i < fileLength && !recordStartingPointFound; ) {
                Character character = randomAccessFile.readChar();
                buffer.insert(0, character);
                if (buffer.toString().endsWith(searchFor)) {
                    recordStartingPointFound = true;
                }
                randomAccessFile.seek(i++);
            }
        }
        return buffer.toString();
    }

    /**
     * Obtains the String staring from the initial position until refered word
     *
     * @param sourcefile    The path for the file to searching for
     * @param startPosition The relative initial position to start to searching for
     * @param searchFor     The String to be searched
     * @return total number of occurences of the String especified in searchFor parameter in a text file identified
     * by sourcefile
     * @throws Exception If any error occur
     */

    public static String getStringFromStartPositionUntilWord(String sourcefile, long startPosition, String searchFor)
            throws Exception {
        File file = new File(sourcefile);
        BufferedReader bout = new BufferedReader(new FileReader(file));
        char character;
        StringBuilder buffer = new StringBuilder();
        int i = 0;
        if (startPosition < file.length()) {
            while ((character = (char) bout.read()) != 0 && i < startPosition) {
                i++;
            }
            if (character != 0) {
                while (character != 0) {
                    buffer.append(character);
                    if (buffer.toString().endsWith(searchFor)) {
                        buffer.delete(buffer.length() - searchFor.length(), buffer.length());
                        break;
                    }
                    character = (char) bout.read();
                }
            }
        }
        return buffer.toString();
    }

    public static boolean isCompressedFile(File file) {
        return file.getPath().contains(".jar") || file.getPath().contains(".zip") || file.getPath().contains(".tar");
    }

    /**
     * Count total number of occurences of a String in a text file
     *
     * @param sourcefile The path for the file to searching for
     * @param searchFor  The String to be searched
     * @return total number of occurences of the String especified in searchFor parameter in a text file identified
     * by sourcefile
     * @throws Exception If any error occur
     */

    public static long wordCount(String sourcefile, String searchFor)
            throws Exception {

        long searchCount = 0;
        BufferedReader bout = new BufferedReader(new FileReader(sourcefile));
        String ffline;
        while ((ffline = bout.readLine()) != null) {
            searchCount += ffline.split(searchFor).length;
        }
        return searchCount;
    }

    public static File getFile(String path) throws FileNotFoundException {
        String outputFile = path;
        ClassLoader classLoader = FileUtils.class.getClassLoader();
        InputStream stream = classLoader.getResourceAsStream(path);
        if (stream != null) {
            try {
                String pathname = System.getProperty("user.home") + File.separator + "tmp";
                File file = new File(pathname);
                file.mkdir();
                File tempConfig = File.createTempFile(new File(path).getName() + "-", ".dat", file);
                org.apache.commons.io.FileUtils.copyInputStreamToFile(stream, tempConfig);
                outputFile = tempConfig.getPath();
            } catch (NullPointerException | IOException e) {
                e.printStackTrace();
            }
        } else {
            try {
                log.info("Attempting to load file: " + path);
                log.info("\tSearching thru classloader (1): " + classLoader.toString());
                URL resource = classLoader.getResource(path);
                InputStream inputstream = classLoader.getResourceAsStream(path);
                if (resource != null) {
                    outputFile = resource.getPath();
                }
                if (inputstream == null) {
                    log.info("\tSearching thru classloader (2): " + classLoader.toString());
                    inputstream = classLoader.getResourceAsStream(path);
                    resource = classLoader.getResource(path);
                    if (resource != null) {
                        outputFile = resource.getPath();
                    }
                    if (inputstream == null) {
                        log.info("\tSearching thru classloader (3): " + classLoader.getParent().toString());
                        inputstream = classLoader.getParent().getResourceAsStream(path);
                        resource = classLoader.getParent().getResource(path);
                        if (resource != null) {
                            outputFile = resource.getPath();
                        }
                        if (inputstream == null) {
                            log.info("\tSearching thru classloader (4): " + ClassLoader.getSystemClassLoader().toString());
                            inputstream = ClassLoader.getSystemClassLoader().getResourceAsStream(path);
                            resource = ClassLoader.getSystemClassLoader().getResource(path);
                            if (resource != null) {
                                outputFile = resource.getPath();
                            }
                            if (inputstream == null) {
                                log.info("\tSearching directly from absolute path (5): " + path);
                                inputstream = new FileInputStream((new File(path)));
                                resource = (new File(path)).toURI().toURL();
                                outputFile = resource.getPath();
                            }
                        }
                    }
                }
                log.info("\tFile: " + path + " found!");
                inputstream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        File file = new File(outputFile);
        if (!file.exists()) {
            throw new FileNotFoundException("File '" + path + "' not found!");
        }
        return file;
    }

    public static boolean isEmpty(File file) throws Exception {
        if (file == null) {
            return false;
        }
        if (file.isDirectory()) {
            String[] list = file.list();
            return list == null || list.length <= 0;
        } else {
            throw new FileNotFoundException("File is not an directory");
        }
    }

    public static File makeDirFromPackageName(File rootDirectory, String packageName) throws IOException {
        return makeDirFromTokens(rootDirectory, packageName, "\\.");
    }

    public static File makeDirFromTokens(File rootDirectory, String directoryName, String regexTokenSeparator) throws IOException {
        if (rootDirectory == null) {
            return null;
        }
        if (StringUtils.isBlank(directoryName)) {
            if (rootDirectory.isDirectory()) {
                return rootDirectory;
            } else if (rootDirectory.isFile()) {
                return rootDirectory.getParentFile();
            }
        }
        if (StringUtils.isBlank(regexTokenSeparator)) {
            if (rootDirectory.isDirectory()) {
                return new File(rootDirectory, directoryName);
            } else if (rootDirectory.isFile()) {
                return new File(rootDirectory.getParentFile(), directoryName);
            }
        }
        File file = rootDirectory;
        try {
            for (String directory : directoryName.split(regexTokenSeparator)) {
                file = new File(file, directory);
                file.mkdir();
            }
        } catch (Throwable t) {
            log.error("Is not possible to create File '" + rootDirectory.getCanonicalPath() + File.separator + "' due exception: " + t.getMessage());
        }
        return file;
    }

    public static File createTempDir(String baseName) {
        File baseDir = new File(System.getProperty("java.io.tmpdir"));
        File tempDir = new File(baseDir, baseName);
        if (tempDir.mkdir()) {
            return tempDir;
        }
        throw new IllegalStateException("Failed to create directory with name '" + baseName + "'");
    }

    /**
     * Helper recursive method to build up a list of file from.
     *
     * @param root   Root directory to append children.
     * @param filter Criteria the children must fit to be appended to the root directory
     * @param list   List of files to be added to the root directory whether they match provided criteria
     */
    private void addAllChildren(File root, FileFilter filter, List<File> list) {
        if (root.isDirectory()) {
            // List all files and directories without filtering.
            File[] array = root.listFiles();
            for (File child : array != null ? array : new File[0]) {
                addAllChildren(child, filter, list);
            }
        }
        // Add this to the list if accepted.
        if (filter == null || filter.accept(root)) {
            list.add(root);
        }
    }

    public void addFilter(FileUtilsFilenameFilter filterImpl) {
        this.filters.add(filterImpl);
    }

    public void addFilters(NotNullsLinkedHashSet<FileUtilsFilenameFilter> filters) {
        CollectionUtils.predicatedCollection(filters, (Predicate) o -> o instanceof FileUtilsFilenameFilterImpl);
        this.filters.addAll(filters);
    }

    /**
     * Crea un String para ser usado como nombre de un archivo. Toma el nombre
     * recibido y le agrega un underscore ("_") y la fecha actual en formato
     * 'yyyyMMddhhmmss', respetando la extension.
     *
     * @param fileName The incoming file name in the form &lt;fileName&gt;.&lt;extension&gt;
     * @return A String of the form &lt;fileName&gt;_yyyyMMddhhmmss.&lt;extension&gt;
     */
    public String buildDatedFileName(String fileName) {
        return buildDatedFileName(fileName.substring(0, fileName.indexOf(".")),
                fileName.substring(fileName.indexOf(".") + 1));
    }

    /**
     * Crea un String para ser usado como nombre de un archivo. Toma el nombre
     * recibido y le agrega un underscore ("_") y la fecha actual en formato
     * 'yyyyMMddhhmmss'. Se le puede especificar una extension TODO: si la
     * extension es null, se le coloca por defecto la extension XML?
     *
     * @param fileName  The incoming file name
     * @param extension An optionally file extension
     * @return A String of the form &lt;fileName&gt;_yyyyMMddhhmmss.&lt;extension|xml&gt;
     */
    public String buildDatedFileName(String fileName, String extension) {
        return fileName + "_" + dateUtils.sdfl.format(new Date()) + "." + (StringUtils.isEmpty(extension) ?
                EXTENSION_XML : extension);
    }

    public File buildJarFile(String zipFullFilePathName, File directory)
            throws IOException {
        final File f = new File(zipFullFilePathName);
        final JarOutputStream out = new JarOutputStream(new FileOutputStream(f));

        if (directory != null) {
            for (Iterator<File> iter = iterateFilesAndDirs(directory,
                    TrueFileFilter.INSTANCE,
                    DirectoryFileFilter.DIRECTORY); iter.hasNext(); ) {
                File file = iter.next();
                if (file.equals(directory)) {
                    continue;
                }
                String relativePath = file.getPath().replaceFirst(Pattern.quote(directory.getPath()),
                        StringUtils.EMPTY).replaceAll("\\\\", "/");
                relativePath = relativePath.replaceFirst("/", StringUtils.EMPTY) + "/";
                if (file.isDirectory()) {
                    out.putNextEntry(new JarEntry(relativePath));
                } else {
                    copyToJarEntry(out, file, relativePath);
                }
            }
        }
        out.close();
        return f;
    }

    private void copyToJarEntry(ZipOutputStream out, File file, String relativePath)
            throws IOException {
        relativePath = relativePath.substring(0, relativePath.length() - 1);
        out.putNextEntry(new JarEntry(relativePath));
        FileInputStream in = new FileInputStream(file);
        IOUtils.copy(in, out);
        in.close();
        out.closeEntry();
    }

    public File buildZipFile(String zipFileName, final File root, final File fileToAdd)
            throws IOException {
        final Set<File> files = new LinkedHashSet<File>() {{
            add(fileToAdd);
        }};
        Map<File, Set<File>> filesMap = new HashMap<File, Set<File>>() {{
            put(root, files);
        }};
        return buildZipFile(zipFileName, filesMap);
    }

    public File buildZipFile(String zipFullFilePathName, Map<File, Set<File>> filesMapToAdd)
            throws IOException {
        final File f = new File(zipFullFilePathName);
        final ZipOutputStream out = new ZipOutputStream(new FileOutputStream(f));
        if (MapUtils.isNotEmpty(filesMapToAdd)) {
            for (Map.Entry<File, Set<File>> entry : filesMapToAdd.entrySet()) {
                File root = entry.getKey();
                if (root != null) {
                    Collection<File> files = entry.getValue();
                    if (org.apache.commons.collections4.CollectionUtils.isNotEmpty(files)) {
                        for (File file : files) {
                            if (file.equals(root)) {
                                continue;
                            }
                            String relativePath = file.getPath().replaceFirst(Pattern.quote(root.getPath()),
                                    StringUtils.EMPTY).replaceAll("\\\\", "/");
                            relativePath = relativePath.replaceFirst("/", StringUtils.EMPTY) + "/";
                            if (file.isDirectory()) {
                                out.putNextEntry(new JarEntry(relativePath));
                            } else {
                                try {
                                    copyToJarEntry(out, file, relativePath);
                                } catch (Throwable ignored) {
                                }
                            }
                        }
                    }
                }
            }
        }
        out.close();
        return f;
    }

    public File buildZipFile(String zipFullFilePathName, File directory)
            throws IOException {
        final File f = new File(zipFullFilePathName);
        final ZipOutputStream out = new ZipOutputStream(new FileOutputStream(f));

        if (directory != null) {
            for (Iterator<File> iter = FileUtils.iterateFilesAndDirs(directory,
                    TrueFileFilter.INSTANCE,
                    DirectoryFileFilter.DIRECTORY); iter.hasNext(); ) {
                File file = iter.next();
                if (file.equals(directory)) {
                    continue;
                }
                String relativePath = file.getPath().replaceFirst(Pattern.quote(directory.getPath()),
                        StringUtils.EMPTY).replaceAll("\\\\", "/");
                relativePath = relativePath.replaceFirst("/", StringUtils.EMPTY) + "/";
                if (file.isDirectory()) {
                    out.putNextEntry(new ZipEntry(relativePath));
                } else {
                    relativePath = relativePath.substring(0, relativePath.length() - 1);
                    out.putNextEntry(new ZipEntry(relativePath));
                    FileInputStream in = new FileInputStream(file);
                    IOUtils.copy(in, out);
                    in.close();
                    out.closeEntry();
                }
            }
        }
        out.close();
        return f;
    }

    // public  void main(String[] args) {
    // generateFile("Hola mundo!", "c:\\pepe.txt");
    // }

    /**
     * Tests if a specified file should be included in a file list.
     *
     * @param dir  - the directory in which the file was found.
     * @param name - the name of the file.
     * @return <code>true</code> if and only if the name should be included in
     * the file list; <code>false</code> otherwise.
     */
    public boolean checkIfMatches(File dir, String name) {
        try {
            return ((StringUtils.isBlank(this.getFilterCriterion())) || (Pattern.compile(this.getFilterCriterion())
                    .matcher(
                            name).matches()));
        } catch (Exception e) {
            log.error("Error checking the file '" + name + "' on dir '" + dir + "'", e);
            return false;
        }
    }

    /**
     * Getter of filterCriterion
     *
     * @return String
     */
    public String getFilterCriterion() {
        return filterCriterion;
    }

    public void setFilterCriterion(String filterCriterion) {
        this.filterCriterion = filterCriterion.replaceAll("[*]", ".*").replaceAll("[?]", ".?");
    }

    /**
     * Complete the path if itsn't end with '/' or '\'.
     *
     * @param path String with the path to check
     * @return String with the complete path
     */
    public String completePath(String path) {
        if (path == null) {
            return null;
        } else {
            if (path.endsWith("\\") || path.endsWith("\\\\") || path.endsWith("/")) {
                return path;
            } else {
                return path + File.separator;
            }
        }
    }

    /**
     * Copia el archivo indicado por input al indicado por output
     *
     * @param input  String con el nombre del archivo original
     * @param output String con el nombre del archivo destino
     */
    public void copyFile(String input, String output) {
        InputStream in = null;
        OutputStream out = null;

        try {
            in = new FileInputStream(new File(input));
            out = new FileOutputStream(new File(output));

            // Transfer bytes from in to out
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }

        } catch (Exception e) {
            log.error("Error copying the file '" + input + "' to file '" + output + "'", e);
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (Exception e) {
                log.error("Error closing file '" + input + "'", e);
            }
            try {
                if (out != null) {
                    out.close();
                }
            } catch (Exception e) {
                log.error("Error closing file '" + output + "'", e);
            }
        }
    }

    public void copyResourceToDirectory(URL url, File dest, String basePath, boolean forceDelete)
            throws IOException {
        if (url != null) {
            if (forceDelete) {
                try {
                    forceDelete(dest);
                } catch (IOException ignored) {
                }
            }
            switch (url.getProtocol()) {
                case "http":
                    copyURLToDirectory(url, dest);
                    break;
                case "jar":
                    JarURLConnection jarConnection = (JarURLConnection) url.openConnection();
                    copyJarToDirectory(jarConnection.getJarFile(), basePath, dest.getCanonicalPath());
                    break;
                case "file":
                    String file = url.getFile();
                    copyDirectory(new File(file).getCanonicalFile(), dest);
                    break;
                case "vfs":
                    break;
            }
        }
    }

    public void copyURLToDirectory(URL url, File dest)
            throws IOException {

        URLConnection urlC = url.openConnection();
        // Copy resource to local file, use remote file
        // if no local file name specified
        InputStream is = url.openStream();
        // Print info about resource
        Date date = new Date(urlC.getLastModified());
        System.out.flush();
        FileOutputStream fos = new FileOutputStream(dest);
        int oneChar;
        while ((oneChar = is.read()) != -1) {
            fos.write(oneChar);
        }
        is.close();
        fos.close();

    }

    /**
     * Copies a directory from a jar file to an external directory.
     *
     * @param fromJar Jar whose directory will be copied
     * @param jarDir  Jar destination
     * @param destDir Path destination
     * @throws IOException If is not possible to copy the embedded directory from the jar to destination path
     */
    public void copyJarToDirectory(JarFile fromJar, String jarDir, String destDir)
            throws IOException {
        for (Enumeration<JarEntry> entries = fromJar.entries(); entries.hasMoreElements(); ) {
            JarEntry entry = entries.nextElement();
            if (!entry.isDirectory() && entry.getName().contains(jarDir)) {
                File dest = new File(destDir + "/" + entry.getName().substring(jarDir.length()));
                File parent = dest.getParentFile();
                if (parent != null) {
                    parent.mkdirs();
                }

                try (FileOutputStream out = new FileOutputStream(dest); InputStream in = fromJar.getInputStream(entry)) {
                    byte[] buffer = new byte[8 * 1024];

                    int s;
                    while ((s = in.read(buffer)) > 0) {
                        out.write(buffer, 0, s);
                    }
                } catch (IOException e) {
                    throw new IOException("Could not copy asset from jar file", e);
                }
            }
        }

    }

    public boolean createDirectory(String path) {
        return (new File(path)).mkdirs();
    }

    /**
     * Create a temporary directory using the given name
     *
     * @param name Prefix for temporary file creation
     * @return Temporary file created
     * @throws IOException If temporary file could not be created
     */
    public File createTempDirectory(String name)
            throws IOException {
        // Start will a temp file
        File temp = File.createTempFile(name, "");
        // Delete the file
        temp.delete();
        // Convert it a directory
        temp.mkdir();
        return temp;
    }

    public boolean fileOrDirectoryExists(String filepath) {
        return (new File(filepath)).exists();
    }

    // Inicio de los metodos usados en SICAM
    // Validar que funcionen y sean utiles

    /**
     * Get all clazz that extends of superClass
     *
     * @param directory  Directory on where children of super class will be searched for
     * @param superClass Super class of the classes that will be searched in the directory
     * @return All classes that extends of provided super class.
     */
    private ArrayList<Class> findClassesThatExtends(File directory, Class<?> superClass) {
        ArrayList<Class> results = new ArrayList<>();
        if (directory.isFile() && directory.getName().endsWith(".class")) {
            String nativeNotation = directory.getAbsolutePath();
            nativeNotation = deleteUntilFirstDot(nativeNotation);
            nativeNotation = replaceAllDiferentToCharacter(nativeNotation);

            Class clazz;
            try {
                clazz = myClassForName(nativeNotation);
                if (superClass.isAssignableFrom(clazz)) {
                    results.add(clazz);
                }
            } catch (Throwable e) {
                log.error(e.getMessage());
                // log.warn("Error loading class = " + nativeNotation);
                // TODO: Validar si colocamos o no esta traza
                // Todas las clases internas fallan y pasan por aqui.
                // log.warn("Is not a valid class = " + nativeNotation, e);
            }
            return results;
        }

        File[] files = directory.listFiles();
        for (int i = 0; files != null && i < files.length; i++) {
            results.addAll(findClassesThatExtends(files[i], superClass));
        }
        return results;
    }

    /**
     * Get all clazz that extends of superClass
     *
     * @param directory            Directory to find implemented classes
     * @param implementedClassName Interface class name of those classes in directory that implement it
     * @return All classes that implement the provided implemented class.
     * @throws ClassNotFoundException ClassNotFoundException
     */
    private ArrayList<Class> findClassesThatImplemented(File directory, String implementedClassName)
            throws ClassNotFoundException {
        ArrayList<Class> results = new ArrayList<>();
        //log.debug("Directory: "+directory.isFile() + " y "+
        // directory.getName());
        if (directory.isFile() && directory.getName().endsWith(".class")) {
            String nativeNotation = directory.getAbsolutePath();
            //log.debug("nativeNotation: "+nativeNotation);
            // String property =
            // properties.getProperty(BASE_PATH_PROPERTY_NAME);
            // nativeNotation = nativeNotation.substring(
            // nativeNotation.indexOf(property) + property.length() + 1,
            // nativeNotation.length());
            nativeNotation = deleteUntilFirstDot(nativeNotation);
            //log.debug("nativeNotation: "+nativeNotation);
            nativeNotation = replaceAllDiferentToCharacter(nativeNotation);
            //log.debug("nativeNotation luego: "+nativeNotation);

            Class clazz = myClassForName(nativeNotation);
            final Class[] interfaces = clazz.getInterfaces();
            Collection<String> classes = CollectionUtils.collect(Arrays.asList(interfaces),
                    ReflectionUtils.CLASS_NAME_TRANSFORMER);
            if (classes.contains(implementedClassName)) {
                results.add(clazz);
            }
            return results;
        }

        File[] files = directory.listFiles();
        for (int i = 0; files != null && i < files.length; i++) {
            results.addAll(findClassesThatImplemented(files[i], implementedClassName));
        }
        return results;
    }

    public String findFileOnDirRecursive(String fileName, String folderName, boolean onlyOne, int tab) {
        String tabStr = StringUtils.leftPad("", tab);
        try {
            if (folderName.charAt(folderName.length() - 1) != File.separator.charAt(0)) {
                folderName += String.valueOf(File.separator.charAt(0));
            }

            //log.debug(tabStr + "Looking for class '" + fileName +
            // "' on dir '" + folderName + "'.");
            String className2u = "/" + fileName + "."; // "/"? sure?

            File dir = new File(folderName);
            //log.debug(tabStr + "Dir: " + dir.getAbsolutePath());

            File[] children = dir.listFiles();
            if (children == null) {
                // Either dir does not exist or is not a directory
                return null;
            } else {
                for (File innerFile : children) {
                    // Get filename of file or directory
                    String innerFileName = innerFile.getName();
                    String innerFilePath = innerFile.getAbsolutePath();
                    //log.debug(tabStr + "File found: " +
                    // innerFileName);
                    if (innerFile.isDirectory()) {
                        // Dir?
                        String path = findFileOnDirRecursive(fileName, innerFilePath, onlyOne, tab + 3);
                        if (null != path) {
                            log.debug(tabStr + "*** Class '" + fileName + "' found on " + "dir '" + innerFilePath +
                                    "'.");
                            if (onlyOne) {
                                return path;
                            }
                        }
                    } else if (innerFileName.length() > 4 && innerFileName.indexOf(".jar") == (innerFileName.length()
                            - 4)) {
                        // Jar?
                        // Otra forma de compararlo es viendo si el
                        // (URL.getProtocol().equals("jar")
                        boolean found = jarUtils.findClassOnJar(fileName, innerFilePath);
                        if (found) {
                            log.debug(tabStr + "*** Class '" + fileName + "' found on " + "jar '" + innerFilePath +
                                    "'.");
                            if (onlyOne) {
                                // TODO: Validar si devolvemos solo el .jar o el .jar con la ruta
                                // hasta el archivo
                                return innerFilePath + File.separator + fileName;
                            }
                        }
                    } else {
                        // File?
                        int nameIndex = className2u.indexOf(innerFileName);
                        //log.debug(tabStr + "nameIndex = " +
                        // nameIndex + " -> a = '" + innerFileName + "'; b = '"
                        // + className2u + "'");
                        if (nameIndex != -1) {
                            log.debug(tabStr + "*** Class '" + innerFileName + "' found " + "" + "" + "on dir '" +
                                    folderName + "'.");
                            if (onlyOne) {
                                return folderName + File.separator + innerFileName;
                            }
                        }
                    }
                }
            }
            //log.debug(tabStr + "Class not found on dir '" +
            // folderName + "'.");
        } catch (Exception e) {
            log.error(tabStr + "Error looking for class '" + fileName + "' on dir '" + folderName + "'", e);
            log.error(tabStr + "Error looking for class '" + fileName + "' on dir '" + folderName + "'");
            log.error(e.getMessage());
        }
        return null;
    }

    /**
     * Busca el path de un archivo en el classpath.
     * No se conoce el comportamiento si el archivo esta duplicado.
     * Si el archivo esta dentro de un .jar, lo encontrara, pero fallara luego quien trate de usar el path devuelto.
     * En esos casos es necesario que la persona llame a loadBundleAsResource(...) o un metodo similar.
     *
     * @param fileName String con el filename a buscar
     * @return String con el path completo del file
     */
    public String findFilePath(String fileName) {
        //log.debug(" Thread.currentThread().getContextClassLoader() "
        // + Thread.currentThread().getContextClassLoader());
        log.debug("{524 - FileUtils.java} Cargando archivo  " + fileName);

        String path;

        // URL resource = Thread.currentThread().getContextClassLoader().getResource(fileName);
        URL resource = ClassLoader.getSystemResource(fileName);
        if (null == resource) {
            resource = SystemInfo.class.getResource(fileName);
            if (null == resource) {
                resource = Thread.currentThread().getContextClassLoader().getResource(fileName);
            }
        }

        if (null == resource) {
            // path = System.getProperty("user.dir")+ "\\" + fileName;
            path = findFileOnDirRecursive(fileName, System.getProperty("user.dir"), true, 0);
            // path = System.getProperty("user.dir") + File.pathSeparator + fileName;
            // No conseguimos el archivo en el classpath. Usaremos el fileName completo
            log.debug("Path (default 1): '" + path + "'");

        } else {
            // Conseguimos el archivo en el classpath
            path = resource.getPath();
            log.debug("Path (1): '" + path + "'");

        }

        //AMM Si todos los intentos anteriores por ubicar el archivo fallan,
        // se intenta este mecanismo experimental que teoricamente busca el archivo partir del primer
        // classloader que encuentre en la cadena de jerarquía que invoco al jar que contiene esta clase
        // iterando hasta alcanzar el SystemClassLoader. En caso de no encontrarlo en toda la jerarquia
        // de ClassLoader cargados en Runtime, intenta ubicar el archivo considerando la variable "path"
        // como una ruta absoluta.

        //        if (StringUtils.isEmptyOrNullValue(path)) {
        //            try {
        //                path = FileAndPropertiesHandlerUtil.getInstance(fileName,
        //                        FileUtils.class.getClassLoader()).getFile().getPath();
        //            }   catch (Throwable ignored) {
        //            }
        //        }

        char primerChar = path.charAt(0);
        log.debug("Path (1.5): '" + File.pathSeparator + "' vs '" + primerChar + "'");
        if (File.pathSeparator.equals(primerChar + "")) {
            // Si el fileName trajo o no barra, puede que el path inicie con
            // barra antes de la unidad.
            path = path.substring(1);
        }

        log.debug("{554} Path (2): '" + path + "'");
        return path;
    }

    // public  void main(String[] args) {
    // getClassesThatImplementsFromJar(IReseteableDao.clas, "C:\\bea", true);
    // }

    public void flushInputStreamToFile(InputStream stream, String filepath)
            throws IOException {
        File file = createFile(filepath);
        OutputStream out;
        log.debug("file: " + file);
        log.debug("filepath: " + filepath);
        out = new FileOutputStream(file);
        byte[] buf = new byte[1024];
        int len;
        while ((len = stream.read(buf)) > 0) {
            out.write(buf, 0, len);
        }
        out.close();
    }

    public File createFile(String filepath)
            throws IOException {
        File file = new File(filepath);
        file.createNewFile();
        return file;
    }

    /**
     * Generates a plain-text file using given data
     *
     * @param data      String with the info to fill the file
     * @param path      String with the path where the file will be located
     * @param fileName  String with the file's name
     * @param extension String with the file's extension
     * @return String with the file's final name if created, null otherwise
     */
    public String generateFile(String data, String path, String fileName, String extension) {
        // String guiDGen = FileUtils.getUniqueName(fileName, false);
        String fullFileName = path + fileName + extension;
        return generateFile(data, fullFileName);
    }

    /**
     * Generates a plain-text file using given data
     *
     * @param data         String with the info to fill the file
     * @param fullFileName String with the file's path, name and extension
     * @return String with the file's final name if created, null otherwise
     */
    public String generateFile(String data, String fullFileName) {
        // TODO: Implementar FileWrite
        // FileWrite result = new FileWrite(fullFileName);
        // result.write(data);
        // result.close();

        OutputStream out = null;
        try {
            out = new FileOutputStream(new File(fullFileName));
            out.write(data.getBytes());
        } catch (Exception e) {
            log.error("Error writing file '" + fullFileName + "'", e);
        } finally {
            try {
                if (null != out) {
                    out.close();
                }
            } catch (Exception e) {
                log.error("Error closing file '" + fullFileName + "'", e);
            }
        }

        return fullFileName;
    }

    /**
     * Generates a plain-text file with an unique name, using given data
     *
     * @param data      String with the info to fill the file
     * @param path      String with the path where the file will be located
     * @param fileName  String with the file's name
     * @param extension String with the file's extension
     * @return String with the file's final name if created, null otherwise
     */
    public String generateUniqueFile(String data, String path, String fileName, String extension) {
        String guiDGen = getUniqueName(fileName, false);
        String fullFileName = path + "_" + guiDGen + extension;
        return generateFile(data, fullFileName);
    }

    // Validar que funcionen y sean utiles
    // Final de los metodos usados en SICAM

    /**
     * Generates a unique name for a file, adding a unique number and a unique
     * date to it. Format: &lt;fileName&gt;&lt;uniqueNumber&gt;&lt;uniqueDate&gt; Example
     * (numeric==true): testName1720101213102118 Example (numeric==false):
     * testName17_Mon_Dec_13_101858_VET_2010
     *
     * @param fileName String with the file proposed name
     * @param numeric  boolean true if fileName will be generated adding only
     *                 numbers, false otherwise
     * @return String with the resulting fileName
     */
    public String getUniqueName(String fileName, boolean numeric) {
        StringBuilder particle;
        if (numeric) {
            // date = DateUtil.sdfl.format(new Date());
            particle = new StringBuilder(numberUtils.getUniqueNumber() + "");
        } else {
            String[] temp = Calendar.getInstance().getTime().toString().split(" ");
            particle = new StringBuilder();
            for (int i = 0; i < temp.length; i++) {
                String[] temp2 = temp[i].split(":");
                temp[i] = "";
                for (String aTemp2 : temp2) {
                    temp[i] += aTemp2;
                }
                particle.append("_").append(temp[i]);
            }
            particle.insert(0, numberUtils.getUniqueNumber());
        }
        return fileName + particle;
    }

    public byte[] getBytesArrayFromFile(String filePath)
            throws IOException {
        File theFile = new File(filePath);
        long length = theFile.length();
        // if (length > Integer.MAX_VALUE) {
        // Archivo muy extenso, validar si es necesario considerar la longitud
        // del archivo
        // }
        byte[] bytes = new byte[(int) length];
        int offset = 0;
        int numRead;
        try (InputStream is = new FileInputStream(theFile)) {
            while (offset < bytes.length && (numRead = is.read(bytes, offset, bytes.length - offset)) >= 0) {
                offset += numRead;
            }
            if (offset < bytes.length) {
                throw new IOException("Es posible que " + theFile.getName() + " no haya sido " + "leido completamente");
            }

        }
        return bytes;
    }

    /**
     * Ejemplo de llamada:
     * Finder.getInstance().getClassesThatExtends(ReiniciableDao.class)
     *
     * @param superClass Super class of the classes that will be searched in current directory
     * @return Classes that extend provided super class on current directory
     * @throws IllegalArgumentException If is not possible to find children for provided super class
     */
    public ArrayList<Class> getClassesThatExtends(Class superClass) {
        if (superClass == null) {
            throw new IllegalArgumentException("Super Class name is null!");
        }
        File directory = new File(getPath());
        // TODO: Estamos buscando en todo el sistema. Deberiamos acotar el
        // dominio para que sea mas rapido?
        // return findClassesThatExtends(directory, superClassName);
        // Class mySuper = Class.forName(superClassName);
        return findClassesThatExtends(directory, superClass);
    }

    public ArrayList<Class> getClassesThatImplements(String implementedClassName)
            throws Exception {
        if (implementedClassName == null) {
            throw new IllegalArgumentException("Implemented Class name is null!");
        }
        File directory = new File(getPath());
        return findClassesThatImplemented(directory, implementedClassName);
    }

    public ArrayList<Class> getClassesThatImplementsFromJar(Class implementedClassName, String jarName) {
        if (implementedClassName == null) {
            throw new IllegalArgumentException("Implemented Class name is null!");
        }
        File jar = new File(jarName);
        return findClassesThatImplementedFromJar(jar, implementedClassName, jarName);
    }

    /**
     * Get all clazz that implemets of superClass from Jar
     *
     * @param jar              Jar file
     * @param implementedClass String
     * @param jarName          Jar name that contains the classes to check if they implement incoming class
     * @return all clazz that implement this implementedClass.
     */
    //TODO: Sacar jarName de jar
    private ArrayList<Class> findClassesThatImplementedFromJar(File jar, Class implementedClass, String jarName) {
        ArrayList<Class> results = new ArrayList<>();

        boolean isFile = jar.isFile();
        //log.debug("[fJAR] jar.isFile() = " + isFile);

        boolean endsWithJar = jar.getName().endsWith(".jar");
        //log.debug("[fJAR] endsWithJar = " + endsWithJar);

        if (isFile && endsWithJar) {
            //log.debug("[fJAR] jar valido");
            ArrayList<String> clazzez = jarUtils.listClassesOnJar(jarName);
            for (String aClasses : clazzez) {
                String nativeNotation = aClasses;
                if (nativeNotation.contains("$")) {
                    log.debug("*** Clase interna " + nativeNotation + " sera ignorada.");
                    // TODO: Estamos ignorando las clases internas. Se puede
                    // parametrizar este comportamiento.
                    continue;
                }
                //log.debug("[fJAR] nativeNotation: " +
                // nativeNotation);
                log.debug("*** Validando clase: " + nativeNotation);
                nativeNotation = deleteUntilFirstDot(nativeNotation);
                //log.debug("[fJAR] nativeNotation luego de borrar punto: "
                // + nativeNotation);
                nativeNotation = replaceAllDiferentToCharacter(nativeNotation);
                //log.debug("[fJAR] nativeNotation luego de cambiarlo a puntos: "
                // + nativeNotation);
                //log.debug(" -> " + nativeNotation);

                Class clazz;
                try {
                    clazz = myClassForName(nativeNotation);
                    log.debug("    clazz = " + clazz.getName() + " -> # interfaces = " + clazz.getInterfaces().length);

                    if (Arrays.asList(clazz.getInterfaces()).contains(implementedClass)) {
                        //log.debug("[fJAR] implements = " +
                        // implementedClass.getName());
                        results.add(clazz);
                    }
                } catch (ClassNotFoundException e) {
                    // TODO: Cuando se vea esta traza es que hay una inconsistencia en el codigo.
                    log.error("    Error buscando la clase '" + nativeNotation + "'.");
                    // Si no se consigue una clase, es que su paquete y su
                    // directorio estan inconsistentes
                    log.error("    Probablemente hay inconsistencia entre su package y " + "su path...");
                    // log.error(e.getMessage());
                } catch (Throwable e) {
                    // TODO: Cuando se vea esta traza es que hay una inconsistencia en el codigo.
                    log.error("    Error desconocido buscando la clase '" + nativeNotation + "'.");
                    // Todas las clases internas fallan y pasan por aqui.
                    log.error("    Ignorar esta traza si la clase es una clase interna." + "." + ".");
                    // log.error(e.getMessage());
                }
            }
            //log.debug("El tamanio de results en findClassesThatImplementedFromJar es
            // "+results.size());
            log.debug("");
            return results;
        }

        return results;
    }

    /**
     * get fileName without first dot
     *
     * @param fileName String
     * @return fileName without first dot.
     */
    private String deleteUntilFirstDot(String fileName) {
        int index;
        if (fileName != null && (index = fileName.indexOf(".")) != -1) {
            return fileName.substring(0, index);
        }
        return fileName;
    }

    /**
     * Delete all slash in nativeNotation.
     *
     * @param nativeNotation String
     * @return Delete all slash in nativeNotation
     */
    private String replaceAllDiferentToCharacter(String nativeNotation) {
        String pattern = "\\W";
        return nativeNotation.replaceAll(pattern, ".");
    }

    /**
     * @param nativeNotation className
     * @return Class
     * @throws ClassNotFoundException ClassNotFoundException
     */
    private Class myClassForName(String nativeNotation)
            throws ClassNotFoundException {
        // return Da algun error ->
        // getClass().getClassLoader().loadClass(nativeNotation);
        // return Da NullPointer ->
        // "".getClass().getClassLoader().loadClass(nativeNotation);
        // return Da OutOfMemory ->
        // ClassLoader.getSystemClassLoader().loadClass(nativeNotation);
        return Class.forName(nativeNotation);
    }

    public File getFile(String fileStr, String tempPath)
            throws IOException {
        return getFile(new File(fileStr), tempPath);
    }

    public File getFile(File file, String tempPath)
            throws IOException {
        if (file != null && file.exists()) {
            return file;
        } else {
            if (file == null) {
                return null;
            }
            String fileStr = file.getPath();
            URL file_ = this.getClass().getResource("/" + fileStr);
            InputStream inputStream = file_.openStream();
            File targetFile = new File(tempPath + File.separator + fileStr);
            final OutputStream outStream = new FileOutputStream(targetFile);

            final byte[] buffer = new byte[8 * 1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outStream.write(buffer, 0, bytesRead);
            }
            IOUtils.closeQuietly(inputStream);
            IOUtils.closeQuietly(outStream);
            return targetFile;
        }
    }

    public File getFileFromFTP(String ftpServerDomain,
                               String ftpServerDomainLogin,
                               String ftpServerDomainPassword,
                               String ftpRemoteFilePath,
                               String ftpLocalFilePath) {

        FTPClient client = new FTPClient();
        FileOutputStream fos = null;

        File file = new File(ftpLocalFilePath);
        forceCreateNewFile(file);
        file.deleteOnExit();
        try {
            client.connect(ftpServerDomain);
            client.login(ftpServerDomainLogin, ftpServerDomainPassword);
            fos = new FileOutputStream(file);
            client.retrieveFile(ftpRemoteFilePath, fos);
        } catch (IOException e) {
            log.error("Exception [" + e.getClass() + "] - " + e.getMessage());
        } finally {
            try {
                if (fos != null) {
                    fos.close();
                }
                client.disconnect();
            } catch (Exception e) {
                log.error("Exception [" + e.getClass() + "] - " + e.getMessage());
            }
        }
        return file;
    }

    public void forceCreateNewFile(File file) {
        try {
            if (file.exists()) {
                file.delete();
            }
            file.createNewFile();
        } catch (IOException e) {
            log.error("Exception [" + e.getClass() + "] - " + e.getMessage());
        }
    }

    public File getFileFromURL(String urlLocalFilePath,
                               String urlRemoteFilePath,
                               String urlServerDomainAndPort,
                               String urlServerDomainLogin,
                               String urlServerDomainPassword) {

        URL u;
        InputStream is = null;
        BufferedReader dis;
        String s;
        File file = new File(urlLocalFilePath);
        forceCreateNewFile(file);
        file.deleteOnExit();
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("http://");
            if (!StringUtils.isBlank(urlServerDomainLogin)) {
                sb.append(urlServerDomainLogin);
            }
            if (!StringUtils.isBlank(urlServerDomainPassword)) {
                sb.append("/").append(urlServerDomainPassword);
            }
            if (!StringUtils.isBlank(urlServerDomainLogin)) {
                sb.append("@");
            }
            sb.append(urlServerDomainAndPort).append("/").append(urlRemoteFilePath);
            u = new URL(sb.toString());
            is = u.openStream();
            dis = new BufferedReader(new InputStreamReader(is));
            FileOutputStream fos = new FileOutputStream(file);
            while ((s = dis.readLine()) != null) {
                s = s.concat("\n");
                fos.write(s.getBytes());
            }

        } catch (IOException ioe) {
            log.error("Exception [" + ioe.getClass() + "] - " + ioe.getMessage());

        } finally {

            try {
                if (is != null) {
                    is.close();
                }
            } catch (Exception e) {
                log.error("Exception [" + e.getClass() + "] - " + e.getMessage());
            }

        }
        return file;
    }

    public NotNullsLinkedHashSet<FileUtilsFilenameFilter> getFilters() {
        return filters;
    }

    public void setFilters(NotNullsLinkedHashSet<FileUtilsFilenameFilter> filters) {
        this.filters = filters;
    }

    private String getPath() {
        /*
         * String path = properties.getProperty(BASE_PATH_PROPERTY_NAME); // No
         * tenemos que validar una excepcion? return (null == path) ?
         * DEFAULT_PATH : path;
         */
        return DEFAULT_PATH;
    }

    public int getRecursionLevel() {
        return recursionLevel;
    }

    public void setRecursionLevel(int recursionLevel) {
        this.recursionLevel = recursionLevel;
    }

    /**
     * The recursive iterator can be used to walk all files in a directory tree.
     * Children will appear before parent directories so this can be used for
     * recursive deletes.
     *
     * @param root   Root file
     * @param filter Criteria for filtering files
     * @return Filtered iterator of files that fit the criteria
     */
    public Iterator<File> getRecursiveIterator(File root, FileFilter filter) {
        // Start with a list
        List<File> list = new LinkedList<>();
        // Build up the list
        addAllChildren(root, filter, list);
        return list.iterator();
    }

    public File getRelativeFile(String fileName) throws IOException {
        File file = null;
        URL url = FileUtils.class.getResource("/" + fileName);
        if (url != null) {
            InputStream stream = url.openStream();
            if (stream == null) {
                url = FileUtils.class.getClassLoader().getResource("/" + fileName);
                if (url != null) {
                    stream = url.openStream();
                    if (stream != null) {
                        return new File(url.getFile());
                    }
                }
            } else {
                return new File(url.getFile());
            }
        }
        return null;
    }

    public String getRelativePathFrom(File baseFile, File absoluteFile) {
        if (baseFile.isFile()) {
            baseFile = baseFile.getParentFile();
        }
        String baseFileStr = baseFile.getAbsolutePath();
        if (absoluteFile.isFile()) {
            absoluteFile = absoluteFile.getParentFile();
        }
        String absoluteFileStr = absoluteFile.getAbsolutePath();
        return StringUtils.replace(absoluteFileStr, baseFileStr, StringUtils.EMPTY);
    }

    /**
     * Convert a text file into a String Collection of lines
     *
     * @param file The file to be converted
     * @return A collection of String with one row per each line of the related file
     * @throws java.io.IOException If us not possible to convert the file into a collection of lines
     */

    public Collection<String> getRowsOfTextFileAsCollection(File file)
            throws IOException {
        ArrayList<String> rows = new ArrayList<>();
        BufferedReader reader = new BufferedReader(new FileReader(file));

        String s;
        while ((s = reader.readLine()) != null) {
            rows.add(s);
        }
        return rows;
    }

    public int getSearchType() {
        return searchType;
    }

    public void setSearchType(int searchType) {
        this.searchType = searchType;
    }

    /**
     * Take a text extract get its content into a String
     *
     * @param file Text file to which the string will be drawn
     * @return A String with the content of the file
     * @throws java.io.IOException if is not possible to take a text extract get its content into a String
     */

    public String getTextFromFile(File file)
            throws IOException {

        StringBuilder sb = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line = br.readLine();

            while (line != null) {
                sb.append(line);
                sb.append("\n");
                line = br.readLine();
            }

        }
        return sb.toString();
    }

    public JarFile jarForClass(Class<?> clazz, JarFile defaultJar) {
        String path = "/" + clazz.getName().replace('.', '/') + ".class";
        URL jarUrl = clazz.getResource(path);
        if (jarUrl == null) {
            return defaultJar;
        }

        String url = jarUrl.toString();
        int bang = url.indexOf("!");
        String JAR_URI_PREFIX = "jar:file:";
        if (url.startsWith(JAR_URI_PREFIX) && bang != -1) {
            try {
                return new JarFile(url.substring(JAR_URI_PREFIX.length(), bang));
            } catch (IOException e) {
                throw new IllegalStateException("Error loading jar file.", e);
            }
        } else {
            return defaultJar;
        }
    }

    /**
     * Lista los archivos dentro de un directorio
     *
     * @param directory String con el nombre del directorio a revisar
     * @return String[] con los nombres de los archivos dentro de directory
     */
    public String[] listFiles(String directory) {
        return new File(directory).list();
    }

    public NotNullsLinkedHashSet<File> listFiles(Collection<File> files) {
        return listFiles(files, filters, DEFAULT_FILTER_TYPE, DEFAULT_RECURSION_LEVEL, DEFAULT_SEARCH_TYPE);
    }

    public NotNullsLinkedHashSet<File> listFiles(Collection<File> files, int filteringType) {
        return listFiles(files, filters, filteringType, DEFAULT_RECURSION_LEVEL, DEFAULT_SEARCH_TYPE);
    }

    public NotNullsLinkedHashSet<File> listFiles(File file, int filteringType) {
        NotNullsLinkedHashSet<File> files = new NotNullsLinkedHashSet<>();
        files.add(file);
        return listFiles(files, filters, filteringType, DEFAULT_RECURSION_LEVEL, DEFAULT_SEARCH_TYPE);
    }

    public NotNullsLinkedHashSet<File> listFiles(File file) {
        NotNullsLinkedHashSet<File> files = new NotNullsLinkedHashSet<>();
        files.add(file);
        return listFiles(files, filters, DEFAULT_FILTER_TYPE, DEFAULT_RECURSION_LEVEL, DEFAULT_SEARCH_TYPE);
    }

    public NotNullsLinkedHashSet<File> listFiles(NotNullsLinkedHashSet<File> files,
                                                 int filteringType,
                                                 int recursionLevel) {
        return listFiles(files, filters, filteringType, recursionLevel, DEFAULT_SEARCH_TYPE);
    }

    public NotNullsLinkedHashSet<File> listFiles(File file, int filteringType, int recursionLevel, int searchType) {
        NotNullsLinkedHashSet<File> files = new NotNullsLinkedHashSet<>();
        files.add(file);
        return listFiles(files, filters, filteringType, recursionLevel, searchType);
    }

    public NotNullsLinkedHashSet<File> listFiles(File file,
                                                 Collection<FileUtilsFilenameFilter> filters,
                                                 int filteringType,
                                                 int recursionLevel,
                                                 int searchType) {
        NotNullsLinkedHashSet<File> files = new NotNullsLinkedHashSet<>();
        files.add(file);
        return listFiles(files, filters, filteringType, recursionLevel, searchType);
    }

    /**
     * @param files         File to filtering
     * @param filters       Filters to be applied
     * @param filteringType Type of evaluation of the filters list
     * @param recurse       Recursion level
     * @param searchType    Searching type
     * @return Collection of files that has matched filters
     */
    @SuppressWarnings("ConstantConditions")
    public NotNullsLinkedHashSet<File> listFiles(final Collection<File> files,
                                                 final Collection<FileUtilsFilenameFilter> filters,
                                                 final int filteringType,
                                                 int recurse,
                                                 int searchType) {

        if (filters != null) {
            filters.addAll(getFilters());
        }
        final NotNullsLinkedHashSet<File> listedFiles = new NotNullsLinkedHashSet<>();

        for (File file1 : files) {
            final NotNullsLinkedHashSet<File> filteredFiles = new NotNullsLinkedHashSet<>();
            final File entry = file1;
            try {
                JarFile module = new JarFile(entry);
                final ArrayList<JarEntry> list = Collections.list(module.entries());
                IterableUtils.forEach(list, o -> filteredFiles.add(new File(entry + File.separator + o.toString())));
            } catch (IOException ignored) {
                try {
                    final File[] listFiles = entry.listFiles();
                    assert listFiles != null;
                    filteredFiles.addAll(Arrays.asList(listFiles));
                } catch (NullPointerException npe) {
                    filteredFiles.add(entry);
                }
            }

            CollectionUtils.filter(filteredFiles, o -> {
                String value = o.toString();
                return !value.endsWith(StringUtils.SLASH) && !value.endsWith("CVS") && !value.endsWith(".svn") &&
                        !value.endsWith(
                                "cvs") && !value.endsWith(".SVN");
            });

            final NotNullsLinkedHashSet<File> filteredFilesTemp = new NotNullsLinkedHashSet<>();
            final NotNullsLinkedHashSet<File> filesToRemove = new NotNullsLinkedHashSet<>();
            for (Object filteredFile : filteredFiles) {
                File fileEntry = (File) filteredFile;
                try {
                    new JarFile(fileEntry);
                    filesToRemove.add(fileEntry);
                    filteredFilesTemp.addAll(listFiles(new NotNullsLinkedHashSet<>(Collections.singletonList
                                    (fileEntry)),
                            filters,
                            filteringType,
                            recurse,
                            searchType));
                } catch (IOException ignored) {
                }
            }
            filteredFiles.removeAll(filesToRemove);
            filteredFiles.addAll(filteredFilesTemp);

            for (Object filteredFile : filteredFiles) {
                File fileEntry = (File) filteredFile;
                String directory;
                String file;
                File incomingFile;

                String entryPath = fileEntry.getPath();
                incomingFile = new File(entryPath);

                String[] jarTokens = entryPath.split("!");
                try {
                    directory = jarTokens[1].replaceAll(StringUtils.DOUBLEBACKSLASH, StringUtils.SLASH);
                    incomingFile = new File(jarTokens[0]);
                    if (directory.startsWith(StringUtils.SLASH)) {
                        directory = directory.replaceFirst(StringUtils.SLASH, StringUtils.EMPTY);
                    }
                    file = incomingFile.getName();
                } catch (Exception ignored2) {
                    try {
                        jarTokens = entryPath.split(".jar");
                        directory = jarTokens[1].replaceAll(StringUtils.DOUBLEBACKSLASH, StringUtils.SLASH);
                        incomingFile = new File(jarTokens[0] + ".jar");
                        if (directory.startsWith(StringUtils.SLASH)) {
                            directory = directory.replaceFirst(StringUtils.SLASH, StringUtils.EMPTY);
                        }
                        file = directory;
                        directory = incomingFile.getPath();
                    } catch (Exception ignored3) {
                        directory = incomingFile.getPath();
                        file = incomingFile.getName();
                        if (incomingFile.isFile()) {
                            directory = stringUtils.replaceLast(directory, file, StringUtils.EMPTY);
                            if (directory.lastIndexOf("\\") == directory.length() - 1) {
                                directory = stringUtils.replaceLast(directory,
                                        StringUtils.DOUBLEBACKSLASH,
                                        StringUtils.EMPTY);
                            }
                            if (directory.lastIndexOf(StringUtils.SLASH) == directory.length() - 1) {
                                directory = stringUtils.replaceLast(directory, StringUtils.SLASH, StringUtils.EMPTY);
                            }
                        } else {
                            file = StringUtils.EMPTY;
                        }
                    }
                }

                final String fileName = file;
                final File directoryFile = new File(directory);

                boolean applyFilter = filters != null;

                Collection<FileUtilsFilenameFilter> filteredEntries;

                if (applyFilter) {
                    filteredEntries = CollectionUtils.select(filters,
                            filterImpl -> filterImpl.accept(directoryFile, fileName));

                    switch (filteringType) {
                        case FILTER_TYPE_ALL:
                            applyFilter = filteredEntries.size() == filters.size();
                            break;
                        case FILTER_TYPE_SOME:
                            applyFilter = filteredEntries.size() > 0;
                            break;
                        case FILTER_TYPE_NONE:
                            applyFilter = filteredEntries.size() == 0;
                            break;
                        case FILTER_TYPE_UNKNOWN:
                        default:
                            break;
                    }

                    if (applyFilter) {
                        listedFiles.add(fileEntry);
                    }

                    if (isCompressedFile(fileEntry)) {
                        if (Math.abs(recurse) < RECURSION_LIMIT && !incomingFile.getPath().equals(entryPath) &&
                                !fileEntry.getName().contains(
                                        ".")) {
                            recurse--;
                            listedFiles.addAll(listFiles(incomingFile, filters, filteringType, recurse, searchType));
                            recurse++;
                        }
                    } else if (Math.abs(recurse) < RECURSION_LIMIT && !fileEntry.isFile() && ((recurse <= -1) ||
                            (recurse > 0 && fileEntry.isDirectory()))) {
                        recurse--;
                        listedFiles.addAll(listFiles(fileEntry, filters, filteringType, recurse, searchType));
                        recurse++;
                    }
                } else {
                    if (Math.abs(recurse) < RECURSION_LIMIT && !fileEntry.isFile() && ((recurse <= -1) || (recurse >
                            0 && fileEntry.isDirectory()))) {
                        recurse--;
                        listedFiles.addAll(listFiles(filteredFiles, filters, filteringType, recurse, searchType));
                        recurse++;
                    }
                }

            }
        }
        return listedFiles;
    }

    public NotNullsLinkedHashSet<File> listPaths(File file) {
        NotNullsLinkedHashSet<File> files = new NotNullsLinkedHashSet<>();
        files.add(file);
        return listFiles(files, filters, DEFAULT_FILTER_TYPE, DEFAULT_RECURSION_LEVEL, SEARCH_INCLUDE_ONLY_PATH);
    }

    public NotNullsLinkedHashSet<File> listPaths(Collection<File> files) {
        return listFiles(files, filters, DEFAULT_FILTER_TYPE, DEFAULT_RECURSION_LEVEL, SEARCH_INCLUDE_ONLY_PATH);
    }

    /**
     * Metodo que busca archivos como recursos del class loader.  Ideal para leer archivos dentro de un .jar.
     *
     * @param fileName String con el nombre del archivo a leer
     * @param bundle   Properties a cargar con la data del archivo
     */
    public void loadBundleAsResource(String fileName, Properties bundle) {
        InputStream fis = null;
        try {
            // Si no encontramos el archivo, puede ser que estemos dentro de un .jar
            fis = ClassLoader.getSystemResourceAsStream(fileName);
            if (null == fis) {
                fis = SystemInfo.class.getResourceAsStream(fileName);
                if (null == fis) {
                    fis = Thread.currentThread().getContextClassLoader().getResourceAsStream(fileName);
                }
            }
            bundle.load(fis);
        } catch (Exception e) {
            log.error(e.getMessage());
        } finally {
            try {
                if (fis != null) {
                    fis.close();
                }
            } catch (Exception ignored) {
            }
        }
    }

    /**
     * Lee un archivo fileToLoad y lee su contenido en un String
     *
     * @param fileToLoad String con el nombre del archivo a leer
     * @return String con el contenido del archivo fileToLoad
     * @throws IOException If file could not be loaded into a String
     */
    public String loadFile(String fileToLoad)
            throws IOException {
        StringBuilder extractedData = new StringBuilder();
        try (FileReader fileDataReader = new FileReader(fileToLoad)) {
            BufferedReader br = new BufferedReader(fileDataReader);
            String readline;
            while ((readline = br.readLine()) != null) {
                extractedData.append(readline);
            }
            // log.debug("extractedData = " + extractedData);
            return extractedData.toString();
        } catch (IOException e) {
            throw new IOException();
        }
    }

    public void prepareFile(File file)
            throws IOException {
        if (file.exists()) {
            if (!file.canWrite()) {
                throw new IOException("File '" + file + "' cannot be written to");
            }
        } else {
            File parent = file.getParentFile();
            if (parent != null) {
                if (!parent.mkdirs() && !parent.isDirectory()) {
                    throw new IOException("Directory '" + parent + "' could not be created");
                }
            }
        }
    }

    public List<String> readFileTextToList(String file) {
        List<String> lines = new ArrayList<>();
        try {
            FileInputStream fstream = new FileInputStream(file);
            DataInputStream in = new DataInputStream(fstream);
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String strLine;
            while ((strLine = br.readLine()) != null) {
                lines.add(strLine);
            }
            in.close();
        } catch (Exception e) {
            log.error("Error: " + e.getMessage(), e);
        }
        return lines;
    }

    /**
     * Read a file into a string.
     *
     * @param toLoad File to be loaded
     * @return A String with the file content
     * @throws IOException If it is not possible to read a file into a String
     */
    public String readToString(File toLoad)
            throws IOException {
        if (toLoad == null)
            throw new IllegalArgumentException("File cannot be null");
        InputStream in = new FileInputStream(toLoad);
        return readToString(in);
    }

    /**
     * Read an input stream into a string.
     *
     * @param in Incoming stream
     * @return A String that represents the content of the incoming stream
     * @throws IOException If it is not possible to read the input stream and getting the string that represents it
     */
    public String readToString(InputStream in)
            throws IOException {
        try {
            BufferedInputStream bufferd = new BufferedInputStream(in);
            byte[] buffer = new byte[1024];
            StringBuilder builder = new StringBuilder();
            int index;
            while ((index = bufferd.read(buffer, 0, buffer.length)) > 0) {
                builder.append(new String(buffer, 0, index, StandardCharsets.UTF_8));
            }
            return builder.toString();
        } finally {
            in.close();
        }
    }

    /**
     * Recursively delete a directory and all sub-directories.
     *
     * @param directory The root directory for recursive deletion
     */
    public void recursivelyDeleteDirectory(File directory) {
        // Get the recursive iterator
        Iterator<File> it = getRecursiveIterator(directory, null);
        while (it.hasNext()) {
            it.next().delete();
        }
    }

    public void removeFilter(FileUtilsFilenameFilter filterImpl) {
        this.filters.remove(filterImpl);
    }

    /**
     * Take a file and replace the text from each line that match with an regular expression, with the incoming text,
     * returning a new temp file with the same incoming lines including the replacements
     *
     * @param file        Text file to be sanitized
     * @param regExp      Regular expression that indicates the pattern to be replaced
     * @param replacement The text for replace
     * @return A sanitized files with the same incoming lines including its replacements
     * @throws java.io.IOException If it is not possible to create a new file with data form incoming text according
     *                             to the criteria
     */

    public File sanitizeTextFileByReplacingTextThatMatchRegExp(File file, String regExp, String replacement)
            throws IOException {
        File result = File.createTempFile("temp_",
                String.valueOf(Calendar.getInstance().getTimeInMillis()),
                file.getParentFile());

        try (BufferedWriter out = new BufferedWriter(new FileWriter(result)); BufferedReader br = new BufferedReader
                (new FileReader(
                        file))) {
            String line = br.readLine();
            while (line != null) {
                out.write(line.replaceAll(regExp, replacement) + "\n");
                line = br.readLine();
            }
        }
        return result;
    }

    /**
     * Take a file and apply a filter according to a regular expression, returning a new temp file with just those
     * lines that matched the criteria
     *
     * @param file   Text file to be sanitized
     * @param regExp Regular expression that indicates which lines are selected to stay
     * @return A sanitized file with just those lines that matched the related regExp
     * @throws java.io.IOException If it is not possible to create a new file with data form incoming text according
     *                             to the criteria
     */

    public File sanitizeTextFileKeepingLinesThatMatchRegExp(File file, String regExp)
            throws IOException {
        File result = File.createTempFile("temp_",
                String.valueOf(Calendar.getInstance().getTimeInMillis()),
                file.getParentFile());
        Pattern r = Pattern.compile(regExp);
        Matcher m;
        try (BufferedWriter out = new BufferedWriter(new FileWriter(result)); BufferedReader br = new BufferedReader
                (new FileReader(
                        file))) {
            String line = br.readLine();
            while (line != null) {
                m = r.matcher(line);
                if (m.find()) {
                    out.write(line + "\n");
                }
                line = br.readLine();
            }
        }
        return result;
    }

    /**
     * Revisa si un archivo fileName existe dentro del directorio folderName No
     * valida de forma recursiva. Para ello usar searchForClassOnDirRec.
     *
     * @param folderName String con el nombre del directorio a revisar
     * @param fileName   String con el nombre del archivo a buscar
     * @return boolean true si el archivo fileName existe dentro del directorio
     * folderName, false en otro caso
     */
    public boolean searchForClassOnDir(String fileName, String folderName) {
        try {
            // Se lee la carpeta raiz y se crea un File por cada xml
            File folder = new File(folderName);
            File[] files = folder.listFiles();

            File file;
            // log.debug("lista.size " + files.length);

            for (File file1 : files != null ? files : new File[0]) {
                file = file1;
                if (fileName.equals(file.getName())) {
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            log.error("Error buscando archivo '" + fileName + "' en directorio '" + folderName + "'.", e);
            return false;
        }
    }

    /**
     * This method searchs for a class inside a given directory. It prints all
     * the jars and filenames that contains the given class. TODO: Al encontrar
     * un resultado, se imprime la traza saliendo en todos los niveles
     *
     * @param fileName   String with the name of the class we are looking for.
     * @param folderName String with the path of the directory we are going to check.
     * @param onlyOne    boolean true if only the first positive-result will be
     *                   returned, false if you want all found classes.
     */
    public void searchForClassOnDirRec(String fileName, String folderName, boolean onlyOne) {
        // return searchForClassOnDirRec(fileName, folderName, onlyOne, 0);
        findFileOnDirRecursive(fileName, folderName, onlyOne, 0);
    }

    public boolean searchForClassOnDirRec(String fileName, String folderName, boolean onlyOne, int tab) {
        return findFileOnDirRecursive(fileName, folderName, onlyOne, tab) != null;
    }

    private static class FileUtilsFilenameFilterCompare implements Comparator {
        public int compare(Object o1, Object o2) {
            String path1 = ((File) o1).getPath();
            String path2 = ((File) o2).getPath();
            return path1.compareTo(path2);
        }
    }

    private static class FileUtilsFilenameFilterCompareIgnoreCase implements Comparator {
        public int compare(Object o1, Object o2) {
            String path1 = ((File) o1).getPath();
            String path2 = ((File) o2).getPath();
            return path1.compareToIgnoreCase(path2);
        }
    }
}
