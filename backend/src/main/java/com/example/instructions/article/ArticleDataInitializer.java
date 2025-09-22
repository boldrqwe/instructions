package com.example.instructions.article;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class ArticleDataInitializer implements CommandLineRunner {

    private final ArticleRepository articleRepository;

    public ArticleDataInitializer(ArticleRepository articleRepository) {
        this.articleRepository = articleRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (articleRepository.count() > 0) {
            return;
        }

        Article first = new Article(
                "Добро пожаловать",
                "<p>Это стартовая статья. Используйте редактор, чтобы создавать подробные инструкции для своей команды." +
                        " Добавляйте заголовки, выделения, списки и даже изображения.</p>"
        );
        Article second = new Article(
                "Как опубликовать инструкцию",
                "<ol><li>Напишите текст в редакторе справа.</li>" +
                        "<li>Нажмите кнопку <strong>\"Сохранить\"</strong>.</li>" +
                        "<li>Поделитесь ссылкой с коллегами.</li></ol>"
        );

        articleRepository.save(first);
        articleRepository.save(second);
    }
}
