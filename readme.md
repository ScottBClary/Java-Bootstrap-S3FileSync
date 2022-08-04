# S3 File Syncer

This app will sync a local folder to your AWS S3.

# Setup

This was built in vscode using the starting kit which is viewable here -> https://code.visualstudio.com/docs/java/java-spring-boot

Requirements : maven

Setting up aws with maven https://docs.aws.amazon.com/sdk-for-java/v1/developer-guide/setup-project-maven.html

If you are set up correctly you should now be able to compile and run in VSCode by right-clicking FilesyncartifactidApplication.java -> Run Java

FYI, to compile in terminal, run "mvn package" to create the JAR. (The pom.xml should be directing where to place this JAR)

Remember to create the required environment variables to communicate with your S3.

```
{
  "configurations": [

    {
      "type": "java",
      "name": "Launch FilesyncartifactidApplication",
      "request": "launch",
      "mainClass": "com.filesync.filesyncartifactid.FilesyncartifactidApplication",
      "projectName": "filesyncartifactid",
      "env": {
        "ACCESS_KEY":
        "SECRET_KEY":
        "BUCKET_NAME":
        "BUCKET_REGION":
      }
    },

  ]
}
```
Additionally, the S3_Data folder that will sync with your s3 will be in Users/%username%/S3_Data.
