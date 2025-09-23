import type { Article, ArticleRequest } from "../types";

export interface ApiError extends Error {
  status?: number;
}

const API_BASE_URL = (import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080").replace(/\/$/, "");
const ARTICLES_ENDPOINT = `${API_BASE_URL}/api/articles`;
const ADMIN_ENDPOINT = `${API_BASE_URL}/api/admin/ping`;

async function parseError(response: Response): Promise<never> {
  let message: string | undefined;
  const clone = response.clone();

  try {
    const data = await clone.json();
    if (typeof data === "string") {
      message = data;
    } else if (data) {
      message = data.message || data.error || data.detail;
    }
  } catch {
    // ignore JSON parsing errors and fallback to reading text
  }

  if (!message) {
    try {
      const fallback = await response.text();
      if (fallback.length > 0) {
        message = fallback;
      }
    } catch {
      // ignore text parsing errors
    }
  }

  let finalMessage: string;
  if (response.status === 401) {
    finalMessage = "Ошибка авторизации. Повторите вход.";
  } else if (response.status === 403) {
    finalMessage = "Недостаточно прав для выполнения действия.";
  } else {
    finalMessage =
      message && message.length
        ? message
        : `Не удалось выполнить запрос (${response.status})`;
  }

  const error = new Error(finalMessage) as ApiError;
  error.status = response.status;
  throw error;
}

async function handleResponse<T>(response: Response): Promise<T> {
  if (!response.ok) {
    return parseError(response);
  }

  if (response.status === 204) {
    return undefined as T;
  }

  return (await response.json()) as T;
}

function authHeaders(authToken: string): Record<string, string> {
  return { Authorization: `Basic ${authToken}` };
}

export async function listArticles(): Promise<Article[]> {
  const response = await fetch(ARTICLES_ENDPOINT);
  return handleResponse<Article[]>(response);
}

export async function createArticle(
  payload: ArticleRequest,
  authToken: string,
): Promise<Article> {
  const response = await fetch(ARTICLES_ENDPOINT, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
      ...authHeaders(authToken),
    },
    body: JSON.stringify(payload),
  });

  return handleResponse<Article>(response);
}

export async function updateArticle(
  id: number,
  payload: ArticleRequest,
  authToken: string,
): Promise<Article> {
  const response = await fetch(`${ARTICLES_ENDPOINT}/${id}`, {
    method: "PUT",
    headers: {
      "Content-Type": "application/json",
      ...authHeaders(authToken),
    },
    body: JSON.stringify(payload),
  });

  return handleResponse<Article>(response);
}

export async function deleteArticle(id: number, authToken: string): Promise<void> {
  const response = await fetch(`${ARTICLES_ENDPOINT}/${id}`, {
    method: "DELETE",
    headers: authHeaders(authToken),
  });

  if (!response.ok) {
    await parseError(response);
  }
}

export async function verifyAdmin(authToken: string): Promise<void> {
  const response = await fetch(ADMIN_ENDPOINT, {
    headers: authHeaders(authToken),
  });

  if (!response.ok) {
    await parseError(response);
  }
}
