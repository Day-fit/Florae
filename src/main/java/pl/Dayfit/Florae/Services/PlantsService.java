package pl.Dayfit.Florae.Services;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

@Service
public class PlantsService {
    public final Path FILES_SAVE_PATH = Path.of("Photos");

    public void saveToServer(MultipartFile file) throws IOException
    {
        if(!Files.isDirectory(FILES_SAVE_PATH)){
            throw new RuntimeException("File of " + FILES_SAVE_PATH.toAbsolutePath().toString() + " is not a directory!");
        }

        if (!FILES_SAVE_PATH.toFile().exists()) {
            Files.createDirectory(FILES_SAVE_PATH);
        }

        UUID uuid = UUID.randomUUID();

        file.transferTo(new File(FILES_SAVE_PATH + uuid.toString()));

        //TODO: change to saving in database
    }

    public String diagnose(MultipartFile file)
    {
        RestTemplate template = new RestTemplate();
        
        
        //TODO: add api diagnose (PLANTNET API IS DEAD AT THIS MOMENT)

        return "";
    }
}