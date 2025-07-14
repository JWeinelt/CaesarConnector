package de.julianweinelt.caesar.reports;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import de.codeblocksmc.codelib.wrapping.GuiBuilder;
import de.codeblocksmc.codelib.wrapping.ItemBuilder;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.*;

public class ReportView implements Listener {
    private int guiRows;
    private JsonObject data;

    private final HashMap<UUID, String> openMenus = new HashMap<>();
    private final HashMap<UUID, UUID> playerSelection = new HashMap<>();

    public static ReportView instance() {
        return ReportManager.instance().getView();
    }

    public ReportView prepare(JsonObject jsonData) {
        if (!jsonData.has("viewPortSize") || !jsonData.has("menus")) {
            throw new IllegalArgumentException("Provided JSON data is missing one or more of these properties: viewPortSize, menus");
        }
        guiRows = jsonData.get("viewPortSize").getAsInt();
        data = jsonData;
        return this;
    }

    public void openPlayerSelection(Player player) {
        GuiBuilder builder = new GuiBuilder("§aReport a player", (int) Math.min(Math.ceil(Bukkit.getOnlinePlayers().size() / 9.0), 6));
        int i = 0;
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (i >= 9*6-2) {
                builder.slot(9*6-2, new ItemBuilder(Material.STICK).displayname("§aPrevious page"));
                builder.slot(9*6-1, new ItemBuilder(Material.ARROW).displayname("§aNext page"));
                break;
            }
            ItemStack head = new ItemBuilder(Material.PLAYER_HEAD).displayname("§c" + p.getName()).build();
            SkullMeta meta = (SkullMeta) head.getItemMeta();
            meta.setPlayerProfile(p.getPlayerProfile());
            head.setItemMeta(meta);
            builder.slot(i, head);
            i++;
        }

        builder.openForPlayer(player);
        openMenus.put(player.getUniqueId(), "playerSelection");
    }

    public void registerSelection(UUID selector, UUID reported) {
        playerSelection.put(selector, reported);
    }

    public void openMenu(Player player, String menu) {
        GuiBuilder builder = new GuiBuilder("Create a new report", guiRows);
        ReportMenu m = getMenu(menu);
        for (ReportItem item : m.getSlots().values()) {
            builder.slot(item.getSlot(), item.build());
        }
        builder.openForPlayer(player);
        openMenus.put(player.getUniqueId(), menu);
    }

    private ReportItem getItem(String menu, int slot) {
        ReportItem item = getMenu(menu).getSlots().getOrDefault(slot, null);
        if (item == null) throw new IllegalArgumentException("Slot " + slot + " is not a valid item.");
        return item;
    }

    private ReportMenu getMenu(String menu) {
        JsonArray menus = data.get("menus").getAsJsonArray();
        for (JsonElement m : menus) {
            if (menu.equals(m.getAsJsonObject().get("name").getAsString())) {
                ReportMenu me = new ReportMenu(m.getAsJsonObject().get("name").getAsString());
                JsonArray slots = m.getAsJsonObject().get("slots").getAsJsonArray();
                for (JsonElement slot : slots) {
                    ReportItem item = ReportItem.create(slot.getAsJsonObject());
                    me.slots.put(item.getSlot(), item);
                }
                return me;
            }
        }
        throw new IllegalArgumentException("Could not find menu with name " + menu);
    }


    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player)) return;
        if (e.getCurrentItem() == null) return;
        Player player = (Player) e.getWhoClicked();
        if (openMenus.getOrDefault(e.getWhoClicked().getUniqueId(), "none").equals("none")) {
            return;
        } else if (openMenus.getOrDefault(e.getWhoClicked().getUniqueId(), "none").equals("playerSelection")) {
            e.setCancelled(true);
            if (e.getCurrentItem().getType().equals(Material.PLAYER_HEAD)) {
                PlayerProfile profile = ((SkullMeta) e.getCurrentItem().getItemMeta()).getPlayerProfile();
                if (profile == null) return;
                playerSelection.put(player.getUniqueId(), profile.getId());
                openMenu(player, "Main");
            }
        } else {
            e.setCancelled(true);
            ReportMenu menu = getMenu(openMenus.get(e.getWhoClicked().getUniqueId()));
            try {
                ReportItem item = getItem(menu.getMenuID(), e.getRawSlot());
                ReportItemActionType type = item.getActionType();
                String property = item.getActionProperty();
                if (type.equals(ReportItemActionType.OPEN_MENU)) {
                    openMenu(player, property);
                } else if (type.equals(ReportItemActionType.CREATE_REPORT)) {

                }
            } catch (IllegalArgumentException ignored) {}

        }
    }


    public enum ReportItemActionType {
        OPEN_MENU("menuID"),
        CREATE_REPORT("reportType"),
        UNKNOWN(""),
        ;

        public final String propertyName;

        ReportItemActionType(String propertyName) {
            this.propertyName = propertyName;
        }
    }

    @Getter
    public static class ReportItem {
        private final int slot;
        private final String displayName;
        private final Material material;
        private final List<String> lore;
        private final boolean glint;
        private ReportItemActionType actionType;
        private String actionProperty;

        public ReportItem(ReportItemActionType actionType, int slot, String displayName, Material material, List<String> lore,
                          boolean glint, String actionProperty) {
            this.actionType = actionType;
            this.slot = slot;
            this.displayName = displayName;
            this.material = material;
            this.lore = lore;
            this.glint = glint;
            this.actionProperty = actionProperty;
        }

        public ItemStack build() {
            ItemBuilder builder = new ItemBuilder(material);
            builder.displayname(displayName);
            builder.lore(lore);
            builder.flags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_POTION_EFFECTS, ItemFlag.HIDE_UNBREAKABLE);
            if (glint) builder.enchant(Enchantment.SILK_TOUCH, 1).flag(ItemFlag.HIDE_ENCHANTS);
            return builder.build();
        }

        public static ReportItem create(JsonObject jsonData) {
            ReportItemActionType type = ReportItemActionType.valueOf(jsonData.get("action").getAsString());
            List<String> lore = new ArrayList<>();

            for (JsonElement je : jsonData.get("lore").getAsJsonArray()) {
                lore.add(je.getAsString());
            }
            return new ReportItem(
                    type,
                    jsonData.get("slot").getAsInt(),
                    jsonData.get("display").getAsString(),
                    Material.valueOf(jsonData.get("material").getAsString()),
                    lore,
                    jsonData.get("glint").getAsBoolean(),
                    jsonData.get("actionProperties").getAsJsonObject().get(type.propertyName).getAsString()
            );
        }
    }

    @Getter
    public static class ReportMenu {
        private final String menuID;

        private final HashMap<Integer, ReportItem> slots = new HashMap<>();

        public ReportMenu(String menuID) {
            this.menuID = menuID;
        }
    }
}