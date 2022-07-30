package com.AWS;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.SdkClientException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.greengrass.model.Function;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.CreateBucketRequest;
import com.amazonaws.services.s3.model.GetBucketLocationRequest;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ListObjectsV2Result;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.filesync.FileSystemChecker;

import java.io.Console;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.codec.digest.DigestUtils;



// public ProfileCredentialsProvider(String profilesConfigFilePath,
//                                   String profileName)
public class CreateBucket {


    // This function will recurse through local files. If any local files are not on S3, it will upload them.
    static void recurse(File[] files, List<String> objects, AmazonS3 s3Client, String bucketName) {
        Console cnsl = System.console();

        for (File file : files) {
            String s3Path = file.getAbsolutePath().replaceAll("\\/Users/.*/S3_Data/", "");
            if (file.isDirectory()) {
                cnsl.printf("Directory: " + file.getAbsolutePath() + "\n");
                recurse(file.listFiles(), objects, s3Client, bucketName); // Calls same method again.
            } else {
                cnsl.printf(" File: " + file.getAbsolutePath() + "\n");
                if (objects.contains(s3Path)) {
                    cnsl.printf("  S3 does have this \n");
                } else {
                    if (file.getName().equals(".DS_Store")) {
                        cnsl.printf("  Ignoring .DS_Store \n");
                    } else {
                        cnsl.printf("  S3 does NOT have this \n");
                        cnsl.printf("  Uploading to s3... \n");
                        System.out.format("  Uploading %s to S3 bucket %s...\n", file.getAbsolutePath(), bucketName);
                        try {
                            s3Client.putObject(bucketName, s3Path, file);
                        } catch (AmazonServiceException e) {
                            System.err.println(e.getErrorMessage());
                            System.exit(1);
                        }
                    }
                }
            }
        }
    }

    public static void resolveLocal(File[] files, List<String> objects, AmazonS3 s3Client, String bucketName) {
        Console cnsl = System.console();

        recurse(files, objects, s3Client, bucketName);
        // for (File file : files) {
        //     if (file.isDirectory()) {
        //         cnsl.printf("Directory: " + file.getAbsolutePath());
        //         resolveLocal(file.listFiles(), objects, s3Client, bucketName); // Calls same method again.
        //     } else {
        //         cnsl.printf("File: " + file.getAbsolutePath() + "\n");
        //         if (objects.contains(file.getAbsolutePath())) {
        //             cnsl.printf("S3 does have this \n");
        //         } else {
        //             if (file.getName().equals(".DS_Store")) {
        //                 cnsl.printf("Ignoring .DS_Store \n");
        //             } else {
        //                 cnsl.printf("S3 does NOT have this \n");
        //                 cnsl.printf("Uploading to s3... \n");
        //                 System.out.format("Uploading %s to S3 bucket %s...\n", file.getAbsolutePath(), bucketName);
        //                 try {
        //                     s3Client.putObject(bucketName, file.getAbsolutePath(), file);
        //                 } catch (AmazonServiceException e) {
        //                     System.err.println(e.getErrorMessage());
        //                     System.exit(1);
        //                 }
        //             }
        //         }
        //     }
        // }
    }

    public static void createBucketFunc(String bucketName, String clientRegion, String accessKey, String secretKey) throws IOException, AmazonServiceException, SdkClientException {
        Console cnsl = System.console();

        String folderLocation = "/Users/" + System.getProperty("user.name") + "/S3_Data/";
        AWSCredentials creds = new BasicAWSCredentials(accessKey, secretKey);
        // File f = new File();

		// 			// Populates the array with names of files and directories
        // List<String> pathnames = new ArrayList<>(Arrays.asList(f.list()));
        AmazonS3 s3Client = AmazonS3ClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(creds))
                .withRegion(clientRegion)
                .build();

        if (!s3Client.doesBucketExistV2(bucketName)) {
            // Because the CreateBucketRequest object doesn't specify a region, the
            // bucket is created in the region specified in the client.
            s3Client.createBucket(new CreateBucketRequest(bucketName));

            // Verify that the bucket was created by retrieving it and checking its location.
            String bucketLocation = s3Client.getBucketLocation(new GetBucketLocationRequest(bucketName));
            cnsl.printf("Bucket location: " + bucketLocation);
        }

