package com.akapps.dailynote.classes.helpers;

import androidx.annotation.Nullable;

import io.realm.Case;
import io.realm.RealmQuery;

public class RealmQueryBuilder<E> {

    private RealmQuery<E> query;

    public RealmQueryBuilder(RealmQuery<E> query) {
        this.query = query;
    }

    public RealmQueryBuilder<E> containsAll(String fieldName, @Nullable String[] values, Case casing) {
        if ((values == null) || (values.length == 0)) {
            query.alwaysFalse();
        } else {
            for (String value : values) {
                if (value != null) {
                    if (casing == Case.SENSITIVE) {
                        query = query.contains(fieldName, value, Case.SENSITIVE).and();
                    } else {
                        query = query.contains(fieldName, value, Case.INSENSITIVE).and();
                    }
                }
            }
            // Remove the final unnecessary ".and()" if any
            query = query.and();
        }
        return this;
    }

    public RealmQueryBuilder<E> or() {
        query = query.or(); // Use "or" to combine with the next condition
        return this;
    }

    public RealmQuery<E> getQuery() {
        return query;
    }
}
