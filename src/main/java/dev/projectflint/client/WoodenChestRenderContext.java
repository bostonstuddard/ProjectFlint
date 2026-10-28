package dev.projectflint.client;
public final class WoodenChestRenderContext { private static final ThreadLocal<Integer> DEPTH=ThreadLocal.withInitial(()->0); private WoodenChestRenderContext(){} public static void push(){DEPTH.set(DEPTH.get()+1);} public static void pop(){DEPTH.set(Math.max(0,DEPTH.get()-1));} public static boolean active(){return DEPTH.get()>0;} }
