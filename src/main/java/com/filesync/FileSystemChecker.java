package com.filesync;



import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import org.apache.commons.codec.digest.DigestUtils;

/*
  Checks if the file is in the filesystem.
 * @param - String filePath- the absolute path to the file
 * @return - long - 'last modified' date of the file if it exists, null otherwise
 */


public class FileSystemChecker {

  public static Long checkFile(String filePath) throws IOException{
   File f = new File(filePath);
   if(f.isFile()) {
      Long result = Long.valueOf(f.lastModified());
      return result;
   } else {
      return null;
   }

 }

}
