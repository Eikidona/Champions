package top.theillusivec4.champions.common.affix;

import net.minecraft.world.damagesource.DamageSource;
import top.theillusivec4.champions.api.IChampion;
import top.theillusivec4.champions.common.affix.core.BasicAffix;
import top.theillusivec4.champions.common.config.ChampionsConfig;

public class DampeningAffix extends BasicAffix {
    @Override
    public float onHurt(IChampion champion, DamageSource source, float amount, float newAmount) {
        return source.isIndirect() ? newAmount * (float) (1.0F
                - ChampionsConfig.dampenedDamageReduction) : newAmount;
    }
}
