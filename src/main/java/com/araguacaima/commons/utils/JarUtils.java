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

import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.jar.*;
import java.util.zip.ZipEntry;

/**
 * Clase utilitaria para manipular archivos .jar <br>
 * Clase: JarUtil.java <br>
 *
 * @author Alejandro Manuel Méndez Araguacaima (AMMA)
 * Changes:<br>
 * <ul>
 * <li> 2014-11-26 (AMMA)  Creacion de la clase. </li>
 * </ul>
 */
@Component
public class JarUtils {

    private static final Logger log = LoggerFactory.getLogger(JarUtils.class);

    public JarUtils() {

    }

    @SuppressWarnings("SameReturnValue")
    public boolean appendFileToJar(File fileToAdd, String jarFileStr) throws IOException {
        JarFile jar = new JarFile(jarFileStr);
        JarOutputStream jarOutputStream = new JarOutputStream(new FileOutputStream(jarFileStr));
        ZipEntry entry = new ZipEntry(fileToAdd.getName());
        jarOutputStream.putNextEntry(entry);
        FileInputStream fileInputStream = new FileInputStream(fileToAdd);
        byte[] buf = new byte[1024];
        int bytesRead;
        // Read the input file by chucks of 1024 bytes
        // and write the read bytes to the zip stream
        while ((bytesRead = fileInputStream.read(buf)) > 0) {
            jarOutputStream.write(buf, 0, bytesRead);
        }
        // close JarEntry to store the stream to the file
        jarOutputStream.closeEntry();
        jarOutputStream.close();
        return true;
    }

    public void add(File source,
                    JarOutputStream target,
                    int offsetExclusionDirectory,
                    String jarOutputFullPath,
                    String rootPath)
            throws IOException {
        BufferedInputStream in = null;
        try {
            final File[] files = source.listFiles();
            if (offsetExclusionDirectory == 0) {
                if (source.isDirectory()) {
                    JarEntry entry = new JarEntry(StringUtils.difference(rootPath, source.getPath()) + "/");
                    entry.setTime(source.lastModified());
                    target.putNextEntry(entry);
                    target.closeEntry();
                    if (files != null) {
                        for (File nestedFile : files) {
                            add(nestedFile, target, offsetExclusionDirectory, jarOutputFullPath, rootPath);
                        }
                    }
                    return;

                }
                String newEntry = StringUtils.difference(rootPath, source.getPath());
                JarEntry entry = new JarEntry(newEntry);
                entry.setTime(source.lastModified());
                target.putNextEntry(entry);
                in = new BufferedInputStream(new FileInputStream(source));

                byte[] buffer = new byte[1024];
                while (true) {
                    int count = in.read(buffer);
                    if (count == -1) {
                        break;
                    }
                    target.write(buffer, 0, count);
                }
                target.closeEntry();
            } else {
                if (files != null) {
                    for (File aNestedFile : files) {
                        if (!aNestedFile.getPath().equals(jarOutputFullPath)) {
                            offsetExclusionDirectory--;
                            add(aNestedFile, target, offsetExclusionDirectory, jarOutputFullPath, source.getPath() + File.separator);
                        }
                    }
                }
            }
        } finally {
            if (in != null) {
                in.close();
            }
        }
    }

    /**
     * This method searchs for a class inside any jar.
     *
     * @param className String with the name of the class we are looking for.
     * @param jarName   String with the name of the jar file we are going to check.
     * @return True if the incoming class name is contained inside jar file
     * @see com.araguacaima.commons.utils.FileUtils searchForClassOnDirRec(...) if you don't know the jar's name
     */
    public boolean findClassOnJar(String className, String jarName) {
        try {
            // log.debug("Looking for class '" + className + "' on jar '" + jarName + "'.");
            String className2u = "/" + className + "."; // "/"? sure?
            // log.debug("className2u = " + className2u);

            JarFile jar = new JarFile(jarName);
            Enumeration innerFiles = jar.entries();
            while (innerFiles.hasMoreElements()) {
                JarEntry inner = (JarEntry) innerFiles.nextElement();
                String innerName = inner.getName();
                // log.debug("innerName = " + innerName);
                if (innerName.contains(className2u)) {
                    log.debug("Class '" + innerName + "' found on jar '" + jarName + "'.");
                    return true;
                }
            }
            // log.debug("Class not found on jar '" + jarName + "'.");
        } catch (Exception e) {
            log.error("Error looking for class '" + className + "' on jar '" + jarName + "'");
            log.error(e.getMessage());
        }
        return false;
    }

    /**
     * Generates a new jar file appending its Manifest and a set of files contained on an incoming directory
     *
     * @param inputDirectory    The incoming directory where the files resides
     * @param jarOutputFullPath The output full path jar name
     * @throws IOException If it could not be created any jar file from provided directory
     */

