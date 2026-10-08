package com.example.addon.modules;

import com.example.addon.AddonTemplate;

import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.EnchantmentListSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.ItemListSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
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

public class AutoAnvilEnchant extends Module {
    private final SettingGroup sg = settings.getDefaultGroup();

    // =========================================================
    // ITEMS
    // =========================================================

    private final Setting<List<Item>> items = sg.add(
        new ItemListSetting.Builder()
            .name("items")
            .description("Những item sẽ được tự động enchant.")
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
            .build()
    );

    // =========================================================
    // ENCHANTMENTS
    // =========================================================

    private final Setting<Set<ResourceKey<Enchantment>>> enchants =
        sg.add(
            new EnchantmentListSetting.Builder()
                .name("enchantments")
                .description("Những enchant được phép sử dụng.")
                .defaultValue(Set.of(
                    Enchantments.MENDING,
                    Enchantments.UNBREAKING,
                    Enchantments.SHARPNESS,
                    Enchantments.EFFICIENCY,
                    Enchantments.PROTECTION
                ))
                .build()
        );

    // =========================================================
    // DELAY
    // =========================================================

    private final Setting<Integer> delay =
        sg.add(
            new IntSetting.Builder()
                .name("delay")
                .description("Thời gian chờ giữa các thao tác.")
                .defaultValue(3)
                .min(0)
                .sliderMax(20)
                .build()
        );

    // =========================================================
    // AUTO TAKE
    // =========================================================

    private final Setting<Boolean> autoTake =
        sg.add(
            new BoolSetting.Builder()
                .name("auto-take")
                .description("Tự động lấy item sau khi enchant.")
                .defaultValue(true)
                .build()
        );

    private int timer;

    public AutoAnvilEnchant() {
        super(
            AddonTemplate.CATEGORY,
            "auto-anvil-enchant",
            "Tự động enchant item trong inventory bằng Anvil."
        );
    }

    // =========================================================
    // ACTIVATE
    // =========================================================

    @Override
    public void onActivate() {
        timer = 0;
    }

    // =========================================================
    // TICK
    // =========================================================

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (mc.player == null || mc.gameMode == null) {
            return;
        }

        /*
         * Chỉ chạy khi người chơi đang mở Anvil.
         */
        if (!(mc.player.containerMenu instanceof AnvilMenu menu)) {
            timer = 0;
            return;
        }

        if (timer > 0) {
            timer--;
            return;
        }

        // =====================================================
        // OUTPUT
        // =====================================================

        ItemStack output = menu.getSlot(2).getItem();

        if (!output.isEmpty()) {

            /*
             * QUAN TRỌNG:
             *
             * Nếu không đủ XP:
             * - Không lấy output
             * - Không trả item
             * - Không trả sách
             * - Không chuyển sang món khác
             *
             * => Đứng yên tại món hiện tại.
             */
            if (!canTake(menu)) {
                return;
            }

            /*
             * Đủ XP -> lấy kết quả.
             */
            if (autoTake.get()) {
                quickMove(menu, 2);
            }

            return;
        }

        // =====================================================
        // LEFT SLOT - ITEM
        // =====================================================

        ItemStack left = menu.getSlot(0).getItem();

        if (left.isEmpty()) {

            /*
             * Tìm item tiếp theo trong toàn bộ inventory.
             */
            int itemSlot = findNextItem(menu);

            if (itemSlot == -1) {
                return;
            }

            quickMove(menu, itemSlot);
            return;
        }

        // =====================================================
        // RIGHT SLOT - BOOK
        // =====================================================

        ItemStack right = menu.getSlot(1).getItem();

