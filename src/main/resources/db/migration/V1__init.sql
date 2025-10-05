CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE article (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    title VARCHAR(512) NOT NULL,
    slug VARCHAR(512) NOT NULL,
    status VARCHAR(16) NOT NULL,
    version INTEGER NOT NULL DEFAULT 1,
    created_by VARCHAR(128) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    search_vector TSVECTOR
);

CREATE UNIQUE INDEX uniq_article_slug_published ON article (slug) WHERE status = 'PUBLISHED';
CREATE INDEX idx_article_status ON article (status);
CREATE INDEX idx_article_search_vector ON article USING GIN (search_vector);

CREATE TABLE chapter (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    article_id UUID NOT NULL REFERENCES article(id) ON DELETE CASCADE,
    title VARCHAR(512) NOT NULL,
    order_index INTEGER NOT NULL
);

CREATE INDEX idx_chapter_article_order ON chapter(article_id, order_index);

CREATE TABLE section (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    chapter_id UUID NOT NULL REFERENCES chapter(id) ON DELETE CASCADE,
    title VARCHAR(512) NOT NULL,
    order_index INTEGER NOT NULL,
    markdown TEXT NOT NULL,
    search_vector TSVECTOR
);

CREATE INDEX idx_section_chapter_order ON section(chapter_id, order_index);
CREATE INDEX idx_section_search_vector ON section USING GIN (search_vector);

CREATE TABLE tag (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(128) NOT NULL UNIQUE,
    slug VARCHAR(128) NOT NULL UNIQUE
);

CREATE TABLE article_tag (
    article_id UUID NOT NULL REFERENCES article(id) ON DELETE CASCADE,
    tag_id UUID NOT NULL REFERENCES tag(id) ON DELETE CASCADE,
    PRIMARY KEY (article_id, tag_id)
);

CREATE TABLE revision (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    article_id UUID NOT NULL REFERENCES article(id) ON DELETE CASCADE,
    version INTEGER NOT NULL,
    snapshot JSONB NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE media_asset (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    url VARCHAR(1024) NOT NULL,
    mime VARCHAR(128) NOT NULL,
    size BIGINT NOT NULL,
    owner_id VARCHAR(128) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE OR REPLACE FUNCTION article_search_vector_update() RETURNS trigger AS $$
BEGIN
    NEW.search_vector := to_tsvector('simple', coalesce(NEW.title, ''));
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION section_search_vector_update() RETURNS trigger AS $$
BEGIN
    NEW.search_vector := to_tsvector('simple', coalesce(NEW.title, '') || ' ' || coalesce(NEW.markdown, ''));
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_article_search_vector BEFORE INSERT OR UPDATE ON article
    FOR EACH ROW EXECUTE FUNCTION article_search_vector_update();

CREATE TRIGGER trg_section_search_vector BEFORE INSERT OR UPDATE ON section
    FOR EACH ROW EXECUTE FUNCTION section_search_vector_update();

-- seed data for tests
INSERT INTO tag (id, name, slug) VALUES
    (gen_random_uuid(), 'PostgreSQL', 'postgresql');

WITH seed_article AS (
    INSERT INTO article (id, title, slug, status, version, created_by)
    VALUES (gen_random_uuid(), 'Welcome to Instructions', 'welcome', 'PUBLISHED', 1, 'system')
    RETURNING id
)
INSERT INTO chapter (id, article_id, title, order_index)
SELECT gen_random_uuid(), id, 'Introduction', 0 FROM seed_article;

WITH seed_article AS (
    SELECT id FROM article WHERE slug = 'welcome'
), seed_chapter AS (
    SELECT id FROM chapter WHERE article_id = (SELECT id FROM seed_article) LIMIT 1
)
INSERT INTO section (id, chapter_id, title, order_index, markdown)
SELECT gen_random_uuid(), (SELECT id FROM seed_chapter), 'Getting Started', 0, 'This is the first section with helpful tips.';

WITH seed_article AS (
    SELECT id FROM article WHERE slug = 'welcome'
), seed_tag AS (
    SELECT id FROM tag WHERE slug = 'postgresql'
)
INSERT INTO article_tag (article_id, tag_id)
SELECT (SELECT id FROM seed_article), (SELECT id FROM seed_tag);
