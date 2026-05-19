export type PatientTagSeverity = 'success' | 'secondary' | 'info' | 'warn' | 'danger' | 'contrast';

export interface PatientStatusCard {
  icon: string;
  label: string;
  value: string;
}

export interface PatientHealthSummaryItem {
  label: string;
  value: string;
}

export interface PatientEmptyPanelConfig {
  kicker: string;
  title: string;
  icon: string;
  description: string;
  heading?: string;
  tagLabel?: string;
  tagSeverity?: PatientTagSeverity;
  actionLabel?: string;
  actionIcon?: string;
  large?: boolean;
}
