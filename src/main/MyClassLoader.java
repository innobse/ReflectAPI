package main;


import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Created by bse71 on 15.02.2017.
 */
public class MyClassLoader extends ClassLoader {
    private final String jarLocal = "down.jar";
    private HashMap classes = new HashMap(); //used to cache already defined classes

    public MyClassLoader() {
        super(MyClassLoader.class.getClassLoader()); //calls the parent class loader's constructor
    }

    public MyClassLoader(ClassLoader parent) {
        super(parent); //calls the parent class loader's constructor
    }

    public Class loadClass(String className, String jarUrl) throws ClassNotFoundException {
        return findClass(className, jarUrl);
    }

    public Class findClass(String className, String jarUrl) {

        Class result = null;

        result = (Class) classes.get(className); //checks in cached classes
        if (result != null) {
            return result;
        }

        try {
            return findSystemClass(className);
        } catch (Exception e) {}

        try {
            result = getClassFromJar(className, jarUrl);
            classes.put(className, result);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    private JarFile downloadJAR(URL jarURL, String jarName) throws IOException {
        InputStream is = null;
        FileOutputStream jarWriter = new FileOutputStream(jarName);
        JarFile jar = null;

        is = jarURL.openStream();
        int nextValue = 0;

        nextValue = is.read();
        while (nextValue != -1) {
            jarWriter.write(nextValue);
            nextValue = is.read();
        }

        jarWriter.close();
        jar = new JarFile(new File(jarName));
        return jar;
    }

    private Class getClassFromJar(String className, String jarUrl) throws MalformedURLException, IOException{
        InputStream is = null;
        URL classUrl = null;
        classUrl = new URL(jarUrl);
        JarFile jar = downloadJAR(classUrl, jarLocal);
        JarEntry entry = jar.getJarEntry(className + ".class");
        is = jar.getInputStream(entry);
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        int nextValue = 0;

        nextValue = is.read();
        while (-1 != nextValue) {
            byteStream.write(nextValue);
            nextValue = is.read();
        }
        byte classByte[] = new byte[0];
        classByte = byteStream.toByteArray();
        return defineClass(className, classByte, 0, classByte.length, null);
    }


}

