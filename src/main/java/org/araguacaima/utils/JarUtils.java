package org.araguacaima.utils;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.jar.*;

/**
 * Clase utilitaria para manipular archivos .jar <p>
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

    private void add(File source,
                     JarOutputStream target,
                     int offsetExclusionDirectory,
                     String jarOutputFullPath,
                     String rootPath)
            throws IOException {
        BufferedInputStream in = null;
        try {
            if (offsetExclusionDirectory == 0) {
                if (source.isDirectory()) {
                    JarEntry entry = new JarEntry(StringUtils.difference(rootPath, source.getPath()) + "/");
                    entry.setTime(source.lastModified());
                    target.putNextEntry(entry);
                    target.closeEntry();
                    for (File nestedFile : source.listFiles()) {
                        add(nestedFile, target, offsetExclusionDirectory, jarOutputFullPath, rootPath);
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
                File[] nestedFile = source.listFiles();
                if (nestedFile != null) {
                    for (File aNestedFile : nestedFile) {
                        if (!aNestedFile.getPath().equals(jarOutputFullPath)) {
                            offsetExclusionDirectory--;
                            add(aNestedFile, target, offsetExclusionDirectory, jarOutputFullPath, rootPath);
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
     * Generates a new jar file appending its Manifest and a set of files contained on an incoming directory
     *
     * @param inputDirectory    The incoming directory where the files resides
     * @param jarOutputFullPath The output full path jar name
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
     * This method searchs for a class inside any jar.
     *
     * @param className String with the name of the class we are looking for.
     * @param jarName   String with the name of the jar file we are going to check.
     * @see org.araguacaima.utils.FileUtils searchForClassOnDirRec(...) if you don't know the jar's name
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
                    System.out.println("Class '" + innerName + "' found on jar '" + jarName + "'.");
                    return true;
                }
            }
            // log.debug("Class not found on jar '" + jarName + "'.");
        } catch (Exception e) {
            System.err.println("Error looking for class '" + className + "' on jar '" + jarName + "'");
            e.printStackTrace();
        }
        return false;
    }

    public ArrayList listClassesOnJar(String jarName) {
        ArrayList result = new ArrayList();
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

}
