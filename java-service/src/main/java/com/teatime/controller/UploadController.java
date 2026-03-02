package com.teatime.controller;

import com.teatime.dto.Result;
import com.teatime.utils.SystemConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/upload")
public class UploadController {

  @Value("${teatime.upload.dir:}")
  private String uploadDirOverride;

  private String getImageUploadDir() {
    return (uploadDirOverride != null && !uploadDirOverride.isEmpty())
        ? uploadDirOverride : SystemConstants.IMAGE_UPLOAD_DIR;
  }

  /**
   * Upload blog image
   * POST /api/upload/blog
   * Form Data: file (MultipartFile)
   */
  @PostMapping("/blog")
  public Result uploadImage(@RequestParam("file") MultipartFile image) {
    try {
      // get original filename
      String originalFilename = image.getOriginalFilename();
      // create new filename
      String fileName = createNewFileName(originalFilename);
      // save file to disk
      image.transferTo(new File(getImageUploadDir(), fileName));
      // return filename
      log.debug("successfully uploaded file: {}", fileName);
      return Result.ok(fileName);
    } catch (IOException e) {
      throw new RuntimeException("file upload failed", e);
    }
  }

  /**
   * Delete blog image
   * GET /api/upload/blog/delete?name={filename}
   */
  @GetMapping("/blog/delete")
  public Result deleteBlogImg(@RequestParam("name") String filename) {
    File file = new File(getImageUploadDir(), filename);
    if (file.isDirectory()) {
      return Result.fail("wrong file name");
    }
    try {
      Files.deleteIfExists(file.toPath());
    } catch (IOException e) {
      throw new RuntimeException("Failed to delete file", e);
    }
    return Result.ok();
  }

  private String createNewFileName(String originalFilename) {
    // get suffix
    String suffix = StringUtils.substringAfterLast(originalFilename, ".");
    if (StringUtils.isBlank(suffix)) {
      suffix = "jpg";
    }
    // generate directory path
    String name = UUID.randomUUID().toString();
    int hash = name.hashCode();
    int d1 = hash & 0xF;
    int d2 = (hash >> 4) & 0xF;
    // create directory if not exists
    File dir = new File(getImageUploadDir(), String.format("/blogs/%d/%d", d1, d2));
    if (!dir.exists()) {
      dir.mkdirs();
    }
    // return new filename with path
    return String.format("/blogs/%d/%d/%s.%s", d1, d2, name, suffix);
  }
}
