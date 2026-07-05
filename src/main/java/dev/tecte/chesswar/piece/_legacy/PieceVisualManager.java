/*
package dev.tecte.chesswar.piece;

import kr.toxicity.model.api.BetterModel;
import kr.toxicity.model.api.tracker.EntityHideOption;
import kr.toxicity.model.api.tracker.EntityTracker;
import kr.toxicity.model.api.tracker.EntityTrackerRegistry;
import kr.toxicity.model.api.tracker.TrackerUpdateAction;
import org.bukkit.entity.LivingEntity;

public class PieceVisualManager {
    public void setupModel(final LivingEntity entity) {
        final EntityTrackerRegistry registry = BetterModel.registry(entity.getUniqueId()).orElse(null);

        if (registry == null) {
            return;
        }

        for (final EntityTracker tracker : registry.trackers()) {
            tracker.hideOption(new EntityHideOption(true, true, true, true));
            tracker.update(TrackerUpdateAction.viewRange(1000f));
        }
    }

    public void snapModel(final LivingEntity entity) {
        final EntityTrackerRegistry registry = BetterModel.registry(entity.getUniqueId()).orElse(null);

        if (registry == null) {
            return;
        }

        for (final EntityTracker tracker : registry.trackers()) {
            tracker.update(TrackerUpdateAction.moveDuration(0));
            tracker.forceUpdate(true);
        }
    }
}
*/
