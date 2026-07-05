/**
 * Utilitaire d'export CSV configurable.
 * Permet d'exporter n'importe quel tableau de données en fichier CSV
 * avec sélection des colonnes et formatage personnalisé.
 */

export interface CSVColumn<T = Record<string, unknown>> {
  /** Clé dans l'objet source */
  key: string;
  /** En-tête de la colonne dans le CSV */
  label: string;
  /** Fonction optionnelle pour formater la valeur */
  format?: (value: unknown, row: T) => string;
}

/**
 * Génère et télécharge un fichier CSV à partir d'un tableau d'objets.
 *
 * @param data      Tableau de données à exporter
 * @param columns   Définition des colonnes (ordre, label, formatage)
 * @param filename  Nom du fichier (sans extension)
 *
 * @example
 * ```tsx
 * exportCSV(users, [
 *   { key: 'id', label: 'ID' },
 *   { key: 'email', label: 'Email' },
 *   { key: 'createdAt', label: 'Date', format: (v) => new Date(v as string).toLocaleDateString() }
 * ], 'utilisateurs');
 * ```
 */
export function exportCSV<T extends Record<string, unknown>>(
  data: T[],
  columns: CSVColumn<T>[],
  filename: string
): void {
  if (!data.length || !columns.length) return;

  // 1. En-têtes
  const header = columns.map((col) => escapeCSV(col.label)).join(';');

  // 2. Lignes de données
  const rows = data.map((row) =>
    columns
      .map((col) => {
        const value = col.format ? col.format(row[col.key], row) : row[col.key];
        return escapeCSV(value);
      })
      .join(';')
  );

  // 3. Assemblage
  const bom = '\uFEFF'; // BOM UTF-8 pour Excel
  const csv = bom + [header, ...rows].join('\r\n');

  // 4. Téléchargement
  const blob = new Blob([csv], { type: 'text/csv;charset=utf-8;' });
  const url = URL.createObjectURL(blob);
  const link = document.createElement('a');
  link.href = url;
  link.download = `${filename}.csv`;
  document.body.appendChild(link);
  link.click();
  document.body.removeChild(link);
  URL.revokeObjectURL(url);
}

/**
 * Échappe une valeur pour le format CSV :
 * - Null/undefined → chaîne vide
 * - Contient séparateur, guillemet ou saut de ligne → encapsulé entre guillemets
 * - Guillemets doublés
 */
function escapeCSV(value: unknown): string {
  if (value === null || value === undefined) return '';
  const str = String(value);
  if (str.includes(';') || str.includes('"') || str.includes('\n') || str.includes('\r')) {
    return `"${str.replace(/"/g, '""')}"`;
  }
  return str;
}
