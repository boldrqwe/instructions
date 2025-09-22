import { useEffect, useState } from 'react';
import {
  fetchInstructionDetail,
  fetchInstructionSummaries,
  InstructionDetail,
  InstructionSummary,
} from './api';
import './App.css';

function difficultyLabel(value: string): string {
  switch (value) {
    case 'beginner':
      return 'Новичок';
    case 'intermediate':
      return 'Средний уровень';
    case 'advanced':
      return 'Продвинутый';
    default:
      return value;
  }
}

function formatMinutes(minutes: number): string {
  return `${minutes} мин`;
}

function App() {
  const [summaries, setSummaries] = useState<InstructionSummary[]>([]);
  const [selected, setSelected] = useState<InstructionDetail | null>(null);
  const [activeSlug, setActiveSlug] = useState<string | null>(null);
  const [loadingCatalog, setLoadingCatalog] = useState(true);
  const [loadingDetail, setLoadingDetail] = useState(false);
  const [catalogError, setCatalogError] = useState<string | null>(null);
  const [detailError, setDetailError] = useState<string | null>(null);

  useEffect(() => {
    fetchInstructionSummaries()
      .then((data) => {
        setSummaries(data);
        setCatalogError(null);
        if (data.length > 0) {
          selectInstruction(data[0].slug);
        }
      })
      .catch((err: Error) => {
        setCatalogError(err.message);
      })
      .finally(() => setLoadingCatalog(false));
  }, []);

  const selectInstruction = (slug: string) => {
    setActiveSlug(slug);
    setLoadingDetail(true);
    fetchInstructionDetail(slug)
      .then((detail) => {
        setSelected(detail);
        setDetailError(null);
      })
      .catch((err: Error) => {
        setDetailError(err.message);
      })
      .finally(() => setLoadingDetail(false));
  };

  return (
    <div className="app">
      <header className="hero">
        <div className="hero__inner">
          <h1>Учебник проекта</h1>
          <p>
            Каталог практических инструкций по сборке фронтенда, настройке бэкенда и инфраструктуры — в стиле
            современных образовательных порталов.
          </p>
          <p className="hero__hint">
            Бэкенд: <code>http://localhost:8080</code> · Фронтенд: <code>http://localhost:5173</code>
          </p>
        </div>
      </header>
      <main className="layout">
        <section className="catalog" aria-label="Каталог инструкций">
          <header className="catalog__header">
            <h2>Руководства</h2>
            <p>
              Выберите тему, чтобы увидеть подробный туториал, кодовые примеры и дополнительные ресурсы — как на
              W3Schools или MDN Learn.
            </p>
          </header>
          {loadingCatalog && <div className="state">Загружаем каталог…</div>}
          {catalogError && !loadingCatalog && <div className="state state--error">{catalogError}</div>}
          {!loadingCatalog && !catalogError && (
            <ul className="catalog__list">
              {summaries.map((summary) => (
                <li key={summary.id}>
                  <button
                    type="button"
                    className={`catalog__card ${summary.slug === activeSlug ? 'catalog__card--active' : ''}`}
                    onClick={() => selectInstruction(summary.slug)}
                  >
                    <span className="catalog__category" aria-hidden="true">
                      {summary.category.icon}
                    </span>
                    <div className="catalog__card-content">
                      <h3>{summary.title}</h3>
                      <p className="catalog__summary">{summary.summary}</p>
                      <div className="catalog__meta">
                        <span className={`badge badge--${summary.difficulty}`}>
                          {difficultyLabel(summary.difficulty)}
                        </span>
                        <span>{formatMinutes(summary.estimatedMinutes)}</span>
                        <span>{summary.category.title}</span>
                      </div>
                      <ul className="catalog__tags">
                        {summary.tags.map((tag) => (
                          <li key={tag}>#{tag}</li>
                        ))}
                      </ul>
                    </div>
                  </button>
                </li>
              ))}
            </ul>
          )}
        </section>
        <section className="details" aria-live="polite">
          {loadingDetail && <div className="state">Готовим туториал…</div>}
          {detailError && !loadingDetail && <div className="state state--error">{detailError}</div>}
          {!loadingDetail && !detailError && selected && (
            <article>
              <header className="details__header">
                <div className="details__category">
                  <span className="details__icon" aria-hidden="true">
                    {selected.category.icon}
                  </span>
                  <div>
                    <h2>{selected.title}</h2>
                    <p>{selected.category.title}</p>
                  </div>
                </div>
                <div className="details__meta">
                  <span className={`badge badge--${selected.difficulty}`}>
                    {difficultyLabel(selected.difficulty)}
                  </span>
                  <span>{formatMinutes(selected.estimatedMinutes)}</span>
                  <span>Обновлено: {new Date(selected.updatedAt).toLocaleDateString()}</span>
                </div>
                <ul className="details__tags">
                  {selected.tags.map((tag) => (
                    <li key={tag}>#{tag}</li>
                  ))}
                </ul>
              </header>
              <section className="details__intro">
                {selected.introduction.split('\n').map((paragraph, index) => (
                  <p key={index}>{paragraph}</p>
                ))}
                {selected.prerequisites && (
                  <aside className="details__prerequisites">
                    <strong>Предварительно подготовьте:</strong>
                    <p>{selected.prerequisites}</p>
                  </aside>
                )}
              </section>
              <section className="details__sections">
                <h3>Шаги</h3>
                <ol>
                  {selected.sections.map((section) => (
                    <li key={section.id}>
                      <article className="details__section">
                        <h4>{section.title}</h4>
                        {section.content.split('\n').map((paragraph, index) => (
                          <p key={index}>{paragraph}</p>
                        ))}
                        {section.codeSnippet && (
                          <div className="details__code">
                            {section.codeTitle && <div className="details__code-title">{section.codeTitle}</div>}
                            <pre>
                              <code>{section.codeSnippet}</code>
                            </pre>
                            {section.codeLanguage && (
                              <span className="details__code-language">{section.codeLanguage}</span>
                            )}
                          </div>
                        )}
                        {section.ctaLabel && section.ctaUrl && (
                          <a
                            className="details__cta"
                            href={section.ctaUrl}
                            target="_blank"
                            rel="noreferrer"
                          >
                            {section.ctaLabel}
                          </a>
                        )}
                      </article>
                    </li>
                  ))}
                </ol>
              </section>
              {selected.resources.length > 0 && (
                <section className="details__resources">
                  <h3>Дополнительные материалы</h3>
                  <ul>
                    {selected.resources.map((resource) => (
                      <li key={resource.id}>
                        <a href={resource.url} target="_blank" rel="noreferrer">
                          <span className={`badge badge--neutral`}>{resource.type}</span>
                          <span className="details__resource-title">{resource.title}</span>
                          <span className="details__resource-description">{resource.description}</span>
                        </a>
                      </li>
                    ))}
                  </ul>
                </section>
              )}
            </article>
          )}
          {!loadingDetail && !detailError && !selected && !loadingCatalog && (
            <div className="state">Выберите инструкцию слева, чтобы увидеть подробности.</div>
          )}
        </section>
      </main>
    </div>
  );
}

export default App;
