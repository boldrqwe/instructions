# Доменная модель

## Сущности
- **Article**: id, title, slug, status(DRAFT|PUBLISHED), version, timestamps, createdBy
- **Chapter**: id, articleId, title, orderIndex
- **Section**: id, chapterId, title, orderIndex, markdown
- **Tag** + связь **ArticleTag**
- **Revision**: snapshot опубликованной версии (для истории/rollback)
- **MediaAsset**: id, url/objectId, mime, size, ownerId (ADMIN), createdAt

## Инварианты и правила
- Публиковать можно только `DRAFT`.
- При публикации:
    - инкремент версии,
    - snapshot в `Revision`,
    - запись в индекс поиска (tsvector).
- `slug` уникален среди PUBLISHED; для черновиков slug может меняться.
- Оглавление строится по `orderIndex` в Chapter и Section.

## Границы
- Вся логика изменения статуса и версий — через сервис публикации (transactional).
- Публичные GET не возвращают DRAFT.
