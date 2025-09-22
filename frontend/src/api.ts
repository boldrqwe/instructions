export interface InstructionCategory {
  id: string;
  slug: string;
  title: string;
  description: string;
  icon: string;
}

export interface InstructionSummary {
  id: string;
  slug: string;
  title: string;
  summary: string;
  difficulty: string;
  estimatedMinutes: number;
  category: InstructionCategory;
  tags: string[];
  updatedAt: string;
}

export interface InstructionSection {
  id: string;
  position: number;
  title: string;
  content: string;
  codeTitle?: string | null;
  codeLanguage?: string | null;
  codeSnippet?: string | null;
  ctaLabel?: string | null;
  ctaUrl?: string | null;
}

export interface InstructionResource {
  id: string;
  position: number;
  type: string;
  title: string;
  description: string;
  url: string;
}

export interface InstructionDetail extends InstructionSummary {
  introduction: string;
  prerequisites?: string | null;
  sections: InstructionSection[];
  resources: InstructionResource[];
  createdAt: string;
}

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8080';

export async function fetchInstructionSummaries(): Promise<InstructionSummary[]> {
  const response = await fetch(`${API_BASE_URL}/api/instructions`);

  if (!response.ok) {
    throw new Error('Ошибка при загрузке каталога инструкций');
  }

  return response.json();
}

export async function fetchInstructionDetail(slug: string): Promise<InstructionDetail> {
  const response = await fetch(`${API_BASE_URL}/api/instructions/slug/${slug}`);

  if (!response.ok) {
    throw new Error('Не удалось загрузить подробную инструкцию');
  }

  return response.json();
}
