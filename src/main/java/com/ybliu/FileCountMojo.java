package com.ybliu;

/**
 * Created by linlinyeyu on 2018/1/11.
 */

import org.apache.maven.model.Resource;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @goal filecount
 * @phase process-sources
 */
public class FileCountMojo extends AbstractMojo{
    private static final String[] DEFAULT_FILES = {"java","xml","properties","sql"};
    private static final String[] RATIOS_DEFAULT = {"1.0","0.25","0.25","0.25"};
    private static final String DOT = ".";

    /**
     * @parameter expression="${project.basedir}"
     * @required
     * @readonly
     */
    private File baseDir;
    /**
     * @parameter expression="${project.build.sourceDirectory}"
     * @required
     * @readonly
     */
    private File sourceDir;
    /**
     * @parameter expression="${project.build.testSourceDirectory}"
     * @required
     * @readonly
     */
    private File testSourceDir;
    /**
     * @parameter expression="${project.resources}"
     * @required
     * @readonly
     */
    private List<Resource> resources;
    /**
     * @parameter expression="${project.testResources}"
     * @required
     * @readonly
     */
    private List<Resource> testResource;
    /**
     * @parameter
     */
    private String[] includes;
    /**
     * @parameter
     */
    private String[] ratios;
    private Map<String,Double> ratioMap = new HashMap<String, Double>();
    private long realTotal;
    private long fakeTotal;

    public void execute() throws MojoExecutionException, MojoFailureException {
        initRatioMap();
        try {
            countDir(sourceDir);
            countDir(testSourceDir);
            for (Resource resource:resources){
                countDir(new File(resource.getDirectory()));
            }
            for (Resource resource:testResource){
                countDir(new File(resource.getDirectory()));
            }
        } catch (IOException e) {
            throw new MojoExecutionException("unable to count lines of code",e);
        }

        getLog().info("TOTAL LINES:"+fakeTotal+"("+realTotal+")");
    }

    private void initRatioMap() throws MojoExecutionException {
        if (includes == null || includes.length == 0){
            includes = DEFAULT_FILES;
            ratios = RATIOS_DEFAULT;
        }
        if (ratios == null || ratios.length == 0){
            ratios = new String[includes.length];
            for (int i = 0;i<includes.length;i++){
                ratios[i] = "1.0";
            }
        }

        if (includes.length != ratios.length){
            throw new MojoExecutionException("pom.xml error: the length of includes is inconsistent with ratios!");
        }
        ratioMap.clear();
        for (int i = 0;i<includes.length;i++){
            ratioMap.put(includes[i].toLowerCase(),Double.parseDouble(ratios[i]));
        }
    }

    private void countDir(File dir) throws IOException {
        if (! dir.exists()){
            return;
        }
        List<File> collected = new ArrayList<File>();
        collectFiles(collected,dir);
        int readLine = 0;
        int fakeLine = 0;
        for (File file:collected){
            int[] line = countLine(file);
            readLine += line[0];
            fakeLine += line[1];
        }

        String path = dir.getAbsolutePath().substring(baseDir.getAbsolutePath().length());
        StringBuilder info = new StringBuilder().append(path).append(" : ").append(fakeLine).append(" ("+readLine+")").append(" lines of code in ").append(collected.size()).append(" files ");
        getLog().info(info.toString());
    }

    private void collectFiles(List<File> collected,File file){
        if (file.isFile()){
            if (isFileTypeInclude(file)){
                collected.add(file);
            }
        }else {
            for (File files : file.listFiles()){
                collectFiles(collected,files);
            }
        }
    }

    private int[] countLine(File file) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(file));
        int readLine = 0;
        try {
            while (reader.ready()){
                reader.readLine();
                readLine ++;
            }
        }finally {
            reader.close();
        }
        int fakeLine = (int)(readLine * getRatio(file));
        realTotal += readLine;
        fakeTotal += fakeLine;

        StringBuilder info = new StringBuilder().append(file.getName()).append(" : ").append(fakeLine).append("("+readLine+")").append("lines");
        getLog().debug(info.toString());
        return new int[]{readLine,fakeLine};
    }

    private double getRatio(File file){
        double ratio = 0;
        String type = getFileType(file);
        if (ratioMap.containsKey(type)){
            ratio = ratioMap.get(type);
        }
        return ratio;
    }

    private boolean isFileTypeInclude(File file){
        boolean result = false;
        String fileType = getFileType(file);
        if (fileType != null && ratioMap.keySet().contains(fileType.toLowerCase())){
            result = true;
        }
        return result;
    }

    private String getFileType(File file){
        String result = null;
        String fname = file.getName();
        int index = fname.lastIndexOf(DOT);
        if (index > 0){
            String type = fname.substring(index+1);
            result  = type.toLowerCase();
        }
        return result;
    }
}
