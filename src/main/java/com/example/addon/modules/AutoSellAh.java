package com.example.addon.modules;

import com.example.addon.AddonTemplate;

import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.ItemSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;

import net.minecraft.world.inventory.ClickType;
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
            .description("Item thứ 1 muốn bán.")
            .defaultValue(Items.DIAMOND)
            .build()
    );

    private final Setting<Integer> price1 = sg.add(
        new IntSetting.Builder()
            .name("price-1")
            .description("Giá bán của Item 1.")
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
            .description("Item thứ 2 muốn bán.")
            .defaultValue(Items.EMERALD)
            .build()
    );

    private final Setting<Integer> price2 = sg.add(
        new IntSetting.Builder()
            .name("price-2")
            .description("Giá bán của Item 2.")
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
            .description("Item thứ 3 muốn bán.")
            .defaultValue(Items.GOLD_INGOT)
            .build()
    );

    private final Setting<Integer> price3 = sg.add(
        new IntSetting.Builder()
            .name("price-3")
            .description("Giá bán của Item 3.")
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
            .description("Item thứ 4 muốn bán.")
            .defaultValue(Items.IRON_INGOT)
            .build()
    );

    private final Setting<Integer> price4 = sg.add(
        new IntSetting.Builder()
            .name("price-4")
            .description("Giá bán của Item 4.")
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
            .description("Item thứ 5 muốn bán.")
            .defaultValue(Items.AIR)
            .build()
    );

    private final Setting<Integer> price5 = sg.add(
        new IntSetting.Builder()
            .name("price-5")
            .description("Giá bán của Item 5.")
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
            .description("Item thứ 6 muốn bán.")
            .defaultValue(Items.AIR)
            .build()
    );

    private final Setting<Integer> price6 = sg.add(
        new IntSetting.Builder()
            .name("price-6")
            .description("Giá bán của Item 6.")
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
            .description("Item thứ 7 muốn bán.")
            .defaultValue(Items.AIR)
            .build()
    );

    private final Setting<Integer> price7 = sg.add(
        new IntSetting.Builder()
            .name("price-7")
            .description("Giá bán của Item 7.")
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
            .description("Item thứ 8 muốn bán.")
            .defaultValue(Items.AIR)
            .build()
    );

    private final Setting<Integer> price8 = sg.add(
        new IntSetting.Builder()
            .name("price-8")
            .description("Giá bán của Item 8.")
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
            .description("Thời gian chờ giữa các lần bán.")
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
            "Tự động lấy item trong inventory và bán bằng /ah sell."
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
        if (mc.player == null || mc.getConnection() == null) {
            return;
        }

        if (timer > 0) {
            timer--;
            return;
        }

        // =====================================================
        // 1. KIỂM TRA MAIN HAND
        // =====================================================

        ItemStack hand = mc.player.getMainHandItem();

        if (!hand.isEmpty()) {
            Item handItem = hand.getItem();

            int price = getPrice(handItem);

            /*
             * Nếu Main Hand là một item đã chọn
             * thì mới được phép bán.
             */
            if (price != -1) {
                mc.player.connection.sendCommand(
                    "ah sell " + price
                );

                timer = delay.get();
                return;
            }
        }

        // =====================================================
        // 2. TÌM ITEM TRONG INVENTORY
        // =====================================================

        int slot = findItem();

        /*
         * Không có item:
         *
         * KHÔNG TẮT MODULE.
         * Tiếp tục chờ.
         */
        if (slot == -1) {
            return;
        }

        // =====================================================
        // 3. ITEM ĐÃ Ở HOTBAR
        // =====================================================

        if (slot < 9) {
            mc.player.getInventory().setSelectedSlot(slot);

            /*
             * Chờ 1 tick để Main Hand cập nhật.
             */
            timer = 1;
            return;
        }

        // =====================================================
        // 4. ITEM Ở INVENTORY
        // =====================================================

        /*
         * Đổi item trong inventory với
         * hotbar slot 0.
         *
         * Sau đó chọn hotbar slot 0.
         */
        mc.gameMode.handleInventoryMouseClick(
            mc.player.containerMenu.containerId,
            slot,
            0,
            ClickType.SWAP,
            mc.player
        );

        mc.player.getInventory().setSelectedSlot(0);

        /*
         * Tick sau mới kiểm tra Main Hand.
         */
        timer = 1;
    }

    // =========================================================
    // TÌM ITEM
    // =========================================================

    private int findItem() {

        /*
         * Ưu tiên tìm trong HOTBAR trước.
         */
        for (int slot = 0; slot < 9; slot++) {
            ItemStack stack =
                mc.player.getInventory().getItem(slot);

            if (stack.isEmpty()) {
                continue;
            }

            if (getPrice(stack.getItem()) != -1) {
                return slot;
            }
        }

        /*
         * Sau đó tìm trong INVENTORY.
         */
        for (int slot = 9; slot < 36; slot++) {
            ItemStack stack =
                mc.player.getInventory().getItem(slot);

            if (stack.isEmpty()) {
                continue;
            }

            if (getPrice(stack.getItem()) != -1) {
                return slot;
            }
        }

        return -1;
    }

    // =========================================================
    // LẤY GIÁ CỦA ITEM
    // =========================================================

    private int getPrice(Item item) {

        if (item == item1.get()) {
            return price1.get();
        }

        if (item == item2.get()) {
            return price2.get();
        }

        if (item == item3.get()) {
            return price3.get();
        }

        if (item == item4.get()) {
            return price4.get();
        }

        if (item == item5.get() && item5.get() != Items.AIR) {
            return price5.get();
        }

        if (item == item6.get() && item6.get() != Items.AIR) {
            return price6.get();
        }

        if (item == item7.get() && item7.get() != Items.AIR) {
            return price7.get();
        }

        if (item == item8.get() && item8.get() != Items.AIR) {
            return price8.get();
        }

        return -1;
    }
}
