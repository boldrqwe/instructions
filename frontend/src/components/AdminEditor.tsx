import { useEffect, useMemo, useState } from "react";
import type { FormEvent } from "react";
import ReactQuill from "react-quill";
import hljs from "highlight.js";
import "highlight.js/styles/github-dark-dimmed.css";
import "react-quill/dist/quill.snow.css";
import "./AdminEditor.css";
import type { Article } from "../types";
import type { ApiError } from "../services/articles";
import {
  createArticle,
  deleteArticle,
  listArticles,
  updateArticle,
} from "../services/articles";
import { ArticleList } from "./ArticleList";

interface AdminEditorProps {
  authToken: string;
  onLogout: () => void;
  onAuthExpired: (message: string) => void;
}

function sortArticles(articles: Article[]): Article[] {
  return [...articles].sort(
    (first, second) =>
      new Date(second.updatedAt).getTime() - new Date(first.updatedAt).getTime(),
  );
}

function stripHtml(value: string): string {
  return value.replace(/<[^>]+>/g, "").trim();
}

function isUnauthorizedError(error: unknown): error is ApiError {
  return typeof (error as ApiError | undefined)?.status === "number"
    && (error as ApiError).status === 401;
}

function getErrorMessage(error: unknown): string {
  if (error instanceof Error) {
    return error.message;
  }

  return "Не удалось выполнить действие. Попробуйте ещё раз.";
}

