package es.amanzag.secretsanta;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class PhotoStorageService {
    
    private final static Logger log = LoggerFactory.getLogger(PhotoStorageService.class);
    
    private Path directory;
    
    public PhotoStorageService(@Value("${data.directory}/photos") Path directory) {
        super();
        this.directory = directory;
        directory.toFile().mkdirs();
    }

    public void savePicture(User user, InputStream imageData) throws IOException {
        log.info("Saving photo for user {}", user.getId());
        Files.copy(imageData, directory.resolve(user.getId()+".jpeg"));
    }
    
    public InputStream getPicture(User user) throws IOException {
        return Files.newInputStream(directory.resolve(user.getId()+".jpeg"));
    }

}
