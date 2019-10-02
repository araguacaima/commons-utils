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

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

@SuppressWarnings("ResultOfMethodCallIgnored")

public class ZipUtils {

    private static final ZipUtils INSTANCE = ZipUtils.getInstance();
    ;

    private ZipUtils() {
        if (INSTANCE != null) {
            throw new IllegalStateException("Already instantiated");
        }
    }

    public static ZipUtils getInstance() {
        return INSTANCE;
    }

    public Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException("Cannot clone instance of this class");
    }

    public boolean isValid(final File file) {
        try (ZipFile zipfile = new ZipFile(file)) {
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * Unzip it
     *
     * @param zipFile      input zip file
     * @param outputFolder zip file output folder
     */
    public void unZip(File zipFile, File outputFolder) {

        byte[] buffer = new byte[1024];

        try {

            //create output directory is not exists
            if (!outputFolder.exists() && !outputFolder.isDirectory()) {
                outputFolder.mkdir();
            }

            //get the zip file content
            ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile));
            //get the zipped file list entry
            ZipEntry ze = zis.getNextEntry();

            while (ze != null) {

                String fileName = ze.getName();
                File newFile = new File(outputFolder + File.separator + fileName);

                //create all non exists folders
                //else you will hit FileNotFoundException for compressed folder
                if (ze.isDirectory()) {
                    FileUtils.forceMkdir(newFile);
                } else {
                    FileUtils.forceMkdir(new File(newFile.getParent()));

                    FileOutputStream fos = new FileOutputStream(newFile);

                    int len;
                    while ((len = zis.read(buffer)) > 0) {
                        fos.write(buffer, 0, len);
                    }

                    fos.close();
                }
                ze = zis.getNextEntry();
            }

            zis.closeEntry();
            zis.close();

        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

}
