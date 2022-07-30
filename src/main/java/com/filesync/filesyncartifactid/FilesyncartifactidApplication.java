package com.filesync.filesyncartifactid;

import com.AWS.CreateBucket;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import java.io.*;
import java.nio.file.FileStore;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

import com.amazonaws.services.s3control.model.*;





@SpringBootApplication
public class FilesyncartifactidApplication {

	//TO DO create a bucket and put objects in it.
// 	public String createBucket(String bucketName) {

//     CreateBucketRequest reqCreateBucket = new CreateBucketRequest()
//             .withBucket(bucketName)
//             .withOutpostId(OutpostId)
//             .withCreateBucketConfiguration(new CreateBucketConfiguration());

//     CreateBucketResult respCreateBucket = s3ControlClient.createBucket(reqCreateBucket);
//     System.out.printf("CreateBucket Response: %s%n", respCreateBucket.toString());

//     return respCreateBucket.getBucketArn();

// }
	public static void main(String[] args) throws IOException {

		SpringApplication.run(FilesyncartifactidApplication.class, args);
		Console cnsl = System.console();
		cnsl.printf("Welcome to file sync program! \n");
		cnsl.printf("This program will sync your computer with an S3 server \n");
		String folderLocation = "/Users/" + System.getProperty("user.name") + "/S3_Data";
		cnsl.printf("Checking if there is already a sync folder at " + folderLocation + "... \n");
		boolean folderExists = Files.exists(Paths.get(folderLocation));
		File f = new File(folderLocation);
		cnsl.printf("%b%n", folderExists);
		if (!folderExists) {
			cnsl.printf("It appears this is the first time running this application, would you like to create a store on your system? \n");

			String str = cnsl.readLine("(Y/N) : ");
			boolean inputInvalid = true;
			if (str.equalsIgnoreCase("Y") || str.equalsIgnoreCase("N")) {
				inputInvalid = false;
			}
			while(inputInvalid) {
										// Read line
				System.out.println("Input invalid.");
				System.out.println("It appears this is the first time running this application, would you like to create a store on your system?");
				str = cnsl.readLine("(Y/N) : ");
				if (str.equalsIgnoreCase("Y") || str.equalsIgnoreCase("N")) { inputInvalid = false; }
				System.out.println("You entered : " + str);
			}

			if (str.equalsIgnoreCase("N")) {
				System.out.println("Goodbye!");
				return;
			} else {
				f.mkdirs();
				cnsl.printf("Folder created. \n");
			}
		}




		cnsl.printf("Running sync program with environment variables: \n");
		cnsl.printf("S3 access key: " + System.getenv("ACCESS_KEY") + "\n");
		cnsl.printf("S3 secret key: " + System.getenv("SECRET_KEY") + "\n");
		cnsl.printf("S3 bucket name: " + System.getenv("BUCKET_NAME") + "\n");
		cnsl.printf("S3 bucket region: " + System.getenv("BUCKET_REGION") + "\n");

		CreateBucket.createBucketFunc(System.getenv("BUCKET_NAME"), System.getenv("BUCKET_REGION"), System.getenv("ACCESS_KEY"), System.getenv("SECRET_KEY"));


	}
}


