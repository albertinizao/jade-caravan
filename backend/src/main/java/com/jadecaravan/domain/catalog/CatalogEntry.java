package com.jadecaravan.domain.catalog;

public interface CatalogEntry {

    String key();

    String name();

    boolean campaignSpecific();

    String source();

    String note();
}
