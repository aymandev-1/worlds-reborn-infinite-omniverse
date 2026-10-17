package com.worldsreborn.core.api;

/**
 * PLANNED extension point (not used yet): describes a franchise campaign mod such as Marvel, DC or Ben 10 so the
 * interface can list campaigns and their category. Franchise-specific systems (Omnitrix selection, vehicles,
 * spacecraft, suit progression) will hang off this once those campaigns exist.
 */
public interface FranchiseModule {
    String id();

    /** Category such as "comics", "animation", "video_games". */
    String category();

    String titleKey();

    /** First quest of the campaign. */
    String openingQuestId();
}
