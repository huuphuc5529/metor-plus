package com.example.addon.modules;

import com.example.addon.AddonTemplate;

import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.ItemSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;

import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import static meteordevelopment.meteorclient.MeteorClient.mc;

public class AutoSellAh extends Module {
    private final SettingGroup sg = settings.getDefaultGroup();

    // =========================================================
    // ITEM 1
    // =========================================================

    private final Setting<Item> item1 = sg.add(
        new ItemSetting.Builder()
            .name("item-1")
            .description("Item 1.")
            .defaultValue(Items.DIAMOND)
            .build()
    );

    private final Setting<Integer> price1 = sg.add(
        new IntSetting.Builder()
            .name("price-1")
            .description("Gia ban cua item 1.")
            .defaultValue(200000)
            .min(1)
            .sliderMax(10000000)
            .build()
    );

    // =========================================================
    // ITEM 2
    // =========================================================

    private final Setting<Item> item2 = sg.add(
        new ItemSetting.Builder()
            .name("item-2")
            .description("Item 2.")
            .defaultValue(Items.EMERALD)
            .build()
    );

    private final Setting<Integer> price2 = sg.add(
        new IntSetting.Builder()
            .name("price-2")
            .description("Gia ban cua item 2.")
            .defaultValue(300000)
            .min(1)
            .sliderMax(10000000)
            .build()
    );

    // =========================================================
    // ITEM 3
    // =========================================================

    private final Setting<Item> item3 = sg.add(
        new ItemSetting.Builder()
            .name("item-3")
            .description("Item 3.")
            .defaultValue(Items.GOLD_INGOT)
            .build()
    );

    private final Setting<Integer> price3 = sg.add(
        new IntSetting.Builder()
            .name("price-3")
            .description("Gia ban cua item 3.")
            .defaultValue(100000)
            .min(1)
            .sliderMax(10000000)
            .build()
    );

    // =========================================================
    // ITEM 4
    // =========================================================

    private final Setting<Item> item4 = sg.add(
        new ItemSetting.Builder()
            .name("item-4")
            .description("Item 4.")
            .defaultValue(Items.IRON_INGOT)
            .build()
    );

    private final Setting<Integer> price4 = sg.add(
        new IntSetting.Builder()
            .name("price-4")
            .description("Gia ban cua item 4.")
            .defaultValue(50000)
            .min(1)
            .sliderMax(10000000)
            .build()
    );

    // =========================================================
    // ITEM 5
    // =========================================================

    private final Setting<Item> item5 = sg.add(
        new ItemSetting.Builder()
            .name("item-5")
            .description("Item 5.")
            .defaultValue(Items.AIR)
            .build()
    );

    private final Setting<Integer> price5 = sg.add(
        new IntSetting.Builder()
            .name("price-5")
            .description("Gia ban cua item 5.")
            .defaultValue(100000)
            .min(1)
            .sliderMax(10000000)
            .build()
    );

    // =========================================================
    // ITEM 6
    // =========================================================

    private final Setting<Item> item6 = sg.add(
        new ItemSetting.Builder()
            .name("item-6")
            .description("Item 6.")
            .defaultValue(Items.AIR)
            .build()
    );

    private final Setting<Integer> price6 = sg.add(
        new IntSetting.Builder()
            .name("price-6")
            .description("Gia ban cua item 6.")
            .defaultValue(100000)
            .min(1)
            .sliderMax(10000000)
            .build()
    );

    // =========================================================
    // ITEM 7
    // =========================================================

    private final Setting<Item> item7 = sg.add(
        new ItemSetting.Builder()
            .name("item-7")
            .description("Item 7.")
            .defaultValue(Items.AIR)
            .build()
    );

    private final Setting<Integer> price7 = sg.add(
        new IntSetting.Builder()
            .name("price-7")
            .description("Gia ban cua item 7.")
            .defaultValue(100000)
            .min(1)
            .sliderMax(10000000)
            .build()
    );

    // =========================================================
    // ITEM 8
    // =========================================================

    private final Setting<Item> item8 = sg.add(
        new ItemSetting.Builder()
            .name("item-8")
            .description("Item 8.")
            .defaultValue(Items.AIR)
            .build()
    );

    private final Setting<Integer> price8 = sg.add(
        new IntSetting.Builder()
            .name("price-8")
            .description("Gia ban cua item 8.")
            .defaultValue(100000)
            .min(1)
            .sliderMax(10000000)
            .build()
    );

    // =========================================================
    // DELAY
    // =========================================================

