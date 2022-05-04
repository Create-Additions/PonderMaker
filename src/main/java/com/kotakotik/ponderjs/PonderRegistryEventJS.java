package com.kotakotik.ponderjs;

import dev.latvian.mods.kubejs.KubeJS;
import dev.latvian.mods.kubejs.event.EventJS;
import dev.latvian.mods.kubejs.item.ingredient.IngredientJS;
import dev.latvian.mods.kubejs.script.ScriptType;
import dev.latvian.mods.kubejs.util.ListJS;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import org.antlr.v4.runtime.misc.Triple;


public class PonderRegistryEventJS extends EventJS {
    public static boolean rerun = false;

    public static void rerunScripts(ScriptType scriptType, String tagItem, String ponder) {
        if (tagItem != null) {
            PonderJS.tagItemEvent.post(scriptType, tagItem);
        }
        if (ponder != null) {
            PonderJS.ponderEvent.post(scriptType, ponder);
        }
    }

    public static void rerunScripts() {
        rerunScripts(ScriptType.CLIENT, "ponder.tag", "ponder.registry");
    }

    public static void regenerateLang() {
//        JsonObject json = new JsonObject();
//        PonderLocalization.generateSceneLang();
        PonderJS.fillPonderLang();
//        PJSLocalization.record(PonderJSPlugin.namespaces,);
        Triple<Boolean, Component, Integer> result = PonderJS.generateJsonLang(PonderJS.LANG);
        boolean success = result.a;
        int count = result.c;
        if (success) {
            if (count > 0) {
                KubeJS.PROXY.reloadLang();
                if (!rerun) {
                    Minecraft.getInstance().reloadResourcePacks();
                }
            }
        } else {
            PonderJSMod.LOGGER.error("Could not generate PonderJS lang!");
//            PonderJS.generatePonderLang();
        }
    }

    public static void runAllRegistration() {
        if (rerun) {
            rerunScripts(ScriptType.CLIENT, null, "ponder.registry");
        } else {
            rerunScripts();
        }
        regenerateLang();
        rerun = true;
    }

    public PonderBuilderJS create(String name, IngredientJS ingredient) {
        return new PonderBuilderJS(name, ingredient.getVanillaItems());
    }

    public static void register(FMLClientSetupEvent event) {
//                PonderRegistry.forComponents(itemProvider)
//                        .addStoryBoard("test", b.function::accept);
//                PonderRegistry.TAGS.forTag(PonderTag.KINETIC_RELAYS)
//                        .add(itemProvider);
        event.enqueueWork(() -> {
            try {
                runAllRegistration();
            } catch (Exception e) { // i think theres a way to do this with the completable future but this is easier
                e.printStackTrace();
                ScriptType.CLIENT.console.error("Error occurred while running ponder events", e);
            }
        });
    }

}