        System.out.format("Objects in S3 bucket %s:\n", bucketName);

        ListObjectsV2Result result = s3Client.listObjectsV2(bucketName);
        cnsl.printf("Number of objects in the bucket: " + result.getKeyCount() + "\n");
        List<S3ObjectSummary> objects = result.getObjectSummaries();
        List<String> objectsAsStrings = objects.stream()
            .map(object -> object.getKey())
            .collect(Collectors.toList());
        // if (result.getKeyCount() == 0) {
        //     cnsl.printf("Uploading files to s3");
        //     System.out.format("Uploading %s to S3 bucket %s...\n", fileLocation, bucketName);
        //     try {
        //         s3Client.putObject(bucketName, "storage", new File(fileLocation));
        //     } catch (AmazonServiceException e) {
        //         System.err.println(e.getErrorMessage());
        //         System.exit(1);
        //     }
        // }


        //This for loop will iterate over s3 objects.
        // If the object is on the local machine, it will update to the newer version
        //      First it will check hashes, if they are not the same, it will update to most recently modified.
        // If the object is not on the local machine, it will download the object from s3 to local.
        for (S3ObjectSummary os : objects) {
            Path filePath = Paths.get(folderLocation + os.getKey());
            String localHash;
            String s3Hash;
            cnsl.printf("* " + os.getKey() + "\n Last modified: " + os.getLastModified() + "\n");
            if (Files.exists(filePath)) {
                /*
                 *
                 * File file = new File(filePath);
                    byte[] bytes = new byte[(int) file.length()];

                    FileInputStream fis = null;
                    try {

                        fis = new FileInputStream(file);

                        //read file into bytes[]
                        fis.read(bytes);
                 *
                 */
                File theFile = filePath.toFile();
                byte[] bytes = new byte[(int) theFile.length()];
                FileInputStream fis = null;
                fis = new FileInputStream(theFile);
                fis.read(bytes);
                fis.close();
                cnsl.printf(" Filename exists on local, getting hashes... \n");
                localHash = DigestUtils.md5Hex(bytes);
                cnsl.printf(os.getKey());
                s3Hash = os.getETag();
                cnsl.printf("  Local hash ** S3 hash:  " + localHash  + "  **  " + s3Hash + "\n");
                if (!s3Hash.equals(localHash)) {
                    cnsl.printf("  Hashes are different \n");
                    Date s3Date = os.getLastModified();
                    long s3DateAsLong = s3Date.getTime();
                    long localDate = theFile.lastModified();
                    cnsl.printf("  Local last modified: " + localDate + "\n");
                    cnsl.printf("  S3 last modified   : " + s3DateAsLong + "\n");
                    if (s3DateAsLong > localDate) {
                        cnsl.printf("  S3 is newer, downloading newer file to local system \n" );
                        s3Client.getObject(new GetObjectRequest(bucketName, os.getKey()), theFile);
                    } else {
                        cnsl.printf("  Local is newer, uploading newer file to S3 \n");
                        s3Client.putObject(bucketName, os.getKey(), theFile);
                    }

                }
                // BasicFileAttributes attr = Files.readAttributes(filePath, BasicFileAttributes.class);
                // System.out.println(" Local last modifed :" + attr.lastModifiedTime());
            } else {
                cnsl.printf("Cant find S3 file on machine-- saving.."+ "\n");
                //This is where the downloaded file will be saved
                File localFile = new File(folderLocation + os.getKey());

                //This returns an ObjectMetadata file but you don't have to use this if you don't want
                s3Client.getObject(new GetObjectRequest(bucketName, os.getKey()), localFile);

                //Now your file will have your image saved
                boolean success = localFile.exists() && localFile.canRead();
                if (success) {
                    cnsl.printf("Success! \n");
                } else {
                    cnsl.printf("Failed.");
                }
            }
        }


        cnsl.printf("Checking local storage at " + folderLocation + "... \n");

        resolveLocal(((new File(folderLocation))).listFiles(), objectsAsStrings, s3Client, bucketName);
    }
}