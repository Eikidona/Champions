package top.theillusivec4.champions.common.capability;

import cpw.mods.util.Lazy;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.capabilities.EntityCapability;
import net.neoforged.neoforge.capabilities.ICapabilityProvider;
import net.neoforged.neoforge.common.NeoForge;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.theillusivec4.champions.Champions;
import top.theillusivec4.champions.api.IAffix;
import top.theillusivec4.champions.api.IChampion;
import top.theillusivec4.champions.common.ChampionEventsHandler;
import top.theillusivec4.champions.common.rank.Rank;
import top.theillusivec4.champions.common.rank.RankManager;
import top.theillusivec4.champions.common.util.ChampionHelper;

import javax.annotation.Nonnull;
import java.util.*;

public class ChampionCapability {

  public static final EntityCapability<IChampion, Void> CHAMPION_CAP = EntityCapability.createVoid(
    new ResourceLocation("mymod", "champion_capability"),
    IChampion.class
  );

  public static final ResourceLocation ID = new ResourceLocation(Champions.MODID, "champion");

  private static final String AFFIX_TAG = "affixes";
  private static final String TIER_TAG = "tier";
  private static final String DATA_TAG = "data";
  private static final String ID_TAG = "identifier";

  public static void register() {
    NeoForge.EVENT_BUS.register(new CapabilityEventHandler());
    NeoForge.EVENT_BUS.register(new ChampionEventsHandler());
  }

  public static Provider createProvider(final LivingEntity livingEntity) {
    return new Provider(livingEntity);
  }

  public static IChampion getCapability(final Entity entity) {
    if (!ChampionHelper.isValidChampion(entity)) {
      return null;
    }
    return entity.getCapability(CHAMPION_CAP,null);
  }

  public static class Champion implements IChampion {

    private final LivingEntity champion;
    private final Client client;
    private final Server server;

    private Champion(final LivingEntity livingEntity) {
      this.champion = livingEntity;
      this.client = new Client();
      this.server = new Server();
    }

    @Override
    public Client getClient() {
      return this.client;
    }

    @Override
    public Server getServer() {
      return this.server;
    }

    @Nonnull
    @Override
    public LivingEntity getLivingEntity() {
      return this.champion;
    }

    public static class Server implements IChampion.Server {

      private final Map<String, CompoundTag> data = new HashMap<>();
      private Rank rank = null;
      private List<IAffix> affixes = new ArrayList<>();

      @Override
      public Optional<Rank> getRank() {
        return Optional.ofNullable(rank);
      }

      @Override
      public void setRank(Rank rank) {
        this.rank = rank;
      }

      @Override
      public List<IAffix> getAffixes() {
        return Collections.unmodifiableList(this.affixes);
      }

      @Override
      public void setAffixes(List<IAffix> affixes) {
        this.affixes = affixes;
      }

      @Override
      public void setData(String identifier, CompoundTag data) {
        this.data.put(identifier, data);
      }

      @Override
      public CompoundTag getData(String identifier) {
        return this.data.getOrDefault(identifier, new CompoundTag());
      }
    }

    public static class Client implements IChampion.Client {

      private final List<IAffix> affixes = new ArrayList<>();
      private final Map<String, IAffix> idToAffix = new HashMap<>();
      private final Map<String, CompoundTag> data = new HashMap<>();
      private Tuple<Integer, Integer> rank = null;

      @Override
      public Optional<Tuple<Integer, Integer>> getRank() {
        return Optional.ofNullable(rank);
      }

      @Override
      public void setRank(Tuple<Integer, Integer> rank) {
        this.rank = rank;
      }

      @Override
      public List<IAffix> getAffixes() {
        return Collections.unmodifiableList(this.affixes);
      }

      @Override
      public void setAffixes(Set<String> affixes) {
        this.affixes.clear();

        for (String affix : affixes) {
          Champions.API.getAffix(affix).ifPresent(val -> {
            this.affixes.add(val);
            this.idToAffix.put(val.getIdentifier(), val);
          });
        }
      }

      @Override
      public Optional<IAffix> getAffix(String id) {
        return Optional.ofNullable(this.idToAffix.get(id));
      }

      @Override
      public void setData(String identifier, CompoundTag data) {
        this.data.put(identifier, data);
      }

      @Override
      public CompoundTag getData(String identifier) {
        return this.data.getOrDefault(identifier, new CompoundTag());
      }
    }
  }

  public static class Provider implements ICapabilityProvider<Entity, Void, IChampion> {

    private final IChampion data;

    Provider(final LivingEntity livingEntity) {
      this.data = new Champion(livingEntity);
    }

    @NotNull
    public <T> T getCapability(@NotNull final EntityCapability<T, ?> cap, @Nullable final Direction side) {
      if (ChampionCapability.CHAMPION_CAP.equals(cap)) {
        return (T) data;
      }
      return null;
    }

    @Override
    public @Nullable IChampion getCapability(Entity entity, Void context) {
      return entity.getCapability(CHAMPION_CAP,null);
    }

    public Tag serializeNBT() {
      CompoundTag compoundNBT = new CompoundTag();
      IChampion.Server champion = data.getServer();
      champion.getRank().ifPresent(rank -> compoundNBT.putInt(TIER_TAG, rank.getTier()));
      List<IAffix> affixes = champion.getAffixes();
      ListTag list = new ListTag();
      affixes.forEach(affix -> {
        CompoundTag tag = new CompoundTag();
        String id = affix.getIdentifier();
        tag.putString(ID_TAG, id);
        tag.put(DATA_TAG, champion.getData(id));
        list.add(tag);
      });
      compoundNBT.put(AFFIX_TAG, list);
      return compoundNBT;
    }

    public void deserializeNBT(final Tag nbt) {
      if (!(nbt instanceof CompoundTag)) {
        return;
      }

      CompoundTag compoundNBT = (CompoundTag) nbt;
      IChampion.Server champion = data.getServer();

      if (compoundNBT.contains(TIER_TAG)) {
        int tier = compoundNBT.getInt(TIER_TAG);
        champion.setRank(RankManager.getRank(tier));
      }

      if (compoundNBT.contains(AFFIX_TAG)) {
        ListTag list = compoundNBT.getList(AFFIX_TAG, CompoundTag.TAG_COMPOUND);
        List<IAffix> affixes = new ArrayList<>();

        for (int i = 0; i < list.size(); i++) {
          CompoundTag tag = list.getCompound(i);
          String id = tag.getString(ID_TAG);
          Champions.API.getAffix(id).ifPresent(affix -> {
            affixes.add(affix);

            if (tag.contains(DATA_TAG)) {
              champion.setData(id, tag.getCompound(DATA_TAG));
            }
          });
        }
        champion.setAffixes(affixes);
      }
    }
  }
}
