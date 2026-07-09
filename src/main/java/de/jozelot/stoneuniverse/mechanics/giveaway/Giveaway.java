package de.jozelot.stoneuniverse.mechanics.giveaway;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class Giveaway {
    private final String id;
    private final long creatorId;
    private String titel;
    private String description;
    private int entryLimit;
    private int winnerCount;
    private long drawDate;

    private final long channelId;
    private long messageId;
    private boolean ended = false;

    private final List<Long> entries = new ArrayList<>();
    private final List<Long> winner = new ArrayList<>();

    public Giveaway(String id, long creatorId, String titel, String description, int entryLimit, int winnerCount, long drawDate, long channelId) {
        this.id = id;
        this.creatorId = creatorId;
        this.titel = titel;
        this.description = description;
        this.entryLimit = entryLimit;
        this.winnerCount = winnerCount;
        this.drawDate = drawDate;
        this.channelId = channelId;
    }

    public boolean roll() {
        if (ended) return false;
        ended = true;
        if (entries.isEmpty()) {
            return true;
        }

        List<Long> pool = new ArrayList<>(this.entries);
        int amountToDraw = Math.min(winnerCount, pool.size());

        var random = ThreadLocalRandom.current();

        for (int i = 0; i < amountToDraw; i++) {
            int randomIndex = random.nextInt(pool.size());

            long winnerId = pool.remove(randomIndex);

            this.winner.add(winnerId);
        }

        return true;
    }

    public boolean cancel() {
        if (ended) return false;
        ended = true;
        return true;
    }

    public GiveawayEnterError addEntry(long userId) {
        if (hasEntered(userId)) {
            return GiveawayEnterError.ALREAD_IN;
        }
        if (entryLimit > 0 && entries.size() >= entryLimit) {
            return GiveawayEnterError.FULL;
        }
        entries.add(userId);
        return GiveawayEnterError.SUCCESS;
    }

    public boolean removeEntry(long userId) {
        return entries.remove(Long.valueOf(userId));
    }

    public boolean hasEntered(long userId) {
        return entries.contains(userId);
    }

    public int getEntryCount() {
        return entries.size();
    }

    public boolean setMessageId(long messageId) {
        if (this.messageId != 0) {
            return false;
        }
        this.messageId = messageId;
        return true;
    }

    public String getId() { return id; }
    public long getCreatorId() { return creatorId; }
    public String getTitel() { return titel; }
    public String getDescription() { return description; }
    public int getEntryLimit() { return entryLimit; }
    public long getDrawDate() { return drawDate; }
    public long getChannelId() { return channelId; }
    public long getMessageId() { return messageId; }
    public int getWinnerCount() {
        return winnerCount;
    }
    public List<Long> getEntries() {
        return Collections.unmodifiableList(entries);
    }
    public List<Long> getWinner() {
        return Collections.unmodifiableList(winner);
    }
    public boolean hasEnded() {
        return ended;
    }
}
