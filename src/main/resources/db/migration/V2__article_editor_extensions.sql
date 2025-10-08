ALTER TABLE article ADD COLUMN IF NOT EXISTS summary TEXT;
ALTER TABLE article ADD COLUMN IF NOT EXISTS cover_image_url TEXT;
ALTER TABLE article ADD COLUMN IF NOT EXISTS tags TEXT[];
ALTER TABLE article ADD COLUMN IF NOT EXISTS content_html TEXT NOT NULL DEFAULT '';
ALTER TABLE article ADD COLUMN IF NOT EXISTS content_json JSONB NOT NULL DEFAULT '{}'::jsonb;
ALTER TABLE article ADD COLUMN IF NOT EXISTS status VARCHAR(16) NOT NULL DEFAULT 'DRAFT';

CREATE OR REPLACE FUNCTION article_search_vector_update() RETURNS trigger AS $$
BEGIN
    NEW.search_vector := to_tsvector('simple',
        coalesce(NEW.title, '') || ' ' ||
        coalesce(NEW.summary, '') || ' ' ||
        coalesce(NEW.content_html, ''));
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_trigger
        WHERE tgname = 'trg_article_search_vector'
    ) THEN
        CREATE TRIGGER trg_article_search_vector
            BEFORE INSERT OR UPDATE ON article
            FOR EACH ROW EXECUTE FUNCTION article_search_vector_update();
    END IF;
END;
$$;

CREATE INDEX IF NOT EXISTS idx_article_search_vector ON article USING GIN (search_vector);
