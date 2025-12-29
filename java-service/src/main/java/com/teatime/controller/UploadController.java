package com.teatime.controller;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import com.teatime.dto.Result;
import com.teatime.utils.SystemConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/upload")
public class UploadController {

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
      image.transferTo(new File(SystemConstants.IMAGE_UPLOAD_DIR, fileName));
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
    File file = new File(SystemConstants.IMAGE_UPLOAD_DIR, filename);
    if (file.isDirectory()) {
      return Result.fail("wrong file name");
    }
    FileUtil.del(file);
    return Result.ok();
  }

  private String createNewFileName(String originalFilename) {
    // get suffix
    String suffix = StrUtil.subAfter(originalFilename, ".", true);
    // generate directory path
    String name = UUID.randomUUID().toString();
    int hash = name.hashCode();
    int d1 = hash & 0xF;
    int d2 = (hash >> 4) & 0xF;
    // create directory if not exists
    File dir = new File(SystemConstants.IMAGE_UPLOAD_DIR, StrUtil.format("/blogs/{}/{}", d1, d2));
    if (!dir.exists()) {
      dir.mkdirs();
    }
    // return new filename with path
    return StrUtil.format("/blogs/{}/{}/{}.{}", d1, d2, name, suffix);
  }
}
