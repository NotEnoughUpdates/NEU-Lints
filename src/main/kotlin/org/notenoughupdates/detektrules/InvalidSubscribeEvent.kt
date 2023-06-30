package org.notenoughupdates.detektrules

import io.gitlab.arturbosch.detekt.api.CodeSmell
import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.api.Debt
import io.gitlab.arturbosch.detekt.api.Entity
import io.gitlab.arturbosch.detekt.api.Issue
import io.gitlab.arturbosch.detekt.api.Rule
import io.gitlab.arturbosch.detekt.api.Severity
import io.gitlab.arturbosch.detekt.api.internal.RequiresTypeResolution
import io.gitlab.arturbosch.detekt.rules.fqNameOrNull
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.psiUtil.isPublic
import org.jetbrains.kotlin.resolve.typeBinding.createTypeBinding
import org.jetbrains.kotlin.types.typeUtil.supertypes

@RequiresTypeResolution
class InvalidSubscribeEvent(config: Config) : Rule(config) {
    override val issue = Issue(
        javaClass.simpleName,
        Severity.Warning,
        "Warn about missing @SubscribeEvent annotations on functions with event parameters or @SubscribeEvents on functions that do not take events or private event handlers",
        Debt.FIVE_MINS,
    )

    override fun visitNamedFunction(function: KtNamedFunction) {
        super.visitNamedFunction(function)
        val singleArg = function.valueParameters.singleOrNull()
        val type = singleArg?.typeReference?.createTypeBinding(bindingContext)?.type
        val hasEventArg = type?.supertypes()?.any { it.fqNameOrNull() == eventType } ?: false
        val hasSubscribeEventAnnotation = function.annotationEntries.any {
            it.typeReference?.createTypeBinding(bindingContext)?.type?.fqNameOrNull() == subscribeEventType
        }
        if (!hasSubscribeEventAnnotation && hasEventArg) {
            report(
                CodeSmell(
                    issue,
                    Entity.atName(function),
                    "Missing @SubscribeEvent annotation on a function that receives an event"
                )
            )
        }
        if (!hasEventArg && hasSubscribeEventAnnotation) {
            report(
                CodeSmell(
                    issue, Entity.atName(function),
                    "Argument list of function with @SubscribeEvent does not match expected (one argument that subclasses Event)"
                )
            )
        }
        if (hasEventArg && hasSubscribeEventAnnotation && !function.isPublic) {
            report(CodeSmell(issue, Entity.atName(function), "@SubscribeEvent event handlers need to be public"))
        }
    }

    companion object {
        val eventType = FqName("net.minecraftforge.fml.common.eventhandler.Event")
        val subscribeEventType = FqName("net.minecraftforge.fml.common.eventhandler.SubscribeEvent")
    }
}
