package org.superbiz.moviefun.albums;

import org.apache.tika.Tika;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.superbiz.moviefun.blobstore.Blob;
import org.superbiz.moviefun.blobstore.BlobStore;

import java.io.*;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

import static java.lang.ClassLoader.getSystemResource;
import static java.lang.String.format;

@Controller
@RequestMapping("/albums")
public class AlbumsController {
    private final BlobStore blobStore;

    private final AlbumsBean albumsBean;

    public AlbumsController(AlbumsBean albumsBean, BlobStore blobStore) {
        this.blobStore = blobStore;
        this.albumsBean = albumsBean;
    }


    @GetMapping
    public String index(Map<String, Object> model) {
        model.put("albums", albumsBean.getAlbums());
        return "albums";
    }

    @GetMapping("/{albumId}")
    public String details(@PathVariable long albumId, Map<String, Object> model) {
        model.put("album", albumsBean.find(albumId));
        return "albumDetails";
    }

    @PostMapping("/{albumId}/cover")
    public String uploadCover(@PathVariable long albumId, @RequestParam("file") MultipartFile uploadedFile) throws IOException {
        saveUploadToFile(uploadedFile, getCoverFile(albumId));

        return format("redirect:/albums/%d", albumId);
    }

    @GetMapping("/{albumId}/cover")
    public HttpEntity<byte[]> getCover(@PathVariable long albumId) throws IOException, URISyntaxException {
        File coverFile = getCoverFile(albumId);
        Blob blob = blobStore.get(coverFile.getName()).orElse(new Blob(coverFile.getName(), AlbumsController.class.getClassLoader().getResourceAsStream("default-cover.jpg"), new Tika().detect(AlbumsController.class.getClassLoader().getResourceAsStream("default-cover.jpg"))));

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        for (int len = 0; (len = blob.inputStream.read(buffer)) > 0;) {
            bos.write(buffer, 0, len);
        }
        byte[] imageBytes = bos.toByteArray();

        HttpHeaders headers = createImageHttpHeaders(blob, imageBytes.length);

        return new HttpEntity<>(imageBytes, headers);
    }


    private void saveUploadToFile(@RequestParam("file") MultipartFile uploadedFile, File targetFile) throws IOException {
        byte[] bytes = uploadedFile.getBytes();
        String contentType = new Tika().detect(bytes);
        Blob b = new Blob(targetFile.getName(), new ByteArrayInputStream(bytes), contentType);
        blobStore.put(b);
    }

    private HttpHeaders createImageHttpHeaders(Blob b, int size) throws IOException {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(b.contentType));
        headers.setContentLength(size);
        return headers;
    }

    private File getCoverFile(@PathVariable long albumId) {
        String coverFileName = format("covers/%d", albumId);
        return new File(coverFileName);
    }

    private Path getExistingCoverPath(@PathVariable long albumId) throws URISyntaxException {
        File coverFile = getCoverFile(albumId);
        Path coverFilePath;

        if (coverFile.exists()) {
            coverFilePath = coverFile.toPath();
        } else {
            coverFilePath = Paths.get(getSystemResource("default-cover.jpg").toURI());
        }


        return coverFilePath;
    }
}
