package de.jozelot.stoneuniverse.mechanics.levelSystem;

import de.jozelot.stoneuniverse.StoneUniverse;
import de.jozelot.stoneuniverse.registry.CommandRegistry;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public class UserLevel {

    private final StoneUniverse bot;
    private static final Logger logger = LoggerFactory.getLogger(UserLevel.class);

    private final long userId;
    private int xp;
    private int level;
    private long lastXpGain;

    public UserLevel(StoneUniverse bot, long userId, int xp, int level) {
        this.bot = bot;
        this.userId = userId;
        this.xp = xp;
        this.level = level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public void addLevel(int amount) {
        level += amount;
    }

    public void removeLevel(int amount) {
        level -= amount;
    }


    public boolean addMessageXp(int min, int max) {
        // Random XP schub
        int xpGained = ThreadLocalRandom.current().nextInt(min, max);
        xp += xpGained;
        return checkLevel();
    }

    public int getXpNeeded() {
        return  5 * (level * level) + (50 * level) + 100;
    }

    public boolean checkLevel() {
        int xpNeeded = 5 * (level * level) + (50 * level) + 100;
        boolean leveledUp = false;

        // Multi Level XP Möglich
        while (this.xp >= xpNeeded) {
            this.xp -= xpNeeded;
            this.level++;
            leveledUp = true;

            xpNeeded = 5 * (level * level) + (50 * level) + 100;
        }
        if (leveledUp) checkForRoleReward();

        return leveledUp;
    }

    public long getUserId() {
        return userId;
    }

    public int getXp() {
        return xp;
    }

    public int getLevel() {
        return level;
    }

    public void setLastXpGain(long xpGain) {
        this.lastXpGain = xpGain;
    }

    public long getLastXpGain() {
        return lastXpGain;
    }

    public void checkForRoleReward() {
        var config = bot.getBootstrap().getConfig();
        var shardManager = bot.getBootstrap().getBotManager().getShardManager();
        Map<Integer, Long> rewards = config.getSystem().getLevel().getRoleRewards();

        if (!rewards.containsKey(level)) {
            return;
        }

        long targetRoleId = rewards.get(level);

        Guild guild = shardManager.getGuildById(config.getBotConf().getGuildId());
        if (guild == null) return;

        Member member = guild.getMemberById(userId);
        if (member == null) return;

        Role targetRole = guild.getRoleById(targetRoleId);
        if (targetRole == null) {
            logger.error("Role reward for level {} defined in config (ID: {}) does not exist on Discord!", level, targetRoleId);
            return;
        }

        for (long oldRoleId : rewards.values()) {
            if (oldRoleId == targetRoleId) {
                continue;
            }

            Role oldRole = guild.getRoleById(oldRoleId);
            if (oldRole != null && member.getRoles().contains(oldRole)) {
                guild.removeRoleFromMember(member, oldRole).queue();
            }
        }

        if (!member.getRoles().contains(targetRole)) {
            guild.addRoleToMember(member, targetRole).queue();
        }
    }

    public boolean isEligibleForXp() {
        Member member = bot.getBootstrap().getBotManager().getShardManager().getGuildById(bot.getBootstrap().getConfig().getBotConf().getGuildId()).getMemberById(userId);
        if (member.getUser().isBot()) return false;
        if (member.getVoiceState() == null) return false;

        var voiceChannel = member.getVoiceState().getChannel();
        if (voiceChannel == null) return false;

        if (voiceChannel.getMembers().size() < 2) return false;

        if (member.getVoiceState().isMuted() || member.getVoiceState().isDeafened()) return false;

        return true;
    }
}
