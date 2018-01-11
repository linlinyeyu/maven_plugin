package com.ybliu;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

/**
 * @goal count
 * @phase process-sources
 */
public class CountMojo extends AbstractMojo {
    private static final String[] INCLUDES_DEFAULT = {"java","xml","sql","properties"};
    private static final String[] RATIOS_DEFAULT = {"1.0","0.25","0.25","0.25"};
    private static final String DOT = ".";
    
    public void execute() throws MojoExecutionException, MojoFailureException {

    }
}
