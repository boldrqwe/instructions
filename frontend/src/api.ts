export interface Instruction {
  id: string;
  title: string;
  content: string;
  createdAt: string;
  updatedAt: string;
}

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8080';

export async function fetchInstructions(): Promise<Instruction[]> {
  const response = await fetch(`${API_BASE_URL}/api/instructions`);

  if (!response.ok) {
    throw new Error('Ошибка при загрузке инструкций');
  }

  return response.json();
}
