1. Core data structure

You need a SlayerTask class:

monsterName / npcIds[]

requiredLevel

minAmount / maxAmount

experience per kill

slayerMaster

And you need something like:

Map<Integer, SlayerTask> tasks; // or a list grouped by master + level ranges

2. Player data

In your Player class add:

int slayerLevel

SlayerTask currentTask

int taskAmount

int taskProgress

SlayerMaster currentMaster

Store/load these in your save file.

3. Assignment flow

When a player talks to a master:

Check their Slayer level.

Pick a random valid task from the task list for that level range.

Set:

player.currentTask = pickedTask

player.taskAmount = random(minAmount, maxAmount)

player.taskProgress = 0

Send a message like “You have been assigned to kill 87 blue dragons.”

4. Kill detection

In NPC death:

Check if the NPC’s ID matches player.currentTask.npcIds.

If yes:

player.taskProgress++

Give slayer XP.

If taskProgress >= taskAmount then complete.

5. Task completion

On completion:

Give points

Reset currentTask

Let the master give a new task

Track streaks (optional)

6. Slayer points

Most sources do this:

+1 to streak

If streak >= 5, award points

Boss tasks give more points

Cancel/block system uses points

7. Dialogue

Masters need simple dialogue handlers:

Assign task

What’s my current task

Can I cancel/block

How many points do I have

8. Files you usually need

Depending on base:

Slayer.java or SlayerHandler.java

SlayerTask.java

SlayerMaster.java

NPC death hook (usually in NPCHandler)

Save/load extensions in PlayerSave

9. Optional features

Add later:

Superior monsters

Task expansions

Wilderness tasks

Konar tasks with regional filtering

Weighted task selection

Unlocks/perks

Block list

Skip list


How to add Slayer in Alter
1. Create a SlayerTask enum or JSON

Alter uses enums for most constant content, so do:

com.alter.content.slayer.SlayerTask.java

Example:

public enum SlayerTask {

    CRAWLING_HAND(
        new int[]{1648, 1649, 1650},
        5,
        15,
        30
    ),

    BANSHEE(
        new int[]{1618},
        15,
        20,
        40
    );

    public final int[] npcIds;
    public final int requiredLevel;
    public final int min;
    public final int max;

    SlayerTask(int[] npcIds, int requiredLevel, int min, int max) {
        this.npcIds = npcIds;
        this.requiredLevel = requiredLevel;
        this.min = min;
        this.max = max;
    }
}


Keep it short. Add more tasks later.

2. Add Slayer data to Player

Go to Player.java and add:

private SlayerTask slayerTask;
private int slayerAmount;
private int slayerProgress;


Add the matching getters/setters.

3. Save and load

Alter uses PlayerSave or JSON serialization. Add entries:

builder.add("slayerTask", slayerTask == null ? -1 : slayerTask.ordinal());
builder.add("slayerAmount", slayerAmount);
builder.add("slayerProgress", slayerProgress);


And on load:

int t = obj.get("slayerTask").getAsInt();
slayerTask = t == -1 ? null : SlayerTask.values()[t];
slayerAmount = obj.get("slayerAmount").getAsInt();
slayerProgress = obj.get("slayerProgress").getAsInt();

4. Create SlayerMaster enum

SlayerMaster.java:

public enum SlayerMaster {
    TURAEL(3),
    VANNAKA(40);

    public final int requiredLevel;

    SlayerMaster(int requiredLevel) {
        this.requiredLevel = requiredLevel;
    }
}

5. Add a SlayerHandler

SlayerHandler.java:

public class SlayerHandler {

    public static void assign(Player p, SlayerMaster m) {

        List<SlayerTask> valid = Arrays.stream(SlayerTask.values())
                .filter(task -> p.getSkills().getLevel(Skills.SLAYER) >= task.requiredLevel)
                .collect(Collectors.toList());

        if (valid.isEmpty()) {
            p.sendMessage("You do not meet the requirements for any task.");
            return;
        }

        SlayerTask task = valid.get(Utils.random(valid.size() - 1));

        int amount = Utils.random(task.min, task.max);

        p.setSlayerTask(task);
        p.setSlayerAmount(amount);
        p.setSlayerProgress(0);

        p.sendMessage("Your new task is to kill " + amount + " " + format(task.name()).toLowerCase() + ".");
    }

    public static void onNpcDeath(Player p, int npcId) {
        SlayerTask task = p.getSlayerTask();
        if (task == null) return;

        if (IntStream.of(task.npcIds).anyMatch(id -> id == npcId)) {
            p.setSlayerProgress(p.getSlayerProgress() + 1);

            p.getSkills().addXp(Skills.SLAYER, 20);

            if (p.getSlayerProgress() >= p.getSlayerAmount()) {
                p.sendMessage("You have completed your task.");
                p.setSlayerTask(null);
            }
        }
    }

    private static String format(String s) {
        return s.replace("_", " ");
    }
}

6. Hook into NPC death

Alter usually has something like:

NPCHandler.java or NpcDeath.java.

Add:

SlayerHandler.onNpcDeath(player, npc.getId());


right after XP drops are handled.

7. Hook into dialogue

For a master NPC, add a dialogue option:

SlayerHandler.assign(player, SlayerMaster.VANNAKA);


This is normally done through Alter’s dialogue system in Dialogues.java.

8. Test flow

Add Slayer skill to your XP table if not already there.

Talk to Vannaka.

Get a task.

Kill an assigned NPC.

Watch progress increase.

Finish task, ensure reset works properly.