package ua.rzs.service;

import com.dropbox.core.DbxDownloader;
import com.dropbox.core.DbxException;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.FileMetadata;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;

@Service
public class DropboxService {
    private final DbxClientV2 client;

    public DropboxService(@Value("${dropbox.access-token}") String accessToken,
                          @Value("${dropbox.app-name}") String appName) {
        DbxRequestConfig config = DbxRequestConfig.newBuilder(appName).build();
        this.client = new DbxClientV2(config, accessToken);
    }

    public String uploadFile(MultipartFile file, String dropboxPath) throws Exception {
        try (InputStream in = file.getInputStream()) {
            FileMetadata metadata = client.files().uploadBuilder(dropboxPath)
                    .uploadAndFinish(in);
            return metadata.getPathLower();
        }
    }

        public Resource downloadAsResource(String dropboxPath) {
            byte[] data;
            try (DbxDownloader<FileMetadata> dl = client.files().downloadBuilder(dropboxPath).start()) {
                data = dl.getInputStream().readAllBytes();
            } catch (IOException | DbxException e) {
                throw new RuntimeException(e);
            }
            return new ByteArrayResource(data);
        }
}

