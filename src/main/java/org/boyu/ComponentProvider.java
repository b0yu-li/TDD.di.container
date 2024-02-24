package org.boyu;

interface ComponentProvider<U> {
    U get(Context context);
}
