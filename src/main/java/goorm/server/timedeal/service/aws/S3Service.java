package goorm.server.timedeal.service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class S3Service {
	private final AmazonS3 amazonS3Client;
	private final RestTemplate restTemplate; // HTTP 요청을 보낼 RestTemplate
	private final String bucketName = "deepdive2team.shop";  // S3 버킷 이름

	public S3Service(AmazonS3 amazonS3Client, RestTemplate restTemplate) {
		this.amazonS3Client = amazonS3Client;
		this.restTemplate = restTemplate;
	}


	private String imageFolder = "timedeal-products";


	// 이미지 파일을 S3에 업로드하고 URL을 반환
	public String uploadImage(MultipartFile file) throws IOException {
		String fileName = "time-deal/" + UUID.randomUUID() + "_" + file.getOriginalFilename();
		ObjectMetadata metadata = new ObjectMetadata();
		metadata.setContentType(file.getContentType());
		metadata.setContentLength(file.getSize());

		// S3에 파일 업로드
		amazonS3Client.putObject(new PutObjectRequest(bucketName, fileName, file.getInputStream(), metadata));

		// 업로드된 파일의 URL 반환
		return amazonS3Client.getUrl(bucketName, fileName).toString();
	}

	/**
	 * URL로 이미지를 받아 S3에 업로드하고, 해당 URL을 반환하는 메서드
	 */
	public String uploadImageFromUrl(String imageUrl) throws IOException {
		log.info("S3 Service-uploadImageFromUrl 업로드하려는 이미지 URL: " + imageUrl);

		// 1. 이미지 URL로 HTTP GET 요청을 보내서 이미지 데이터 가져오기
		byte[] imageBytes = restTemplate.getForObject(imageUrl, byte[].class);

		// 2. 파일명 생성: UUID로 고유한 파일 이름 생성
		String fileName = UUID.randomUUID().toString() + ".jpg";  // 예시로 .jpg 확장자 사용

		// 3. S3에 업로드하기 위한 InputStream 생성
		assert imageBytes != null;
		InputStream inputStream = new ByteArrayInputStream(imageBytes);

		// 4. ObjectMetadata 생성 및 설정
		ObjectMetadata metadata = new ObjectMetadata();
		metadata.setContentLength(imageBytes.length); // Content-Length 설정
		metadata.setContentType("image/jpeg"); // MIME 유형 설정

		// 5. S3에 이미지 업로드
		amazonS3Client.putObject(new PutObjectRequest(bucketName, imageFolder + "/" + fileName, inputStream, metadata));

		// 6. 업로드한 이미지의 URL 반환
		return amazonS3Client.getUrl(bucketName, imageFolder + "/" + fileName).toString();
	}

}
