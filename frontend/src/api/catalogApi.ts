import { httpClient } from '@/api/httpClient';
import type { CatalogName, CatalogResponse } from '@/types/catalog';

export async function getCatalog(catalogName: CatalogName): Promise<CatalogResponse> {
  return httpClient.get<CatalogResponse>(`/catalogs/${catalogName}`);
}
