package com.magadiflo.app.resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/file")
public class FileResource {

    private static final Logger LOG = LoggerFactory.getLogger(FileResource.class);

    //Importante: El directorio /uploads, debe estar creado
    public static final String DIRECTORY = System.getProperty("user.home") + "/Downloads/uploads";

    @PostMapping("/upload")
    public ResponseEntity<List<String>> uploadFiles(@RequestParam("files") List<MultipartFile> multipartFiles) throws IOException {
        List<String> filenames = new ArrayList<>();
        for (MultipartFile file : multipartFiles) {
            String filename = StringUtils.cleanPath(file.getOriginalFilename());
            Path fileStorage = Paths.get(DIRECTORY, filename).toAbsolutePath().normalize();

            LOG.info("fileStorage: {}", fileStorage.toString());

            Files.copy(file.getInputStream(), fileStorage, StandardCopyOption.REPLACE_EXISTING);
            filenames.add(filename);
        }
        return ResponseEntity.ok().body(filenames);
    }

    @GetMapping("/download/{filename}")
    public ResponseEntity<Resource> downloadFiles(@PathVariable String filename) throws IOException {
        Path filePath = Paths.get(DIRECTORY).toAbsolutePath().normalize().resolve(filename);

        if (!Files.exists(filePath)) {
            throw new FileNotFoundException(filename.concat(" was not found on the server"));
        }

        Resource resource = new UrlResource(filePath.toUri());
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("File-Name", filename);
        httpHeaders.add(HttpHeaders.CONTENT_DISPOSITION, "attachment;File-Name=".concat(resource.getFilename()));

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(Files.probeContentType(filePath)))
                .headers(httpHeaders)
                .body(resource);
    }

}


/**
 * System.getProperty("user.home")
 * Devolverá el directorio de inicio del usuario conectado actualmente
 * <p>
 * StringUtils.cleanPath
 * Normalice la ruta suprimiendo secuencias como "ruta/.." y puntos simples internos.
 * Un ejemplo de su uso:
 * Se tiene la siguiente ruta:
 * String ruta = StringUtils.cleanPath("C:\\Users\\archivo");
 * System.out.println("ruta = " + ruta); //ruta = C:/Users/archivo
 * Como se observa los "\" que es como se concatena las rutas en windows se
 * invirtieron por "/"
 */