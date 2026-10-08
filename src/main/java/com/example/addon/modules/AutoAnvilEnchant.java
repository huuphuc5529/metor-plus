package com.example.addon.modules;

import com.example.addon.AddonTemplate;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.ItemEnchantments;

import java.util.List;
import java.util.Set;

import static meteordevelopment.meteorclient.MeteorClient.mc;

/**
 * Auto Anvil Enchant
 * Minecraft 26.2 / Meteor 26.2
 */
public class AutoAnvilEnchant extends Module {
    private final SettingGroup sg = settings.getDefaultGroup();

    private final Setting<List<Item>> items = sg.add(new ItemListSetting.Builder()
        .name("items")
        .description("Chỉ enchant những đồ này.")
        .defaultValue(List.of(
            Items.DIAMOND_SWORD,
            Items.DIAMOND_PICKAXE,
            Items.DIAMOND_AXE,
            Items.DIAMOND_HELMET,
            Items.DIAMOND_CHESTPLATE,
            Items.DIAMOND_LEGGINGS,
            Items.DIAMOND_BOOTS,
            Items.NETHERITE_SWORD,
            Items.NETHERITE_PICKAXE,
            Items.NETHERITE_AXE,
            Items.NETHERITE_HELMET,
            Items.NETHERITE_CHESTPLATE,
            Items.NETHERITE_LEGGINGS,
            Items.NETHERITE_BOOTS
        ))
        .build());

    private final Setting<Set<ResourceKey<Enchantment>>> enchants =
        sg.add(new EnchantmentListSetting.Builder()
            .name("enchantments")
            .description("Chỉ dùng sách có những enchant này.")
            .defaultValue(Set.of(
                Enchantments.MENDING,
                Enchantments.UNBREAKING,
                Enchantments.SHARPNESS,
                Enchantments.PROTECTION,
                Enchantments.EFFICIENCY
            ))
            .build());

    private final Setting<Integer> delay = sg.add(new IntSetting.Builder()
        .name("delay")
        .description("Số tick chờ giữa các thao tác.")
        .defaultValue(3)
        .min(0)
        .sliderMax(20)
        .build());

    private final Setting<Boolean> autoTake = sg.add(new BoolSetting.Builder()
        .name("auto-take")
        .description("Tự lấy kết quả ra khỏi đe.")
        .defaultValue(true)
        .build());

    private int timer;
    private boolean done;

    public AutoAnvilEnchant() {
        super(
            AddonTemplate.CATEGORY,
            "auto-anvil-enchant",
            "Mở đe: tự đặt đồ đang cầm và sách enchant còn thiếu."
        );
    }

    @Override
    public void onActivate() {
        timer = 0;
        done = false;
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (mc.player == null || mc.gameMode == null) return;

        if (!(mc.player.containerMenu instanceof AnvilMenu menu)) {
            done = false;
            return;
        }

        if (done) return;

        if (timer > 0) {
            timer--;
            return;
        }

        ItemStack left = menu.getSlot(0).getItem();
        ItemStack right = menu.getSlot(1).getItem();
        ItemStack output = menu.getSlot(2).getItem();

        // Bước 3: lấy kết quả
        if (!output.isEmpty()) {
            if (autoTake.get() && canTake(menu)) {
                click(menu, 2);
            }

            done = true;
            return;
        }

        // Bước 1: đặt đồ đang cầm vào ô trái
        if (left.isEmpty()) {
            int heldSlot = 30 + mc.player.getInventory().getSelectedSlot();
            ItemStack held = menu.getSlot(heldSlot).getItem();

            if (held.isEmpty()
                || !items.get().contains(held.getItem())
                || findBook(menu, held) == -1) {

                done = true;
                return;
            }

            click(menu, heldSlot);
            return;
        }

        // Bước 2: đặt sách vào ô phải
        if (right.isEmpty()) {
            int book = findBook(menu, left);

            if (book == -1) {
                done = true;
                return;
            }

            click(menu, book);
        }
    }

    /**
     * Minecraft 26.2:
     * ClickType + handleInventoryMouseClick()
     * đã được thay bằng ContainerInput + handleContainerInput().
     */
    private void click(AnvilMenu menu, int slot) {
        mc.gameMode.handleContainerInput(
            menu.containerId,
            slot,
            0,
            ContainerInput.QUICK_MOVE,
            mc.player
        );

        timer = delay.get();
    }

    private boolean canTake(AnvilMenu menu) {
        if (mc.player.isCreative()) return true;

        int cost = menu.getCost();

        return cost > 0
            && cost < 40
            && mc.player.experienceLevel >= cost;
    }

    private int findBook(AnvilMenu menu, ItemStack target) {
        for (int i = 3; i < menu.slots.size(); i++) {
            ItemStack book = menu.getSlot(i).getItem();

            if (book.is(Items.ENCHANTED_BOOK)
                && hasUsefulEnchant(target, book)) {
                return i;
            }
        }

        return -1;
    }

    private boolean hasUsefulEnchant(
        ItemStack target,
        ItemStack book
    ) {
        ItemEnchantments stored =
            book.getOrDefault(
                DataComponents.STORED_ENCHANTMENTS,
                ItemEnchantments.EMPTY
            );

        ItemEnchantments current =
            EnchantmentHelper.getEnchantmentsForCrafting(target);

        for (Holder<Enchantment> ench : stored.keySet()) {
            if (ench.unwrapKey().isEmpty()) continue;

            if (!enchants.get().contains(ench.unwrapKey().get())) {
                continue;
            }

            if (!ench.value().isSupportedItem(target)) {
                continue;
            }

            boolean conflict = false;

            for (Holder<Enchantment> existing : current.keySet()) {
                if (!existing.equals(ench)
                    && !Enchantment.areCompatible(ench, existing)) {

                    conflict = true;
                    break;
                }
            }

            if (conflict) continue;

            if (stored.getLevel(ench) > current.getLevel(ench)) {
                return true;
            }
        }

        return false;
    }
}

Lưu ý: phần này chỉ sửa lỗi "ClickType". Lỗi "HUD_GROUP" đã được sửa ở "AddonTemplate.java". Sau khi dán file trên, chạy lại:

./gradlew build

Nếu lại xuất hiện lỗi khác, gửi nguyên log mới cho mình; mình sẽ tiếp tục sửa theo API 26.2.