    private final Setting<Integer> delay = sg.add(
        new IntSetting.Builder()
            .name("delay")
            .description("So tick cho giua cac lan ban.")
            .defaultValue(20)
            .min(1)
            .sliderMax(100)
            .build()
    );

    private int timer;

    // =========================================================
    // CONSTRUCTOR
    // =========================================================

    public AutoSellAh() {
        super(
            AddonTemplate.CATEGORY,
            "auto-sell-ah",
            "Tu dong lay item trong inventory va ban bang /ah sell."
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
    // DEACTIVATE
    // =========================================================

    @Override
    public void onDeactivate() {
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

        if (mc.getConnection() == null) {
            return;
        }

        if (timer > 0) {
            timer--;
            return;
        }

        // =====================================================
        // 1. KIEM TRA MAIN HAND
        // =====================================================

        ItemStack hand = mc.player.getMainHandItem();

        if (!hand.isEmpty()) {
            Item handItem = hand.getItem();

            int sellPrice = getPrice(handItem);

            /*
             * Chi ban neu Main Hand dung item
             * ma nguoi dung da chon.
             */
            if (sellPrice > 0) {
                mc.player.connection.sendCommand(
                    "ah sell " + sellPrice
                );

                timer = delay.get();
                return;
            }
        }

        // =====================================================
        // 2. TIM ITEM
        // =====================================================

        int slot = findItem();

        /*
         * Khong co item thi KHONG tat module.
         * Tiep tuc cho item moi.
         */
        if (slot == -1) {
            return;
        }

        // =====================================================
        // 3. ITEM NAM TRONG HOTBAR
        // =====================================================

        if (slot < 9) {
            /*
             * Chon truc tiep hotbar slot.
             */
            mc.player.getInventory().setSelectedSlot(slot);

            /*
             * Cho 1 tick de Main Hand cap nhat.
             */
            timer = 1;
            return;
        }

        // =====================================================
        // 4. ITEM NAM TRONG INVENTORY
        // =====================================================

        /*
         * Player Inventory:
         *
         * 0  - 8  = hotbar
         * 9  - 35 = inventory
         *
         * InventoryMenu cua Minecraft:
         *
         * 9  - 35 = inventory
         *
         * SWAP button 0:
         * doi item nay voi hotbar slot 0.
         */

        mc.gameMode.handleContainerInput(
            mc.player.containerMenu.containerId,
            slot,
            0,
            ContainerInput.SWAP,
            mc.player
        );

        /*
         * Chon hotbar slot 0.
         */
        mc.player.getInventory().setSelectedSlot(0);

        /*
         * Tick sau moi kiem tra Main Hand.
         */
        timer = 1;
    }

    // =========================================================
    // TIM ITEM TRONG INVENTORY
    // =========================================================

    private int findItem() {

        /*
         * Uu tien HOTBAR.
         */
        for (int slot = 0; slot < 9; slot++) {
            ItemStack stack =
                mc.player.getInventory().getItem(slot);

            if (stack.isEmpty()) {
                continue;
            }

            if (getPrice(stack.getItem()) > 0) {
                return slot;
            }
        }

        /*
         * Sau do tim INVENTORY.
         */
        for (int slot = 9; slot < 36; slot++) {
            ItemStack stack =
                mc.player.getInventory().getItem(slot);

            if (stack.isEmpty()) {
                continue;
            }

            if (getPrice(stack.getItem()) > 0) {
                return slot;
            }
        }

        return -1;
    }

    // =========================================================
    // LAY GIA THEO ITEM
    // =========================================================

    private int getPrice(Item item) {

        /*
         * Item 1
         */
        if (item == item1.get()) {
            return price1.get();
        }

        /*
         * Item 2
         */
        if (item == item2.get()) {
            return price2.get();
        }

        /*
         * Item 3
         */
        if (item == item3.get()) {
            return price3.get();
        }

        /*
         * Item 4
         */
        if (item == item4.get()) {
            return price4.get();
        }

        /*
         * Item 5
         */
        if (item5.get() != Items.AIR && item == item5.get()) {
            return price5.get();
        }

        /*
         * Item 6
         */
        if (item6.get() != Items.AIR && item == item6.get()) {
            return price6.get();
        }

        /*
         * Item 7
         */
        if (item7.get() != Items.AIR && item == item7.get()) {
            return price7.get();
        }

        /*
         * Item 8
         */
        if (item8.get() != Items.AIR && item == item8.get()) {
            return price8.get();
        }

        return -1;
    }
}
