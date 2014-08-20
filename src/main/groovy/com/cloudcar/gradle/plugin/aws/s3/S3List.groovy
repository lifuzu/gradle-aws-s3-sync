package com.cloudcar.gradle.plugin.aws.s3

import org.gradle.api.DefaultTask
import org.gradle.api.PathValidation
import org.gradle.api.tasks.TaskAction
import org.jets3t.service.Constants
import org.jets3t.service.Jets3tProperties
import org.jets3t.service.S3ServiceException
import org.jets3t.service.impl.rest.httpclient.RestS3Service
import org.jets3t.service.model.S3Object
import org.jets3t.service.security.AWSCredentials

import com.amazonaws.services.s3.model.ListObjectsRequest

/**
 * Main task class for the plugin
 *
 * @author
 */
class S3List extends DefaultTask {

    def accessKey

    def secretKey

    def configFile

    ACL acl = ACL.Private

    ReportLevel reportLevel = ReportLevel.All

    private String originalSourcePath

    private File sourceDir

    private String destination

    private String delimiter = '/'

    void from(sourcePath) {
        originalSourcePath = sourcePath
        sourceDir = project.file(sourcePath)
    }

    void into(destinationPath) {
        destination = destinationPath.toString()
    }

    @TaskAction
    def list() {
        def awsCredentials = new AWSCredentials(accessKey, secretKey)
        def s3Service = new RestS3Service(awsCredentials)

        //Jets3tProperties properties = loadProperties()

        println(originalSourcePath)

        try {
            S3Object[] objects;
            if (destination) {
                if (!destination.endsWith(delimiter)) destination += delimiter
                println(destination)
                ListObjectsRequest listObjectsRequest = new ListObjectsRequest()
                        .withBucketName(originalSourcePath).withPrefix(destination)
                        .withDelimiter(delimiter);
                objects = s3Service.listObjects(listObjectsRequest);
                for ( S3Object object in objects.getCommonPrefixes() ) {
                    println(object.getKey() + " (" + object.getContentLength() + " bytes)")
                }
            } else {
                objects = s3Service.listObjects(originalSourcePath);
            }

            for (int i = 0; i < objects.length; i++) {
                println(objects[i].getKey() + " (" + objects[i].getContentLength() + " bytes)");
            }
        } catch (S3ServiceException e) {
            if (e.getCause() instanceof IOException) {
                throw (IOException) e.getCause();
            }
            throw new Exception(e);
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