-- Очистка старых ограничений и триггеров
DO $$ BEGIN
  IF EXISTS (SELECT 1 FROM pg_indexes WHERE indexname = 'uniq_article_slug_published') THEN
    DROP INDEX uniq_article_slug_published;
  END IF;
END $$;

DO $$ BEGIN
  IF EXISTS (SELECT 1 FROM pg_trigger WHERE tgname = 'trg_article_search_vector') THEN
    DROP TRIGGER trg_article_search_vector ON article;
  END IF;
END $$;

DO $$ BEGIN
  IF EXISTS (SELECT 1 FROM pg_proc WHERE proname = 'article_search_vector_update') THEN
    DROP FUNCTION article_search_vector_update();
  END IF;
END $$;

-- Статус + контент
ALTER TABLE article
  ADD COLUMN IF NOT EXISTS content_html TEXT NOT NULL DEFAULT '',
  ADD COLUMN IF NOT EXISTS content_json JSONB NOT NULL DEFAULT '{}'::jsonb,
  ADD COLUMN IF NOT EXISTS summary TEXT,
  ADD COLUMN IF NOT EXISTS cover_image_url TEXT,
  ADD COLUMN IF NOT EXISTS tags TEXT[],
  ADD COLUMN IF NOT EXISTS status VARCHAR(16) NOT NULL DEFAULT 'DRAFT';

-- Уникальность слага
ALTER TABLE article ADD COLUMN IF NOT EXISTS slug TEXT;
UPDATE article SET slug = coalesce(slug, regexp_replace(lower(title), '[^a-z0-9]+', '-', 'g')) WHERE slug IS NULL;
ALTER TABLE article ALTER COLUMN slug SET NOT NULL;
DO $$ BEGIN
  IF NOT EXISTS(SELECT 1 FROM pg_indexes WHERE indexname='uk_article_slug') THEN
    CREATE UNIQUE INDEX uk_article_slug ON article (slug);
  END IF;
END $$;

-- Полнотекстовый поиск по HTML (очистка тегов)
ALTER TABLE article
  ADD COLUMN IF NOT EXISTS search_vector tsvector;

-- Хранимый вычисляемый столбец (если версия БД позволяет) ИЛИ триггер:
DO $$
BEGIN
  IF EXISTS (SELECT 1 FROM pg_proc WHERE proname='to_tsvector') THEN
    -- вариант через триггер, чтобы совместимо со старыми версиями:
    CREATE OR REPLACE FUNCTION article_tsvector_update() RETURNS trigger AS $f$
    BEGIN
      NEW.search_vector := to_tsvector('russian',
        coalesce(NEW.title,'') || ' ' ||
        regexp_replace(coalesce(NEW.content_html,''), '<[^>]+>', ' ', 'g'));
      RETURN NEW;
    END $f$ LANGUAGE plpgsql;

    DROP TRIGGER IF EXISTS trg_article_tsvector ON article;
    CREATE TRIGGER trg_article_tsvector
      BEFORE INSERT OR UPDATE OF title, content_html ON article
      FOR EACH ROW EXECUTE FUNCTION article_tsvector_update();
  END IF;
END $$;

-- Индекс для поиска
DO $$ BEGIN
  IF NOT EXISTS(SELECT 1 FROM pg_indexes WHERE indexname='idx_article_search_vector') THEN
    CREATE INDEX idx_article_search_vector ON article USING GIN (search_vector);
  END IF;
END $$;

-- Статусы по умолчанию
UPDATE article SET status='DRAFT' WHERE status IS NULL;

-- Обновление search_vector для существующих записей
UPDATE article SET content_html = content_html;
