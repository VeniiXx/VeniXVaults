package me.venixvaults.core.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

public final class ItemSerializer {
    private ItemSerializer() {
    }

    public static String toBase64(ItemStack[] items) throws IOException {
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        try (BukkitObjectOutputStream output = new BukkitObjectOutputStream(byteStream)) {
            output.writeInt(items.length);
            for (ItemStack item : items) {
                output.writeObject(item);
            }
        }
        return Base64.getEncoder().encodeToString(byteStream.toByteArray());
    }

    public static ItemStack[] fromBase64(String data) throws IOException, ClassNotFoundException {
        byte[] bytes = Base64.getDecoder().decode(data);
        try (BukkitObjectInputStream input = new BukkitObjectInputStream(new ByteArrayInputStream(bytes))) {
            int length = input.readInt();
            ItemStack[] items = new ItemStack[length];
            for (int slot = 0; slot < length; slot++) {
                Object object = input.readObject();
                items[slot] = object instanceof ItemStack ? (ItemStack) object : null;
            }
            return items;
        }
    }
}
