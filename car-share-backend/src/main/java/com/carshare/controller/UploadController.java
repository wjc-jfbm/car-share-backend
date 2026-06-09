package com.carshare.controller;

import com.carshare.common.Result;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.UUID;

@RestController
@RequestMapping("/api/upload")
public class UploadController {

    @Value("${file.upload-path:./uploads}")
    private String uploadPath;

    @PostMapping
    public Result<?> upload(@RequestParam("file") MultipartFile file) {
        return doUpload(file, "上传成功");
    }

    @PostMapping("/evidence")
    public Result<?> uploadEvidence(@RequestParam("file") MultipartFile file) {
        return doUpload(file, "凭证图片上传成功");
    }

    private Result<?> doUpload(MultipartFile file, String successMsg) {
        if (file.isEmpty()) {
            return Result.fail("文件不能为空");
        }
        String originalFilename = file.getOriginalFilename();
        String ext = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            ext = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        try {
            File dir = Paths.get(uploadPath).toFile();
            if (!dir.exists()) {
                dir.mkdirs();
            }
            String newFilename = UUID.randomUUID().toString().replace("-", "") + ext;
            File dest = new File(dir, newFilename);
            file.transferTo(dest.getAbsoluteFile());
            String url = "/api/file/" + newFilename;
            return Result.success(url, successMsg);
        } catch (IOException e) {
            return Result.fail("上传失败: " + e.getMessage());
        }
    }
}

@RestController
@RequestMapping("/api/file")
class FileController {

    @Value("${file.upload-path:./uploads}")
    private String uploadPath;

    @GetMapping("/{filename:.+}")
    public ResponseEntity<Resource> getFile(@PathVariable String filename) {
        File file = Paths.get(uploadPath, filename).toFile();
        if (!file.exists() || !file.isFile()) {
            return ResponseEntity.notFound().build();
        }

        Resource resource = new FileSystemResource(file);
        String contentType = getContentType(filename);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CACHE_CONTROL, "max-age=86400")
                .body(resource);
    }

    private String getContentType(String filename) {
        String lower = filename.toLowerCase();
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) return "image/jpeg";
        if (lower.endsWith(".png")) return "image/png";
        if (lower.endsWith(".gif")) return "image/gif";
        if (lower.endsWith(".webp")) return "image/webp";
        if (lower.endsWith(".bmp")) return "image/bmp";
        return "application/octet-stream";
    }
}
