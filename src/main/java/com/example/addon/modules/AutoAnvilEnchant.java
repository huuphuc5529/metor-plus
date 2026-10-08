package com.example.addon.modules;

import com.example.addon.AddonTemplate;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.EnchantmentListSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;

import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.ItemEnchantments;

import java.util.Set;

import static meteordevelopment.meteorclient.MeteorClient.mc;

public class AutoAnvilEnchant extends Module {
    private final SettingGroup sg = settings.getDefaultGroup();

    private final Setting<Set<ResourceKey<Enchantment>>> enchants =
        sg.add(new EnchantmentListSetting.Builder()
            .name("enchantments")
            .description("Những enchant được phép tự động ghép.")
            .defaultValue(Set.of(
                Enchantments.MENDING,
                Enchantments.UNBREAKING,
                Enchantments.SHARPNESS,
                Enchantments.PROTECTION,
                Enchantments.EFFICIENCY
            ))
            .build());

    private final Setting<Integer> delay =
        sg.add(new IntSetting.Builder()
            .name("delay")
            .description("Thời gian chờ giữa mỗi thao tác.")
            .defaultValue(3)
            .min(0)
            .sliderMax(20)
            .build());

    private final Setting<Boolean> autoTake =
        sg.add(new BoolSetting.Builder()
            .name("auto-take")
            .description("Tự động lấy kết quả từ Anvil.")
            .defaultValue(true)
            .build());

    private int timer = 0;

    public AutoAnvilEnchant() {
        super(
            AddonTemplate.CATEGORY,
            "auto-anvil-enchant",
            "Tự động enchant toàn bộ item trong inventory bằng Anvil."
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
        if (mc.player == null || mc.gameMode == null) return;

        /*
         * Chỉ hoạt động khi người chơi đang mở Anvil.
         * Không cần đóng/mở lại Anvil giữa các item.
         */
        if (!(mc.player.containerMenu instanceof AnvilMenu menu)) {
            timer = 0;
            return;
        }

        if (timer > 0) {
            timer--;
            return;
        }

        /*
         * ---------------------------------------------------------
         * 1. KIỂM TRA KẾT QUẢ
         * ---------------------------------------------------------
         */
        ItemStack output = menu.getSlot(2).getItem();

        if (!output.isEmpty()) {
            if (!autoTake.get()) return;

            if (!canTake(menu)) {
                return;
            }

            quickMove(menu, 2);
            return;
        }

        /*
         * ---------------------------------------------------------
         * 2. Ô TRÁI TRỐNG
         *
         * Tìm món đầu tiên trong toàn bộ inventory có thể enchant.
         * ---------------------------------------------------------
         */
        ItemStack left = menu.getSlot(0).getItem();

        if (left.isEmpty()) {
            int targetSlot = findNextItem(menu);

            if (targetSlot == -1) {
                /*
                 * Không còn item nào cần enchant.
                 */
                return;
            }

            quickMove(menu, targetSlot);
            return;
        }

        /*
         * ---------------------------------------------------------
         * 3. ĐÃ CÓ ITEM Ở Ô TRÁI
         *
         * Tìm sách enchant phù hợp trong inventory.
         * ---------------------------------------------------------
         */
        ItemStack right = menu.getSlot(1).getItem();

        if (right.isEmpty()) {
            int bookSlot = findBook(menu, left);

            if (bookSlot == -1) {
                /*
                 * Item này không có sách phù hợp.
                 *
                 * Trả item về inventory để module có thể
                 * tiếp tục kiểm tra những item khác.
                 */
                quickMove(menu, 0);
                return;
            }

            quickMove(menu, bookSlot);
            return;
        }

        /*
         * Nếu cả hai ô đã có item nhưng chưa có output,
         * chờ Anvil/server cập nhật.
         */
    }

    /**
     * Tìm item tiếp theo trong inventory.
     *
     * AnvilMenu:
     *
     * 0 = item trái
     * 1 = sách
     * 2 = output
     *
     * 3 -> 38 = inventory + hotbar của người chơi.
     */
    private int findNextItem(AnvilMenu menu) {
        for (int slot = 3; slot < menu.slots.size(); slot++) {
            ItemStack stack = menu.getSlot(slot).getItem();

            if (stack.isEmpty()) continue;

            /*
             * Không lấy Enchanted Book làm item chính.
             */
            if (stack.is(Items.ENCHANTED_BOOK)) continue;

            /*
             * Kiểm tra xem item có ít nhất một sách enchant
             * phù hợp trong inventory hay không.
             */
            if (findBook(menu, stack) != -1) {
                return slot;
            }
        }

        return -1;
    }

    /**
     * Tìm Enchanted Book phù hợp với item.
     */
    private int findBook(AnvilMenu menu, ItemStack target) {
        for (int slot = 3; slot < menu.slots.size(); slot++) {
            ItemStack book = menu.getSlot(slot).getItem();

            if (book.isEmpty()) continue;

            if (!book.is(Items.ENCHANTED_BOOK)) continue;

            if (hasUsefulEnchant(target, book)) {
                return slot;
            }
        }

        return -1;
    }

    /**
     * Kiểm tra sách có enchant nào:
     *
     * - nằm trong danh sách enchant được phép
     * - dùng được cho item
     * - không conflict
     * - level sách cao hơn level hiện tại
     */
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

            if (enchantment.unwrapKey().isEmpty()) {
                continue;
            }

            ResourceKey<Enchantment> key =
                enchantment.unwrapKey().get();

            /*
             * Không nằm trong danh sách enchant được phép.
             */
            if (!enchants.get().contains(key)) {
                continue;
            }

            /*
             * Enchant này không dùng được cho item.
             */
            if (!enchantment.value().isSupportedItem(target)) {
                continue;
            }

            /*
             * Kiểm tra conflict với enchant hiện tại.
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
             * Chỉ ghép nếu sách có level cao hơn level hiện tại.
             */
            int bookLevel = stored.getLevel(enchantment);
            int currentLevel = current.getLevel(enchantment);

            if (bookLevel > currentLevel) {
                return true;
            }
        }

        return false;
    }

    /**
     * Lấy item ra khỏi Anvil hoặc đưa item từ inventory vào Anvil.
     *
     * Minecraft 26.2 dùng:
     * ContainerInput.QUICK_MOVE
     * thay cho ClickType.QUICK_MOVE.
     */
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

    /**
     * Kiểm tra người chơi có thể lấy output.
     */
    private boolean canTake(AnvilMenu menu) {
        if (mc.player.isCreative()) {
            return true;
        }

        int cost = menu.getCost();

        return cost > 0
            && cost < 40
            && mc.player.experienceLevel >= cost;
    }
}
