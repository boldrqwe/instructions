package com.example.instructions.api.controller;


import org.springframework.core.io.FileSystemResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;

@RestController
public class SitemapController {

    @GetMapping(value = "/sitemap.xml", produces = MediaType.APPLICATION_XML_VALUE)
    public ResponseEntity<FileSystemResource> getSitemap() {
        // ⚙️ путь должен совпадать с тем, где генерируется файл
        File file = new File(System.getProperty("java.io.tmpdir"), "sitemap.xml");

        if (!file.exists()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(new FileSystemResource(file));
    }
}

