package me.johardt.mixin

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.api.storage.party.PartyStore
import com.cobblemon.mod.common.block.entity.HealingMachineBlockEntity
import me.johardt.CobblemonCharged
import me.johardt.energy.PoweredHealingMachine
import net.minecraft.core.BlockPos
import net.minecraft.core.HolderLookup
import net.minecraft.nbt.CompoundTag
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.entity.BlockEntityType
import net.minecraft.world.level.block.state.BlockState
import net.neoforged.neoforge.energy.EnergyStorage
import net.neoforged.neoforge.energy.IEnergyStorage
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.Shadow
import org.spongepowered.asm.mixin.Unique
import org.spongepowered.asm.mixin.gen.Accessor
import org.spongepowered.asm.mixin.gen.Invoker
import org.spongepowered.asm.mixin.injection.At
import org.spongepowered.asm.mixin.injection.Inject
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
import java.util.UUID
import kotlin.math.ceil
import kotlin.math.min

private const val COBBLEMON_CHARGED_ENERGY_KEY: String = "ChargedEnergy"
private const val COBBLEMON_CHARGED_ENERGY_PER_CHARGE: Int = 4_000

@Mixin(HealingMachineBlockEntity::class)
abstract class HealingMachineBlockEntityMixin(
    type: BlockEntityType<*>,
    pos: BlockPos,
    state: BlockState
) : BlockEntity(type, pos, state), PoweredHealingMachine {
    @field:Unique
    private var `cobblemonCharged$energyStorage`: HealingMachineEnergyStorage? = null

    @Shadow(remap = false)
    abstract fun getMaxCharge(): Float

    @Shadow(remap = false)
    abstract fun setHealingCharge(healingCharge: Float)

    @Invoker(value = "updateRedstoneSignal", remap = false)
    protected abstract fun `cobblemonCharged$invokeUpdateRedstoneSignal`()

    @Invoker(value = "updateBlockChargeLevel", remap = false)
    protected abstract fun `cobblemonCharged$invokeUpdateBlockChargeLevel`(chargeLevel: Int?)

    @Accessor(value = "infinite", remap = false)
    protected abstract fun `cobblemonCharged$getInfinite`(): Boolean

    @Inject(method = ["<init>"], at = [At("TAIL")])
    private fun `cobblemonCharged$initEnergy`(pos: BlockPos, state: BlockState, ci: CallbackInfo) {
        `cobblemonCharged$energyStorage` = HealingMachineEnergyStorage(
            capacity = `cobblemonCharged$getEnergyCapacity`(),
            onChanged = {
                `cobblemonCharged$syncDisplayedCharge`()
                setChanged()
            }
        )
    }

    @Inject(method = ["loadAdditional", "method_11014"], at = [At("TAIL")], remap = false)
    private fun `cobblemonCharged$readEnergy`(
        tag: CompoundTag,
        registries: HolderLookup.Provider,
        ci: CallbackInfo
    ) {
        val energyStorage = `cobblemonCharged$getInternalEnergyStorage`()
        energyStorage.setStoredEnergy(tag.getInt(COBBLEMON_CHARGED_ENERGY_KEY))
        `cobblemonCharged$syncDisplayedCharge`()
    }

    @Inject(method = ["saveAdditional", "method_11007"], at = [At("TAIL")], remap = false)
    private fun `cobblemonCharged$writeEnergy`(
        tag: CompoundTag,
        registries: HolderLookup.Provider,
        ci: CallbackInfo
    ) {
        tag.putInt(COBBLEMON_CHARGED_ENERGY_KEY, `cobblemonCharged$getInternalEnergyStorage`().energyStored)
    }

    @Inject(method = ["canHeal"], at = [At("HEAD")], cancellable = true, remap = false)
    private fun `cobblemonCharged$checkEnergyForHealing`(
        party: PartyStore,
        cir: CallbackInfoReturnable<Boolean>
    ) {
        cir.returnValue = `cobblemonCharged$canHealWithEnergy`(party)
    }

    @Inject(method = ["activate"], at = [At("HEAD")], cancellable = true, remap = false)
    private fun `cobblemonCharged$consumeEnergy`(user: UUID, party: PartyStore, ci: CallbackInfo) {
        if (Cobblemon.config.infiniteHealerCharge || `cobblemonCharged$getInfinite`()) {
            return
        }

        val energyCost = `cobblemonCharged$getEnergyCost`(party)
        if (energyCost <= 0) {
            return
        }

        val extracted = `cobblemonCharged$getInternalEnergyStorage`().extractInternally(energyCost)
        if (extracted != energyCost) {
            CobblemonCharged.LOGGER.warn(
                "Healing machine at {} was blocked because it lacked stored energy (needed {}, extracted {}).",
                blockPos,
                energyCost,
                extracted
            )
            ci.cancel()
        }
    }

    @Inject(method = ["activate"], at = [At("TAIL")], remap = false)
    private fun `cobblemonCharged$resyncAfterActivation`(user: UUID, party: PartyStore, ci: CallbackInfo) {
        `cobblemonCharged$syncDisplayedCharge`()
    }

    override fun `cobblemonCharged$getEnergyStorage`(): IEnergyStorage {
        return `cobblemonCharged$getInternalEnergyStorage`()
    }

    @Unique
    private fun `cobblemonCharged$getInternalEnergyStorage`(): HealingMachineEnergyStorage {
        return `cobblemonCharged$energyStorage`
            ?: error("Healing machine energy storage was not initialized")
    }

    @Unique
    protected fun `cobblemonCharged$canHealWithEnergy`(party: PartyStore): Boolean {
        if (Cobblemon.config.infiniteHealerCharge || `cobblemonCharged$getInfinite`()) {
            return true
        }

        return `cobblemonCharged$getInternalEnergyStorage`().energyStored >= `cobblemonCharged$getEnergyCost`(party)
    }

    @Unique
    protected fun `cobblemonCharged$getEnergyCapacity`(): Int {
        return ceil(getMaxCharge().toDouble() * COBBLEMON_CHARGED_ENERGY_PER_CHARGE.toDouble())
            .coerceAtLeast(1.0)
            .coerceAtMost(Int.MAX_VALUE.toDouble())
            .toInt()
    }

    @Unique
    protected fun `cobblemonCharged$getEnergyCost`(party: PartyStore): Int {
        val healingRemainder = party.getHealingRemainderPercent().coerceAtLeast(0.0F)
        return ceil(healingRemainder.toDouble() * COBBLEMON_CHARGED_ENERGY_PER_CHARGE.toDouble())
            .coerceAtLeast(0.0)
            .coerceAtMost(Int.MAX_VALUE.toDouble())
            .toInt()
    }

    @Unique
    protected fun `cobblemonCharged$syncDisplayedCharge`() {
        val currentLevel = level ?: return
        if (currentLevel.isClientSide) {
            return
        }

        val displayedCharge = `cobblemonCharged$getInternalEnergyStorage`().energyStored.toFloat() / COBBLEMON_CHARGED_ENERGY_PER_CHARGE
        setHealingCharge(min(getMaxCharge(), displayedCharge))
        `cobblemonCharged$invokeUpdateRedstoneSignal`()
        `cobblemonCharged$invokeUpdateBlockChargeLevel`(null)
    }

    private class HealingMachineEnergyStorage(
        capacity: Int,
        private val onChanged: () -> Unit
    ) : EnergyStorage(capacity, capacity, 0) {
        fun setStoredEnergy(amount: Int) {
            energy = amount.coerceIn(0, capacity)
        }

        fun extractInternally(amount: Int): Int {
            if (amount <= 0) {
                return 0
            }

            val extracted = min(energy, amount)
            if (extracted > 0) {
                energy -= extracted
                onChanged()
            }
            return extracted
        }

        override fun receiveEnergy(toReceive: Int, simulate: Boolean): Int {
            val received = super.receiveEnergy(toReceive, simulate)
            if (!simulate && received > 0) {
                onChanged()
            }
            return received
        }

        override fun extractEnergy(toExtract: Int, simulate: Boolean): Int {
            return 0
        }
    }

    private companion object {
        @JvmStatic
        @Suppress("CAST_NEVER_SUCCEEDS")
        @Inject(method = ["TICKER\$lambda\$0"], at = [At("TAIL")], remap = false)
        private fun `cobblemonCharged$syncTickerCharge`(
            level: Level,
            pos: BlockPos,
            state: BlockState,
            blockEntity: HealingMachineBlockEntity,
            ci: CallbackInfo
        ) {
            (blockEntity as HealingMachineBlockEntityMixin).`cobblemonCharged$syncDisplayedCharge`()
        }
    }
}
