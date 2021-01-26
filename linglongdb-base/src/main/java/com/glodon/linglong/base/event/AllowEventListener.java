package com.glodon.linglong.base.event;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;

/**
 * @author Stereo
 */
final class AllowEventListener extends FilteredEventListener {
    static EventListener make(EventListener listener, EventType.Category... categories) {
        final EventListener original = listener;

        Set<EventType.Category> categorySet;
        if (categories.length == 0) {
            categorySet = Collections.emptySet();
        } else {
            categorySet = new HashSet<>(Arrays.asList(categories));
        }

        Set<Level> levelSet;
        if (listener instanceof AllowEventListener) {
            AllowEventListener allowed = (AllowEventListener) listener;
            listener = allowed.mListener;
            categorySet.retainAll(allowed.mCategories);
            levelSet = allowed.mLevels;
        } else {
            levelSet = null;
        }

        return make(original, listener, categorySet, levelSet);
    }

    static EventListener make(EventListener listener, Level... levels) {
        final EventListener original = listener;

        Set<Level> levelSet;
        if (levels.length == 0) {
            levelSet = Collections.emptySet();
        } else {
            levelSet = new HashSet<>(Arrays.asList(levels));
        }

        Set<EventType.Category> categorySet;
        if (listener instanceof AllowEventListener) {
            AllowEventListener allowed = (AllowEventListener) listener;
            listener = allowed.mListener;
            levelSet.retainAll(allowed.mLevels);
            categorySet = allowed.mCategories;
        } else {
            categorySet = null;
        }

        return make(original, listener, categorySet, levelSet);
    }

    private static EventListener make(EventListener original, EventListener listener,
                                      Set<EventType.Category> categories, Set<Level> levels) {
        EventListener newListener = new AllowEventListener(listener, categories, levels);
        return newListener.equals(original) ? original : newListener;
    }

    AllowEventListener(EventListener listener,
                       Set<EventType.Category> categories, Set<Level> levels) {
        super(listener, categories, levels);
    }

    @Override
    public boolean isObserved(EventType.Category category) {
        return mCategories == null || mCategories.contains(category);
    }

    @Override
    public boolean isObserved(Level level) {
        return mLevels == null || mLevels.contains(level);
    }

    @Override
    protected boolean sameClass(Object obj) {
        return obj.getClass() == AllowEventListener.class;
    }
}
