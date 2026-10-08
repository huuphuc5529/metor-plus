package com.example.addon.modules;

import com.example.addon.AddonTemplate;

import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.ItemSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;

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
        // KIỂM TRA ITEM ĐANG CẦM
        // =========================

        ItemStack hand = mc.player.getMainHandItem();

        /*
         * Chỉ bán khi tay đang cầm ĐÚNG item đã chọn.
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
         * Không có item:
         * Không tắt module.
         * Chỉ đứng chờ.
         */
        if (slot == -1) {
            return;
        }

        // =========================
        // ĐƯA ITEM LÊN TAY
        // =========================

        if (slot < 9) {
            /*
             * Item nằm trong hotbar.
             */
            mc.player.getInventory().setSelectedSlot(slot);
        } else {
            /*
             * Item nằm trong inventory.
             *
             * Đưa item vào hotbar slot 0.
             */
            mc.player.getInventory().swapPaint(slot);
            mc.player.getInventory().setSelectedSlot(0);
        }

        /*
         * Chờ 1 tick để kiểm tra lại Main Hand.
         */
        timer = 1;
    }

    // =========================
    // TÌM ITEM TRONG INVENTORY
    // =========================

    private int findItem() {
        for (int slot = 0; slot < 36; slot++) {
            ItemStack stack =
                mc.player.getInventory().getItem(slot);

            if (stack.isEmpty()) {
                continue;
            }

            /*
             * Chỉ đúng loại item đã chọn.
             */
            if (stack.getItem() == item.get()) {
                return slot;
            }
        }

        return -1;
    }
}
