/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.dromara.dynamictp.jvmti;

import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

/**
 * NativeUtil related
 *
 * @author yanhom
 * @since 1.0.0
 **/
public class NativeUtil {

    private static final String NATIVE_FOLDER_PATH_PREFIX = "dtp_native_";

    private static File temporaryDir;

    private NativeUtil() {
    }

    public static void loadLibraryFromJar(String filename) throws IOException {

        if (StringUtils.isBlank(filename)) {
            throw new IllegalArgumentException("The filename cannot be null");
        }

        if (temporaryDir == null) {
            temporaryDir = createTempDirectory(NATIVE_FOLDER_PATH_PREFIX);
            temporaryDir.deleteOnExit();
        }

        File temp = new File(temporaryDir, filename);
        try (InputStream is = NativeUtil.class.getClassLoader().getResourceAsStream(filename)) {
            assert is != null;
            Files.copy(is, temp.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            Files.delete(temp.toPath());
            throw e;
        } catch (NullPointerException e) {
            Files.delete(temp.toPath());
            throw new FileNotFoundException("File " + filename + " was not found inside JAR.");
        }

        try {
            System.load(temp.getAbsolutePath());
        } finally {
            temp.deleteOnExit();
        }
    }

    private static File createTempDirectory(String prefix) throws IOException {
        String tempDir = System.getProperty("java.io.tmpdir");
        File generatedDir = new File(tempDir, prefix + System.nanoTime());
        if (!generatedDir.mkdir()) {
            throw new IOException("Failed to create temp directory " + generatedDir.getName());
        }
        return generatedDir;
    }
}
