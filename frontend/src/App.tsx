import { useEffect, useState } from 'react';
import { fetchInstructions, Instruction } from './api';
import './App.css';

function App() {
  const [instructions, setInstructions] = useState<Instruction[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    fetchInstructions()
      .then((data) => {
        setInstructions(data);
        setError(null);
      })
      .catch((err: Error) => {
        setError(err.message);
      })
      .finally(() => setLoading(false));
  }, []);

  return (
    <div className="app">
      <header className="hero">
        <div className="hero__inner">
          <h1>Инструкции</h1>
          <p>Быстрый старт по развитию проекта: бэкенд, фронтенд и рабочие процессы.</p>
          <p className="hero__hint">
            Бэкенд доступен по адресу <code>http://localhost:8080</code>, фронтенд — <code>http://localhost:5173</code>.
          </p>
        </div>
      </header>
      <main className="content">
        {loading && <div className="state">Загружаем инструкции…</div>}
        {error && !loading && <div className="state state--error">Не удалось получить данные: {error}</div>}
        {!loading && !error && (
          <ul className="instructions">
            {instructions.map((instruction) => (
              <li key={instruction.id} className="instructions__item">
                <article>
                  <h2>{instruction.title}</h2>
                  <div className="instructions__meta">
                    <span>Создано: {new Date(instruction.createdAt).toLocaleString()}</span>
                    <span>Обновлено: {new Date(instruction.updatedAt).toLocaleString()}</span>
                  </div>
                  <div className="instructions__content">
                    {instruction.content.split('\n').map((paragraph, index) => (
                      <p key={index}>{paragraph}</p>
                    ))}
                  </div>
                </article>
              </li>
            ))}
          </ul>
        )}
      </main>
    </div>
  );
}

export default App;
