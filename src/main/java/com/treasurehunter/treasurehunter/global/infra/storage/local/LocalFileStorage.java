package com.treasurehunter.treasurehunter.global.infra.storage.local;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Component
public class LocalFileStorage {

    @Value("${file.image.directory.path}")
    private String IMAGE_DIRECTORY_PATH;

    public void saveMultipartFile(
            final MultipartFile multipartFile,
            final String objectKey // 파일 이름 포함한 path ( = Image 엔티티의 objectKey와 같은 값)
    ){
        //파일 저장 위치 생성
        final String savePath = IMAGE_DIRECTORY_PATH + File.separator + objectKey;

        //파일 객체 생성
        final File file = new File(savePath);

        try{
            //부모 디렉토리 확인후 생성
            final File parentFile = file.getParentFile();
            if(parentFile != null && !parentFile.exists()){
                if(!parentFile.mkdirs()){
                    throw new IOException("부모 디렉토리 생성 실패");
                }
            }

            //실제 파일 저장
            multipartFile.transferTo(file);
        } catch (IOException ex) {
            throw new RuntimeException("파일 생성 실패");
        }
    }

    public Resource getImage(final String objectKey){
        final Path baseDir = Paths.get(IMAGE_DIRECTORY_PATH);
        final Path normalizedPath = baseDir.resolve(objectKey).normalize();

        //경로 탈출 방지
        if(!normalizedPath.startsWith(baseDir)){
            throw new RuntimeException("올바르지 않은 경로입니다.");
        }

        //존재 유무 확인
        if(!Files.exists(normalizedPath) || !Files.isRegularFile(normalizedPath)){
            throw new RuntimeException("파일을 찾을 수 없습니다.");
        }

        return new FileSystemResource(normalizedPath);
    }

    public void deleteImage(final String objectKey) throws IOException {

        final File file = new File(IMAGE_DIRECTORY_PATH + File.separator + objectKey);

        //존재 유무 확인
        if(!file.exists()){
            throw new RuntimeException("파일이 없습니다.");
        }

        //삭제 시도
        if(!file.delete()){
            throw new IOException("파일 삭제에 실패했습니다.");
        }
    }
}
