package ru.bulldog.smaragdor.mixin;

import appeng.core.definitions.AEItems;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.EmptyBlockView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import appeng.items.parts.FacadeItem;
import net.minecraft.item.ItemStack;

import java.util.HashSet;
import java.util.Set;

@Mixin(value = FacadeItem.class, priority = 900)
public class AE2CrashFixMixin {

    @Inject(at = @At("HEAD"), method = "getTextureItem", cancellable = true)
    private void fixProminentInit(ItemStack is, CallbackInfoReturnable<ItemStack> cir) {
        NbtCompound nbt = is.getNbt();
        if (nbt == null) {
            cir.setReturnValue(ItemStack.EMPTY);
        } else {
            Identifier itemId = new Identifier(nbt.getString("item"));
            if (smf_isInvalidItem(itemId)) {
                cir.setReturnValue(ItemStack.EMPTY);
                return;
            }

            Item baseItem = Registries.ITEM.get(itemId);
            cir.setReturnValue(new ItemStack(baseItem, 1));
        }
    }

    @Inject(at = @At("HEAD"), method = "createFromID", cancellable = true)
    private void fixProminentRemoveId(int id, CallbackInfoReturnable<ItemStack> cir) {
        ItemStack facadeStack = AEItems.FACADE.stack();
        Item item = Registries.ITEM.get(id);

        Identifier itemId = Registries.ITEM.getId(item);
        if (item == Items.AIR || smf_isInvalidItem(itemId)) {
            cir.setReturnValue(ItemStack.EMPTY);
            return;
        }

        NbtCompound facadeTag = new NbtCompound();
        facadeTag.putString("item", itemId.toString());
        facadeStack.setNbt(facadeTag);

        cir.setReturnValue(facadeStack);
    }

    @Inject(at = @At("HEAD"), method = "createFacadeForItemUnchecked", cancellable = true)
    private void fixProminentRemoveUnchecked(ItemStack itemStack, CallbackInfoReturnable<ItemStack> cir) {
        ItemStack is = new ItemStack((ItemConvertible) this);
        Identifier itemId = Registries.ITEM.getId(itemStack.getItem());
        if (smf_isInvalidItem(itemId)) {
            cir.setReturnValue(ItemStack.EMPTY);
            return;
        }

        NbtCompound data = new NbtCompound();
        data.putString("item", itemId.toString());
        is.setNbt(data);

        cir.setReturnValue(is);
    }

    @Unique
    private boolean smf_isInvalidItem(Identifier itemId) {
        if (smf_isModBlocked(itemId.getNamespace()) ||
            smf_isItemBlocked(itemId))
        {
            return true;
        }

        Block block = Registries.BLOCK.get(itemId);
        BlockState blockState = block.getDefaultState();
        boolean isSolid = blockState.isSolidBlock(EmptyBlockView.INSTANCE, BlockPos.ORIGIN);
        boolean isFullCube = blockState.isFullCube(EmptyBlockView.INSTANCE, BlockPos.ORIGIN);
        return blockState.isAir() || !blockState.getFluidState().isEmpty() || !isSolid ||
                blockState.exceedsCube() || blockState.hasBlockEntity() || !isFullCube ||
                blockState.hasSidedTransparency();
    }

    @Unique
    private boolean smf_isModBlocked(String modId) {
        return modId.equals("handcrafted") || modId.equals("archers");
    }

    @Unique
    private boolean smf_isItemBlocked(Identifier itemId) {
        return smf_BlockedItems.contains(itemId);
    }

    @Unique
    private static Set<Identifier> smf_BlockedItems;

    static {
        smf_BlockedItems = new HashSet<>();
        smf_BlockedItems.add(Identifier.tryParse("chipped:botanist_workbench"));
        smf_BlockedItems.add(Identifier.tryParse("chipped:alchemy_bench"));
        smf_BlockedItems.add(Identifier.tryParse("chipped:loom_table"));
        smf_BlockedItems.add(Identifier.tryParse("chipped:carpenters_table"));
        smf_BlockedItems.add(Identifier.tryParse("chipped:mason_table"));
        smf_BlockedItems.add(Identifier.tryParse("chipped:tinkering_table"));
        smf_BlockedItems.add(Identifier.tryParse("chipped:glassblower"));
        smf_BlockedItems.add(Identifier.tryParse("soulsweapons:altar_block"));
        smf_BlockedItems.add(Identifier.tryParse("soulsweapons:chungus_monolith"));
        smf_BlockedItems.add(Identifier.tryParse("soulsweapons:blackstone_pedestal"));
        smf_BlockedItems.add(Identifier.tryParse("create:wooden_bracket"));
        smf_BlockedItems.add(Identifier.tryParse("create:metal_bracket"));
        smf_BlockedItems.add(Identifier.tryParse("jewelry:jewelers_kit"));
    }
}
