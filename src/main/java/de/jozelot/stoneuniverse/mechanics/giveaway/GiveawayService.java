package de.jozelot.stoneuniverse.mechanics.giveaway;

import de.jozelot.stoneuniverse.StoneUniverse;

import java.util.*;

public class GiveawayService {

    private final StoneUniverse bot;
    private final GiveawayUI giveawayUI;

    private final List<Giveaway> giveaways = new ArrayList<>();

    public GiveawayService(StoneUniverse bot) {
        this.bot = bot;
        this.giveawayUI = new GiveawayUI(bot);
    }

    public Giveaway createGiveaway(long creatorId, String titel, String description, int entryLimit, int winnerCount, long drawDate, long channelId) {
        String uniqueId = UUID.randomUUID().toString().split("-")[0];

        Giveaway giveaway = new Giveaway(uniqueId, creatorId, titel, description, entryLimit, winnerCount, drawDate, channelId);
        giveaways.add(giveaway);
        return giveaway;
    }

    public Giveaway getGiveawayById(String id) {
        return giveaways.stream()
                .filter(g -> g.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    public GiveawayUI getGiveawayUI() {
        return giveawayUI;
    }
}
