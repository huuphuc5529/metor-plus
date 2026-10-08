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
    // ITEM MUỐN BÁN
    // =========================

    private final Setting<Item> item = sg.add(
        new ItemSetting.Builder()
            .name("item")
            .description("Chọn đúng item muốn bán.")
            .defaultValue(net.minecraft.world.item.Items.DIAMOND)
            .build()
    );

    // =========================
    // GIÁ BÁN
    // =========================

    private final Setting<Integer> price = sg.add(
        new IntSetting.Builder()
            .name("price")
            .description("Giá bán cho mỗi lần /ah sell.")
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

    public AutoSellAH() {
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

        // ==========================================
        // KIỂM TRA ITEM ĐANG CẦM
        // ==========================================

        ItemStack hand = mc.player.getMainHandItem();

        /*
         * Nếu tay đang cầm đúng item cần bán
         * thì mới được gửi lệnh.
         */
        if (hand.getItem() == item.get()) {

            mc.player.connection.sendCommand(
                "ah sell " + price.get()
            );

            timer = delay.get();
            return;
        }

        // ==========================================
        // TÌM ITEM TRONG INVENTORY
        // ==========================================

        int slot = findItem();

        /*
         * Không còn item -> tắt module.
         */
        if (slot == -1) {
            toggle();
            return;
        }

        // ==========================================
        // ĐƯA ITEM LÊN MAIN HAND
        // ==========================================

        if (slot < 9) {
            /*
             * Item nằm trong hotbar.
             */
            mc.player.getInventory().setSelectedSlot(slot);
        } else {
            /*
             * Item nằm trong inventory.
             *
             * Đổi item vào slot hotbar 0.
             */
            mc.player.getInventory().swapPaint(
                slot
            );

            mc.player.getInventory().setSelectedSlot(0);
        }

        /*
         * Không bán ngay trong tick này.
         *
         * Tick tiếp theo sẽ kiểm tra:
         * main hand có đúng item không.
         */
        timer = 1;
    }

    // ==========================================
    // TÌM ITEM
    // ==========================================

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
