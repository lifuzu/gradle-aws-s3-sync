package com.cloudcar.gradle.plugin.aws.s3

import org.jets3t.service.model.S3Object
import org.gradle.api.DefaultTask
import org.gradle.api.PathValidation
import org.gradle.api.tasks.TaskAction
import org.jets3t.service.Constants
import org.jets3t.service.Jets3tProperties
import org.jets3t.service.impl.rest.httpclient.RestS3Service
import org.jets3t.service.security.AWSCredentials

/**
 * Main task class for the plugin
 *
 * @author
 */
class S3Delete extends DefaultTask {

    def accessKey

    def secretKey

    def quiet

    def noProgress

    def force

    def configFile

    def bucket

    ACL acl = ACL.Private

    ReportLevel reportLevel = ReportLevel.All

    private String originalSourcePath

    private File sourceDir

    private String destination

    void from(sourcePath) {
        originalSourcePath = sourcePath
        sourceDir = project.file(sourcePath)
    }

    void delete(destinationPath) {
        destination = destinationPath.toString()
    }

    @TaskAction
    def delete() {
        def awsCredentials = new AWSCredentials(accessKey, secretKey)
        def s3Service = new RestS3Service(awsCredentials)

        //Jets3tProperties properties = loadProperties()

        println(destination)
        println(originalSourcePath)
        if (destination) {
            S3Object object = new S3Object(destination)
            s3Service.deleteObject(originalSourcePath, object.getKey())
        } else {
            logger.error("No file found as destination.")
        }

    }

    Jets3tProperties loadProperties() {
        Jets3tProperties myProperties =
            Jets3tProperties.getInstance(Constants.JETS3T_PROPERTIES_FILENAME);

        // Read the Synchronize properties file from the classpath
        File synchronizeProperties = project.file(configFile, PathValidation.FILE)
        if (synchronizeProperties.canRead()) {
            synchronizeProperties.withInputStream {
                myProperties.loadAndReplaceProperties(it, configFile + " in the user config")
            }
        } else {
            throw new IllegalStateException("the config file cannot be read : " + synchronizeProperties.absolutePath)
        }
        return myProperties
    }
}