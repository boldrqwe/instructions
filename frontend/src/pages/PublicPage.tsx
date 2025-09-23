import { useEffect, useMemo, useRef, useState } from "react";
import { Link } from "react-router-dom";
import hljs from "highlight.js";
import "highlight.js/styles/github-dark-dimmed.css";
import { listArticles } from "../services/articles";
import type { Article } from "../types";
import "./PublicPage.css";

function sortArticles(articles: Article[]): Article[] {
  return [...articles].sort(
    (first, second) =>
      new Date(second.updatedAt).getTime() - new Date(first.updatedAt).getTime(),
  );
}

function formatDate(dateIso: string): string {
  const formatter = new Intl.DateTimeFormat("ru-RU", {
    dateStyle: "medium",
    timeStyle: "short",
  });

  try {
    return formatter.format(new Date(dateIso));
  } catch (error) {
    console.error("Не удалось отформатировать дату", error);
    return dateIso;
  }
}

async function copyToClipboard(text: string): Promise<boolean> {
  if (!text.length) {
    return false;
  }

  if (navigator.clipboard?.writeText) {
    try {
      await navigator.clipboard.writeText(text);
      return true;
    } catch {
      // fallback to execCommand
    }
  }

  try {
    const textarea = document.createElement("textarea");
    textarea.value = text;
    textarea.setAttribute("readonly", "true");
    textarea.style.position = "absolute";
    textarea.style.left = "-9999px";
    document.body.appendChild(textarea);
    textarea.select();
    const result = document.execCommand("copy");
    document.body.removeChild(textarea);
    return result;
  } catch {
    return false;
  }
}

export function PublicPage() {
  const [articles, setArticles] = useState<Article[]>([]);
  const [selectedId, setSelectedId] = useState<number | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const contentRef = useRef<HTMLDivElement>(null);

  const selectedArticle = useMemo(() => {
    if (selectedId === null) {
      return null;
    }

    return articles.find((article) => article.id === selectedId) ?? null;
  }, [articles, selectedId]);

  async function refreshArticles() {
    setLoading(true);
    try {
      const fetchedArticles = await listArticles();
      const ordered = sortArticles(fetchedArticles);
      setArticles(ordered);

      if (ordered.length === 0) {
        setSelectedId(null);
      } else {
        setSelectedId((current) => {
          if (current !== null && ordered.some((article) => article.id === current)) {
            return current;
          }

          return ordered[0].id;
        });
      }

      setError(null);
    } catch (loadError) {
      setError(
        loadError instanceof Error
          ? loadError.message
          : "Не удалось загрузить статьи. Попробуйте обновить страницу.",
      );
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    void refreshArticles();
  }, []);

  useEffect(() => {
    if (!selectedArticle || !contentRef.current) {
      return;
    }

    const container = contentRef.current;
    container.querySelectorAll(".code-block__copy-button").forEach((button) => button.remove());

    container.querySelectorAll("pre code").forEach((block) => {
      hljs.highlightElement(block as HTMLElement);
      const pre = block.parentElement;
      if (!pre) {
        return;
      }

      pre.classList.add("code-block");

      if (pre.querySelector(".code-block__copy-button")) {
        return;
      }

      const button = document.createElement("button");
      button.type = "button";
      button.className = "code-block__copy-button";
      button.textContent = "Копировать";
      button.addEventListener("click", async () => {
        const snippet = block.textContent ?? "";
        const success = await copyToClipboard(snippet);
        if (success) {
          button.textContent = "Скопировано!";
          button.classList.add("code-block__copy-button--copied");
        } else {
          button.textContent = "Не удалось";
          button.classList.add("code-block__copy-button--error");
        }

        window.setTimeout(() => {
          button.textContent = "Копировать";
          button.classList.remove(
            "code-block__copy-button--copied",
            "code-block__copy-button--error",
          );
        }, 2000);
      });
      pre.append(button);
    });
  }, [selectedArticle]);

  return (
    <div className="public-page">
      <header className="public-page__header">
        <div className="public-page__headline">
          <h1>База знаний компании</h1>
          <p>
            Набор проверенных инструкций, которые помогут сотрудникам и клиентам быстро решать
            рабочие задачи.
          </p>
        </div>
        <Link className="public-page__admin-link" to="/admin">
          Войти как администратор
        </Link>
      </header>
      <main className="public-page__content">
        <aside className="public-page__sidebar">
          <div className="public-page__sidebar-header">
            <h2>Статьи</h2>
            <button
              type="button"
              className="public-page__refresh"
              onClick={() => {
                void refreshArticles();
              }}
              disabled={loading}
              title="Обновить список"
            >
              ⟳
            </button>
          </div>
          {loading ? (
            <p className="public-page__status">Загрузка…</p>
          ) : error ? (
            <p className="public-page__status public-page__status--error">{error}</p>
          ) : articles.length === 0 ? (
            <p className="public-page__status">Статей пока нет.</p>
          ) : (
            <ul className="public-page__list">
              {articles.map((article) => (
                <li key={article.id}>
                  <button
                    type="button"
                    className={`public-page__item${
                      selectedId === article.id ? " public-page__item--active" : ""
                    }`}
                    onClick={() => setSelectedId(article.id)}
                  >
                    <span className="public-page__item-title">{article.title}</span>
                    <span className="public-page__item-date">
                      {formatDate(article.updatedAt)}
                    </span>
                  </button>
                </li>
              ))}
            </ul>
          )}
        </aside>
        <section className="article-viewer">
          {selectedArticle ? (
            <>
              <header className="article-viewer__header">
                <h2>{selectedArticle.title}</h2>
                <span className="article-viewer__meta">
                  Обновлено {formatDate(selectedArticle.updatedAt)}
                </span>
              </header>
              <div
                className="article-viewer__content"
                ref={contentRef}
                dangerouslySetInnerHTML={{ __html: selectedArticle.content }}
              />
            </>
          ) : (
            <div className="article-viewer__placeholder">
              <p>Выберите статью, чтобы прочитать её содержимое.</p>
            </div>
          )}
        </section>
      </main>
    </div>
  );
}
