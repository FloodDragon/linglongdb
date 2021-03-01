package com.linglong.engine.event;

import java.util.Objects;
import java.util.Set;
import java.util.logging.Level;

/**
 * @author Stereo
 */
abstract class FilteredEventListener extends SafeEventListener {
    protected final Set<EventType.Category> mCategories;
    protected final Set<Level> mLevels;

    FilteredEventListener(EventListener listener,
                          Set<EventType.Category> categories, Set<Level> levels) {
        super(listener);
        mCategories = categories;
        mLevels = levels;
    }

    @Override
    final boolean shouldNotify(EventType type) {
        return isObserved(type.category) && isObserved(type.level);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj != null && sameClass(obj)) {
            FilteredEventListener other = (FilteredEventListener) obj;
            return mListener.equals(other.mListener)
                    && Objects.equals(mCategories, other.mCategories)
                    && Objects.equals(mLevels, other.mLevels);
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = super.hashCode();
        hash = hash * 31 + Objects.hashCode(mCategories);
        hash = hash * 31 + Objects.hashCode(mLevels);
        return hash;
    }

    protected abstract boolean sameClass(Object obj);
}