    public void generateJarFromDirectory(File inputDirectory, File jarOutputFullPath)
            throws IOException {
        generateJarFromDirectory(inputDirectory, jarOutputFullPath, 1);
    }

    /**
     * Generates a new jar file appending its Manifest and a set of files contained on an incoming directory
     *
     * @param inputDirectory    The incoming directory where the files resides
     * @param jarOutputFullPath The output full path jar name
     * @throws IOException If it could not be created any jar file from provided directory
     */

    public void generateJarFromDirectory(String inputDirectory, String jarOutputFullPath)
            throws IOException {
        generateJarFromDirectory(inputDirectory, jarOutputFullPath, 1);
    }

    /**
     * Generates a new jar file appending its Manifest and a set of files contained on an incoming directory
     *
     * @param inputDirectory           The incoming directory where the files resides
     * @param jarOutputFullPath        The output full path jar name
     * @param offsetExclusionDirectory The offset deep tree of the directory for excluding
     * @throws IOException If it could not be created any jar file from provided directory
     */

    public void generateJarFromDirectory(File inputDirectory, File jarOutputFullPath, int offsetExclusionDirectory)
            throws IOException {
        generateJarFromDirectory(inputDirectory.getCanonicalPath(), jarOutputFullPath.getCanonicalPath(), offsetExclusionDirectory);
    }

    /**
     * Generates a new jar file appending its Manifest and a set of files contained on an incoming directory
     *
     * @param inputDirectory           The incoming directory where the files resides
     * @param jarOutputFullPath        The output full path jar name
     * @param offsetExclusionDirectory The offset deep tree of the directory for excluding
     * @throws IOException If it could not be created any jar file from provided directory
     */

    public void generateJarFromDirectory(String inputDirectory, String jarOutputFullPath, int offsetExclusionDirectory)
            throws IOException {
        Manifest manifest = new Manifest();
        String MANIFEST_VERSION = "1.0";
        manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, MANIFEST_VERSION);
        JarOutputStream target = new JarOutputStream(new FileOutputStream(jarOutputFullPath), manifest);
        add(new File(inputDirectory),
                target,
                offsetExclusionDirectory,
                jarOutputFullPath,
                FilenameUtils.getFullPath(jarOutputFullPath));
        target.close();
    }

    public ArrayList<String> listClassesOnJar(String jarName) {
        ArrayList<String> result = new ArrayList<>();
        try {
            log.debug("Looking for classes on jar '" + jarName + "'.");
            //           String pattern = ".\\$.";
            //           String pattern2 = ".[\\$].";
            JarFile jar = new JarFile(jarName);
            Enumeration innerFiles = jar.entries();
            while (innerFiles.hasMoreElements()) {
                JarEntry inner = (JarEntry) innerFiles.nextElement();
                //               log.debug("inner: "+inner.getName() + " aplicarle pattern1: " + inner.getName()
                // .matches(pattern)+ " pattern2: " + inner.getName().matches(pattern2));
                //              log.debug("patter 1 : "+"p$p".matches(pattern) + " pattern2: " + "p$p".matches
                // (pattern2));
                if (inner.getName().endsWith(".class")) {
                    String innerName = inner.getName();
                    //                   log.debug("Class '" + innerName + "' found on jar '" + jarName + "'.");
                    result.add(innerName);
                }
            }
            // log.debug("Class not found on jar '" + jarName + "'.");
        } catch (Exception e) {
            log.error("Error looking on jar '" + jarName + "'");
        }
        return result;
    }

    public void unZip(String destinationDir, String jarPath) throws IOException {
        File file = new File(jarPath);
        JarFile jar = new JarFile(file);

        // fist get all directories,
        // then make those directory on the destination Path
        for (Enumeration<JarEntry> enums = jar.entries(); enums.hasMoreElements(); ) {
            JarEntry entry = enums.nextElement();

            String fileName = destinationDir + File.separator + entry.getName();
            File f = new File(fileName);

            if (fileName.endsWith("/")) {
                f.mkdirs();
            }

        }

        //now create all files
        for (Enumeration<JarEntry> enums = jar.entries(); enums.hasMoreElements(); ) {
            JarEntry entry = enums.nextElement();

            String fileName = destinationDir + File.separator + entry.getName();
            File f = new File(fileName);

            if (!fileName.endsWith("/")) {
                InputStream is = jar.getInputStream(entry);
                FileOutputStream fos = new FileOutputStream(f);

                // write contents of 'is' to 'fos'
                while (is.available() > 0) {
                    fos.write(is.read());
                }

                fos.close();
                is.close();
            }
        }
    }

}
