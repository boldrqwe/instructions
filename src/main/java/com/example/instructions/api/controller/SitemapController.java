package com.example.instructions.api.controller;


import org.springframework.core.io.FileSystemResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;

@RestController("/api/v1/")
public class SitemapController {

    @GetMapping(value = "/sitemap.xml", produces = MediaType.APPLICATION_XML_VALUE)
    public ResponseEntity<FileSystemResource> getSitemap() {
        File file = new File("sitemap.xml");
        return ResponseEntity.ok(new FileSystemResource(file));
    }
}
