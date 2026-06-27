import type { CalculationIssue, CalculationBreakdownItem } from '../types';

type Severity = 'danger' | 'warning' | 'info';

export interface PresentationIssue extends CalculationIssue {
  severity: Severity;
}

export interface PresentationMetric {
  label: string;
  value: string;
  detail?: string;
  tone?: Severity;
}

export function formatText(value: unknown, fallback = '—'): string {
  if (value === null || value === undefined) {
    return fallback;
  }

  const text = String(value).trim();
  return text.length > 0 ? text : fallback;
}

export function formatBoolean(value: boolean | undefined | null, trueLabel = 'Sí', falseLabel = 'No'): string {
  if (value === undefined || value === null) {
    return '—';
  }

  return value ? trueLabel : falseLabel;
}

export function formatCount(value: number | undefined | null): string {
  if (value === undefined || value === null || Number.isNaN(value)) {
    return '—';
  }

  return new Intl.NumberFormat('es-ES').format(value);
}

export function formatCp(value: number | string | undefined | null): string {
  if (value === undefined || value === null) {
    return '—';
  }

  return `${value} cp`;
}

export function formatDecimal(value: number | string | undefined | null): string {
  return formatText(value);
}

export function formatList(values: Array<string | null | undefined>, fallback = '—'): string {
  const normalized = values
    .map((value) => formatText(value, ''))
    .filter((value) => value.length > 0);

  return normalized.length > 0 ? normalized.join(' · ') : fallback;
}

export function buildIssue(issue: CalculationIssue, severity: Severity): PresentationIssue {
  return {
    ...issue,
    severity,
  };
}

export function combineIssues(
  warnings: CalculationIssue[] = [],
  blockers: CalculationIssue[] = [],
): PresentationIssue[] {
  return [
    ...blockers.map((issue) => buildIssue(issue, 'danger')),
    ...warnings.map((issue) => buildIssue(issue, 'warning')),
  ];
}

export function sortIssues(issues: PresentationIssue[]): PresentationIssue[] {
  return [...issues].sort((left, right) => {
    const severityWeight = { danger: 0, warning: 1, info: 2 } as const;
    const severityDelta = severityWeight[left.severity] - severityWeight[right.severity];
    if (severityDelta !== 0) {
      return severityDelta;
    }

    return left.code.localeCompare(right.code);
  });
}

export function severityLabel(severity: Severity): string {
  return severity === 'danger' ? 'Bloqueo' : severity === 'warning' ? 'Aviso' : 'Info';
}

export function toneClass(severity: Severity): string {
  return severity === 'danger' ? 'tone tone--danger' : severity === 'warning' ? 'tone tone--warning' : 'tone tone--info';
}

export function summarizeBreakdown(breakdown: CalculationBreakdownItem[]): Array<{
  concept: string;
  value: string;
  source: string;
  notes: string;
}> {
  return breakdown.map((item) => ({
    concept: item.concept,
    value: item.value,
    source: item.source,
    notes: item.notes ?? '—',
  }));
}

export function formatRatio(part: string | number | null | undefined, total: string | number | null | undefined): string {
  return `${formatText(part)} / ${formatText(total)}`;
}

export function formatNullableNumber(value: number | null | undefined): string {
  return value === null || value === undefined ? '—' : new Intl.NumberFormat('es-ES').format(value);
}
