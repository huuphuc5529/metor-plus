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

import static meteordevelopment.meteorclient.MeteorClient.mc;

public class AutoSellAh extends Module {
    private final SettingGroup sg = settings.getDefaultGroup();

    // =========================
    // ITEM
    // =========================

    private final Setting<Item> item = sg.add(
        new ItemSetting.Builder()
            .name("item")
            .description("Chọn item muốn bán.")
            .defaultValue(net.minecraft.world.item.Items.DIAMOND)
            .build()
    );

    // =========================
    // GIÁ BÁN
    // =========================

    private final Setting<Integer> price = sg.add(
        new IntSetting.Builder()
            .name("price")
            .description("Giá bán bằng lệnh /ah sell.")
            .defaultValue(200000)
            .min(1)
            .sliderMax(10000000)
            .build()
    );

    // =========================
    // DELAY
    // =========================

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

    public AutoSellAh() {
        super(
            AddonTemplate.CATEGORY,
            "auto-sell-ah",
            "Tự động cầm đúng item và bán bằng /ah sell."
        );
    }

    @Override
    public void onActivate() {
        timer = 0;
    }

    @Override
    public void onDeactivate() {
        timer = 0;
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (mc.player == null || mc.getConnection() == null) {
            return;
        }

        if (timer > 0) {
            timer--;
            return;
        }

        // =========================
        // KIỂM TRA MAIN HAND
        // =========================

        ItemStack hand = mc.player.getMainHandItem();

        /*
         * CHỈ được bán nếu Main Hand
         * đúng loại item đã chọn.
         */
        if (hand.getItem() == item.get()) {
            mc.player.connection.sendCommand(
                "ah sell " + price.get()
            );

            timer = delay.get();
            return;
        }

        // =========================
        // TÌM ITEM
        // =========================

        int slot = findItem();

        /*
         * Không có item thì KHÔNG tắt module.
         * Đứng chờ đến khi có item.
         */
        if (slot == -1) {
            return;
        }

        // =========================
        // ITEM TRONG HOTBAR
        // =========================

        if (slot < 9) {
            mc.player.getInventory().setSelectedSlot(slot);

            /*
             * Chờ 1 tick rồi kiểm tra lại Main Hand.
             */
            timer = 1;
            return;
        }

        // =========================
        // ITEM TRONG INVENTORY
        // =========================

        /*
         * Đưa item từ inventory vào
         * hotbar slot 0.
         *
         * SWAP + button 0
         * = đổi với hotbar slot 0.
         */
        mc.gameMode.handleContainerInput(
            mc.player.containerMenu.containerId,
            slot,
            0,
            ContainerInput.SWAP,
            mc.player
        );

        /*
         * Chọn hotbar slot 0.
         */
        mc.player.getInventory().setSelectedSlot(0);

        /*
         * Chưa bán ngay.
         * Tick sau sẽ kiểm tra Main Hand.
         */
        timer = 1;
    }

    // =========================
    // TÌM ITEM
    // =========================

    private int findItem() {
        for (int slot = 0; slot < 36; slot++) {
            ItemStack stack =
                mc.player.getInventory().getItem(slot);

            if (stack.isEmpty()) {
                continue;
            }

            /*
             * Chỉ đúng loại item được chọn.
             */
            if (stack.getItem() == item.get()) {
                return slot;
            }
        }

        return -1;
    }
}
