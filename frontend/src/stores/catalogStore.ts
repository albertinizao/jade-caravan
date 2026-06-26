import { defineStore } from 'pinia';
import { getCatalog } from '@/api/catalogApi';
import type { CatalogName, CatalogResponse } from '@/types/catalog';

const ALL_CATALOG_NAMES: CatalogName[] = ['cart-types', 'upgrades', 'cargo', 'roles', 'feats', 'beasts'];

interface CatalogState {
  catalogs: Partial<Record<CatalogName, CatalogResponse>>;
  status: 'idle' | 'loading' | 'ready' | 'error';
  activeCatalogName: CatalogName | null;
  errorMessage: string | null;
}

function toErrorMessage(error: unknown): string {
  if (error instanceof Error && error.message) {
    return error.message;
  }

  return 'No se han podido cargar los catálogos.';
}

export const useCatalogStore = defineStore('catalog', {
  state: (): CatalogState => ({
    catalogs: {},
    status: 'idle',
    activeCatalogName: null,
    errorMessage: null,
  }),
  getters: {
    activeCatalog: (state) =>
      state.activeCatalogName ? state.catalogs[state.activeCatalogName] ?? null : null,
  },
  actions: {
    async loadCatalog(catalogName: CatalogName) {
      this.status = 'loading';
      this.activeCatalogName = catalogName;
      this.errorMessage = null;

      try {
        const catalog = await getCatalog(catalogName);
        this.catalogs[catalogName] = catalog;
        this.status = 'ready';
        return catalog;
      } catch (error) {
        this.status = 'error';
        this.errorMessage = toErrorMessage(error);
        throw error;
      }
    },
    async loadAllCatalogs() {
      this.status = 'loading';
      this.errorMessage = null;

      try {
        const loadedCatalogs = await Promise.all(ALL_CATALOG_NAMES.map((catalogName) => getCatalog(catalogName)));
        this.catalogs = loadedCatalogs.reduce<Partial<Record<CatalogName, CatalogResponse>>>((accumulator, catalog) => {
          accumulator[catalog.catalogName] = catalog;
          return accumulator;
        }, {});
        this.activeCatalogName = this.activeCatalogName ?? 'cart-types';
        this.status = 'ready';
        return this.catalogs;
      } catch (error) {
        this.status = 'error';
        this.errorMessage = toErrorMessage(error);
        throw error;
      }
    },
    clearCatalogs() {
      this.catalogs = {};
      this.activeCatalogName = null;
      this.status = 'idle';
      this.errorMessage = null;
    },
  },
});
