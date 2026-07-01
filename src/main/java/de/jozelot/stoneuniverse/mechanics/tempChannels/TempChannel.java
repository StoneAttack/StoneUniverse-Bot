package de.jozelot.stoneuniverse.mechanics.tempChannels;

public class TempChannel {

    private long ownerId;
    private final long channelId;

    public TempChannel(long ownerId, long channelId) {
        this.ownerId = ownerId;
        this.channelId = channelId;
    }

    public long getOwnerId() {
        return ownerId;
    }

    public long getChannelId() {
        return channelId;
    }

    public void setNewOwner(long id) {
        this.ownerId = id;
    }
}