        if (right.isEmpty()) {

            /*
             * Tìm sách phù hợp với item.
             */
            int bookSlot = findBook(menu, left);

            if (bookSlot == -1) {
                /*
                 * Không có sách phù hợp.
                 *
                 * Trả item về inventory để module
                 * có thể tìm món khác.
                 */
                quickMove(menu, 0);
                return;
            }

            quickMove(menu, bookSlot);
        }
    }

    // =========================================================
    // TÌM ITEM TIẾP THEO
    // =========================================================

    private int findNextItem(AnvilMenu menu) {

        /*
         * Slot 3 trở đi là inventory/hotbar
         * của người chơi trong AnvilMenu.
         */
        for (int slot = 3; slot < menu.slots.size(); slot++) {

            ItemStack stack = menu.getSlot(slot).getItem();

            if (stack.isEmpty()) {
                continue;
            }

            /*
             * Chỉ lấy item mà người dùng đã chọn
             * trong setting "Items".
             */
            if (!items.get().contains(stack.getItem())) {
                continue;
            }

            /*
             * Không lấy Enchanted Book làm item chính.
             */
            if (stack.is(Items.ENCHANTED_BOOK)) {
                continue;
            }

            /*
             * Phải có ít nhất một sách phù hợp.
             */
            if (findBook(menu, stack) == -1) {
                continue;
            }

            return slot;
        }

        return -1;
    }

    // =========================================================
    // TÌM SÁCH
    // =========================================================

    private int findBook(
        AnvilMenu menu,
        ItemStack target
    ) {

        for (int slot = 3; slot < menu.slots.size(); slot++) {

            ItemStack book = menu.getSlot(slot).getItem();

            if (book.isEmpty()) {
                continue;
            }

            /*
             * Chỉ lấy Enchanted Book.
             */
            if (!book.is(Items.ENCHANTED_BOOK)) {
                continue;
            }

            if (hasUsefulEnchant(target, book)) {
                return slot;
            }
        }

        return -1;
    }

    // =========================================================
    // KIỂM TRA ENCHANT
    // =========================================================

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

        for (Holder<Enchantment> enchantment : stored.keySet()) {

            /*
             * Không lấy được ResourceKey.
             */
            if (enchantment.unwrapKey().isEmpty()) {
                continue;
            }

            ResourceKey<Enchantment> key =
                enchantment.unwrapKey().get();

            /*
             * Enchant không được người dùng chọn.
             */
            if (!enchants.get().contains(key)) {
                continue;
            }

            /*
             * Item không hỗ trợ enchant.
             */
            if (!enchantment.value().isSupportedItem(target)) {
                continue;
            }

            /*
             * Kiểm tra conflict.
             */
            boolean conflict = false;

            for (Holder<Enchantment> existing : current.keySet()) {

                if (existing.equals(enchantment)) {
                    continue;
                }

                if (!Enchantment.areCompatible(
                    enchantment,
                    existing
                )) {
                    conflict = true;
                    break;
                }
            }

            if (conflict) {
                continue;
            }

            /*
             * Chỉ ghép nếu sách có level cao hơn
             * level hiện tại của item.
             */
            int bookLevel =
                stored.getLevel(enchantment);

            int currentLevel =
                current.getLevel(enchantment);

            if (bookLevel > currentLevel) {
                return true;
            }
        }

        return false;
    }

    // =========================================================
    // QUICK MOVE
    // =========================================================

    private void quickMove(
        AnvilMenu menu,
        int slot
    ) {

        mc.gameMode.handleContainerInput(
            menu.containerId,
            slot,
            0,
            ContainerInput.QUICK_MOVE,
            mc.player
        );

        timer = delay.get();
    }

    // =========================================================
    // KIỂM TRA XP
    // =========================================================

    private boolean canTake(AnvilMenu menu) {

        /*
         * Creative không cần XP.
         */
        if (mc.player.isCreative()) {
            return true;
        }

        int cost = menu.getCost();

        /*
         * Anvil quá đắt hoặc không có XP.
         */
        if (cost <= 0) {
            return false;
        }

        if (cost >= 40) {
            return false;
        }

        return mc.player.experienceLevel >= cost;
    }
}
