package org.scalaide.ui.internal.editor

import scala.tools.eclipse.contribution.weaving.jdt.ui.javaeditor.IScalaEditor
import org.eclipse.jdt.ui.text.IJavaPartitions
import org.eclipse.ui.IEditorReference
import org.eclipse.ui.IFileEditorInput
import org.eclipse.ui.IWorkbenchPage
import org.scalaide.core.IScalaProject
import org.scalaide.core.lexical.ScalaPartitions
import org.scalaide.util.eclipse.EclipseUtils
import org.scalaide.core.lexical.ScalaCodePartitioner
import org.scalaide.ui.editor.ISourceViewerEditor
import org.scalaide.ui.editor.InteractiveCompilationUnitEditor

trait ScalaEditor extends IScalaEditor with ISourceViewerEditor with InteractiveCompilationUnitEditor {

  override def createDocumentPartitioner = ScalaCodePartitioner.documentPartitioner()

}

object ScalaEditor {

  val LEGAL_CONTENT_TYPES = Array[String](
    IJavaPartitions.JAVA_DOC,
    IJavaPartitions.JAVA_MULTI_LINE_COMMENT,
    IJavaPartitions.JAVA_SINGLE_LINE_COMMENT,
    IJavaPartitions.JAVA_STRING,
    IJavaPartitions.JAVA_CHARACTER,
    ScalaPartitions.SCALA_MULTI_LINE_STRING)

  import org.scalaide.util.Utils.WithAsInstanceOfOpt

  /**
   * Checks whether there's at least one open editor related to given project
   */
  def projectHasOpenEditors(project: IScalaProject): Boolean = {
    def hasOpenEditorForThisProject(page: IWorkbenchPage) = {
      val editorRefs = page.getEditorReferences
      editorRefs exists hasEqualProject
    }

    def hasEqualProject(editorRef: IEditorReference) = {
      val isEqual = for {
        editor <- Option(editorRef.getEditor( /*restore =*/ false))
        input <- editor.getEditorInput.asInstanceOfOpt[IFileEditorInput]
      } yield {
        val file = input.getFile
        project.underlying equals file.getProject
      }
      isEqual.getOrElse(false)
    }

    EclipseUtils.getWorkbenchPages.exists(hasOpenEditorForThisProject)
  }
}
