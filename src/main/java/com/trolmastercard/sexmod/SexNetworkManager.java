package com.trolmastercard.sexmod;

import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelConfig;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelId;
import io.netty.channel.ChannelMetadata;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.ChannelProgressivePromise;
import io.netty.channel.ChannelPromise;
import io.netty.channel.EventLoop;
import io.netty.channel.Channel.Unsafe;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import java.net.SocketAddress;
import net.minecraft.network.EnumPacketDirection;
import net.minecraft.network.NetworkManager;

/**
 * Dummy {@link NetworkManager} used only to construct
 * {@link ClientNetHandlerOverride} (which in turn is required by
 * {@link SexWorldClient}'s super constructor). The overridden
 * {@link #channel()} returns a stub {@link Channel} whose every method is a
 * no-op or returns {@code null}/{@code false}.
 * <p>
 * <b>Pitfall:</b> this manager is never actually connected to a server — it
 * exists so the client-only preload world can be created without a live
 * network stack. Do not route real packet traffic through it, and do not
 * "improve" {@code channel()} to return a working channel: the preload world
 * must never send or receive anything.
 */
public class SexNetworkManager extends NetworkManager {
   public SexNetworkManager(EnumPacketDirection direction) {
      super(direction);
   }

   /**
    * Returns a fully inert {@link Channel} stub so the base
    * {@link NetworkManager} construction succeeds. Every channel method returns
    * a neutral value (see class javadoc — nothing may ever actually flow
    * through this channel).
    */
   public Channel channel() {
      return new Channel() {
         public int compareTo(Channel other) { return 0; }

         public ChannelId id() {
            return null;
         }

         public EventLoop eventLoop() {
            return null;
         }

         public Channel parent() {
            return null;
         }

         public ChannelConfig config() {
            return null;
         }

         public boolean isOpen() {
            return false;
         }

         public boolean isRegistered() {
            return false;
         }

         public boolean isActive() {
            return false;
         }

         public ChannelMetadata metadata() {
            return null;
         }

         public SocketAddress localAddress() {
            return null;
         }

         public SocketAddress remoteAddress() {
            return null;
         }

         public ChannelFuture closeFuture() {
            return null;
         }

         public boolean isWritable() {
            return false;
         }

         public long bytesBeforeUnwritable() {
            return 0L;
         }

         public long bytesBeforeWritable() {
            return 0L;
         }

         public Unsafe unsafe() {
            return null;
         }

         public ChannelPipeline pipeline() {
            return null;
         }

         public ByteBufAllocator alloc() {
            return null;
         }

         public ChannelPromise newPromise() {
            return null;
         }

         public ChannelProgressivePromise newProgressivePromise() {
            return null;
         }

         public ChannelFuture newSucceededFuture() {
            return null;
         }

         public ChannelFuture newFailedFuture(Throwable cause) {
            return null;
         }

         public ChannelPromise voidPromise() {
            return null;
         }

         public ChannelFuture bind(SocketAddress localAddress) {
            return null;
         }

         public ChannelFuture connect(SocketAddress remoteAddress) {
            return null;
         }

         public ChannelFuture connect(SocketAddress remoteAddress, SocketAddress localAddress) {
            return null;
         }

         public ChannelFuture disconnect() {
            return null;
         }

         public ChannelFuture close() {
            return null;
         }

         public ChannelFuture deregister() {
            return null;
         }

         public ChannelFuture bind(SocketAddress localAddress, ChannelPromise promise) {
            return null;
         }

         public ChannelFuture connect(SocketAddress remoteAddress, ChannelPromise promise) {
            return null;
         }

         public ChannelFuture connect(SocketAddress remoteAddress, SocketAddress localAddress, ChannelPromise promise) {
            return null;
         }

         public ChannelFuture disconnect(ChannelPromise promise) {
            return null;
         }

         public ChannelFuture close(ChannelPromise promise) {
            return null;
         }

         public ChannelFuture deregister(ChannelPromise promise) {
            return null;
         }

         public Channel read() {
            return null;
         }

         public ChannelFuture write(Object msg) {
            return null;
         }

         public ChannelFuture write(Object msg, ChannelPromise promise) {
            return null;
         }

         public Channel flush() {
            return null;
         }

         public ChannelFuture writeAndFlush(Object msg, ChannelPromise promise) {
            return null;
         }

         public ChannelFuture writeAndFlush(Object msg) {
            return null;
         }

         public <T> Attribute<T> attr(AttributeKey<T> key) {
            return new Attribute<T>() {
               public T setIfAbsent(T key) {
                  return null;
               }

               public T getAndSet(T key) {
                  return null;
               }

               public AttributeKey<T> key() {
                  return null;
               }

               public T getAndRemove() {
                  return null;
               }

               public void remove() {
               }

               public T get() {
                  return null;
               }

               public boolean compareAndSet(T key, T actual) {
                  return false;
               }

               public void set(T key) {
               }
            };
         }

         public <T> boolean hasAttr(AttributeKey<T> key) {
            return false;
         }

         public int getNextPacketId(Channel channel) {
            return 0;
         }
      };
   }
}
