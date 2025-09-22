import { FormEvent, useEffect, useMemo, useState } from 'react';
import {
  createInstruction,
  fetchInstructionCategories,
  InstructionCategory,
  InstructionDetail,
  InstructionPayload,
} from './api';

interface InstructionFormProps {
  onCreated: (instruction: InstructionDetail) => void;
  onCancel: () => void;
}

interface SectionDraft {
  title: string;
  content: string;
  codeTitle: string;
  codeLanguage: string;
  codeSnippet: string;
  ctaLabel: string;
  ctaUrl: string;
}

interface ResourceDraft {
  type: string;
  title: string;
  description: string;
  url: string;
}

const defaultSectionDraft = (): SectionDraft => ({
  title: '',
  content: '',
  codeTitle: '',
  codeLanguage: '',
  codeSnippet: '',
  ctaLabel: '',
  ctaUrl: '',
});

const defaultResourceDraft = (): ResourceDraft => ({
  type: '',
  title: '',
  description: '',
  url: '',
});

function InstructionForm({ onCreated, onCancel }: InstructionFormProps) {
  const [categories, setCategories] = useState<InstructionCategory[]>([]);
  const [loadingCategories, setLoadingCategories] = useState(true);
  const [categoryError, setCategoryError] = useState<string | null>(null);

  const [slug, setSlug] = useState('');
  const [title, setTitle] = useState('');
  const [summary, setSummary] = useState('');
  const [introduction, setIntroduction] = useState('');
  const [difficulty, setDifficulty] = useState('beginner');
  const [estimatedMinutes, setEstimatedMinutes] = useState(30);
  const [categorySlug, setCategorySlug] = useState('');
  const [prerequisites, setPrerequisites] = useState('');
  const [tagsText, setTagsText] = useState('');
  const [sections, setSections] = useState<SectionDraft[]>([defaultSectionDraft()]);
  const [resources, setResources] = useState<ResourceDraft[]>([]);

  const [submitting, setSubmitting] = useState(false);
  const [submitError, setSubmitError] = useState<string | null>(null);
  const [submitSuccess, setSubmitSuccess] = useState<string | null>(null);

  useEffect(() => {
    setLoadingCategories(true);
    fetchInstructionCategories()
      .then((loaded) => {
        setCategories(loaded);
        setCategoryError(null);
        if (loaded.length > 0) {
          setCategorySlug((current) => (current ? current : loaded[0].slug));
        }
      })
      .catch((err: Error) => {
        setCategoryError(err.message);
      })
      .finally(() => setLoadingCategories(false));
  }, []);

  const parsedTags = useMemo(
    () =>
      tagsText
        .split(',')
        .map((tag) => tag.trim())
        .filter((tag) => tag.length > 0),
    [tagsText],
  );

  const canSubmit = useMemo(() => {
    if (!slug.trim() || !title.trim() || !summary.trim() || !introduction.trim()) {
      return false;
    }
    if (!categorySlug) {
      return false;
    }
    if (sections.length === 0) {
      return false;
    }
    if (!sections.every((section) => section.title.trim() && section.content.trim())) {
      return false;
    }
    const resourceDraftsValid = resources.every((resource) => {
      const values = [resource.type, resource.title, resource.description, resource.url];
      const filled = values.filter((value) => value.trim().length > 0);
      return filled.length === 0 || filled.length === values.length;
    });
    return resourceDraftsValid;
  }, [slug, title, summary, introduction, categorySlug, sections, resources]);

  const handleSectionChange = (index: number, field: keyof SectionDraft, value: string) => {
    setSections((prev) => {
      const next = [...prev];
      next[index] = { ...next[index], [field]: value };
      return next;
    });
  };

  const handleResourceChange = (index: number, field: keyof ResourceDraft, value: string) => {
    setResources((prev) => {
      const next = [...prev];
      next[index] = { ...next[index], [field]: value };
      return next;
    });
  };

  const resetForm = () => {
    setSlug('');
    setTitle('');
    setSummary('');
    setIntroduction('');
    setDifficulty('beginner');
    setEstimatedMinutes(30);
    setPrerequisites('');
    setTagsText('');
    setSections([defaultSectionDraft()]);
    setResources([]);
  };

  const handleSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    if (!canSubmit || submitting) {
      return;
    }

    setSubmitting(true);
    setSubmitError(null);
    setSubmitSuccess(null);

    const payload: InstructionPayload = {
      slug: slug.trim(),
      title: title.trim(),
      summary: summary.trim(),
      introduction,
      difficulty,
      estimatedMinutes,
      categorySlug,
      prerequisites: prerequisites.trim() ? prerequisites.trim() : null,
      tags: parsedTags,
      sections: sections.map((section) => ({
        title: section.title.trim(),
        content: section.content,
        codeTitle: section.codeTitle.trim() || null,
        codeLanguage: section.codeLanguage.trim() || null,
        codeSnippet: section.codeSnippet ? section.codeSnippet : null,
        ctaLabel: section.ctaLabel.trim() || null,
        ctaUrl: section.ctaUrl.trim() || null,
      })),
      resources: resources
        .filter((resource) => resource.type || resource.title || resource.description || resource.url)
        .map((resource) => ({
          type: resource.type.trim(),
          title: resource.title.trim(),
          description: resource.description.trim(),
          url: resource.url.trim(),
        })),
    };

    try {
      const created = await createInstruction(payload);
      setSubmitSuccess('Инструкция успешно сохранена.');
      resetForm();
      onCreated(created);
    } catch (err) {
      setSubmitError(err instanceof Error ? err.message : 'Произошла непредвиденная ошибка');
    } finally {
      setSubmitting(false);
    }
  };

  if (loadingCategories) {
    return <div className="state">Загружаем данные редактора…</div>;
  }

  if (categoryError) {
    return <div className="state state--error">{categoryError}</div>;
  }

  return (
    <form className="editor" onSubmit={handleSubmit}>
      <div className="editor__header">
        <div>
          <h2>Новая инструкция</h2>
          <p>
            Заполните обязательные поля и опишите шаги руководства. Текст и примеры можно оформлять с помощью синтаксиса,
            как в Markdown.
          </p>
        </div>
        <div className="editor__actions">
          <button type="button" className="button button--ghost" onClick={onCancel} disabled={submitting}>
            Отмена
          </button>
          <button type="submit" className="button button--primary" disabled={!canSubmit || submitting}>
            {submitting ? 'Сохраняем…' : 'Сохранить инструкцию'}
          </button>
        </div>
      </div>

      <div className="editor__grid">
        <label className="editor__field">
          <span>Slug*</span>
          <input
            className="input"
            type="text"
            value={slug}
            onChange={(event) => setSlug(event.target.value)}
            placeholder="html-building-blocks"
            autoComplete="off"
          />
        </label>
        <label className="editor__field">
          <span>Заголовок*</span>
          <input
            className="input"
            type="text"
            value={title}
            onChange={(event) => setTitle(event.target.value)}
            placeholder="Название инструкции"
          />
        </label>
        <label className="editor__field editor__field--wide">
          <span>Краткое описание*</span>
          <textarea
            className="input input--textarea"
            value={summary}
            onChange={(event) => setSummary(event.target.value)}
            placeholder="Пара предложений, чтобы заинтересовать читателя"
            rows={3}
          />
        </label>
        <label className="editor__field editor__field--wide">
          <span>Введение*</span>
          <textarea
            className="input input--textarea input--code"
            value={introduction}
            onChange={(event) => setIntroduction(event.target.value)}
            placeholder="Опишите цель и итог инструкции"
            rows={6}
          />
        </label>
        <label className="editor__field">
          <span>Сложность*</span>
          <select
            className="input"
            value={difficulty}
            onChange={(event) => setDifficulty(event.target.value)}
          >
            <option value="beginner">Новичок</option>
            <option value="intermediate">Средний уровень</option>
            <option value="advanced">Продвинутый</option>
          </select>
        </label>
        <label className="editor__field">
          <span>Время, мин*</span>
          <input
            className="input"
            type="number"
            min={1}
            value={estimatedMinutes}
            onChange={(event) => setEstimatedMinutes(Number(event.target.value))}
          />
        </label>
        <label className="editor__field">
          <span>Категория*</span>
          <select
            className="input"
            value={categorySlug}
            onChange={(event) => setCategorySlug(event.target.value)}
          >
            {categories.map((category) => (
              <option key={category.id} value={category.slug}>
                {category.title}
              </option>
            ))}
          </select>
        </label>
        <label className="editor__field">
          <span>Предподготовка</span>
          <textarea
            className="input input--textarea"
            value={prerequisites}
            onChange={(event) => setPrerequisites(event.target.value)}
            placeholder="Что нужно подготовить заранее"
            rows={3}
          />
        </label>
        <label className="editor__field editor__field--wide">
          <span>Теги (через запятую)</span>
          <input
            className="input"
            type="text"
            value={tagsText}
            onChange={(event) => setTagsText(event.target.value)}
            placeholder="frontend, react"
          />
        </label>
      </div>

      <section className="editor__group">
        <div className="editor__group-header">
          <h3>Шаги*</h3>
          <button
            type="button"
            className="button button--ghost"
            onClick={() => setSections((prev) => [...prev, defaultSectionDraft()])}
          >
            Добавить шаг
          </button>
        </div>
        <p className="editor__hint">Каждая секция — это отдельный шаг руководства. Минимум один шаг с заголовком и содержанием.</p>
        {sections.map((section, index) => (
          <div className="editor__section" key={index}>
            <div className="editor__section-header">
              <h4>
                Шаг {index + 1}
              </h4>
              {sections.length > 1 && (
                <button
                  type="button"
                  className="button button--ghost"
                  onClick={() => setSections((prev) => prev.filter((_, pos) => pos !== index))}
                >
                  Удалить
                </button>
              )}
            </div>
            <label className="editor__field editor__field--wide">
              <span>Заголовок шага*</span>
              <input
                className="input"
                type="text"
                value={section.title}
                onChange={(event) => handleSectionChange(index, 'title', event.target.value)}
              />
            </label>
            <label className="editor__field editor__field--wide">
              <span>Описание шага*</span>
              <textarea
                className="input input--textarea input--code"
                value={section.content}
                onChange={(event) => handleSectionChange(index, 'content', event.target.value)}
                rows={6}
              />
            </label>
            <details className="editor__optional">
              <summary>Код или дополнительные материалы</summary>
              <div className="editor__optional-grid">
                <label className="editor__field">
                  <span>Заголовок блока кода</span>
                  <input
                    className="input"
                    type="text"
                    value={section.codeTitle}
                    onChange={(event) => handleSectionChange(index, 'codeTitle', event.target.value)}
                  />
                </label>
                <label className="editor__field">
                  <span>Язык кода</span>
                  <input
                    className="input"
                    type="text"
                    value={section.codeLanguage}
                    onChange={(event) => handleSectionChange(index, 'codeLanguage', event.target.value)}
                  />
                </label>
                <label className="editor__field editor__field--wide">
                  <span>Код</span>
                  <textarea
                    className="input input--textarea input--code"
                    value={section.codeSnippet}
                    onChange={(event) => handleSectionChange(index, 'codeSnippet', event.target.value)}
                    rows={4}
                  />
                </label>
                <label className="editor__field">
                  <span>Текст кнопки</span>
                  <input
                    className="input"
                    type="text"
                    value={section.ctaLabel}
                    onChange={(event) => handleSectionChange(index, 'ctaLabel', event.target.value)}
                  />
                </label>
                <label className="editor__field">
                  <span>Ссылка кнопки</span>
                  <input
                    className="input"
                    type="text"
                    value={section.ctaUrl}
                    onChange={(event) => handleSectionChange(index, 'ctaUrl', event.target.value)}
                  />
                </label>
              </div>
            </details>
          </div>
        ))}
      </section>

      <section className="editor__group">
        <div className="editor__group-header">
          <h3>Дополнительные ресурсы</h3>
          <button
            type="button"
            className="button button--ghost"
            onClick={() => setResources((prev) => [...prev, defaultResourceDraft()])}
          >
            Добавить ресурс
          </button>
        </div>
        <p className="editor__hint">Ресурсы отображаются отдельным списком в конце инструкции.</p>
        {resources.length === 0 && <p className="editor__empty">Пока без дополнительных материалов.</p>}
        {resources.map((resource, index) => (
          <div className="editor__section" key={index}>
            <div className="editor__section-header">
              <h4>Ресурс {index + 1}</h4>
              <button
                type="button"
                className="button button--ghost"
                onClick={() => setResources((prev) => prev.filter((_, pos) => pos !== index))}
              >
                Удалить
              </button>
            </div>
            <div className="editor__optional-grid">
              <label className="editor__field">
                <span>Тип*</span>
                <input
                  className="input"
                  type="text"
                  value={resource.type}
                  onChange={(event) => handleResourceChange(index, 'type', event.target.value)}
                />
              </label>
              <label className="editor__field">
                <span>Название*</span>
                <input
                  className="input"
                  type="text"
                  value={resource.title}
                  onChange={(event) => handleResourceChange(index, 'title', event.target.value)}
                />
              </label>
              <label className="editor__field editor__field--wide">
                <span>Описание*</span>
                <textarea
                  className="input input--textarea"
                  value={resource.description}
                  onChange={(event) => handleResourceChange(index, 'description', event.target.value)}
                  rows={3}
                />
              </label>
              <label className="editor__field">
                <span>Ссылка*</span>
                <input
                  className="input"
                  type="text"
                  value={resource.url}
                  onChange={(event) => handleResourceChange(index, 'url', event.target.value)}
                />
              </label>
            </div>
          </div>
        ))}
      </section>

      {submitError && <div className="editor__feedback editor__feedback--error">{submitError}</div>}
      {submitSuccess && <div className="editor__feedback editor__feedback--success">{submitSuccess}</div>}
    </form>
  );
}

export default InstructionForm;
