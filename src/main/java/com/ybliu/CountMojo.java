package com.ybliu;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

/**
 * @goal count
 * @phase process-sources
 */
public class CountMojo extends AbstractMojo {
    /**
     * @parameter expression="${name}"
     */
    private String name = "yeyu";

    public void execute() throws MojoExecutionException, MojoFailureException {
        getLog().info("测试插件"+name);
    }
}
