package org.notenoughupdates.detektrules

import io.gitlab.arturbosch.detekt.api.*
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.name.parentOrNull
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.renderer.render
import org.jetbrains.kotlin.resolve.bindingContextUtil.getReferenceTargets
import org.jetbrains.kotlin.resolve.descriptorUtil.fqNameOrNull
import java.util.*

class GlStateManagerWarnings(config: Config) : Rule(config) {
    override val issue: Issue = Issue(
        javaClass.simpleName,
        Severity.Warning,
        "Warn about invalid usages of GlStateManager",
        Debt.TEN_MINS,
    )
    val glStackDepthViolation = Issue(
        "GlStackDepthViolation",
        Severity.Warning,
        "Calling GlStateManager.pushX() without calling GlStateManager.popX() can cause issues",
        Debt.TWENTY_MINS
    )

    val pushAttribStateViolation = Issue(
        "PushAttribStateViolation",
        Severity.Warning,
        "Calling a state remembering function while inside of a GlStateManager.pushAttrib() block will cause future calls to that method to be ignored.",
        Debt.FIVE_MINS,
    )

    override fun visitDeclaration(dcl: KtDeclaration) {
        super.visitDeclaration(dcl)
        if (dcl is KtFunction) {
            val pushAttribCalls: Stack<KtExpression> = Stack()
            var pushMatrixCalls: Stack<KtExpression> = Stack()
            dcl.bodyExpression?.accept(object : KtTreeVisitorVoid() {
                override fun visitCallExpression(expression: KtCallExpression) {
                    super.visitCallExpression(expression)
                    val target = expression.calleeExpression ?: return
                    if (target !is KtNameReferenceExpression) return
                    val tn = target.getReferenceTargets(bindingContext).singleOrNull()?.fqNameOrNull() ?: return
                    if (tn.parentOrNull() != GlStateManager) return
                    if (tn == pushAttrib) {
                        pushAttribCalls.push(target)
                    } else if (tn == popAttrib) {
                        if (pushAttribCalls.empty()) {
                            report(
                                CodeSmell(
                                    glStackDepthViolation,
                                    Entity.from(expression),
                                    "GlStateManager.popAttrib without GlStateManager.pushAttrib"
                                )
                            )
                        } else {
                            pushAttribCalls.pop()
                        }
                    } else if (!pushAttribCalls.empty() && tn in attributeSavingModificationFunctions) {
                        report(
                            CodeSmell(
                                pushAttribStateViolation,
                                Entity.from(expression),
                                "${tn.render()} called while inside of a GlStateManager.pushAttrib() block. This can lead to future calls to this method being ignored."
                            )
                        )
                    } else if (tn == pushMatrix) {
                        pushMatrixCalls.push(target)
                    } else if (tn == popMatrix) {
                        if (pushMatrixCalls.empty()) {
                            report(
                                CodeSmell(
                                    glStackDepthViolation,
                                    Entity.from(expression),
                                    "GlStateManager.popMatrix without GlStateManager.pushMatrix"
                                )
                            )
                        } else {
                            pushMatrixCalls.pop()
                        }
                    }
                }
            }, null)
            for (callsite in pushAttribCalls) {
                report(
                    CodeSmell(
                        glStackDepthViolation,
                        Entity.from(callsite),
                        "GlStateManager.pushAttrib without GlStateManager.popAttrib"
                    )
                )
            }
            for (callsite in pushMatrixCalls) {
                report(
                    CodeSmell(
                        glStackDepthViolation,
                        Entity.from(callsite),
                        "GlStateManager.pushMatrix without GlStateManager.popMatrix"
                    )
                )
            }
        }
    }

    companion object {
        val GlStateManager = FqName("net.minecraft.client.renderer.GlStateManager")
        val pushAttrib = GlStateManager.child(Name.identifier("pushAttrib"))
        val popAttrib = GlStateManager.child(Name.identifier("popAttrib"))
        val pushMatrix = GlStateManager.child(Name.identifier("pushMatrix"))
        val popMatrix = GlStateManager.child(Name.identifier("popMatrix"))
        val attributeSavingModificationFunctions = listOf(
            "disableAlpha",
            "enableAlpha",
            "alphaFunc",
            "enableLighting",
            "disableLighting",
            "enableLight",
            "disableLight",
            "enableColorMaterial",
            "disableColorMaterial",
            "colorMaterial",
            "disableDepth",
            "enableDepth",
            "depthFunc",
            "depthMask",
            "disableBlend",
            "enableBlend",
            "blendFunc",
            "tryBlendFuncSeparate",
            "enableFog",
            "disableFog",
            "setFog",
            "setFogDensity",
            "setFogStart",
            "setFogEnd",
            "enableCull",
            "disableCull",
            "cullFace",
            "enablePolygonOffset",
            "disablePolygonOffset",
            "doPolygonOffset",
            "enableColorLogic",
            "disableColorLogic",
            "colorLogicOp",
            "enableTexGenCoord",
            "disableTexGenCoord",
            "texGen",
            "setActiveTexture",
            "enableTexture2D",
            "disableTexture2D",
            "bindTexture",
            "enableNormalize",
            "disableNormalize",
            "shadeModel",
            "enableRescaleNormal",
            "disableRescaleNormal",
            "colorMask",
            "clearDepth",
            "clearColor",
            "color",
            "resetColor",
        ).map { GlStateManager.child(Name.identifier(it)) }
    }
}
