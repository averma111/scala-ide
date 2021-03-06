package org.scalaide.ui.internal.editor.decorators.semantichighlighting

import org.scalaide.ui.syntax.ScalaSyntaxClasses
import org.scalaide.ui.syntax.ScalaSyntaxClasses._
import org.scalaide.core.internal.decorators.semantichighlighting.Position
import org.scalaide.core.internal.decorators.semantichighlighting.classifier.SymbolTypes
import org.scalaide.core.internal.decorators.semantichighlighting.classifier.SymbolTypes._
import org.eclipse.jface.text.TextAttribute
import org.eclipse.swt.SWT
import org.eclipse.swt.custom.StyleRange
import org.scalaide.ui.syntax.ScalaSyntaxClass

case class HighlightingStyle(styledTextAttribute: TextAttribute, enabled: Boolean, unstyledTextAttribute: TextAttribute, deprecation: DeprecationStyle, interpolation: StringInterpolationStyle) {
  val ta = if (enabled) styledTextAttribute else unstyledTextAttribute
  lazy val deprecatedTextAttribute: TextAttribute = deprecation.buildTextAttribute(ta)
  lazy val interpolationTextAttribute: TextAttribute = interpolation.buildTextAttribute(ta)

  def style(position: Position): StyleRange = {
    val textAttribute = if (position.deprecated) deprecatedTextAttribute else if (position.inInterpolatedString) interpolationTextAttribute else ta
    val s = textAttribute.getStyle()
    val fontStyle = s & (SWT.ITALIC | SWT.BOLD | SWT.NORMAL)
    val styleRange = new StyleRange(position.getOffset(), position.getLength(), textAttribute.getForeground(), textAttribute.getBackground(), fontStyle)
    styleRange.strikeout = (s & TextAttribute.STRIKETHROUGH) != 0
    styleRange.underline = (s & TextAttribute.UNDERLINE) != 0
    styleRange
  }
}

object HighlightingStyle {
  def apply(preferences: Preferences, symbolType: SymbolTypes.SymbolType): HighlightingStyle = {
    val syntaxClass = symbolTypeToSyntaxClass(symbolType)
    val enabled = syntaxClass.enabled(preferences.store)
    val deprecation = DeprecationStyle(preferences.isStrikethroughDeprecatedDecorationEnabled())
    val stringInterpolation = StringInterpolationStyle(preferences.isInterpolatedStringCodeDecorationEnabled(), preferences.interpolatedStringTextAttribute())
    HighlightingStyle(syntaxClass.getTextAttribute(preferences.store), enabled, ScalaSyntaxClasses.DEFAULT.getTextAttribute(preferences.store), deprecation, stringInterpolation)
  }

  def symbolTypeToSyntaxClass(symbolType: SymbolTypes.SymbolType): ScalaSyntaxClass = {
    symbolType match {
      case Annotation        => ANNOTATION
      case CaseClass         => CASE_CLASS
      case CaseObject        => CASE_OBJECT
      case Class             => CLASS
      case LazyLocalVal      => LAZY_LOCAL_VAL
      case LazyTemplateVal   => LAZY_TEMPLATE_VAL
      case LocalVal          => LOCAL_VAL
      case LocalVar          => LOCAL_VAR
      case Method            => METHOD
      case Param             => PARAM
      case Object            => OBJECT
      case Package           => PACKAGE
      case TemplateVar       => TEMPLATE_VAR
      case TemplateVal       => TEMPLATE_VAL
      case Trait             => TRAIT
      case Type              => TYPE
      case TypeParameter     => TYPE_PARAMETER
      case DynamicSelect     => DYNAMIC_SELECT
      case DynamicUpdate     => DYNAMIC_UPDATE
      case DynamicApply      => DYNAMIC_APPLY
      case DynamicApplyNamed => DYNAMIC_APPLY_NAMED
      case CallByNameParameter => CALL_BY_NAME_PARAMETER
    }
  }
}

case class DeprecationStyle(enabled: Boolean) {
  def buildTextAttribute(ta: TextAttribute) = if (enabled) new TextAttribute(ta.getForeground, ta.getBackground, ta.getStyle | TextAttribute.STRIKETHROUGH, ta.getFont) else ta
}

case class StringInterpolationStyle(enabled: Boolean, modifier: TextAttribute) {
  def buildTextAttribute(ta: TextAttribute) = if (enabled) new TextAttribute(ta.getForeground, Option(ta.getBackground).getOrElse(modifier.getBackground), ta.getStyle | modifier.getStyle, ta.getFont) else ta
}
