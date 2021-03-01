package com.linglong.engine.event;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;

/**
 * @author Stereo
 */
final class DisallowEventListener extends FilteredEventListener {
    static EventListener make(EventListener listener, EventType.Category... categories) {
        if (categories.length == 0) {
            return listener;
        }

        final EventListener original = listener;

        Set<EventType.Category> categorySet = new HashSet<>(Arrays.asList(categories));

        Set<Level> levelSet;
        if (listener instanceof DisallowEventListener) {
            DisallowEventListener disallowed = (DisallowEventListener) listener;
            listener = disallowed.mListener;
            categorySet.addAll(disallowed.mCategories);
            levelSet = disallowed.mLevels;
        } else {
            levelSet = null;
        }

        return make(original, listener, categorySet, levelSet);
    }

    static EventListener make(EventListener listener, Level... levels) {
        if (levels.length == 0) {
            return listener;
        }

        final EventListener original = listener;

        Set<Level> levelSet = new HashSet<>(Arrays.asList(levels));

        Set<EventType.Category> categorySet;
        if (listener instanceof DisallowEventListener) {
            DisallowEventListener disallowed = (DisallowEventListener) listener;
            listener = disallowed.mListener;
            levelSet.addAll(disallowed.mLevels);
            categorySet = disallowed.mCategories;
        } else {
            categorySet = null;
        }

        return make(original, listener, categorySet, levelSet);
    }

    private static EventListener make(EventListener original, EventListener listener,
                                      Set<EventType.Category> categories, Set<Level> levels) {
        EventListener newListener = new DisallowEventListener(listener, categories, levels);
        return newListener.equals(original) ? original : newListener;
    }

    DisallowEventListener(EventListener listener,
                          Set<EventType.Category> categories, Set<Level> levels) {
        super(listener, categories, levels);
    }

    @Override
    public boolean isObserved(EventType.Category category) {
        return mCategories == null || !mCategories.contains(category);
    }

    @Override
    public boolean isObserved(Level level) {
        return mLevels == null || !mLevels.contains(level);
    }

    @Override
    protected boolean sameClass(Object obj) {
        return obj.getClass() == DisallowEventListener.class;
    }
}
