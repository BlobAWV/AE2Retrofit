package blobawv.ae2retrofit;

import blobawv.ae2retrofit.client.RetrofitScreenRegistration;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import blobawv.ae2retrofit.common.ExtendedRegistryHandler;

@Mod(AE2Retrofit.MODID)
public class AE2Retrofit {

    public static final String MODID = "ae2retrofit";

    public static final ItemGroup ITEM_GROUP = new ItemGroup(MODID) {
        @Override
        public ItemStack createIcon() {
            return new ItemStack(ExtendedRegistryHandler.ITEM);
        } // This is extended interface texture.
    };

    public AE2Retrofit() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        bus.register(new ExtendedRegistryHandler());
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
            bus.addListener(this::clientSetup);
        });
    }



    private void clientSetup(FMLClientSetupEvent evt) {
        evt.enqueueWork(RetrofitScreenRegistration::register);
    }
}