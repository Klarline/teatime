package com.teatime.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.http.MediaType;
import org.springframework.web.util.NestedServletException;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.io.File;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class UploadControllerTest {

  private MockMvc mockMvc;

  private UploadController uploadController;

  @TempDir
  Path tempDir;

  @BeforeEach
  void setUp() {
    uploadController = new UploadController();
    // Inject test upload directory via reflection (avoids hardcoded path)
    File uploadDir = tempDir.toFile();
    ReflectionTestUtils.setField(uploadController, "uploadDirOverride", uploadDir.getAbsolutePath());
    mockMvc = MockMvcBuilders.standaloneSetup(uploadController).build();
  }

  @Test
  void testUploadImage_Success() throws Exception {
    MockMultipartFile file = new MockMultipartFile(
        "file",
        "test-image.png",
        MediaType.IMAGE_PNG_VALUE,
        "test image content".getBytes()
    );

    mockMvc.perform(multipart("/api/upload/blog").file(file))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data").isString());
  }

  @Test
  void testDeleteBlogImg_Success() throws Exception {
    // Create a file in the upload dir first
    File blogsDir = new File(tempDir.toFile(), "blogs/0/0");
    blogsDir.mkdirs();
    File testFile = new File(blogsDir, "test-delete.jpg");
    testFile.createNewFile();

    mockMvc.perform(get("/api/upload/blog/delete")
            .param("name", "blogs/0/0/test-delete.jpg"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true));
  }

  @Test
  void testDeleteBlogImg_DirectoryName_ReturnsFail() throws Exception {
    // Create "blogs" as a directory so isDirectory() returns true
    File blogsDir = new File(tempDir.toFile(), "blogs");
    blogsDir.mkdirs();

    mockMvc.perform(get("/api/upload/blog/delete")
            .param("name", "blogs"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.errorMsg").value("wrong file name"));
  }

  @Test
  void testUploadImage_IOException_ThrowsRuntimeException() {
    MockMultipartFile file = new MockMultipartFile(
        "file",
        "test.png",
        MediaType.IMAGE_PNG_VALUE,
        "x".getBytes()
    ) {
      @Override
      public void transferTo(java.io.File dest) throws java.io.IOException {
        throw new java.io.IOException("simulated upload failure");
      }
    };

    assertThrows(NestedServletException.class,
        () -> mockMvc.perform(multipart("/api/upload/blog").file(file)));
  }

}
