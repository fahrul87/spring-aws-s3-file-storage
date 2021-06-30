package com.fahrul.springs3filestorage.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.amazonaws.util.IOUtils;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class StorageService {

	@Value("${application.bucket.name}")
	private String bucketName;

	@Autowired
	private AmazonS3 amazonS3;

	public String uploadFile(MultipartFile multipartFile) {
		File fileObj = convertMultiPartFileToFile(multipartFile);
		String fileName = System.currentTimeMillis() + "_" + multipartFile.getOriginalFilename();
		amazonS3.putObject(new PutObjectRequest(bucketName, fileName, fileObj));
		fileObj.delete();
		return "File uploaded : " + fileName;
	}

	public byte[] downloadFile(String fileName) {
		S3Object s3Object = amazonS3.getObject(bucketName, fileName);
		S3ObjectInputStream inputStream = s3Object.getObjectContent();
		try {
			byte[] content = IOUtils.toByteArray(inputStream);
			return content;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public String deleteFile(String fileName) {
		amazonS3.deleteObject(bucketName, fileName);
		return fileName + " removed.";
	}

	private File convertMultiPartFileToFile(MultipartFile file) {
		File covertedFile = new File(file.getOriginalFilename());
		try (FileOutputStream fileOutputStream = new FileOutputStream(covertedFile);) {
			fileOutputStream.write(file.getBytes());
		} catch (IOException e) {
			log.error("Error converting multipartFile to file", e);
		}
		return covertedFile;
	}

}
