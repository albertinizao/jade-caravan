package com.jadecaravan.domain.campaign;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

final class DomainCollections {

    private DomainCollections() {
    }

    static <T> List<T> immutableCopy(List<T> values) {
        return values == null ? List.of() : List.copyOf(values);
    }

    static <K, V> Map<K, V> immutableCopy(Map<K, V> values) {
        return values == null ? Map.of() : Map.copyOf(values);
    }

    static <T> List<T> append(List<T> values, T value) {
        ArrayList<T> copy = new ArrayList<>(immutableCopy(values));
        copy.add(value);
        return List.copyOf(copy);
    }

    static <T> List<T> replace(List<T> values, Predicate<? super T> predicate, T value) {
        ArrayList<T> copy = new ArrayList<>(immutableCopy(values));
        int index = -1;
        for (int i = 0; i < copy.size(); i++) {
            if (predicate.test(copy.get(i))) {
                index = i;
                break;
            }
        }
        if (index >= 0) {
            copy.set(index, value);
        } else {
            copy.add(value);
        }
        return List.copyOf(copy);
    }

    static <T> List<T> filter(List<T> values, Predicate<? super T> predicate) {
        return immutableCopy(values).stream().filter(predicate).toList();
    }
}
