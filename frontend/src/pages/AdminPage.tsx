import { useEffect, useRef, useState } from "react";
import type { FormEvent } from "react";
import { Link } from "react-router-dom";
import { AdminEditor } from "../components/AdminEditor";
import { verifyAdmin } from "../services/articles";
import "./AdminPage.css";

const STORAGE_KEY = "instructions.adminAuthToken";

function getStoredToken(): string | null {
  if (typeof window === "undefined") {
    return null;
  }

  return window.sessionStorage.getItem(STORAGE_KEY);
}

export function AdminPage() {
  const initialTokenRef = useRef<string | null>(getStoredToken());
  const [authToken, setAuthToken] = useState<string | null>(initialTokenRef.current);
  const [isCheckingStoredToken, setIsCheckingStoredToken] = useState<boolean>(
    () => initialTokenRef.current !== null,
  );
  const [statusMessage, setStatusMessage] = useState<string | null>(null);
  const [loginError, setLoginError] = useState<string | null>(null);
  const [username, setUsername] = useState("admin");
  const [password, setPassword] = useState("");
  const [isSubmitting, setIsSubmitting] = useState(false);

  function persistToken(token: string) {
    if (typeof window !== "undefined") {
      window.sessionStorage.setItem(STORAGE_KEY, token);
    }

    initialTokenRef.current = token;
    setAuthToken(token);
    setStatusMessage(null);
    setLoginError(null);
  }

  function clearToken(message?: string) {
    if (typeof window !== "undefined") {
      window.sessionStorage.removeItem(STORAGE_KEY);
    }

    initialTokenRef.current = null;
    setAuthToken(null);
    setPassword("");

    if (message) {
      setStatusMessage(message);
    }
  }

  useEffect(() => {
    const token = initialTokenRef.current;
    if (!token) {
      setIsCheckingStoredToken(false);
      return;
    }

    verifyAdmin(token)
      .then(() => {
        setIsCheckingStoredToken(false);
        setAuthToken(token);
      })
      .catch(() => {
        clearToken("Сессия истекла. Войдите снова.");
        setIsCheckingStoredToken(false);
      });
  }, []);

  async function handleLogin(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setLoginError(null);
    setStatusMessage(null);
    setIsSubmitting(true);

    try {
      const normalizedUsername = username.trim();
      const credentials = window.btoa(`${normalizedUsername}:${password}`);
      await verifyAdmin(credentials);
      persistToken(credentials);
      setPassword("");
    } catch (authError) {
      setLoginError(
        authError instanceof Error
          ? authError.message
          : "Не удалось войти. Проверьте логин и пароль.",
      );
    } finally {
      setIsSubmitting(false);
    }
  }

  function handleLogout() {
    clearToken("Вы вышли из режима редактирования.");
  }

  function handleAuthExpired(message: string) {
    clearToken(message);
  }

  if (isCheckingStoredToken) {
    return (
      <div className="admin-page admin-page--loading">
        <div className="admin-page__spinner" aria-live="polite">
          Проверка доступа…
        </div>
      </div>
    );
  }

  if (authToken) {
    return (
      <AdminEditor authToken={authToken} onLogout={handleLogout} onAuthExpired={handleAuthExpired} />
    );
  }

  return (
    <div className="admin-page">
      <div className="admin-page__card">
        <h1 className="admin-page__title">Вход для редактора</h1>
        <p className="admin-page__description">
          Используйте учётные данные администратора, чтобы создавать и редактировать инструкции.
        </p>
        {statusMessage && (
          <p className="admin-page__message admin-page__message--info">{statusMessage}</p>
        )}
        {loginError && (
          <p className="admin-page__message admin-page__message--error">{loginError}</p>
        )}
        <form className="admin-page__form" onSubmit={handleLogin}>
          <label className="admin-page__field">
            <span>Логин</span>
            <input
              type="text"
              value={username}
              onChange={(event) => setUsername(event.target.value)}
              autoComplete="username"
              required
            />
          </label>
          <label className="admin-page__field">
            <span>Пароль</span>
            <input
              type="password"
              value={password}
              onChange={(event) => setPassword(event.target.value)}
              autoComplete="current-password"
              required
            />
          </label>
          <button type="submit" className="admin-page__submit" disabled={isSubmitting}>
            {isSubmitting ? "Проверка…" : "Войти"}
          </button>
        </form>
        <Link className="admin-page__link" to="/">
          ← Вернуться к опубликованным инструкциям
        </Link>
      </div>
    </div>
  );
}
