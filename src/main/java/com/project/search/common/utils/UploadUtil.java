package com.project.search.common.utils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.Date;

import static com.project.search.common.utils.DateFormatUtil.DAY_FORMAT;
import static com.project.search.common.utils.DateFormatUtil.TIME_FORMAT;
import static com.project.search.common.utils.DateFormatUtil.dateToString;


@Component
public class UploadUtil {
	
	private static String uploadPath;
	
//	@Value("${spring.upload.tmp.path}")
//	public void setUploadPath(String uploadPath) {
//		UploadUtil.uploadPath = uploadPath;
//	}
//
//	public static String upload(MultipartFile file, String userId) throws Exception {
//		String fileName = file.getOriginalFilename();
//		String name = fileName.substring(0, fileName.lastIndexOf("."));
//		String suffix = fileName.substring(fileName.lastIndexOf("."), fileName.length());
//		String filePath = uploadPath + userId + "/" + dateToString(new Date(), DAY_FORMAT) + "/" + name + "-" + dateToString(new Date(), TIME_FORMAT) + suffix;
//		File saveFile = new File(filePath);
//		if (!saveFile.getParentFile().exists()){
//			saveFile.getParentFile().mkdirs();
//        }
//		file.transferTo(saveFile);
//		return filePath;
//	}

	public static byte[] fileToBytes(String filePath){
		File file = new File(filePath);
		try {
			FileInputStream fis = new FileInputStream(file);
			ByteArrayOutputStream bos = new ByteArrayOutputStream(1024);
			byte[] b = new byte[1024];
			int n;
			while ((n = fis.read(b)) != -1) {
				bos.write(b, 0, n);
			}
			fis.close();
			byte[] data = bos.toByteArray();
			bos.close();
			return data;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}
