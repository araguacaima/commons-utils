package com.araguacaima.commons.utils;

import org.apache.commons.io.FileUtils;
import org.joor.Reflect;
import org.joor.ReflectException;

import javax.tools.*;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class CompilerUtils {

    private static final JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
    private static final CompilerUtils INSTANCE = CompilerUtils.getInstance();
    private JsonUtils jsonUtils = new JsonUtils();
    private MapUtils mapUtils = MapUtils.getInstance();

    private CompilerUtils() {
        if (INSTANCE != null) {
            throw new IllegalStateException("Already instantiated");
        }
    }
    ;

    public static CompilerUtils getInstance() {
        return INSTANCE;
    }

    public Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException("Cannot clone instance of this class");
    }

    @FunctionalInterface
    private interface ThrowingBiFunction<T, U, R> {
        R apply(T t, U u) throws Exception;
    }

    @SuppressWarnings("unused")
    public static class FilesCompiler<T extends ClassLoader> {

        private T classLoader;

        public FilesCompiler(T classLoader) {
            this.classLoader = classLoader;
        }

        public Set<Class<?>> compile(List<String> options, File sourceCodeDirectory, File compiledClassesDirectory, Collection<File> files) throws IOException {
            List<CharSequenceJavaFileObject> files_ = new ArrayList<>();
            for (File file : files) {
                PackageClassUtils packageClassUtils = PackageClassUtils.instance(sourceCodeDirectory, file, ".java");
                String content = org.apache.commons.io.FileUtils.readFileToString(file, StandardCharsets.UTF_8);
                String className = packageClassUtils.getFullyQualifiedClassName();
                files_.add(new CharSequenceJavaFileObject(className, content));
            }
            return compile(options, files_, compiledClassesDirectory);
        }

        public Set<Class<?>> compile(List<String> options, List<CharSequenceJavaFileObject> files, File compiledClassesDirectory) {
            try {
                ClassFileManager fileManager = new ClassFileManager(compiler.getStandardFileManager(null, null, null));
                StringWriter out = new StringWriter();
                DiagnosticCollector<javax.tools.JavaFileObject> diagnostics = new DiagnosticCollector<>();
                JavaCompiler.CompilationTask task = compiler.getTask(out, fileManager, diagnostics, options, null, files);

                if (!task.call()) {
                    for (Diagnostic d : diagnostics.getDiagnostics()) {
                        String err = String.format("Compilation error: Line %d - %s%n", d.getLineNumber(), d.getMessage(null));
                        System.err.print(err);
                    }
                }

                if (fileManager.isEmpty()) {
                    throw new ReflectException("Compilation error: " + out);
                }

                Set<Class<?>> resultList = new LinkedHashSet<>();
                for (CharSequenceJavaFileObject file : files) {
                    String className = PackageClassUtils.instance(file.getName()).getFullyQualifiedClassName();
                    try {
                        resultList.add(classLoader.loadClass(className));
                    } catch (Throwable ignored) {
                        resultList.add(fileManager.loadAndReturnMainClass(className, (name, bytes) -> {
                            Class clazz = Reflect.on(classLoader).call("defineClass", name, bytes, 0, bytes.length).get();
                            PackageClassUtils packageClassUtils = PackageClassUtils.instance(clazz.getName());
                            String packageName = packageClassUtils.getPackageName();
                            File classFile = new File(com.araguacaima.commons.utils.FileUtils.makeDirFromPackageName(compiledClassesDirectory, packageName), packageClassUtils.getClassName() + ".class");
                            FileUtils.writeByteArrayToFile(classFile, bytes);
                            return clazz;
                        }));
                    }
                }
                return resultList;
            } catch (ReflectException e) {
                throw e;
            } catch (Exception e) {
                throw new ReflectException("Error while compiling classes: " + files, e);
            }
        }

        public Set<Class<?>> compile(File sourceCodeDirectory, File compiledClassesDirectory, Collection<File> listFiles) throws IOException, URISyntaxException {
            List<String> options = new ArrayList<>(Arrays.asList("-d", compiledClassesDirectory.getCanonicalPath()));
            if (!options.contains("-classpath")) {
                StringBuilder classpath = new StringBuilder();
                String separator = System.getProperty("path.separator");
                String prop = System.getProperty("java.class.path");
                if (prop != null && !"".equals(prop)) {
                    classpath.append(prop);
                }
                if (classLoader instanceof URLClassLoader) {
                    for (URL url : ((URLClassLoader) classLoader).getURLs()) {
                        if (classpath.length() > 0) {
                            classpath.append(separator);
                        }
                        if ("file".equals(url.getProtocol())) {
                            classpath.append(new File(url.toURI()));
                        }
                    }
                }
                options.addAll(Arrays.asList("-classpath", classpath.toString()));
            }
            return compile(options, sourceCodeDirectory, compiledClassesDirectory, listFiles);
        }

        public T getClassLoader() {
            return classLoader;
        }

        public void setClassLoader(T classLoader) {
            this.classLoader = classLoader;
        }

        private static final class JavaFileObject extends SimpleJavaFileObject {
            final ByteArrayOutputStream os = new ByteArrayOutputStream();

            JavaFileObject(String name, Kind kind) {
                super(URI.create("string:///" + name.replace('.', '/') + kind.extension), kind);
            }

            byte[] getBytes() {
                return os.toByteArray();
            }

            @Override
            public OutputStream openOutputStream() {
                return os;
            }
        }

        private static final class ClassFileManager extends ForwardingJavaFileManager<StandardJavaFileManager> {
            private final Map<String, JavaFileObject> fileObjectMap;
            private Map<String, byte[]> classes;

            ClassFileManager(StandardJavaFileManager standardManager) {
                super(standardManager);
                fileObjectMap = new HashMap<>();
            }

            @Override
            public JavaFileObject getJavaFileForOutput(
                    Location location,
                    String className,
                    JavaFileObject.Kind kind,
                    FileObject sibling) {
                JavaFileObject result = new JavaFileObject(className, kind);
                fileObjectMap.put(className, result);
                return result;
            }

            boolean isEmpty() {
                return fileObjectMap.isEmpty();
            }

            Map<String, byte[]> classes() {
                if (classes == null) {
                    classes = new HashMap<>();

                    for (Map.Entry<String, JavaFileObject> entry : fileObjectMap.entrySet()) {
                        classes.put(entry.getKey(), entry.getValue().getBytes());
                    }
                }

                return classes;
            }

            Class<?> loadAndReturnMainClass(String mainClassName, ThrowingBiFunction<String, byte[], Class<?>> definer) throws Exception {
                Class<?> result = null;

                for (Map.Entry<String, byte[]> entry : classes().entrySet()) {
                    Class<?> c = definer.apply(entry.getKey(), entry.getValue());
                    if (mainClassName.equals(entry.getKey()))
                        result = c;
                }

                return result;
            }
        }

        private static final class CharSequenceJavaFileObject extends SimpleJavaFileObject {
            final CharSequence content;

            CharSequenceJavaFileObject(String className, CharSequence content) {
                super(URI.create("string:///" + className.replace('.', '/') + Kind.SOURCE.extension), Kind.SOURCE);
                this.content = content;
            }

            @Override
            public CharSequence getCharContent(boolean ignoreEncodingErrors) {
                return content;
            }
        }

        private static final class ReloadableClassLoader extends URLClassLoader {

            ReloadableClassLoader(ClassLoader parent) throws IOException {
                super(new URL[]{}, parent);
                Enumeration<URL> resources = parent.getResources(".");
                while (resources.hasMoreElements()) {
                    this.addURL(resources.nextElement());
                }
            }

            Class loadClass(String name, URL myUrl) {

                try {
                    URLConnection connection = myUrl.openConnection();
                    InputStream input = connection.getInputStream();
                    ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                    int data = input.read();

                    while (data != -1) {
                        buffer.write(data);
                        data = input.read();
                    }

                    input.close();

                    byte[] classData = buffer.toByteArray();

                    return defineClass(name, classData, 0, classData.length);

                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }

            Class loadClass(String name, byte[] classData) {
                return defineClass(name, classData, 0, classData.length);
            }
        }

    }
}
