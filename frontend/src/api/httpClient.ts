export interface HttpClient {
  get<T>(path: string): Promise<T>;
  post<T>(path: string, body?: unknown): Promise<T>;
}

const baseUrl = import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8080/api/v1';

interface RequestOptions {
  method?: 'GET' | 'POST';
  body?: unknown;
}

interface ProblemDetails {
  type?: string;
  title?: string;
  status?: number;
  detail?: string;
}

class HttpError extends Error {
  constructor(
    message: string,
    public readonly status: number,
    public readonly problem?: ProblemDetails,
  ) {
    super(message);
    this.name = 'HttpError';
  }
}

async function readProblemDetails(response: Response): Promise<ProblemDetails | undefined> {
  const contentType = response.headers.get('content-type') ?? '';

  if (contentType.includes('application/json') || contentType.includes('application/problem+json')) {
    try {
      return (await response.json()) as ProblemDetails;
    } catch {
      return undefined;
    }
  }

  const detail = await response.text();
  if (!detail) {
    return undefined;
  }

  return {
    title: response.statusText || 'Request failed',
    detail,
    status: response.status,
  };
}

async function request<T>(path: string, options: RequestOptions = {}): Promise<T> {
  const response = await fetch(`${baseUrl}${path}`, {
    method: options.method ?? 'GET',
    headers: {
      Accept: 'application/json',
      ...(options.body === undefined
        ? {}
        : {
            'Content-Type': 'application/json',
          }),
    },
    body: options.body === undefined ? undefined : JSON.stringify(options.body),
  });

  if (!response.ok) {
    const problem = await readProblemDetails(response);
    const message = problem?.detail ?? problem?.title ?? `Request failed with status ${response.status}`;
    throw new HttpError(message, response.status, problem);
  }

  if (response.status === 204) {
    return undefined as T;
  }

  const text = await response.text();
  if (!text) {
    return undefined as T;
  }

  return JSON.parse(text) as T;
}

export const httpClient: HttpClient = {
  get: (path) => request(path),
  post: (path, body) => request(path, { method: 'POST', body }),
};