export function AdminEditor({ authToken, onLogout, onAuthExpired }: AdminEditorProps) {
  const [articles, setArticles] = useState<Article[]>([]);
  const [selectedId, setSelectedId] = useState<number | null>(null);
  const [title, setTitle] = useState("");
  const [content, setContent] = useState("");
  const [loadingList, setLoadingList] = useState(false);
  const [isSaving, setIsSaving] = useState(false);
  const [message, setMessage] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);

  const quillModules = useMemo(
    () => ({
      toolbar: [
        [{ header: [1, 2, 3, 4, false] }],
        ["bold", "italic", "underline", "strike"],
        [{ color: [] }, { background: [] }],
        [{ script: "sub" }, { script: "super" }],
        [{ list: "ordered" }, { list: "bullet" }],
        [{ indent: "-1" }, { indent: "+1" }],
        [{ align: [] }],
        ["blockquote", "code-block"],
        ["link", "image", "video"],
        ["clean"],
      ],
      syntax: {
        highlight: (text: string) => hljs.highlightAuto(text).value,
      },
    }),
    [],
  );

  useEffect(() => {
    async function bootstrap() {
      setLoadingList(true);
      try {
        const fetchedArticles = await listArticles();
        setArticles(sortArticles(fetchedArticles));
      } catch (loadError) {
        setError(getErrorMessage(loadError));
      } finally {
        setLoadingList(false);
      }
    }

    void bootstrap();
  }, []);

  const isEditing = selectedId !== null;
  const canDelete = isEditing && !isSaving;

  async function refreshArticles() {
    setLoadingList(true);
    try {
      const fetchedArticles = await listArticles();
      const ordered = sortArticles(fetchedArticles);
      setArticles(ordered);

      if (selectedId !== null) {
        const current = ordered.find((article) => article.id === selectedId);
        if (current) {
          setTitle(current.title);
          setContent(current.content);
        } else {
          resetForm();
        }
      }
    } catch (refreshError) {
      setError(getErrorMessage(refreshError));
    } finally {
      setLoadingList(false);
    }
  }

  function resetForm() {
    setSelectedId(null);
    setTitle("");
    setContent("");
  }

  function handleNewArticle() {
    resetForm();
    setMessage(null);
    setError(null);
  }

  function handleSelectArticle(article: Article) {
    setSelectedId(article.id);
    setTitle(article.title);
    setContent(article.content);
    setMessage(null);
    setError(null);
  }

  function resolveActionError(actionError: unknown): string {
    if (isUnauthorizedError(actionError)) {
      onAuthExpired("Сессия истекла. Войдите снова.");
      return "Сессия истекла. Пожалуйста, выполните вход заново.";
    }

    return getErrorMessage(actionError);
  }

  async function handleSave() {
    const trimmedTitle = title.trim();
    const plainContent = stripHtml(content);

    if (!trimmedTitle.length) {
      setError("Введите заголовок статьи.");
      return;
    }

    if (!plainContent.length) {
      setError("Введите содержимое статьи.");
      return;
    }

    setIsSaving(true);
    setError(null);

    try {
      const payload = { title: trimmedTitle, content };
      let saved: Article;

      if (selectedId === null) {
        saved = await createArticle(payload, authToken);
      } else {
        saved = await updateArticle(selectedId, payload, authToken);
      }

      setArticles((prev) =>
        sortArticles([saved, ...prev.filter((article) => article.id !== saved.id)]),
      );
      setSelectedId(saved.id);
      setTitle(saved.title);
      setContent(saved.content);
      setMessage(selectedId === null ? "Статья создана" : "Статья обновлена");
    } catch (saveError) {
      setError(resolveActionError(saveError));
      setMessage(null);
    } finally {
      setIsSaving(false);
    }
  }

  async function handleDelete() {
    if (selectedId === null) {
      return;
    }

    if (!window.confirm("Удалить выбранную статью?")) {
      return;
    }

    setIsSaving(true);
    setError(null);

    try {
      await deleteArticle(selectedId, authToken);
      setArticles((prev) => prev.filter((article) => article.id !== selectedId));
      resetForm();
      setMessage("Статья удалена");
    } catch (deleteError) {
      setError(resolveActionError(deleteError));
      setMessage(null);
    } finally {
      setIsSaving(false);
    }
  }

  function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    void handleSave();
  }

  return (
    <div className="admin-editor">
      <header className="admin-editor__header">
        <div className="admin-editor__title">
          <h1>Конструктор инструкций</h1>
          <p className="admin-editor__subtitle">
            Создавайте, редактируйте и публикуйте статьи с богатыми форматами.
          </p>
        </div>
        <button type="button" className="admin-editor__logout" onClick={onLogout}>
          Выйти
        </button>
      </header>
      <main className="admin-editor__content">
        <ArticleList
          articles={articles}
          selectedId={selectedId}
          loading={loadingList}
          onSelect={handleSelectArticle}
          onCreate={handleNewArticle}
          onRefresh={() => {
            void refreshArticles();
          }}
        />
        <section className="editor">
          <div className="editor__header">
            <h2>{isEditing ? "Редактирование статьи" : "Новая статья"}</h2>
            {message && (
              <span className="editor__status editor__status--success">{message}</span>
            )}
            {error && (
              <span className="editor__status editor__status--error">{error}</span>
            )}
          </div>
          <form className="editor__form" onSubmit={handleSubmit}>
            <label className="editor__field">
              <span>Заголовок</span>
              <input
                type="text"
                value={title}
                onChange={(event) => setTitle(event.target.value)}
                placeholder="Например, как развернуть приложение"
                disabled={isSaving}
                required
              />
            </label>
            <label className="editor__field">
              <span>Содержимое</span>
              <ReactQuill
                theme="snow"
                value={content}
                onChange={setContent}
                modules={quillModules}
                formats={[
                  "header",
                  "bold",
                  "italic",
                  "underline",
                  "strike",
                  "color",
                  "background",
                  "script",
                  "list",
                  "indent",
                  "align",
                  "blockquote",
                  "code-block",
                  "link",
                  "image",
                  "video",
                ]}
                placeholder="Опишите шаги, добавьте изображения и ссылки..."
              />
            </label>
            <div className="editor__actions">
              <button type="submit" className="primary" disabled={isSaving}>
                {isSaving ? "Сохранение…" : "Сохранить"}
              </button>
              <button
                type="button"
                onClick={handleNewArticle}
                className="secondary"
                disabled={isSaving}
              >
                Очистить
              </button>
              <button
                type="button"
                onClick={() => {
                  void handleDelete();
                }}
                className="danger"
                disabled={!canDelete}
              >
                Удалить
              </button>
            </div>
          </form>
        </section>
      </main>
    </div>
  );
}
