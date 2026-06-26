export type CatalogName = 'cart-types' | 'upgrades' | 'cargo' | 'roles' | 'feats' | 'beasts';

export interface CatalogEntry {
  entryType: string;
  key: string;
  name: string;
  campaignSpecific: boolean;
  source: string;
  note?: string | null;
  attributes: Record<string, unknown>;
}

export interface CatalogResponse {
  catalogName: CatalogName;
  title: string;
  description: string;
  versionId: string;
  campaignAware: boolean;
  entries: CatalogEntry[];
}
