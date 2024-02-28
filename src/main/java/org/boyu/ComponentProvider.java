package org.boyu;

import java.util.List;

interface ComponentProvider<U> {
    U get(Context context);

    List<Class<?>> getDependencies();
}
