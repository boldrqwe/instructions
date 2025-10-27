package com.example.instructions.service;

import com.example.instructions.domain.Article;
import com.example.instructions.domain.ArticleStatus;
import com.example.instructions.repo.ArticleRepository;
import com.redfin.sitemapgenerator.ChangeFreq;
import com.redfin.sitemapgenerator.WebSitemapGenerator;
import com.redfin.sitemapgenerator.WebSitemapUrl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.File;
import java.net.MalformedURLException;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class SitemapService {

    private final ArticleRepository articleRepository;
    private static final String BASE_URL = "https://devhandbook.ru";

    public void generateSitemap() {
        try {
            // ✅ Используем текущую рабочую директорию (Spring Boot root)
            File baseDir = new File(System.getProperty("user.dir"));
            if (!baseDir.exists()) {
                baseDir.mkdirs();
            }

            WebSitemapGenerator generator = new WebSitemapGenerator(BASE_URL, baseDir);

            Set<Article> articles = articleRepository.findAllByStatus(ArticleStatus.PUBLISHED);
            for (Article article : articles) {
                String url = BASE_URL + "/articles/" + article.getSlug();
                WebSitemapUrl sitemapUrl = new WebSitemapUrl.Options(url)
                        .lastMod(java.util.Date.from(article.getUpdatedAt().toInstant()))
                        .priority(0.8)
                        .changeFreq(ChangeFreq.WEEKLY)
                        .build();
                generator.addUrl(sitemapUrl);
            }

            // Добавляем главную и общие страницы
            generator.addUrl(BASE_URL + "/");
            generator.addUrl(BASE_URL + "/articles");

            // ✅ Пишем в baseDir/sitemap.xml
            generator.write();

            System.out.println("✅ sitemap.xml успешно сгенерирован: " +
                    new File(baseDir, "sitemap.xml").getAbsolutePath());

        } catch (MalformedURLException e) {
            throw new RuntimeException("Ошибка при генерации sitemap.xml", e);
        } catch (Exception e) {
            throw new RuntimeException("Не удалось создать sitemap.xml", e);
        }
    }
}
