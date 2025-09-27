package dev.jsinco.gringotts.events;

import io.papermc.paper.event.player.AsyncChatEvent;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ChatPromptInputListener implements Listener {

    private static final Queue<ChatInputCallback> queuedChatPrompts = new ConcurrentLinkedQueue<>();


    @EventHandler(priority = EventPriority.HIGHEST)
    public void onAsyncChat(AsyncChatEvent event) {
        if (queuedChatPrompts.isEmpty()) {
            return;
        }

        Player player = event.getPlayer();

        queuedChatPrompts.removeIf(callback -> {
            if (callback.isTimedOut()) return true;
            if (callback.uuid.equals(player.getUniqueId())) {
                String input = PlainTextComponentSerializer.plainText().serialize(event.originalMessage());
                event.setCancelled(true);
                if (!input.equals("cancel")) {
                    callback.handler.handle(input);
                } else {
                    callback.cancelled.run();
                }
                return true;
            }
            return false;
        });
    }


    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static class ChatInputCallback {

        private static final long TIMEOUT = 30000;

        public static ChatInputCallback of(HumanEntity player, @Nullable Title title, @Nullable String msg, ChatInputCallbackHandler handler, Runnable cancelled) {
            if (title != null) {
                player.showTitle(title);
            }
            if (msg != null) {
                player.sendMessage(msg);
            }
            ChatInputCallback chatInputCallback = new ChatInputCallback(player.getUniqueId(), System.currentTimeMillis(), handler, cancelled);
            queuedChatPrompts.add(chatInputCallback);
            return chatInputCallback;
        }

        private final UUID uuid;
        private final long timestamp;
        private final ChatInputCallbackHandler handler;
        private final Runnable cancelled;

        public boolean isTimedOut() {
            boolean bool = System.currentTimeMillis() - timestamp >= TIMEOUT;
            Player player = player();
            if (bool && player != null) {
                player.sendMessage("Your prompt input timed out.");
                if (cancelled != null) cancelled.run();
            }
            return bool;
        }

        @Nullable
        public Player player() {
            return Bukkit.getPlayer(uuid);
        }

        @Override
        public boolean equals(Object object) {
            if (object == null || getClass() != object.getClass()) return false;
            ChatInputCallback that = (ChatInputCallback) object;
            return Objects.equals(uuid, that.uuid);
        }

        @Override
        public int hashCode() {
            return Objects.hash(uuid, timestamp);
        }

        public interface ChatInputCallbackHandler {
            void handle(String input);
        }
    }
}
