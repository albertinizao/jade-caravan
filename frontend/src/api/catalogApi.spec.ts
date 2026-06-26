import { beforeEach, describe, expect, it, vi } from 'vitest';
import { httpClient } from '@/api/httpClient';
import { getCatalog } from './catalogApi';

vi.mock('@/api/httpClient', () => ({
  httpClient: {
    get: vi.fn(),
    post: vi.fn(),
  },
}));

describe('catalogApi', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('loads a catalog from the versioned endpoint', async () => {
    vi.mocked(httpClient.get).mockResolvedValue({
      catalogName: 'cart-types',
      title: 'Tipos de carro',
      description: 'Catálogo de carros base y campaña',
      versionId: 'catalogs-v1',
      campaignAware: true,
      entries: [],
    });

    await getCatalog('cart-types');

    expect(httpClient.get).toHaveBeenCalledWith('/catalogs/cart-types');
  });
});
