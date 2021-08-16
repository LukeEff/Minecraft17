package io.github.lukeeff.minecraft17.packetlistener;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.WrappedWatchableObject;
import io.github.lukeeff.minecraft17.Minecraft17;
import net.minecraft.world.entity.EntityPose;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.List;

import static io.github.lukeeff.minecraft17.packetlistener.OnEntityMetaData.EntityData.*;
import static net.minecraft.world.entity.EntityPose.*;

public class OnEntityMetaData {

    public enum Poses {

        STANDING(a),
        FALL_FLYING(b),
        SLEEPING(c),
        SWIMMING(d),
        SPIN_ATTACK(e),
        CROUCHING(f),
        LONG_JUMPING(g),
        DYING(h);

        final EntityPose pose;

        Poses(final EntityPose pose) {
            this.pose = pose;
        }

        public EntityPose getPose() {
            return pose;
        }
    }

    public enum EntityData {

        ON_FIRE((byte) 0x01),
        CROUCHING((byte) 0x02),
        SPRINTING((byte) 0x08),
        SWIMMING((byte) 0x10),
        INVISIBLE((byte) 0x20),
        GLOWING((byte) 0x40),
        ELYTRA_FLY((byte) 0x80);

        final byte bitMask;

        EntityData(final byte bitMask) {
            this.bitMask = bitMask;
        }

        public byte getBitMask() {
            return bitMask;
        }

        public boolean isPresent(final byte bitMask) {
            return (this.bitMask & bitMask) == this.bitMask;
        }
    }

    private final Minecraft17 plugin;

    public OnEntityMetaData(final Minecraft17 plugin) {
        this.plugin = plugin;
        plugin.getManager().addPacketListener(getMetaDataPacketAdapter());
    }

    public PacketAdapter getMetaDataPacketAdapter() {
        return new PacketAdapter(plugin, ListenerPriority.NORMAL, PacketType.Play.Server.ENTITY_METADATA) {
            @Override
            public void onPacketSending(PacketEvent event) {
                final PacketContainer packet = event.getPacket().deepClone(); // Deep clone to avoid unwanted side effects.
                final int entityId = event.getPacket().getIntegers().read(0); // Get the entityID that might be changed.
                final Player player = getPlayer(entityId); // The player that is having metadata modified in packet. (Or null if the id wasn't associated with a player).
                if (player != event.getPlayer()) { // Don't modify the packet for the client.
                    addDataToPacket(packet, CROUCHING, GLOWING);
                    removeDataFromPacket(packet, SPRINTING);
                    event.setPacket(packet); // Set packet as the clone instead of original.
                }
            }
        };
    }

    /**
     * Modify the metadata packet's bitmask data before it gets sent to the clients.
     *
     * @param packet packet that was intercepted.
     * @param add should new data be added.
     * @param data data that should be added or removed from the bitmask.
     */
    private void modifyPacketData(final PacketContainer packet, final boolean add, final EntityData... data) {
        final List<WrappedWatchableObject> packetContents = packet.getWatchableCollectionModifier().read(0);
        final WrappedWatchableObject bitMaskObjectContainer = packetContents.get(0);
        if (!(bitMaskObjectContainer.getValue() instanceof Byte)) return;
        final byte bitMaskData = getNewBitMask(bitMaskObjectContainer, add, data);
        //Logger.debug(ChatColor.GOLD + "Bitmask: " + bitMaskData);
        bitMaskObjectContainer.setValue(bitMaskData, true);
        packet.getWatchableCollectionModifier().write(0, packetContents);
    }

    /**
     * Gets the new bitmask data for the packet based on the passed parameters.
     *
     * @param bitMaskContentContainer packet byte index wrapper that was intercepted.
     * @param add should the data byte values be added or subtracted.
     * @param data data to be added or subtracted.
     * @return the new bitmask that corresponds to the changes.
     */
    private byte getNewBitMask(final WrappedWatchableObject bitMaskContentContainer, final boolean add, final EntityData... data) {
        byte bitMaskData = (byte) bitMaskContentContainer.getValue();
        for (final EntityData entityData: data) {
            if (shouldIgnore(bitMaskData, add, entityData)) continue;
            bitMaskData += (add) ? entityData.bitMask : entityData.bitMask * -1;
        }
        return bitMaskData;
    }

    /**
     * Checks to see if an operation should not happen. Bitmask shouldn't have sums if the byte is already present
     * and the bitmask shouldn't subtract if the byte is not present.
     *
     * @param bitMask bitmask in the packet.
     * @param add is this an addition operation or a subtraction.
     * @param data data to be compared with bitmask.
     * @return true if the operation should not happen.
     */
    private boolean shouldIgnore(final byte bitMask, final boolean add, final EntityData data) {
        final boolean present = data.isPresent(bitMask);
        return (present && add) || (!present && !add);
    }

    private void addDataToPacket(final PacketContainer packet, final EntityData... data) {
        modifyPacketData(packet, true, data);
    }

    private void removeDataFromPacket(final PacketContainer packet, final EntityData... data) {
        modifyPacketData(packet, false, data);
    }

    private Player getPlayer(final int entityID) {
        for (final Player player : Bukkit.getServer().getOnlinePlayers()) {
            if (player.getEntityId() == entityID) return player;
        }
        return null;
    }
}
