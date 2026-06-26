import { createPinia, setActivePinia } from 'pinia';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { getCatalog } from '@/api/catalogApi';
import { useCatalogStore } from './catalogStore';

vi.mock('@/api/catalogApi', () => ({
  getCatalog: vi.fn(),
}));

describe('useCatalogStore', () => {
  beforeEach(() => {
    setActivePinia(createPinia());
    vi.clearAllMocks();
  });

  it('loads one catalog and exposes it as the active catalog', async () => {
    vi.mocked(getCatalog).mockResolvedValue({
      catalogName: 'cart-types',
      title: 'Tipos de carro',
      description: 'Catálogo de carros base y campaña',
      versionId: 'catalogs-v1',
      campaignAware: true,
      entries: [],
    });

    const store = useCatalogStore();

    await store.loadCatalog('cart-types');

    expect(store.status).toBe('ready');
    expect(store.activeCatalog?.catalogName).toBe('cart-types');
    expect(store.catalogs['cart-types']).toBeDefined();
  });
});
