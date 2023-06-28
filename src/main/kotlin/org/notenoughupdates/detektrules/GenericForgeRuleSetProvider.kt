package org.notenoughupdates.detektrules

import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.api.RuleSet
import io.gitlab.arturbosch.detekt.api.RuleSetProvider

class GenericForgeRuleSetProvider : RuleSetProvider {
    override val ruleSetId: String = "forge"

    override fun instance(config: Config): RuleSet {
        return RuleSet(
            ruleSetId,
            listOf(
                InvalidSubscribeEvent(config),
            ),
        )
    }
}
